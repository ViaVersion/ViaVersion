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
package com.viaversion.viaversion.bungee.storage;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.StorableObject;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeStorage implements StorableObject {
    private static Field bossField;

    static {
        try {
            Class<?> user = Class.forName("net.md_5.bungee.UserConnection");
            bossField = user.getDeclaredField("sentBossBars");
            bossField.setAccessible(true);
        } catch (ClassNotFoundException e) {
            // Not supported *shrug* probably modified
        } catch (NoSuchFieldException e) {
            // Not supported, old version probably
        }
    }

    private final ProxiedPlayer player;
    private String currentServer;
    private Set<UUID> bossbar;

    public BungeeStorage(ProxiedPlayer player) {
        this.player = player;
        this.currentServer = "";

        // Get bossbar list if it's supported
        if (bossField != null) {
            try {
                bossbar = (Set<UUID>) bossField.get(player);
            } catch (IllegalAccessException e) {
                Via.getPlatform().getLogger().log(Level.WARNING, "Failed to get bossbar list", e);
            }
        }
    }

    public ProxiedPlayer getPlayer() {
        return player;
    }

    public String getCurrentServer() {
        return currentServer;
    }

    public void setCurrentServer(String currentServer) {
        this.currentServer = currentServer;
    }

    public Set<UUID> getBossbar() {
        return bossbar;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BungeeStorage that = (BungeeStorage) o;
        if (!Objects.equals(player, that.player)) return false;
        if (!Objects.equals(currentServer, that.currentServer)) return false;
        return Objects.equals(bossbar, that.bossbar);
    }

    @Override
    public int hashCode() {
        int result = player != null ? player.hashCode() : 0;
        result = 31 * result + (currentServer != null ? currentServer.hashCode() : 0);
        result = 31 * result + (bossbar != null ? bossbar.hashCode() : 0);
        return result;
    }
}
