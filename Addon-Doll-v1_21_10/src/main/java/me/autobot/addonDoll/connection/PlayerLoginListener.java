package me.autobot.addonDoll.connection;

import com.mojang.authlib.GameProfile;
import me.autobot.addonDoll.player.TransPlayer;
import me.autobot.playerdoll.api.PlayerDollAPI;
import me.autobot.playerdoll.api.ReflectionUtil;
import me.autobot.playerdoll.api.connection.ConnectionFetcher;
import me.autobot.playerdoll.api.constant.AbsServerBranch;
import me.autobot.playerdoll.api.doll.PlayerConvertInjector;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.login.ServerboundLoginAcknowledgedPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.UUID;

public class PlayerLoginListener extends ServerLoginPacketListenerImpl {
    private final MinecraftServer server;
    private final ServerPlayer player;
    private final TransPlayer transPlayer;
    private static final Field serverPlayerField;
    private static final Field authenticatedProfileField;
    private static final Field transferredField;

    static {
        serverPlayerField = Arrays.stream(ServerLoginPacketListenerImpl.class.getDeclaredFields())
                .filter(field -> field.getType() == ServerPlayer.class)
                .findFirst()
                .orElse(null);
        if (serverPlayerField != null) {
            serverPlayerField.setAccessible(true);
        } else {
            PlayerDollAPI.getLogger().warning("ServerLoginPacketListenerImpl has no ServerPlayer field. Using fallback path.");
        }

        authenticatedProfileField = Arrays.stream(ServerLoginPacketListenerImpl.class.getDeclaredFields())
                .filter(field -> field.getType() == GameProfile.class)
                .findFirst()
                .orElse(null);
        if (authenticatedProfileField != null) {
            authenticatedProfileField.setAccessible(true);
        }
        transferredField = Arrays.stream(ServerLoginPacketListenerImpl.class.getDeclaredFields())
                .filter(field -> field.getType() == boolean.class)
                .filter(field -> field.getName().equals("transferred"))
                .findFirst()
                .orElse(null);
        if (transferredField != null) {
            transferredField.setAccessible(true);
        }

        PlayerConvertInjector.swapListenerFunc = (oldListener) -> {
            ServerLoginPacketListenerImpl l = (ServerLoginPacketListenerImpl) oldListener;
            ServerPlayer oldPlayer = getPlayer(l);
            if (oldPlayer == null && authenticatedProfileField != null) {
                GameProfile profile = ReflectionUtil.getField(GameProfile.class, authenticatedProfileField, l);
                oldPlayer = createFallbackPlayer((MinecraftServer) ReflectionUtil.getDedicatedServerInstance(), profile);
            }
            if (oldPlayer == null) {
                PlayerDollAPI.getLogger().warning("Cannot resolve original ServerPlayer for converted player; skipping listener swap.");
                return;
            }
            boolean transfer = transferredField != null && Boolean.TRUE.equals(ReflectionUtil.getField(transferredField, l));
            ConnectionFetcher.setPacketListener(l.connection, new PlayerLoginListener((MinecraftServer) ReflectionUtil.getDedicatedServerInstance(), l.connection, oldPlayer, transfer));
        };

    }
    public PlayerLoginListener(MinecraftServer minecraftserver, Connection networkmanager, ServerPlayer player, boolean transfer) {
        super(minecraftserver, networkmanager, transfer);
        this.server = minecraftserver;
        this.player = player;
        transPlayer = new TransPlayer(player);
    }

    @Override
    public void handleLoginAcknowledgement(ServerboundLoginAcknowledgedPacket serverboundloginacknowledgedpacket) {
        if (PlayerDollAPI.getServerBranch() != AbsServerBranch.FOLIA) {
            PacketUtils.ensureRunningOnSameThread(serverboundloginacknowledgedpacket, this, this.server.overworld());
        }
        this.connection.setupOutboundProtocol(ConfigurationProtocols.CLIENTBOUND);
        CommonListenerCookie commonlistenercookie = CommonListenerCookie.createInitial(player.getGameProfile(), false);
        ConfigurationListenerFactory.setLegacySavedPlayer(this.connection, transPlayer);
        ServerConfigurationPacketListenerImpl serverconfigurationpacketlistenerimpl = ConfigurationListenerFactory.create(this.server, this.connection, commonlistenercookie, transPlayer);
        this.connection.setupInboundProtocol(ConfigurationProtocols.SERVERBOUND, serverconfigurationpacketlistenerimpl);
        serverconfigurationpacketlistenerimpl.startConfiguration();
    }

    public static ServerPlayer getPlayer(ServerLoginPacketListenerImpl instance) {
        if (serverPlayerField == null) {
            return null;
        }
        return ReflectionUtil.getField(ServerPlayer.class, serverPlayerField, instance);
    }

    private static ServerPlayer createFallbackPlayer(MinecraftServer server, GameProfile profile) {
        if (profile == null) {
            return null;
        }
        Player bukkit = null;
        UUID uuid = profile.id();
        if (uuid != null) {
            bukkit = Bukkit.getPlayer(uuid);
        }
        if (bukkit != null) {
            return (ServerPlayer) ReflectionUtil.bukkitToNMSPlayer(bukkit);
        }
        return new ServerPlayer(server, server.overworld(), profile, ClientInformation.createDefault());
    }
}
