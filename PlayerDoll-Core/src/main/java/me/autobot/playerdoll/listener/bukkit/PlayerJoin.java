package me.autobot.playerdoll.listener.bukkit;

import me.autobot.playerdoll.api.PlayerDollAPI;
import me.autobot.playerdoll.api.ReflectionUtil;
import me.autobot.playerdoll.api.config.impl.BasicConfig;
import me.autobot.playerdoll.api.constant.AbsServerBranch;
import me.autobot.playerdoll.api.doll.Doll;
import me.autobot.playerdoll.api.doll.DollConfig;
import me.autobot.playerdoll.api.doll.DollNameUtil;
import me.autobot.playerdoll.api.doll.DollStorage;
import me.autobot.playerdoll.api.inv.button.GlobalFlagButton;
import me.autobot.playerdoll.api.inv.button.PersonalFlagButton;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.util.Map;


public class PlayerJoin implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Plugin plugin = PlayerDollAPI.getInstance();
        BasicConfig basicConfig = PlayerDollAPI.getConfigLoader().getBasicConfig();
        Player player = event.getPlayer();
        final boolean isDoll = ReflectionUtil.bukkitToNMSPlayer(player) instanceof Doll ;
        if (isDoll) {
            if (!basicConfig.broadcastDollJoin.getValue()) {
                event.setJoinMessage(null);
            }
            Doll doll = (Doll) ReflectionUtil.bukkitToNMSPlayer(player);
            Player caller = doll.getCaller();
            if (caller != null && !caller.getUniqueId().equals(player.getUniqueId())) {
                // Keep legacy behavior for manual spawns.
                if (PlayerDollAPI.getServerBranch() == AbsServerBranch.FOLIA) {
                    PlayerDollAPI.getScheduler().foliaTeleportAync(player, caller.getLocation());
                } else {
                    PlayerDollAPI.getScheduler().entityTaskDelayed(() -> player.teleport(caller), player, 5);
                }
            } else {
                DollConfig tempConfig = DollConfig.getTemporaryConfig(DollNameUtil.dollShortName(player.getName()));
                if (tempConfig.dollSetting.get(GlobalFlagButton.JOIN_AT_START).getValue()) {
                    Location targetLocation = tempConfig.getLastLocation();
                    if (targetLocation != null) {
                        if (PlayerDollAPI.getServerBranch() == AbsServerBranch.FOLIA) {
                            PlayerDollAPI.getScheduler().foliaTeleportAync(player, targetLocation);
                        } else {
                            Location finalTargetLocation = targetLocation;
                            PlayerDollAPI.getScheduler().entityTaskDelayed(() -> player.teleport(finalTargetLocation), player, 5);
                        }
                    }
                }
            }
        }

        // Hide players
        if (!player.isOp() || (player.isOp() && !basicConfig.opCanSeeHiddenDoll.getValue())) {
            DollStorage.ONLINE_DOLLS.values().forEach(doll -> {
                Player dollPlayer = doll.getBukkitPlayer();
                DollConfig dollConfig = DollConfig.getTemporaryConfig(dollPlayer.getName());
                boolean hide = dollConfig.generalSetting.get(PersonalFlagButton.HIDDEN);
                if (!isDoll) {
                    Map<PersonalFlagButton, Boolean> playerSettings = dollConfig.playerSetting.get(player.getUniqueId());
                    if (playerSettings == null) {
                        if (hide) {
                            player.hidePlayer(plugin, dollPlayer);
                        } else {
                            player.showPlayer(plugin, dollPlayer);
                        }
                    } else {
                        boolean playerHide = playerSettings.get(PersonalFlagButton.HIDDEN);
                        if (playerHide) {
                            player.hidePlayer(plugin, dollPlayer);
                        } else {
                            player.showPlayer(plugin, dollPlayer);
                        }
                    }
                }
            });
        }
    }
}
