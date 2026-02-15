package me.autobot.addonDoll.connection;

import com.mojang.authlib.GameProfile;
import me.autobot.addonDoll.player.ServerDoll;
import me.autobot.playerdoll.api.PlayerDollAPI;
import me.autobot.playerdoll.api.ReflectionUtil;
import me.autobot.playerdoll.api.connection.ConnectionFetcher;
import me.autobot.playerdoll.api.constant.AbsServerBranch;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.network.protocol.login.ServerboundLoginAcknowledgedPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Arrays;

public class DollLoginListener extends ServerLoginPacketListenerImpl implements Listener {
    private static final int MAX_CONFIGURATION_WAIT_TICKS = 200;
    private final GameProfile profile;
    private final Player caller;
    private static final Method startClientVerificationMethod;

    static {
        startClientVerificationMethod = Arrays.stream(ServerLoginPacketListenerImpl.class.getDeclaredMethods())
                .filter(method -> method.getModifiers() == 0)
                .filter(method -> method.getReturnType() == void.class && method.getParameterCount() == 1 && method.getParameterTypes()[0] == GameProfile.class)
                .findFirst()
                .orElseThrow();
        startClientVerificationMethod.setAccessible(true);
    }
    public DollLoginListener(MinecraftServer minecraftserver, Connection networkmanager, GameProfile profile, Player caller) {
        super(minecraftserver, networkmanager, false);
        this.profile = profile;
        this.caller = caller;
        Bukkit.getPluginManager().registerEvents(this, PlayerDollAPI.getInstance());
        handleHello(new ServerboundHelloPacket(profile.getName(), profile.getId()));
    }
    @Override
    public void handleHello(ServerboundHelloPacket packetlogininstart) {
        callPreLogin();
    }
    private void callPreLogin() {
        Thread preLogin = new Thread(() -> {
            AsyncPlayerPreLoginEvent preLoginEvent = new AsyncPlayerPreLoginEvent(profile.getName(), ((InetSocketAddress)this.connection.getRemoteAddress()).getAddress(), profile.getId());
            preLoginEvent.setLoginResult(AsyncPlayerPreLoginEvent.Result.ALLOWED);
            preLoginEvent.setKickMessage("PlayerDoll");
            Bukkit.getPluginManager().callEvent(preLoginEvent);
        });
        preLogin.start();
    }
    @EventHandler(priority = EventPriority.MONITOR)
    private void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getUniqueId().equals(profile.getId())) {
            PlayerDollAPI.getScheduler().globalTaskDelayed(() -> ReflectionUtil.invokeMethod(startClientVerificationMethod, this, profile), 5);
            AsyncPlayerPreLoginEvent.getHandlerList().unregister(this);
        }
    }

    @Override
    public void handleLoginAcknowledgement(ServerboundLoginAcknowledgedPacket serverboundloginacknowledgedpacket) {
        // Avoid IllegalStateException "Asynchronous Chunk getEntities call!"
        PacketUtils.ensureRunningOnSameThread(serverboundloginacknowledgedpacket, this, (MinecraftServer) ReflectionUtil.getDedicatedServerInstance());
        this.connection.setupOutboundProtocol(ConfigurationProtocols.CLIENTBOUND);
        ServerPlayer oldPlayer = resolveOldPlayer();
        ServerDoll player = ServerDoll.callSpawn(profile, oldPlayer);
        player.setup(caller);
        CommonListenerCookie commonlistenercookie = CommonListenerCookie.createInitial(player.getGameProfile(), false);
        ConfigurationListenerFactory.setLegacySavedPlayer(this.connection, player);
        ServerConfigurationPacketListenerImpl serverconfigurationpacketlistenerimpl = ConfigurationListenerFactory.create(player.level().getServer(), this.connection, commonlistenercookie, player);
        serverconfigurationpacketlistenerimpl.handleClientInformation(new ServerboundClientInformationPacket(ClientInformation.createDefault()));
        this.connection.setupInboundProtocol(ConfigurationProtocols.SERVERBOUND, serverconfigurationpacketlistenerimpl);
        serverconfigurationpacketlistenerimpl.startConfiguration();
        waitForGamePlayListener(player, 0);
    }

    private ServerPlayer resolveOldPlayer() {
        ServerPlayer oldPlayer = PlayerLoginListener.getPlayer(this);
        if (oldPlayer != null) {
            return oldPlayer;
        }
        if (caller != null) {
            return (ServerPlayer) ReflectionUtil.bukkitToNMSPlayer(caller);
        }
        MinecraftServer server = (MinecraftServer) ReflectionUtil.getDedicatedServerInstance();
        return new ServerPlayer(server, server.overworld(), profile, ClientInformation.createDefault());
    }

    private void waitForGamePlayListener(ServerDoll doll, int tries) {
        if (tries >= MAX_CONFIGURATION_WAIT_TICKS) {
            PlayerDollAPI.getLogger().warning("Timeout waiting for configuration finish on doll " + doll.getName().getString());
            return;
        }

        Object packetListener = ConnectionFetcher.getPacketListener(this.connection);
        if (packetListener instanceof ServerGamePlayListener) {
            return;
        }
        if (packetListener instanceof ServerGamePacketListenerImpl) {
            Runnable task = () -> {
                ServerGamePlayListener gamePlayListener = new ServerGamePlayListener(doll, this.connection, doll.getGameProfile());
                ConfigurationListenerFactory.setLegacySavedPlayer(this.connection, null);
                ConnectionFetcher.setPacketListener(this.connection, gamePlayListener);
                doll.callDollJoinEvent();
                doll.connection = gamePlayListener;
            };
            if (PlayerDollAPI.getServerBranch() == AbsServerBranch.FOLIA) {
                PlayerDollAPI.getScheduler().entityTask(task, ReflectionUtil.NMSToBukkitPlayer(doll));
            } else {
                task.run();
            }
            return;
        }

        PlayerDollAPI.getScheduler().globalTaskDelayed(() -> waitForGamePlayListener(doll, tries + 1), 1L);
    }
}
