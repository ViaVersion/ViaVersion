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
package com.viaversion.viaversion.api.type.types.chunk;

import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSectionImpl;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.CompactArrayUtil;
import io.netty.buffer.ByteBuf;

public class ChunkSectionType1_9 extends Type<ChunkSection> {
    private static final int GLOBAL_PALETTE = 13;

    public ChunkSectionType1_9() {
        super(ChunkSection.class);
    }

    @Override
    public ChunkSection read(ByteBuf buffer) throws Exception {
        // Reaad bits per block
        int bitsPerBlock = buffer.readUnsignedByte();
        if (bitsPerBlock < 4) {
            bitsPerBlock = 4;
        }
        if (bitsPerBlock > 8) {
            bitsPerBlock = GLOBAL_PALETTE;
        }

        // Read palette
        int paletteLength = Type.VAR_INT.readPrimitive(buffer);
        ChunkSection chunkSection = bitsPerBlock != GLOBAL_PALETTE ? new ChunkSectionImpl(true, paletteLength) : new ChunkSectionImpl(true);
        DataPalette blockPalette = chunkSection.palette(PaletteType.BLOCKS);
        for (int i = 0; i < paletteLength; i++) {
            if (bitsPerBlock != GLOBAL_PALETTE) {
                blockPalette.addId(Type.VAR_INT.readPrimitive(buffer));
            } else {
                Type.VAR_INT.readPrimitive(buffer);
            }
        }

        // Read blocks
        long[] blockData = Type.LONG_ARRAY_PRIMITIVE.read(buffer);
        if (blockData.length > 0) {
            int expectedLength = (int) Math.ceil(ChunkSection.SIZE * bitsPerBlock / 64.0);
            if (blockData.length == expectedLength) {
                CompactArrayUtil.iterateCompactArray(bitsPerBlock, ChunkSection.SIZE, blockData,
                        bitsPerBlock == GLOBAL_PALETTE ? blockPalette::setIdAt : blockPalette::setPaletteIndexAt);
            }
        }

        return chunkSection;
    }

    @Override
    public void write(ByteBuf buffer, ChunkSection chunkSection) throws Exception {
        int bitsPerBlock = 4;
        DataPalette blockPalette = chunkSection.palette(PaletteType.BLOCKS);
        while (blockPalette.size() > 1 << bitsPerBlock) {
            bitsPerBlock += 1;
        }

        if (bitsPerBlock > 8) {
            bitsPerBlock = GLOBAL_PALETTE;
        }

        buffer.writeByte(bitsPerBlock);

        // Write palette
        if (bitsPerBlock != GLOBAL_PALETTE) {
            Type.VAR_INT.writePrimitive(buffer, blockPalette.size());
            for (int i = 0; i < blockPalette.size(); i++) {
                Type.VAR_INT.writePrimitive(buffer, blockPalette.idByIndex(i));
            }
        } else {
            Type.VAR_INT.writePrimitive(buffer, 0);
        }

        long[] data = CompactArrayUtil.createCompactArray(bitsPerBlock, ChunkSection.SIZE,
                bitsPerBlock == GLOBAL_PALETTE ? blockPalette::idAt : blockPalette::paletteIndexAt);
        Type.LONG_ARRAY_PRIMITIVE.write(buffer, data);
    }
}
