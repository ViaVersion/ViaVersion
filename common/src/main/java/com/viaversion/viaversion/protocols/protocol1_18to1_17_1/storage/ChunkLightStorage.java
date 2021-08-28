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
package com.viaversion.viaversion.protocols.protocol1_18to1_17_1.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ChunkLightStorage implements StorableObject {

    private final Map<Long, ChunkLight> lightPackets = new HashMap<>();
    private final Set<Long> loadedChunks = new HashSet<>();

    public void storeLight(final int x, final int z, final ChunkLight chunkLight) {
        lightPackets.put(getChunkSectionIndex(x, z), chunkLight);
    }

    public @Nullable ChunkLight removeLight(final int x, final int z) {
        return lightPackets.remove(getChunkSectionIndex(x, z));
    }

    public @Nullable ChunkLight getLight(final int x, final int z) {
        return lightPackets.get(getChunkSectionIndex(x, z));
    }

    public boolean addLoadedChunk(final int x, final int z) {
        return loadedChunks.add(getChunkSectionIndex(x, z));
    }

    public boolean isLoaded(final int x, final int z) {
        return loadedChunks.contains(getChunkSectionIndex(x, z));
    }

    public void clear(final int x, final int z) {
        final long index = getChunkSectionIndex(x, z);
        lightPackets.remove(index);
        loadedChunks.remove(index);
    }

    public void clear() {
        loadedChunks.clear();
        lightPackets.clear();
    }

    private long getChunkSectionIndex(final int x, final int z) {
        return ((x & 0x3FFFFFFL) << 38) | (z & 0x3FFFFFFL);
    }

    public record ChunkLight(boolean trustEdges, long[] skyLightMask, long[] blockLightMask,
                             long[] emptySkyLightMask, long[] emptyBlockLightMask,
                             byte[][] skyLight, byte[][] blockLight) {
    }
}
