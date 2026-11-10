/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.bukkit.listeners;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.bukkit.handlers.BukkitEncodeHandler;
import com.viaversion.viaversion.bukkit.util.NMSUtil;
import com.viaversion.viaversion.connection.ConnectionDetails;
import io.netty.channel.Channel;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.Nullable;

public class JoinListener implements Listener {

    private static final Method GET_HANDLE;
    private static final Field CONNECTION;
    private static final Field NETWORK_MANAGER;
    private static final Field CHANNEL;

    static {
        Method getHandleMethod = null;
        Field gamePacketListenerField = null, connectionField = null, channelField = null;
        try {
            getHandleMethod = NMSUtil.obc("entity.CraftPlayer").getDeclaredMethod("getHandle");
            gamePacketListenerField = findField(false, getHandleMethod.getReturnType(), "PlayerConnection", "ServerGamePacketListenerImpl");
            connectionField = findField(true, gamePacketListenerField.getType(), "NetworkManager", "Connection");
            channelField = findField(connectionField.getType(), Class.forName("io.netty.channel.Channel"));
        } catch (NoSuchMethodException | NoSuchFieldException | ClassNotFoundException e) {
            Via.getPlatform().getLogger().log(Level.WARNING, """
                Couldn't find reflection methods/fields to access Channel from player.
                Login race condition fixer will be disabled.
                Some plugins that use ViaAPI on join event may work incorrectly.""", e);
        }
        GET_HANDLE = getHandleMethod;
        CONNECTION = gamePacketListenerField;
        NETWORK_MANAGER = connectionField;
        CHANNEL = channelField;
    }

    // Loosely search a field with any name, as long as it matches a type name.
    private static Field findField(boolean checkSuperClass, Class<?> clazz, String... types) throws NoSuchFieldException {
        for (Field field : clazz.getDeclaredFields()) {
            String fieldTypeName = field.getType().getSimpleName();
            for (String type : types) {
                if (!fieldTypeName.equals(type)) {
                    continue;
                }

                if (!Modifier.isPublic(field.getModifiers())) {
                    field.setAccessible(true);
                }
                return field;
            }
        }

        if (checkSuperClass && clazz != Object.class && clazz.getSuperclass() != null) {
            return findField(true, clazz.getSuperclass(), types);
        }

        throw new NoSuchFieldException(types[0]);
    }

    private static Field findField(Class<?> clazz, Class<?> fieldType) throws NoSuchFieldException {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType() != fieldType) {
                continue;
            }

            if (!Modifier.isPublic(field.getModifiers())) {
                field.setAccessible(true);
            }
            return field;
        }
        throw new NoSuchFieldException(fieldType.getSimpleName());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        if (CHANNEL == null) return;
        Player player = e.getPlayer();

        Channel channel;
        try {
            channel = getChannel(player);
        } catch (Exception ex) {
            Via.getPlatform().getLogger().log(Level.WARNING, ex,
                () -> "Could not find Channel for logging-in player " + player.getUniqueId());
            return;
        }
        // The connection has already closed, that was a quick leave
        // Channel may be null if a plugin is manually calling the event for a non-player...
        if (channel == null || !channel.isOpen()) {
            return;
        }

        UserConnection user = getUserConnection(channel);
        if (user == null) {
            Via.getPlatform().getLogger().log(Level.WARNING,
                "Could not find UserConnection for logging-in player {0}",
                player.getUniqueId());
            return;
        }
        ProtocolInfo info = user.getProtocolInfo();
        info.setUuid(player.getUniqueId());
        info.setUsername(player.getName());
        Via.getManager().getConnectionManager().onLoginSuccess(user);

        ConnectionDetails.sendConnectionDetails(user, ConnectionDetails.SERVER_CHANNEL);
    }

    private @Nullable UserConnection getUserConnection(Channel channel) {
        BukkitEncodeHandler encoder = channel.pipeline().get(BukkitEncodeHandler.class);
        return encoder != null ? encoder.connection() : null;
    }

    private Channel getChannel(Player player) throws Exception {
        Object entityPlayer = GET_HANDLE.invoke(player);
        Object pc = CONNECTION.get(entityPlayer);
        Object nm = NETWORK_MANAGER.get(pc);
        return (Channel) CHANNEL.get(nm);
    }

}
