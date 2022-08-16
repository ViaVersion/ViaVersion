/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2022 ViaVersion and contributors
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

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

public class Int2IntMapMappings implements Mappings {
    private final Int2IntMap mappings;
    private final int mappedIds;

    protected Int2IntMapMappings(final Int2IntMap mappings, final int mappedIds) {
        this.mappings = mappings;
        this.mappedIds = mappedIds;
        mappings.defaultReturnValue(-1);
    }

    public static Int2IntMapMappings of(final Int2IntMap mappings, final int mappedIds) {
        return new Int2IntMapMappings(mappings, mappedIds);
    }

    public static Int2IntMapMappings of() {

        return new Int2IntMapMappings(new Int2IntOpenHashMap(), -1);
    }

    @Override
    public int getNewId(final int id) {
        return mappings.get(id);
    }

    @Override
    public void setNewId(final int id, final int newId) {
        mappings.put(id, newId);
    }

    @Override
    public int size() {
        return mappings.size();
    }

    @Override
    public int mappedSize() {
        return mappedIds;
    }
}
