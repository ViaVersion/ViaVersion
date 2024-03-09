/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
package com.viaversion.viaversion.velocity.storage;

import com.velocitypowered.api.proxy.Player;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.util.ReflectionUtil;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class VelocityStorage implements StorableObject {
    private final Player player;
    private String currentServer;
    private List<UUID> cachedBossbar;
    private static Method getServerBossBars;
    private static Class<?> clientPlaySessionHandler;
    private static Method getMinecraftConnection;

    static {
        try {
            clientPlaySessionHandler = Class.forName("com.velocitypowered.proxy.connection.client.ClientPlaySessionHandler");
            getServerBossBars = clientPlaySessionHandler
                    .getDeclaredMethod("getServerBossBars");
            getMinecraftConnection = Class.forName("com.velocitypowered.proxy.connection.client.ConnectedPlayer")
                    .getDeclaredMethod("getMinecraftConnection");
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            Via.getPlatform().getLogger().log(Level.SEVERE, "Failed to initialize Velocity bossbar support, bossbars will not work.", e);
        }
    }

    public VelocityStorage(Player player) {
        this.player = player;
        this.currentServer = "";
    }

    public List<UUID> getBossbar() {
        if (cachedBossbar == null) {
            if (clientPlaySessionHandler == null) return null;
            if (getServerBossBars == null) return null;
            if (getMinecraftConnection == null) return null;
            // Get bossbar list if it's supported
            try {
                Object connection = getMinecraftConnection.invoke(player);
                Object sessionHandler = ReflectionUtil.invoke(connection, "getSessionHandler");
                if (clientPlaySessionHandler.isInstance(sessionHandler)) {
                    cachedBossbar = (List<UUID>) getServerBossBars.invoke(sessionHandler);
                }
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                Via.getPlatform().getLogger().log(Level.SEVERE, "Failed to get bossbar list", e);
            }
        }
        return cachedBossbar;
    }

    public Player getPlayer() {
        return player;
    }

    public String getCurrentServer() {
        return currentServer;
    }

    public void setCurrentServer(final String currentServer) {
        this.currentServer = currentServer;
    }

    public List<UUID> getCachedBossbar() {
        return cachedBossbar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VelocityStorage that = (VelocityStorage) o;
        if (!Objects.equals(player, that.player)) return false;
        if (!Objects.equals(currentServer, that.currentServer)) return false;
        return Objects.equals(cachedBossbar, that.cachedBossbar);
    }

    @Override
    public int hashCode() {
        int result = player != null ? player.hashCode() : 0;
        result = 31 * result + (currentServer != null ? currentServer.hashCode() : 0);
        result = 31 * result + (cachedBossbar != null ? cachedBossbar.hashCode() : 0);
        return result;
    }
}
