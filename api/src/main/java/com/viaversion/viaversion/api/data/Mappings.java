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
package com.viaversion.viaversion.api.data;

public interface Mappings {

    /**
     * Returns the mapped id from the given id, or -1 if invalid/out of bounds.
     *
     * @param id unmapped id
     * @return mapped id, or -1 if invalid/out of bounds
     */
    int getNewId(int id);

    /**
     * Returns the mapped id from the given id, or the given default if unmapped/invalid.
     *
     * @param id  unmapped id
     * @param def fallback return value
     * @return mapped id, or def if invalid/out of bounds
     */
    default int getNewIdOrDefault(int id, int def) {
        final int mappedId = getNewId(id);
        return mappedId != -1 ? mappedId : def;
    }

    /**
     * Returns whether the id has a mapping.
     *
     * @param id unmapped id
     * @return whether the id has a mapped id
     */
    default boolean contains(int id) {
        return getNewId(id) != -1;
    }

    /**
     * Manually maps a specific id.
     *
     * @param id       unmapped id
     * @param mappedId mapped id
     * @throws IndexOutOfBoundsException if the unmapped id is invalid
     */
    void setNewId(int id, int mappedId);

    /**
     * Returns amount of unmapped entries, being the size of the mapping.
     *
     * @return amount of unmapped entries
     */
    int size();

    /**
     * Returns the amount of new ids total, even if it does not have a direct mapping.
     * Returns -1 if unknown.
     *
     * @return amount of new ids, or -1 if unknown
     */
    int mappedSize();

    /**
     * Mappings with keys and values swapped.
     *
     * @return mappings with keys and values swapped
     */
    Mappings inverse();
}
