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
package com.viaversion.viaversion.api.minecraft.chunks;

import org.checkerframework.checker.nullness.qual.Nullable;

public interface ChunkSection {

    /**
     * Size (dimensions) of blocks in a chunks section.
     */
    int SIZE = 16 * 16 * 16; // width * depth * height

    /**
     * Returns the block index of the given coordinates within a section.
     *
     * @param x section x
     * @param y section y
     * @param z section z
     * @return section block index
     */
    static int index(int x, int y, int z) {
        return y << 8 | z << 4 | x;
    }

    int getFlatBlock(int idx);

    int getFlatBlock(int x, int y, int z);

    /**
     * Set a block state in the chunk
     * This method will not update non-air blocks count
     *
     * @param idx Index
     * @param id  The raw or flat id of the block
     */
    void setFlatBlock(int idx, int id);

    default void setFlatBlock(int x, int y, int z, int type) {
        setFlatBlock(index(x, y, z), type);
    }

    default int getBlockWithoutData(int x, int y, int z) {
        return getFlatBlock(x, y, z) >> 4;
    }

    default int getBlockData(int x, int y, int z) {
        return getFlatBlock(x, y, z) & 0xF;
    }

    /**
     * Set a block in the chunks
     * This method will not update non-air blocks count
     *
     * @param x    Block X
     * @param y    Block Y
     * @param z    Block Z
     * @param type The type of the block
     * @param data The data value of the block
     */
    default void setBlockWithData(int x, int y, int z, int type, int data) {
        setFlatBlock(index(x, y, z), type << 4 | (data & 0xF));
    }

    default void setBlockWithData(int idx, int type, int data) {
        setFlatBlock(idx, type << 4 | (data & 0xF));
    }

    void setPaletteIndex(int idx, int index);

    int getPaletteIndex(int idx);

    int getPaletteSize();

    int getPaletteEntry(int index);

    void setPaletteEntry(int index, int id);

    void replacePaletteEntry(int oldId, int newId);

    void addPaletteEntry(int id);

    void clearPalette();

    int getNonAirBlocksCount();

    void setNonAirBlocksCount(int nonAirBlocksCount);

    default boolean hasLight() {
        return getLight() != null;
    }

    @Nullable ChunkSectionLight getLight();

    void setLight(@Nullable ChunkSectionLight light);
}
