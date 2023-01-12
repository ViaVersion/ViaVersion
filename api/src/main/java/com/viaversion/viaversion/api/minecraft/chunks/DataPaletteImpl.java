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
package com.viaversion.viaversion.api.minecraft.chunks;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public final class DataPaletteImpl implements DataPalette {

    private final IntList palette;
    private final Int2IntMap inversePalette;
    private final int[] values;
    private final int sizeBits;

    public DataPaletteImpl(final int valuesLength) {
        this(valuesLength, 8);
    }

    public DataPaletteImpl(final int valuesLength, final int expectedPaletteLength) {
        this.values = new int[valuesLength];
        sizeBits = Integer.numberOfTrailingZeros(valuesLength) / 3;
        // Pre-size the palette array/map
        palette = new IntArrayList(expectedPaletteLength);
        inversePalette = new Int2IntOpenHashMap(expectedPaletteLength);
        inversePalette.defaultReturnValue(-1);
    }

    @Override
    public int index(final int x, final int y, final int z) {
        return (y << this.sizeBits | z) << this.sizeBits | x;
    }

    @Override
    public int idAt(final int sectionCoordinate) {
        final int index = values[sectionCoordinate];
        return palette.getInt(index);
    }

    @Override
    public void setIdAt(final int sectionCoordinate, final int id) {
        int index = inversePalette.get(id);
        if (index == -1) {
            index = palette.size();
            palette.add(id);
            inversePalette.put(id, index);
        }

        values[sectionCoordinate] = index;
    }

    @Override
    public int paletteIndexAt(final int packedCoordinate) {
        return values[packedCoordinate];
    }

    @Override
    public void setPaletteIndexAt(final int sectionCoordinate, final int index) {
        values[sectionCoordinate] = index;
    }

    @Override
    public int size() {
        return palette.size();
    }

    @Override
    public int idByIndex(final int index) {
        return palette.getInt(index);
    }

    @Override
    public void setIdByIndex(final int index, final int id) {
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
    public void replaceId(final int oldId, final int newId) {
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
    public void addId(final int id) {
        inversePalette.put(id, palette.size());
        palette.add(id);
    }

    @Override
    public void clear() {
        palette.clear();
        inversePalette.clear();
    }
}
