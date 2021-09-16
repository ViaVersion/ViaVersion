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
package com.viaversion.viaversion.api.type.types.version;

import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSectionImpl;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.DataPaletteImpl;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.CompactArrayUtil;
import io.netty.buffer.ByteBuf;

public class ChunkSectionType1_18 extends Type<ChunkSection> {
    private static final int GLOBAL_PALETTE = 15;

    public ChunkSectionType1_18() {
        super("Chunk Section Type", ChunkSection.class);
    }

    @Override
    public ChunkSection read(final ByteBuf buffer) throws Exception {
        final ChunkSection chunkSection = new ChunkSectionImpl();
        chunkSection.setNonAirBlocksCount(buffer.readShort());
        chunkSection.addPalette(readPalette(buffer, PaletteType.BLOCKS));
        chunkSection.addPalette(readPalette(buffer, PaletteType.BIOMES));
        return chunkSection;
    }

    @Override
    public void write(final ByteBuf buffer, final ChunkSection section) throws Exception {
        buffer.writeShort(section.getNonAirBlocksCount());
        writePalette(buffer, section.palette(PaletteType.BLOCKS));
        writePalette(buffer, section.palette(PaletteType.BIOMES));
    }

    private DataPalette readPalette(final ByteBuf buffer, final PaletteType type) {
        int bitsPerValue = buffer.readByte();
        final int originalBitsPerValue = bitsPerValue;

        if (bitsPerValue > type.highestBitsPerValue()) {
            bitsPerValue = GLOBAL_PALETTE;
        }

        // Read palette
        final DataPaletteImpl palette;
        if (bitsPerValue == 0) {
            //TODO Create proper singleton palette Object
            palette = new DataPaletteImpl(type, 1);
            palette.addEntry(Type.VAR_INT.readPrimitive(buffer));
            Type.VAR_INT.readPrimitive(buffer); // 0 values length
            return palette;
        }

        if (bitsPerValue != GLOBAL_PALETTE) {
            final int paletteLength = Type.VAR_INT.readPrimitive(buffer);
            palette = new DataPaletteImpl(type, paletteLength);
            for (int i = 0; i < paletteLength; i++) {
                palette.addEntry(Type.VAR_INT.readPrimitive(buffer));
            }
        } else {
            palette = new DataPaletteImpl(type);
        }

        // Read values
        final long[] values = new long[Type.VAR_INT.readPrimitive(buffer)];
        if (values.length > 0) {
            final char valuesPerLong = (char) (64 / bitsPerValue);
            final int expectedLength = (ChunkSection.SIZE + valuesPerLong - 1) / valuesPerLong;
            if (values.length != expectedLength) {
                throw new IllegalStateException("Palette data length (" + values.length + ") does not match expected length (" + expectedLength + ")! bitsPerValue=" + bitsPerValue + ", originalBitsPerValue=" + originalBitsPerValue);
            }

            for (int i = 0; i < values.length; i++) {
                values[i] = buffer.readLong();
            }
            CompactArrayUtil.iterateCompactArrayWithPadding(bitsPerValue, ChunkSection.SIZE, values,
                    bitsPerValue == GLOBAL_PALETTE ? palette::setValue : palette::setIndex);
        }
        return palette;
    }

    private void writePalette(final ByteBuf buffer, final DataPalette palette) {
        int bitsPerValue = 0;
        while (palette.size() > 1 << bitsPerValue) {
            bitsPerValue += 1;
        }

        if (bitsPerValue > palette.type().highestBitsPerValue()) {
            bitsPerValue = GLOBAL_PALETTE;
        }

        buffer.writeByte(bitsPerValue);

        if (bitsPerValue == 0) {
            // Write single value
            Type.VAR_INT.writePrimitive(buffer, palette.entry(0));
            Type.VAR_INT.writePrimitive(buffer, 0); // Empty values length
            return;
        }

        if (bitsPerValue != GLOBAL_PALETTE) {
            // Write pallete
            Type.VAR_INT.writePrimitive(buffer, palette.size());
            for (int i = 0; i < palette.size(); i++) {
                Type.VAR_INT.writePrimitive(buffer, palette.entry(i));
            }
        }

        final long[] data = CompactArrayUtil.createCompactArrayWithPadding(bitsPerValue, ChunkSection.SIZE, bitsPerValue == GLOBAL_PALETTE ? palette::value : palette::index);
        Type.VAR_INT.writePrimitive(buffer, data.length);
        for (final long l : data) {
            buffer.writeLong(l);
        }
    }
}
