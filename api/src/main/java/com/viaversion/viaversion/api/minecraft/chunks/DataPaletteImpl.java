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

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public final class DataPaletteImpl implements DataPalette {

    private final IntList palette;
    private final Int2IntMap inversePalette;
    private final int[] values;
    private final PaletteType type;

    public DataPaletteImpl(final PaletteType type) {
        this.type = type;
        this.values = new int[ChunkSection.SIZE];
        palette = new IntArrayList();
        inversePalette = new Int2IntOpenHashMap();
        inversePalette.defaultReturnValue(-1);
    }

    public DataPaletteImpl(final PaletteType type, final int expectedPaletteLength) {
        this.type = type;
        this.values = new int[ChunkSection.SIZE];
        // Pre-size the palette array/map
        palette = new IntArrayList(expectedPaletteLength);
        inversePalette = new Int2IntOpenHashMap(expectedPaletteLength);
        inversePalette.defaultReturnValue(-1);
    }

    @Override
    public int value(final int idx) {
        final int index = values[idx];
        return palette.getInt(index);
    }

    @Override
    public void setValue(final int idx, final int id) {
        int index = inversePalette.get(id);
        if (index == -1) {
            index = palette.size();
            palette.add(id);
            inversePalette.put(id, index);
        }

        values[idx] = index;
    }

    @Override
    public int index(final int idx) {
        return values[idx];
    }

    @Override
    public void setIndex(final int idx, final int index) {
        values[idx] = index;
    }

    @Override
    public int size() {
        return palette.size();
    }

    @Override
    public int entry(final int index) {
        return palette.getInt(index);
    }

    @Override
    public void setEntry(final int index, final int id) {
        final int oldId = palette.set(index, id);
        if (oldId == id) return;

        inversePalette.put(id, index);
        if (inversePalette.get(oldId) == index) {
            inversePalette.remove(oldId);
            for (int i = 0; i < palette.size(); i++) {
                if (palette.getInt(i) == oldId) {
                    inversePalette.put(oldId, i);
                    break;
                }
            }
        }
    }

    @Override
    public void replaceEntry(final int oldId, final int newId) {
        final int index = inversePalette.remove(oldId);
        if (index == -1) return;

        inversePalette.put(newId, index);
        for (int i = 0; i < palette.size(); i++) {
            if (palette.getInt(i) == oldId) {
                palette.set(i, newId);
            }
        }
    }

    @Override
    public void addEntry(final int id) {
        inversePalette.put(id, palette.size());
        palette.add(id);
    }

    @Override
    public void clear() {
        palette.clear();
        inversePalette.clear();
    }

    @Override
    public PaletteType type() {
        return type;
    }
}
