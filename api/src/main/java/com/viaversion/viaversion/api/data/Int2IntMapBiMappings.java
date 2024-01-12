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

import com.viaversion.viaversion.util.Int2IntBiMap;

public class Int2IntMapBiMappings implements BiMappings {

    private final Int2IntBiMap mappings;
    private final Int2IntMapBiMappings inverse;

    protected Int2IntMapBiMappings(final Int2IntBiMap mappings) {
        this.mappings = mappings;
        this.inverse = new Int2IntMapBiMappings(mappings.inverse(), this);
        mappings.defaultReturnValue(-1);
    }

    private Int2IntMapBiMappings(final Int2IntBiMap mappings, final Int2IntMapBiMappings inverse) {
        this.mappings = mappings;
        this.inverse = inverse;
    }

    public static Int2IntMapBiMappings of(final Int2IntBiMap mappings) {
        return new Int2IntMapBiMappings(mappings);
    }

    @Override
    public int getNewId(final int id) {
        return mappings.get(id);
    }

    @Override
    public void setNewId(final int id, final int mappedId) {
        mappings.put(id, mappedId);
    }

    @Override
    public int size() {
        return mappings.size();
    }

    @Override
    public int mappedSize() {
        return mappings.inverse().size();
    }

    @Override
    public BiMappings inverse() {
        return this.inverse;
    }
}
