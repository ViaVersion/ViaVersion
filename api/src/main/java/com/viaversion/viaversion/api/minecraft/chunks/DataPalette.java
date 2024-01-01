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
package com.viaversion.viaversion.api.minecraft.chunks;

public interface DataPalette {

    /**
     * Returns the packet section index of the given coordinates.
     *
     * @param x x
     * @param y y
     * @param z z
     * @return packed section index of the given coordinates
     */
    int index(final int x, final int y, final int z);

    /**
     * Returns the value of the given chunk coordinate.
     *
     * @param sectionCoordinate section index within the section
     * @return section state of the given index
     */
    int idAt(int sectionCoordinate);

    /**
     * Returns the value of the section coordinate.
     *
     * @param sectionX section x
     * @param sectionY section y
     * @param sectionZ section z
     * @return id of the given section coordinate
     */
    default int idAt(final int sectionX, final int sectionY, final int sectionZ) {
        return idAt(index(sectionX, sectionY, sectionZ));
    }

    /**
     * Set a value in the chunk section.
     * This method does not update non-air blocks count.
     *
     * @param sectionCoordinate section index within the section
     * @param id                id value
     */
    void setIdAt(int sectionCoordinate, int id);

    /**
     * Set a value in the chunk section.
     * This method does not update non-air blocks count.
     *
     * @param sectionX section x
     * @param sectionY section y
     * @param sectionZ section z
     * @param id       id value
     */
    default void setIdAt(final int sectionX, final int sectionY, final int sectionZ, final int id) {
        setIdAt(index(sectionX, sectionY, sectionZ), id);
    }

    /**
     * Returns the id assigned to the given palette index.
     *
     * @param index palette index
     * @return id assigned to the given palette index
     */
    int idByIndex(int index);

    /**
     * Assigns an id assigned to the given palette index.
     *
     * @param index palette index
     * @param id    id value
     */
    void setIdByIndex(int index, int id);

    /**
     * Returns the palette index of the given section index.
     *
     * @param packedCoordinate section index
     * @return palette index of the given section index
     */
    int paletteIndexAt(int packedCoordinate);

    /**
     * Sets the index of the given section coordinate.
     *
     * @param sectionCoordinate section index
     * @param index             palette index
     */
    void setPaletteIndexAt(int sectionCoordinate, int index);

    /**
     * Adds a new id to the palette.
     *
     * @param id id value
     */
    void addId(int id);

    /**
     * Replaces an id in the palette.
     *
     * @param oldId old id
     * @param newId new id
     */
    void replaceId(int oldId, int newId);

    /**
     * Returns the size of the palette.
     *
     * @return palette size
     */
    int size();

    /**
     * Clears the palette.
     */
    void clear();
}
