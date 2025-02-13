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

import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSectionImpl;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

public class ChunkSectionType1_18 extends Type<ChunkSection> {

    private final PaletteTypeBase blockPaletteType;
    private final PaletteTypeBase biomePaletteType;

    public ChunkSectionType1_18(final int globalPaletteBlockBits, final int globalPaletteBiomeBits) {
        super(ChunkSection.class);
        this.blockPaletteType = new PaletteType1_18(PaletteType.BLOCKS, globalPaletteBlockBits);
        this.biomePaletteType = new PaletteType1_18(PaletteType.BIOMES, globalPaletteBiomeBits);
    }

    protected ChunkSectionType1_18(final PaletteTypeBase blockPaletteType, PaletteTypeBase biomePaletteType) {
        super(ChunkSection.class);
        this.blockPaletteType = blockPaletteType;
        this.biomePaletteType = biomePaletteType;
    }

    @Override
    public ChunkSection read(final ByteBuf buffer) {
        final ChunkSection chunkSection = new ChunkSectionImpl();
        chunkSection.setNonAirBlocksCount(buffer.readShort());
        chunkSection.addPalette(PaletteType.BLOCKS, blockPaletteType.read(buffer));
        chunkSection.addPalette(PaletteType.BIOMES, biomePaletteType.read(buffer));
        return chunkSection;
    }

    @Override
    public void write(final ByteBuf buffer, final ChunkSection section) {
        buffer.writeShort(section.getNonAirBlocksCount());
        blockPaletteType.write(buffer, section.palette(PaletteType.BLOCKS));
        biomePaletteType.write(buffer, section.palette(PaletteType.BIOMES));
    }

    public int serializedSize(final Chunk chunk) {
        int length = 0;
        for (final ChunkSection section : chunk.getSections()) {
            length += Short.BYTES
                + blockPaletteType.serializedSize(section.palette(PaletteType.BLOCKS))
                + biomePaletteType.serializedSize(section.palette(PaletteType.BIOMES));
        }
        return length;
    }
}
