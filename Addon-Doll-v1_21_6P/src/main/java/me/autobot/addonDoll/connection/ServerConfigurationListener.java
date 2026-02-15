package me.autobot.addonDoll.connection;

import me.autobot.addonDoll.player.ServerDoll;
import me.autobot.playerdoll.api.PlayerDollAPI;
import me.autobot.playerdoll.api.ReflectionUtil;
import me.autobot.playerdoll.api.connection.ConnectionFetcher;
import me.autobot.playerdoll.api.constant.AbsServerBranch;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;

public class ServerConfigurationListener extends ServerConfigurationPacketListenerImpl {
    private final ServerDoll doll;

    public ServerConfigurationListener(MinecraftServer minecraftserver, Connection networkmanager, ServerDoll doll) {
        super(minecraftserver, networkmanager, CommonListenerCookie.createInitial(doll.getGameProfile(), false), doll);
        this.doll = doll;
        handleClientInformation(new ServerboundClientInformationPacket(ClientInformation.createDefault()));
    }

    @Override
    public void startConfiguration() {
        super.startConfiguration();
    }

    @Override
    public void handleConfigurationFinished(ServerboundFinishConfigurationPacket serverboundfinishconfigurationpacket) {
        super.handleConfigurationFinished(serverboundfinishconfigurationpacket);
        Runnable task = () -> {
            ServerGamePlayListener gamePlayListener = new ServerGamePlayListener(this.doll, this.connection, playerProfile());
            ConnectionFetcher.setPacketListener(this.connection, gamePlayListener);
            this.doll.callDollJoinEvent();
            this.doll.connection = gamePlayListener;
        };
        if (PlayerDollAPI.getServerBranch() == AbsServerBranch.FOLIA) {
            PlayerDollAPI.getScheduler().entityTask(task, ReflectionUtil.NMSToBukkitPlayer(this.doll));
        } else {
            task.run();
        }
    }
}
