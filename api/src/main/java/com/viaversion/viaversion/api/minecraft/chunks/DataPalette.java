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

public interface DataPalette {

    /**
     * Returns the block state of the given index.
     *
     * @param idx block index within the section
     * @return block state of the given index
     */
    int value(int idx);

    /**
     * Returns the block state of the section coordinate.
     *
     * @param x section x
     * @param y section y
     * @param z section z
     * @return block state of the given section coordinate
     */
    default int value(final int x, final int y, final int z) {
        return value(ChunkSection.index(x, y, z));
    }

    /**
     * Set a block state in the chunk section.
     * This method will not update non-air blocks count.
     *
     * @param idx block index within the section
     * @param id  raw or flat id of the block state
     */
    void setValue(int idx, int id);

    /**
     * Set a block state in the chunk section.
     * This method will not update non-air blocks count.
     *
     * @param x  section x
     * @param y  section y
     * @param z  section z
     * @param id raw or flat id of the block state
     */
    default void setValue(final int x, final int y, final int z, final int id) {
        setValue(ChunkSection.index(x, y, z), id);
    }

    // ----------------------------------------------------------------------------

    default int rawDataBlock(final int x, final int y, final int z) {
        return value(x, y, z) >> 4;
    }

    default int dataBlock(final int x, final int y, final int z) {
        return value(x, y, z) & 0xF;
    }

    default void setDataBlock(final int x, final int y, final int z, final int type, final int data) {
        setValue(ChunkSection.index(x, y, z), type << 4 | (data & 0xF));
    }

    default void setDataBlock(final int idx, final int type, final int data) {
        setValue(idx, type << 4 | (data & 0xF));
    }

    // ----------------------------------------------------------------------------

    /**
     * Sets a block to the given palette index.
     *
     * @param idx   block index
     * @param index palette index
     */
    void setIndex(int idx, int index);

    /**
     * Returns the palette index of the given block index.
     *
     * @param idx block index
     * @return palette index of the given block index
     */
    int index(int idx);

    /**
     * Returns the size of the palette.
     *
     * @return palette size
     */
    int size();

    /**
     * Returns the block state assigned to the given palette index.
     *
     * @param index palette index
     * @return block state assigned to the given palette index
     */
    int entry(int index);

    /**
     * Assigns a block state assigned to the given palette index.
     *
     * @param index palette index
     * @param id    block state
     */
    void setEntry(int index, int id);

    /**
     * Replaces a block state in the palette.
     *
     * @param oldId old block state
     * @param newId new block state
     */
    void replaceEntry(int oldId, int newId);

    /**
     * Adds a new block state to the palette.
     *
     * @param id block state
     */
    void addEntry(int id);

    /**
     * Clears the palette.
     */
    void clear();

    /**
     * Returns the type of data this palette holds.
     *
     * @return type of data this palette holds
     */
    PaletteType type();
}
