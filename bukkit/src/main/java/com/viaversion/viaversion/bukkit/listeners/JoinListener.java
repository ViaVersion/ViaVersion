/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2022 ViaVersion and contributors
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
import io.netty.channel.Channel;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;

public class JoinListener implements Listener {

    private static final Method GET_HANDLE;
    private static final Field CONNECTION;
    private static final Field NETWORK_MANAGER;
    private static final Field CHANNEL;

    static {
        Method gh = null;
        Field conn = null, nm = null, ch = null;
        try {
            gh = NMSUtil.obc("entity.CraftPlayer").getDeclaredMethod("getHandle");
            conn = findField(gh.getReturnType(), "PlayerConnection");
            nm = findField(conn.getType(), "NetworkManager");
            ch = findField(nm.getType(), "Channel");
        } catch (NoSuchMethodException | NoSuchFieldException | ClassNotFoundException e) {
            Via.getPlatform().getLogger().log(
                    Level.WARNING,
                    "Couldn't find reflection methods/fields to access Channel from player.\n" +
                            "Login race condition fixer will be disabled.\n" +
                            " Some plugins that use ViaAPI on join event may work incorrectly.", e);
        }
        GET_HANDLE = gh;
        CONNECTION = conn;
        NETWORK_MANAGER = nm;
        CHANNEL = ch;
    }

    // Loosely search a field with any name, as long as it matches the type
    private static Field findField(Class<?> cl, String type) throws NoSuchFieldException {
        for (Field field : cl.getDeclaredFields()) {
            if (field.getType().getSimpleName().equals(type))
                return field;
        }
        throw new NoSuchFieldException(type);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        if (CHANNEL == null) return;
        Player player = e.getPlayer();

        Channel channel = getChannel(player);
        if (channel == null) {
            Via.getPlatform().getLogger().log(Level.WARNING,
                    "Could not find Channel for logging-in player {0}",
                    player.getUniqueId());
            return;
        }
        // The connection has already closed, that was a quick leave
        if (!channel.isOpen()) return;

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
    }

    private UserConnection getUserConnection(Channel channel) {
        BukkitEncodeHandler encoder = channel.pipeline().get(BukkitEncodeHandler.class);
        return encoder != null ? encoder.getInfo() : null;
    }

    private Channel getChannel(Player player) {
        try {
            Object entityPlayer = GET_HANDLE.invoke(player);
            Object pc = CONNECTION.get(entityPlayer);
            Object nm = NETWORK_MANAGER.get(pc);
            return (Channel) CHANNEL.get(nm);
        } catch (Exception e) { // Wildcard-catch everything
            e.printStackTrace();
        }
        return null;
    }

}
