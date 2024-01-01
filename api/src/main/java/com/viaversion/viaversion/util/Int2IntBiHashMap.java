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
package com.viaversion.viaversion.util;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;

public class Int2IntBiHashMap implements Int2IntBiMap {

    private final Int2IntMap map;
    private final Int2IntBiHashMap inverse;

    public Int2IntBiHashMap() {
        this.map = new Int2IntOpenHashMap();
        this.inverse = new Int2IntBiHashMap(this, -1);
    }

    public Int2IntBiHashMap(final int expected) {
        this.map = new Int2IntOpenHashMap(expected);
        this.inverse = new Int2IntBiHashMap(this, expected);
    }

    private Int2IntBiHashMap(final Int2IntBiHashMap inverse, final int expected) {
        this.map = expected != -1 ? new Int2IntOpenHashMap(expected) : new Int2IntOpenHashMap();
        this.inverse = inverse;
    }

    @Override
    public Int2IntBiMap inverse() {
        return inverse;
    }

    @Override
    public int put(final int key, final int value) {
        if (containsKey(key) && value == get(key)) return value;

        Preconditions.checkArgument(!containsValue(value), "value already present: %s", value);
        map.put(key, value);
        inverse.map.put(value, key);
        return defaultReturnValue();
    }

    @Override
    public boolean remove(final int key, final int value) {
        map.remove(key, value);
        return inverse.map.remove(key, value);
    }

    @Override
    public int get(final int key) {
        return map.get(key);
    }

    @Override
    public void clear() {
        map.clear();
        inverse.map.clear();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public void putAll(@NonNull Map<? extends Integer, ? extends Integer> m) {
        for (final Map.Entry<? extends Integer, ? extends Integer> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void defaultReturnValue(final int rv) {
        map.defaultReturnValue(rv);
        inverse.map.defaultReturnValue(rv);
    }

    @Override
    public int defaultReturnValue() {
        return map.defaultReturnValue();
    }

    @Override
    public ObjectSet<Entry> int2IntEntrySet() {
        return map.int2IntEntrySet();
    }

    @Override
    public @NonNull IntSet keySet() {
        return map.keySet();
    }

    @Override
    public @NonNull IntSet values() {
        return inverse.map.keySet();
    }

    @Override
    public boolean containsKey(final int key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(final int value) {
        return inverse.map.containsKey(value);
    }
}
