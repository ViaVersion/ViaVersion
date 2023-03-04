/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2023 ViaVersion and contributors
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

public class IntArrayBiMappings implements BiMappings {

    private final Mappings mappings;
    private final IntArrayBiMappings inverse;

    protected IntArrayBiMappings(final IntArrayMappings mappings) {
        this.mappings = mappings;

        final int[] raw = mappings.raw();
        final int[] inverseMappings = new int[mappings.mappedSize()];
        Arrays.fill(inverseMappings, -1);
        for (int id = 0; id < raw.length; id++) {
            final int mappedId = raw[id];
            inverseMappings[mappedId] = id;
        }
        this.inverse = new IntArrayBiMappings(new IntArrayMappings(inverseMappings, raw.length), this);
    }

    protected IntArrayBiMappings(final Mappings mappings, final IntArrayBiMappings inverse) {
        this.mappings = mappings;
        this.inverse = inverse;
    }

    private IntArrayBiMappings(final int[] mappings, final int mappedIds) {
        this(new IntArrayMappings(mappings, mappedIds));
    }

    public static IntArrayBiMappings of(final IntArrayMappings mappings) {
        return new IntArrayBiMappings(mappings);
    }

    public static Mappings.Builder<IntArrayBiMappings> builder() {
        return new Builder<>(IntArrayBiMappings::new);
    }

    @Override
    public BiMappings inverse() {
        return this.inverse;
    }

    @Override
    public int getNewId(final int id) {
        return mappings.getNewId(id);
    }

    @Override
    public void setNewId(final int id, final int mappedId) {
        mappings.setNewId(id, mappedId);
        inverse.mappings.setNewId(mappedId, id);
    }

    @Override
    public int size() {
        return mappings.size();
    }

    @Override
    public int mappedSize() {
        return mappings.mappedSize();
    }

    @Override
    public Mappings createInverse() {
        return mappings.createInverse();
    }
}
