/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_21to1_21_2.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.minecraft.ChunkPosition;
import java.util.HashSet;
import java.util.Set;

public class ChunkLoadTracker implements StorableObject {

    private final Set<Long> loadedChunks = new HashSet<>();

    public void addChunk(final int x, final int z) {
        this.loadedChunks.add(ChunkPosition.chunkKey(x, z));
    }

    public void removeChunk(final int x, final int z) {
        this.loadedChunks.remove(ChunkPosition.chunkKey(x, z));
    }

    public boolean isChunkLoaded(final int x, final int z) {
        return this.loadedChunks.contains(ChunkPosition.chunkKey(x, z));
    }

    public void clear() {
        this.loadedChunks.clear();
    }

}
