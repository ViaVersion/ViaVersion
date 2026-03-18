/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_12_2to1_13.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.ChunkPosition;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.HashMap;
import java.util.Map;

public class BlockStorage implements StorableObject {
    private static final IntSet WHITELIST = new IntOpenHashSet(46);
    private final Map<Long, Map<BlockPosition, ReplacementData>> blocks = new HashMap<>();

    static {
        // Flower pots
        WHITELIST.add(5266);

        // Add those red beds
        for (int i = 0; i < 16; i++) {
            WHITELIST.add(972 + i);
        }

        // Add the white banners
        for (int i = 0; i < 20; i++) {
            WHITELIST.add(6854 + i);
        }

        // Add the white wall banners
        for (int i = 0; i < 4; i++) {
            WHITELIST.add(7110 + i);
        }

        // Skeleton skulls
        for (int i = 0; i < 5; i++) {
            WHITELIST.add(5447 + i);
        }
    }

    public void store(BlockPosition position, int block) {
        store(position, block, -1);
    }

    public void store(BlockPosition position, int block, int replacementId) {
        if (!WHITELIST.contains(block))
            return;

        final long chunkKey = ChunkPosition.chunkKeyForBlock(position.x(), position.z());
        blocks.computeIfAbsent(chunkKey, key -> new HashMap<>()).put(position, new ReplacementData(block, replacementId));
    }

    public boolean isWelcome(int block) {
        return WHITELIST.contains(block);
    }

    public boolean contains(BlockPosition position) {
        final Map<BlockPosition, ReplacementData> chunkBlocks = blocks.get(ChunkPosition.chunkKeyForBlock(position.x(), position.z()));
        return chunkBlocks != null && chunkBlocks.containsKey(position);
    }

    public ReplacementData get(BlockPosition position) {
        final Map<BlockPosition, ReplacementData> chunkBlocks = blocks.get(ChunkPosition.chunkKeyForBlock(position.x(), position.z()));
        return chunkBlocks != null ? chunkBlocks.get(position) : null;
    }

    public ReplacementData remove(BlockPosition position) {
        final long chunkKey = ChunkPosition.chunkKeyForBlock(position.x(), position.z());
        final Map<BlockPosition, ReplacementData> chunkBlocks = blocks.get(chunkKey);
        if (chunkBlocks == null) {
            return null;
        }

        final ReplacementData removed = chunkBlocks.remove(position);
        if (chunkBlocks.isEmpty()) {
            blocks.remove(chunkKey);
        }
        return removed;
    }

    public void removeChunk(int chunkX, int chunkZ) {
        blocks.remove(ChunkPosition.chunkKey(chunkX, chunkZ));
    }

    public void clear() {
        blocks.clear();
    }

    public static final class ReplacementData {
        private final int original;
        private int replacement;

        public ReplacementData(int original, int replacement) {
            this.original = original;
            this.replacement = replacement;
        }

        public int getOriginal() {
            return original;
        }

        public int getReplacement() {
            return replacement;
        }

        public void setReplacement(int replacement) {
            this.replacement = replacement;
        }
    }
}
