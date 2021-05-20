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
import org.checkerframework.checker.nullness.qual.Nullable;

public class ChunkSectionImpl implements ChunkSection {

    private final IntList palette;
    private final Int2IntMap inversePalette;
    private final int[] blocks;
    private ChunkSectionLight light;
    private int nonAirBlocksCount;

    public ChunkSectionImpl(boolean holdsLight) {
        this.blocks = new int[SIZE];
        palette = new IntArrayList();
        inversePalette = new Int2IntOpenHashMap();
        inversePalette.defaultReturnValue(-1);
        if (holdsLight) {
            this.light = new ChunkSectionLightImpl();
        }
    }

    public ChunkSectionImpl(boolean holdsLight, int expectedPaletteLength) {
        this.blocks = new int[SIZE];
        if (holdsLight) {
            this.light = new ChunkSectionLightImpl();
        }

        // Pre-size the palette array/map
        palette = new IntArrayList(expectedPaletteLength);
        inversePalette = new Int2IntOpenHashMap(expectedPaletteLength);
        inversePalette.defaultReturnValue(-1);
    }

    @Override
    public int getFlatBlock(int idx) {
        int index = blocks[idx];
        return palette.getInt(index);
    }

    @Override
    public void setFlatBlock(int idx, int id) {
        int index = inversePalette.get(id);
        if (index == -1) {
            index = palette.size();
            palette.add(id);
            inversePalette.put(id, index);
        }

        blocks[idx] = index;
    }

    @Override
    public int getPaletteIndex(int idx) {
        return blocks[idx];
    }

    @Override
    public void setPaletteIndex(int idx, int index) {
        blocks[idx] = index;
    }

    @Override
    public int getPaletteSize() {
        return palette.size();
    }

    @Override
    public int getPaletteEntry(int index) {
        return palette.getInt(index);
    }

    @Override
    public void setPaletteEntry(int index, int id) {
        int oldId = palette.set(index, id);
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
    public void replacePaletteEntry(int oldId, int newId) {
        int index = inversePalette.remove(oldId);
        if (index == -1) return;

        inversePalette.put(newId, index);
        for (int i = 0; i < palette.size(); i++) {
            if (palette.getInt(i) == oldId) {
                palette.set(i, newId);
            }
        }
    }

    @Override
    public void addPaletteEntry(int id) {
        inversePalette.put(id, palette.size());
        palette.add(id);
    }

    @Override
    public void clearPalette() {
        palette.clear();
        inversePalette.clear();
    }

    @Override
    public int getNonAirBlocksCount() {
        return nonAirBlocksCount;
    }

    @Override
    public void setNonAirBlocksCount(int nonAirBlocksCount) {
        this.nonAirBlocksCount = nonAirBlocksCount;
    }

    @Override
    public @Nullable ChunkSectionLight getLight() {
        return light;
    }

    @Override
    public void setLight(@Nullable ChunkSectionLight light) {
        this.light = light;
    }
}
