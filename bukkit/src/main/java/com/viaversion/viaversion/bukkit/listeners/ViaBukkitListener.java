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
package com.viaversion.viaversion.bukkit.listeners;

import com.viaversion.viaversion.ViaListener;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.Protocol;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class ViaBukkitListener extends ViaListener implements Listener {
    private final Plugin plugin;

    public ViaBukkitListener(Plugin plugin, Class<? extends Protocol> requiredPipeline) {
        super(requiredPipeline);
        this.plugin = plugin;
    }

    /**
     * Get the UserConnection from a player
     *
     * @param player Player object
     * @return The UserConnection
     */
    protected UserConnection getUserConnection(Player player) {
        return getUserConnection(player.getUniqueId());
    }

    /**
     * Checks if the player is on the selected pipe
     *
     * @param player Player Object
     * @return True if on pipe
     */
    protected boolean isOnPipe(Player player) {
        return isOnPipe(player.getUniqueId());
    }

    /**
     * Register as Bukkit event
     */
    @Override
    public void register() {
        if (isRegistered()) {
            return;
        }

        setRegistered(true);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public Plugin getPlugin() {
        return plugin;
    }
}
