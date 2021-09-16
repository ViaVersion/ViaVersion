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
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.DataPaletteImpl;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.type.PartialType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.CompactArrayUtil;
import io.netty.buffer.ByteBuf;

public final class PaletteType1_18 extends PartialType<DataPalette, PaletteType> {
    private static final int GLOBAL_PALETTE = 15;

    public PaletteType1_18(final PaletteType type) {
        super(type, DataPalette.class);
    }

    @Override
    public DataPalette read(final ByteBuf buffer, final PaletteType type) throws Exception {
        int bitsPerValue = buffer.readByte();
        final int originalBitsPerValue = bitsPerValue;

        if (bitsPerValue > type.highestBitsPerValue()) {
            bitsPerValue = GLOBAL_PALETTE;
        }

        // Read palette
        final DataPaletteImpl palette;
        if (bitsPerValue == 0) {
            //TODO Create proper singleton palette Object
            palette = new DataPaletteImpl(1);
            palette.addId(Type.VAR_INT.readPrimitive(buffer));
            Type.VAR_INT.readPrimitive(buffer); // 0 values length
            return palette;
        }

        if (bitsPerValue != GLOBAL_PALETTE) {
            final int paletteLength = Type.VAR_INT.readPrimitive(buffer);
            palette = new DataPaletteImpl(paletteLength);
            for (int i = 0; i < paletteLength; i++) {
                palette.addId(Type.VAR_INT.readPrimitive(buffer));
            }
        } else {
            palette = new DataPaletteImpl();
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
                    bitsPerValue == GLOBAL_PALETTE ? palette::setIdAt : palette::setPaletteIndexAt);
        }
        return palette;
    }

    @Override
    public void write(final ByteBuf buffer, final PaletteType type, final DataPalette palette) throws Exception {
        int bitsPerValue;
        if (palette.size() > 1) {
            // 1, 2, and 3 bit linear palettes can't be read by the client
            bitsPerValue = type == PaletteType.BLOCKS ? 4 : 1;
            while (palette.size() > 1 << bitsPerValue) {
                bitsPerValue += 1;
            }

            if (bitsPerValue > type.highestBitsPerValue()) {
                bitsPerValue = GLOBAL_PALETTE;
            }
        } else {
            bitsPerValue = 0;
        }

        buffer.writeByte(bitsPerValue);

        if (bitsPerValue == 0) {
            // Write single value
            Type.VAR_INT.writePrimitive(buffer, palette.idByIndex(0));
            Type.VAR_INT.writePrimitive(buffer, 0); // Empty values length
            return;
        }

        if (bitsPerValue != GLOBAL_PALETTE) {
            // Write pallete
            Type.VAR_INT.writePrimitive(buffer, palette.size());
            for (int i = 0; i < palette.size(); i++) {
                Type.VAR_INT.writePrimitive(buffer, palette.idByIndex(i));
            }
        }

        final long[] data = CompactArrayUtil.createCompactArrayWithPadding(bitsPerValue, ChunkSection.SIZE, bitsPerValue == GLOBAL_PALETTE ? palette::idAt : palette::paletteIndexAt);
        Type.VAR_INT.writePrimitive(buffer, data.length);
        for (final long l : data) {
            buffer.writeLong(l);
        }
    }
}
