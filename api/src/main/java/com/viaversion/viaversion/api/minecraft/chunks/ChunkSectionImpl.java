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

import java.util.EnumMap;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ChunkSectionImpl implements ChunkSection {

    private final EnumMap<PaletteType, DataPalette> palettes = new EnumMap<>(PaletteType.class);
    private ChunkSectionLight light;
    private int nonAirBlocksCount;

    public ChunkSectionImpl() {
    }

    public ChunkSectionImpl(final boolean holdsLight) {
        addPalette(PaletteType.BLOCKS, new DataPaletteImpl(ChunkSection.SIZE));
        if (holdsLight) {
            this.light = new ChunkSectionLightImpl();
        }
    }

    public ChunkSectionImpl(final boolean holdsLight, final int expectedPaletteLength) {
        addPalette(PaletteType.BLOCKS, new DataPaletteImpl(ChunkSection.SIZE, expectedPaletteLength));
        if (holdsLight) {
            this.light = new ChunkSectionLightImpl();
        }
    }

    @Override
    public int getNonAirBlocksCount() {
        return nonAirBlocksCount;
    }

    @Override
    public void setNonAirBlocksCount(final int nonAirBlocksCount) {
        this.nonAirBlocksCount = nonAirBlocksCount;
    }

    @Override
    public @Nullable ChunkSectionLight getLight() {
        return light;
    }

    @Override
    public void setLight(@Nullable final ChunkSectionLight light) {
        this.light = light;
    }

    @Override
    public DataPalette palette(final PaletteType type) {
        return palettes.get(type);
    }

    @Override
    public void addPalette(final PaletteType type, final DataPalette palette) {
        palettes.put(type, palette);
    }

    @Override
    public void removePalette(final PaletteType type) {
        palettes.remove(type);
    }
}
