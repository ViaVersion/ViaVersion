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

import com.viaversion.viaversion.api.minecraft.chunks.BaseChunk;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSectionImpl;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import java.util.ArrayList;
import java.util.Arrays;

public class ChunkUtil {

    public static Chunk createEmptyChunk(final int chunkX, final int chunkZ) {
        return createEmptyChunk(chunkX, chunkZ, 16, 0xFFFF);
    }

    public static Chunk createEmptyChunk(final int chunkX, final int chunkZ, final int sectionCount) {
        int sectionBitmask = 0;
        for (int i = 0; i < sectionCount; i++) sectionBitmask = (sectionBitmask << 1) | 1;
        return createEmptyChunk(chunkX, chunkZ, sectionCount, sectionBitmask);
    }

    public static Chunk createEmptyChunk(final int chunkX, final int chunkZ, final int sectionCount, final int bitmask) {
        final ChunkSection[] airSections = new ChunkSection[sectionCount];
        for (int i = 0; i < airSections.length; i++) {
            airSections[i] = new ChunkSectionImpl(true);
            airSections[i].palette(PaletteType.BLOCKS).addId(0);
        }
        return new BaseChunk(chunkX, chunkZ, true, false, bitmask, airSections, new int[256], new ArrayList<>());
    }

    public static void setDummySkylight(final Chunk chunk) {
        setDummySkylight(chunk, false);
    }

    public static void setDummySkylight(final Chunk chunk, final boolean fullbright) {
        for (final ChunkSection section : chunk.getSections()) {
            if (section == null) continue;
            if (section.hasLight()) {
                final byte[] skyLight = new byte[2048];
                if (fullbright) {
                    Arrays.fill(skyLight, (byte) 255);
                }
                section.getLight().setSkyLight(skyLight);
            }
        }
    }

}