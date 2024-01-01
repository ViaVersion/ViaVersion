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
package com.viaversion.viaversion.bukkit.providers;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.blockconnections.providers.BlockConnectionProvider;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BukkitBlockConnectionProvider extends BlockConnectionProvider {
    private Chunk lastChunk;

    @Override
    public int getWorldBlockData(UserConnection user, int bx, int by, int bz) {
        UUID uuid = user.getProtocolInfo().getUuid();
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            World world = player.getWorld();
            int x = bx >> 4;
            int z = bz >> 4;
            if (world.isChunkLoaded(x, z)) {
                Chunk c = getChunk(world, x, z);
                Block b = c.getBlock(bx, by, bz);
                return b.getTypeId() << 4 | b.getData();
            }
        }
        return 0;
    }

    public Chunk getChunk(World world, int x, int z) {
        if (lastChunk != null && lastChunk.getWorld().equals(world) && lastChunk.getX() == x && lastChunk.getZ() == z) {
            return lastChunk;
        }
        return lastChunk = world.getChunkAt(x, z);
    }
}
