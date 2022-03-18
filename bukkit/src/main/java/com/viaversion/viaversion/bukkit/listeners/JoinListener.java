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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

public class JoinListener implements Listener {

    private final Method getHandle;
    private final Field connection;
    private final Field networkManager;
    private final Field channel;

    public JoinListener() {
        try {
            getHandle = NMSUtil.obc("entity.CraftPlayer").getDeclaredMethod("getHandle");
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException("Couldn't find CraftPlayer", e);
        }
        try {
            connection = NMSUtil.nms("EntityPlayer").getDeclaredField("playerConnection");
        } catch (NoSuchFieldException | ClassNotFoundException e) {
            throw new RuntimeException("Couldn't find Player Connection", e);
        }
        try {
            networkManager = NMSUtil.nms("PlayerConnection").getDeclaredField("networkManager");
        } catch (NoSuchFieldException | ClassNotFoundException e) {
            throw new RuntimeException("Couldn't find Network Manager", e);
        }
        try {
            channel = NMSUtil.nms("NetworkManager").getDeclaredField("channel");
        } catch (NoSuchFieldException | ClassNotFoundException e) {
            throw new RuntimeException("Couldn't find Channel", e);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        UserConnection user = getUserConnection(player);
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

    private UserConnection getUserConnection(Player player) {
        Channel channel = getChannel(player);
        if (channel == null) return null;
        BukkitEncodeHandler encoder = channel.pipeline().get(BukkitEncodeHandler.class);
        return encoder != null ? encoder.getInfo() : null;
    }

    private Channel getChannel(Player player) {
        try {
            Object entityPlayer = getHandle.invoke(player);
            Object pc = connection.get(entityPlayer);
            Object nm = networkManager.get(pc);
            return (Channel) channel.get(nm);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

}
