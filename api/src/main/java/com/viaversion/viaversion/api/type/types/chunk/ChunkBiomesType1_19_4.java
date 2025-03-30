/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
package com.viaversion.viaversion.api.type.types.chunk;

import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

public class ChunkBiomesType1_19_4 extends Type<DataPalette[]> {
    private final PaletteTypeBase paletteType;
    private final int ySectionCount;

    public ChunkBiomesType1_19_4(final int ySectionCount, final int globalPaletteBiomeBits) {
        this(ySectionCount, new PaletteType1_18(PaletteType.BIOMES, globalPaletteBiomeBits));
    }

    protected ChunkBiomesType1_19_4(final int ySectionCount, final PaletteTypeBase paletteType) {
        super(DataPalette[].class);
        this.paletteType = paletteType;
        this.ySectionCount = ySectionCount;
    }

    @Override
    public DataPalette[] read(final ByteBuf buffer) {
        final ByteBuf data = buffer.readSlice(Types.VAR_INT.readPrimitive(buffer));
        final DataPalette[] sections = new DataPalette[ySectionCount];
        for (int i = 0; i < ySectionCount; i++) {
            sections[i] = paletteType.read(data);
        }
        return sections;
    }

    @Override
    public void write(final ByteBuf buffer, final DataPalette[] value) {
        int size = 0;
        for (final DataPalette biomes : value) {
            size += paletteType.serializedSize(biomes);
        }
        Types.VAR_INT.writePrimitive(buffer, size);
        for (final DataPalette biomes : value) {
            paletteType.write(buffer, biomes);
        }
    }
}
