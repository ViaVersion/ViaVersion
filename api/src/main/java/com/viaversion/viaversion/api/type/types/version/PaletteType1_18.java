/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2022 ViaVersion and contributors
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

import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.DataPaletteImpl;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.CompactArrayUtil;
import com.viaversion.viaversion.util.MathUtil;
import io.netty.buffer.ByteBuf;

public final class PaletteType1_18 extends Type<DataPalette> {
    private final int globalPaletteBits;
    private final PaletteType type;

    public PaletteType1_18(final PaletteType type, final int globalPaletteBits) {
        super(DataPalette.class);
        this.globalPaletteBits = globalPaletteBits;
        this.type = type;
    }

    @Override
    public DataPalette read(final ByteBuf buffer) throws Exception {
        final int originalBitsPerValue = buffer.readByte();
        int bitsPerValue = originalBitsPerValue;

        // Read palette
        final DataPaletteImpl palette;
        if (bitsPerValue == 0) {
            //TODO Create proper singleton palette Object
            palette = new DataPaletteImpl(type.size(), 1);
            palette.addId(Type.VAR_INT.readPrimitive(buffer));
            Type.VAR_INT.readPrimitive(buffer); // 0 values length
            return palette;
        }

        if (bitsPerValue < 0 || bitsPerValue > type.highestBitsPerValue()) {
            bitsPerValue = globalPaletteBits;
        } else if (type == PaletteType.BLOCKS && bitsPerValue < 4) {
            bitsPerValue = 4; // Linear block palette values are always 4 bits
        }

        if (bitsPerValue != globalPaletteBits) {
            final int paletteLength = Type.VAR_INT.readPrimitive(buffer);
            palette = new DataPaletteImpl(type.size(), paletteLength);
            for (int i = 0; i < paletteLength; i++) {
                palette.addId(Type.VAR_INT.readPrimitive(buffer));
            }
        } else {
            palette = new DataPaletteImpl(type.size());
        }

        // Read values
        final int valuesLength = Type.VAR_INT.readPrimitive(buffer);
        if (valuesLength > 0) {
            final int valuesPerLong = (char) (64 / bitsPerValue);
            final int expectedLength = (type.size() + valuesPerLong - 1) / valuesPerLong;
            if (valuesLength != expectedLength) {
                throw new IllegalStateException("Palette data length (" + valuesLength + ") does not match expected length (" + expectedLength + ")! bitsPerValue=" + bitsPerValue + ", originalBitsPerValue=" + originalBitsPerValue);
            }

            final long[] values = new long[valuesLength];
            for (int i = 0; i < values.length; i++) {
                values[i] = buffer.readLong();
            }
            CompactArrayUtil.iterateCompactArrayWithPadding(bitsPerValue, type.size(), values,
                    bitsPerValue == globalPaletteBits ? palette::setIdAt : palette::setPaletteIndexAt);
        }
        return palette;
    }

    @Override
    public void write(final ByteBuf buffer, final DataPalette palette) throws Exception {
        if (palette.size() == 1) {
            // Single value palette
            buffer.writeByte(0); // 0 bit storage
            Type.VAR_INT.writePrimitive(buffer, palette.idByIndex(0));
            Type.VAR_INT.writePrimitive(buffer, 0); // Empty values length
            return;
        }/* else if (palette.size() == 0) {
            Via.getPlatform().getLogger().warning("Palette size is 0!");
        }*/

        // 1, 2, and 3 bit linear block palettes can't be read by the client
        final int min = type == PaletteType.BLOCKS ? 4 : 1;
        int bitsPerValue = Math.max(min, MathUtil.ceilLog2(palette.size()));
        if (bitsPerValue > type.highestBitsPerValue()) {
            bitsPerValue = globalPaletteBits;
        }

        buffer.writeByte(bitsPerValue);

        if (bitsPerValue != globalPaletteBits) {
            // Write pallete
            Type.VAR_INT.writePrimitive(buffer, palette.size());
            for (int i = 0; i < palette.size(); i++) {
                Type.VAR_INT.writePrimitive(buffer, palette.idByIndex(i));
            }
        }

        final long[] data = CompactArrayUtil.createCompactArrayWithPadding(bitsPerValue, type.size(), bitsPerValue == globalPaletteBits ? palette::idAt : palette::paletteIndexAt);
        Type.VAR_INT.writePrimitive(buffer, data.length);
        for (final long l : data) {
            buffer.writeLong(l);
        }
    }
}
