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

import java.util.Arrays;

public class IntArrayMappings implements Mappings {
    private final int[] mappings;
    private final int mappedIds;

    protected IntArrayMappings(final int[] mappings, final int mappedIds) {
        this.mappings = mappings;
        this.mappedIds = mappedIds;
    }

    public static IntArrayMappings of(final int[] mappings, final int mappedIds) {
        return new IntArrayMappings(mappings, mappedIds);
    }

    @Override
    public int getNewId(int id) {
        return id >= 0 && id < mappings.length ? mappings[id] : -1;
    }

    @Override
    public void setNewId(int id, int mappedId) {
        mappings[id] = mappedId;
    }

    @Override
    public int size() {
        return mappings.length;
    }

    @Override
    public int mappedSize() {
        return mappedIds;
    }

    @Override
    public Mappings inverse() {
        final int[] inverse = new int[mappedIds];
        Arrays.fill(inverse, -1);
        for (int id = 0; id < mappings.length; id++) {
            final int mappedId = mappings[id];
            if (mappedId != -1 && inverse[mappedId] == -1) {
                inverse[mappedId] = id;
            }
        }
        return of(inverse, mappings.length);
    }

    public int[] raw() {
        return mappings;
    }
}
