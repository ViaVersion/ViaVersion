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
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.blockconnections.providers;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.platform.providers.Provider;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BlockConnectionProvider implements Provider {

    public int getBlockData(UserConnection connection, int x, int y, int z) {
        int oldId = getWorldBlockData(connection, x, y, z);
        return Protocol1_13To1_12_2.MAPPINGS.getBlockMappings().getNewId(oldId);
    }

    public int getWorldBlockData(UserConnection connection, int x, int y, int z) {
        return -1;
    }

    public void storeBlock(UserConnection connection, int x, int y, int z, int blockState) {

    }

    public void removeBlock(UserConnection connection, int x, int y, int z) {

    }

    public void clearStorage(UserConnection connection) {

    }

    public void modifiedBlock(UserConnection connection, Position position) {

    }

    public void unloadChunk(UserConnection connection, int x, int z) {

    }

    public void unloadChunkSection(UserConnection connection, int chunkX, int chunkY, int chunkZ) {

    }

    /**
     * True if blocks are stored, and are known to be accurate around the given position.
     * If the client has modified the position (ie: placed or broken a block) this should return false.
     *
     * @param position The position at which block reliability should be checked, null for general-purpose
     * @return true if the block & its neighbors are known to be correct
     */
    public boolean storesBlocks(UserConnection user, @Nullable Position position) {
        return false;
    }

    public UserBlockData forUser(UserConnection connection) {
        return (x, y, z) -> getBlockData(connection, x, y, z);
    }

}
