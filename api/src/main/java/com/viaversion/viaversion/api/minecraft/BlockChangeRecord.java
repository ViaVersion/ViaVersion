/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
package com.viaversion.viaversion.api.minecraft;

public interface BlockChangeRecord {

    /**
     * Returns the relative x coordinate within the chunk section.
     *
     * @return relative x coordinate within the chunk section
     */
    byte getSectionX();

    /**
     * Returns the relative y coordinate within the chunk section.
     *
     * @return relative y coordinate within the chunk section
     */
    byte getSectionY();

    /**
     * Returns the relative z coordinate within the chunk section.
     *
     * @return relative z coordinate within the chunk section
     */
    byte getSectionZ();

    /**
     * Returns the absolute y coordinate based on the given chunk section y.
     *
     * @param chunkSectionY chunk section
     * @return absolute y coordinate
     */
    short getY(int chunkSectionY);

    /**
     * Returns the absolute y coordinate - only works for sub 1.16 protocols.
     *
     * @return absolute y coordinate
     */
    default short getY() {
        return getY(-1);
    }

    int getBlockId();

    void setBlockId(int blockId);
}