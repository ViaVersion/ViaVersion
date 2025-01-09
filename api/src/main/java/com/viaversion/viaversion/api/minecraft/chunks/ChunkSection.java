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

import org.checkerframework.checker.nullness.qual.Nullable;

public interface ChunkSection {

    /**
     * Size (dimensions) of blocks in a chunks section.
     */
    int SIZE = 16 * 16 * 16; // width * depth * height

    /**
     * Size (dimensions) of biomes in a chunks section.
     */
    int BIOME_SIZE = 4 * 4 * 4;

    static int index(int x, int y, int z) {
        return y << 8 | z << 4 | x;
    }

    static int xFromIndex(int idx) {
        return idx & 0xF;
    }

    static int yFromIndex(int idx) {
        return idx >> 8 & 0xF;
    }

    static int zFromIndex(int idx) {
        return idx >> 4 & 0xF;
    }

    /**
     * Returns the number of non-air blocks in this section.
     *
     * @return non-air blocks in this section
     */
    int getNonAirBlocksCount();

    void setNonAirBlocksCount(int nonAirBlocksCount);

    /**
     * Returns whether this section holds light data.
     * Only true for &lt; 1.14 chunks.
     *
     * @return whether this section holds light data
     */
    default boolean hasLight() {
        return getLight() != null;
    }

    /**
     * Returns the light of the chunk section.
     * Only present for &lt; 1.14 chunks, otherwise sent separately.
     *
     * @return chunk section light if present
     */
    @Nullable ChunkSectionLight getLight();

    void setLight(@Nullable ChunkSectionLight light);

    /**
     * Returns the palette of the given type if present.
     *
     * @param type type of the palette
     * @return palette
     */
    @Nullable DataPalette palette(PaletteType type);

    void addPalette(PaletteType type, DataPalette blockPalette);

    void removePalette(PaletteType type);
}
