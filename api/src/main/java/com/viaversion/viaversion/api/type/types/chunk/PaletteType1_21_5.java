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
import com.viaversion.viaversion.api.minecraft.chunks.DataPaletteImpl;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.type.types.LongArrayType;
import com.viaversion.viaversion.util.CompactArrayUtil;
import io.netty.buffer.ByteBuf;

// Values are written without the explicit length, as the length is always known
public final class PaletteType1_21_5 extends PaletteType1_18 {

    public PaletteType1_21_5(final PaletteType type, final int globalPaletteBits) {
        super(type, globalPaletteBits);
    }

    @Override
    protected void readValues(final ByteBuf buffer, final int bitsPerValue, final DataPaletteImpl palette) {
        if (bitsPerValue == 0) {
            return;
        }

        final int valuesPerLong = (char) (64 / bitsPerValue);
        final int expectedLength = (type.size() + valuesPerLong - 1) / valuesPerLong;
        final long[] values = LongArrayType.readFixedLength(buffer, expectedLength);
        if (values.length != 0) {
            CompactArrayUtil.iterateCompactArrayWithPadding(
                bitsPerValue,
                type.size(),
                values,
                bitsPerValue == globalPaletteBits ? palette::setIdAt : palette::setPaletteIndexAt
            );
        }
    }

    @Override
    protected void writeValues(final ByteBuf buffer, final DataPalette palette, final int bitsPerValue) {
        if (bitsPerValue == 0) {
            return;
        }

        final long[] values = CompactArrayUtil.createCompactArrayWithPadding(
            bitsPerValue,
            type.size(),
            bitsPerValue == globalPaletteBits ? palette::idAt : palette::paletteIndexAt
        );
        LongArrayType.writeFixedLength(buffer, values);
    }

    @Override
    protected int serializedValuesSize(final int values) {
        return Long.BYTES * values;
    }
}
