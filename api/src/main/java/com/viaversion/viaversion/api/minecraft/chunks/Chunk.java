/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.viaversion.viaversion.api.minecraft.chunks;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import java.util.BitSet;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

// TODO specialized sub interfaces
public interface Chunk {

    /**
     * Returns the chunk x coordinate.
     *
     * @return chunk x coordinate
     */
    int getX();

    /**
     * Returns the chunk z coordinate.
     *
     * @return chunk z coordinate
     */
    int getZ();

    /**
     * Returns whether this chunk holds biome data, always true for 1.17+ chunks.
     *
     * @return true if this chunk holds biome data
     */
    boolean isBiomeData();

    /**
     * Returns whether this is a full chunk, always true for 1.17+ chunks.
     *
     * @return true if this is a full chunk
     */
    boolean isFullChunk();

    boolean isIgnoreOldLightData();

    void setIgnoreOldLightData(boolean ignoreOldLightData);

    /**
     * Returns the chunk section bit mask for chunks &lt; 1.17.
     *
     * @return chunk section bit mask for chunks &lt; 1.17
     * @see #getChunkMask()
     */
    int getBitmask();

    void setBitmask(int bitmask);

    /**
     * Returns the chunk section bit mask, only non-null for 1.17+ chunks.
     *
     * @return chunk section bit mask, only non-null for 1.17+ chunks
     * @see #getBitmask()
     */
    @Nullable
    BitSet getChunkMask();

    void setChunkMask(BitSet chunkSectionMask);

    /**
     * Returns an array of nullable chunk section entries.
     *
     * @return array of nullable chunk sections
     */
    @Nullable
    ChunkSection[] getSections();

    void setSections(ChunkSection[] sections);

    /**
     * Returns the chunk's raw biome data. The format the biomes are stored may vary.
     *
     * @return raw biome data
     */
    int @Nullable [] getBiomeData();

    void setBiomeData(int @Nullable [] biomeData);

    /**
     * Returns a compoundtag containing the chunk's heightmaps if present.
     *
     * @return compoundtag containing heightmaps if present
     */
    @Nullable
    CompoundTag getHeightMap();

    void setHeightMap(@Nullable CompoundTag heightMap);

    Heightmap[] heightmaps();

    void setHeightmaps(Heightmap[] heightmaps);

    /**
     * Returns a list of block entities.
     *
     * @return list of block entities
     */
    List<CompoundTag> getBlockEntities();

    /**
     * Returns a list of block entities.
     *
     * @return list of block entities
     */
    List<BlockEntity> blockEntities();
}
