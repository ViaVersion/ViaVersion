/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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
package com.viaversion.viaversion.bukkit.platform;

import com.viaversion.viaversion.ViaAPIBase;
import com.viaversion.viaversion.ViaVersionPlugin;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.bukkit.util.ProtocolSupportUtil;
import io.netty.buffer.ByteBuf;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BukkitViaAPI extends ViaAPIBase<Player> {
    private final ViaVersionPlugin plugin;

    public BukkitViaAPI(ViaVersionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public int getPlayerVersion(Player player) {
        return getPlayerVersion(player.getUniqueId());
    }

    @Override
    public int getPlayerVersion(UUID uuid) {
        UserConnection connection = Via.getManager().getConnectionManager().getConnectedClient(uuid);
        if (connection == null) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && isProtocolSupport()) {
                return ProtocolSupportUtil.getProtocolVersion(player);
            }
            return -1;
        }
        return connection.getProtocolInfo().getProtocolVersion();
    }

    @Override
    public void sendRawPacket(Player player, ByteBuf packet) throws IllegalArgumentException {
        sendRawPacket(player.getUniqueId(), packet);
    }

    /**
     * Returns if this version is a compatibility build for spigot.
     * Eg. 1.9.1 / 1.9.2 allow certain versions to connect
     *
     * @return true if compat Spigot build
     */
    public boolean isCompatSpigotBuild() {
        return plugin.isCompatSpigotBuild();
    }

    /**
     * Returns if ProtocolSupport is also being used.
     *
     * @return true if ProtocolSupport is used
     */
    public boolean isProtocolSupport() {
        return plugin.isProtocolSupport();
    }

}
