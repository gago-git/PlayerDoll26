package me.autobot.addonDoll.connection;

import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

final class ConfigurationListenerFactory {
    private static final Constructor<?> listenerConstructor;
    private static final Field savedPlayerForLegacyEventsField;

    static {
        listenerConstructor = Arrays.stream(ServerConfigurationPacketListenerImpl.class.getDeclaredConstructors())
                .filter(constructor -> {
                    Class<?>[] params = constructor.getParameterTypes();
                    if (params.length != 3 && params.length != 4) {
                        return false;
                    }
                    if (params[0] != MinecraftServer.class || params[1] != Connection.class || params[2] != CommonListenerCookie.class) {
                        return false;
                    }
                    return params.length != 4 || ServerPlayer.class.isAssignableFrom(params[3]);
                })
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cannot find ServerConfigurationPacketListenerImpl constructor for current runtime"));
        listenerConstructor.setAccessible(true);

        savedPlayerForLegacyEventsField = Arrays.stream(Connection.class.getDeclaredFields())
                .filter(field -> field.getType() == ServerPlayer.class)
                .filter(field -> field.getName().equals("savedPlayerForLoginEventLegacy")
                        || field.getName().equals("savedPlayerForLegacyEvents"))
                .findFirst()
                .orElse(null);
        if (savedPlayerForLegacyEventsField != null) {
            savedPlayerForLegacyEventsField.setAccessible(true);
        }
    }

    private ConfigurationListenerFactory() {
    }

    static ServerConfigurationPacketListenerImpl create(MinecraftServer server, Connection connection, CommonListenerCookie cookie, ServerPlayer player) {
        try {
            if (listenerConstructor.getParameterCount() == 4) {
                return (ServerConfigurationPacketListenerImpl) listenerConstructor.newInstance(server, connection, cookie, player);
            }
            return (ServerConfigurationPacketListenerImpl) listenerConstructor.newInstance(server, connection, cookie);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Cannot instantiate ServerConfigurationPacketListenerImpl", e);
        }
    }

    static void setLegacySavedPlayer(Connection connection, ServerPlayer player) {
        if (savedPlayerForLegacyEventsField == null) {
            return;
        }
        try {
            savedPlayerForLegacyEventsField.set(connection, player);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot set legacy saved player field", e);
        }
    }
}
