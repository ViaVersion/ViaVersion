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
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.storage;

import com.google.common.collect.EvictingQueue;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.minecraft.Position;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BlockConnectionStorage implements StorableObject {
    private static Constructor<?> fastUtilLongObjectHashMap;

    private final Map<Long, SectionData> blockStorage = createLongObjectMap();
    @SuppressWarnings("UnstableApiUsage")
    private final Queue<Position> modified = EvictingQueue.create(5);

    // Cache to retrieve section quicker
    private long lastIndex = -1;
    private SectionData lastSection;

    static {
        try {
            //noinspection StringBufferReplaceableByString - prevent relocation
            String className = new StringBuilder("it").append(".unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap").toString();
            fastUtilLongObjectHashMap = Class.forName(className).getConstructor();
            Via.getPlatform().getLogger().info("Using FastUtil Long2ObjectOpenHashMap for block connections");
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
        }
    }

    public static void init() {
    }

    public void store(int x, int y, int z, int blockState) {
        long index = getChunkSectionIndex(x, y, z);
        SectionData section = getSection(index);
        if (section == null) {
            if (blockState == 0) {
                // No need to store empty sections
                return;
            }

            blockStorage.put(index, section = new SectionData());
            lastSection = section;
            lastIndex = index;
        }

        section.setBlockAt(x, y, z, blockState);
    }

    public int get(int x, int y, int z) {
        long pair = getChunkSectionIndex(x, y, z);
        SectionData section = getSection(pair);
        if (section == null) {
            return 0;
        }

        return section.blockAt(x, y, z);
    }

    public void remove(int x, int y, int z) {
        long index = getChunkSectionIndex(x, y, z);
        SectionData section = getSection(index);
        if (section == null) {
            return;
        }

        section.setBlockAt(x, y, z, 0);

        if (section.nonEmptyBlocks() == 0) {
            removeSection(index);
        }
    }

    public void markModified(Position pos) {
        // Avoid saving the same pos twice
        if (!modified.contains(pos)) {
            this.modified.add(pos);
        }
    }

    public boolean recentlyModified(Position pos) {
        for (Position p : modified) {
            if (Math.abs(pos.x() - p.x()) + Math.abs(pos.y() - p.y()) + Math.abs(pos.z() - p.z()) <= 2) {
                return true;
            }
        }
        return false;
    }

    public void clear() {
        blockStorage.clear();
        lastSection = null;
        lastIndex = -1;
        modified.clear();
    }

    public void unloadChunk(int x, int z) {
        for (int y = 0; y < 16; y++) {
            unloadSection(x, y, z);
        }
    }

    public void unloadSection(int x, int y, int z) {
        removeSection(getChunkSectionIndex(x << 4, y << 4, z << 4));
    }

    private @Nullable SectionData getSection(long index) {
        if (lastIndex == index) {
            return lastSection;
        }
        lastIndex = index;
        return lastSection = blockStorage.get(index);
    }

    private void removeSection(long index) {
        blockStorage.remove(index);
        if (lastIndex == index) {
            lastIndex = -1;
            lastSection = null;
        }
    }

    private static long getChunkSectionIndex(int x, int y, int z) {
        return (((x >> 4) & 0x3FFFFFFL) << 38) | (((y >> 4) & 0xFFFL) << 26) | ((z >> 4) & 0x3FFFFFFL);
    }

    private <T> Map<Long, T> createLongObjectMap() {
        if (fastUtilLongObjectHashMap != null) {
            try {
                //noinspection unchecked
                return (Map<Long, T>) fastUtilLongObjectHashMap.newInstance();
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return new HashMap<>();
    }

    private static final class SectionData {
        private final short[] blockStates = new short[4096];
        private short nonEmptyBlocks;

        public int blockAt(int x, int y, int z) {
            return blockStates[encodeBlockPos(x, y, z)];
        }

        public void setBlockAt(int x, int y, int z, int blockState) {
            int index = encodeBlockPos(x, y, z);
            if (blockState == blockStates[index]) {
                return;
            }

            blockStates[index] = (short) blockState;
            if (blockState == 0) {
                nonEmptyBlocks--;
            } else {
                nonEmptyBlocks++;
            }
        }

        public short nonEmptyBlocks() {
            return nonEmptyBlocks;
        }

        private static int encodeBlockPos(int x, int y, int z) {
            return ((y & 0xF) << 8) | ((x & 0xF) << 4) | (z & 0xF);
        }
    }
}