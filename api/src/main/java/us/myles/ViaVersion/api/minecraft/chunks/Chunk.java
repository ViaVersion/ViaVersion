/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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
package us.myles.ViaVersion.api.minecraft.chunks;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;
import java.util.List;

public interface Chunk {

    int getX();

    int getZ();

    /**
     * @return whether this chunk holds biome data, always true for 1.17+ chunks
     */
    boolean isBiomeData();

    /**
     * @return whether this is a full chunk, always true for 1.17+ chunks
     */
    boolean isFullChunk();

    @Deprecated
    default boolean isGroundUp() {
        return isFullChunk();
    }

    boolean isIgnoreOldLightData();

    void setIgnoreOldLightData(boolean ignoreOldLightData);

    /**
     * @return chunk section bit mask for chunks &lt; 1.17
     * @see #getChunkMask()
     */
    int getBitmask();

    void setBitmask(int bitmask);

    /**
     * @return chunk section bit mask, only non-null available for 1.17+ chunks
     * @see #getBitmask()
     */
    @Nullable
    BitSet getChunkMask();

    void setChunkMask(BitSet chunkSectionMask);

    ChunkSection[] getSections();

    void setSections(ChunkSection[] sections);

    @Nullable
    int[] getBiomeData();

    void setBiomeData(int[] biomeData);

    CompoundTag getHeightMap();

    void setHeightMap(CompoundTag heightMap);

    List<CompoundTag> getBlockEntities();
}
