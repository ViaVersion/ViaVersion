/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.common.type;

import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Lengths read from a buffer must be validated against the bytes they actually require, before anything is
 * allocated for them. Otherwise a malformed packet can make a read allocate far more than the packet's own size.
 */
class LengthValidationTest {

    @Test
    void testLongArrayLengthCountsBytes() {
        final ByteBuf buf = Unpooled.buffer();
        Types.VAR_INT.writePrimitive(buf, 1024); // 1024 longs require 8192 bytes
        buf.writeBytes(new byte[1024]);

        // Must be rejected before allocating long[1024], not while reading past the end of the buffer
        Assertions.assertThrows(IllegalArgumentException.class, () -> Types.LONG_ARRAY_PRIMITIVE.read(buf));
    }

    @Test
    void testIntArrayLengthCountsBytes() {
        final ByteBuf buf = Unpooled.buffer();
        Types.VAR_INT.writePrimitive(buf, 1024); // 1024 ints require 4096 bytes
        buf.writeBytes(new byte[1024]);

        Assertions.assertThrows(IllegalArgumentException.class, () -> Types.INT_ARRAY_PRIMITIVE.read(buf));
    }

    @Test
    void testNegativeLengthsRejected() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Types.LONG_ARRAY_PRIMITIVE.read(negativeLength()));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Types.INT_ARRAY_PRIMITIVE.read(negativeLength()));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Types.VAR_INT_ARRAY_PRIMITIVE.read(negativeLength()));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Types.BYTE_ARRAY_PRIMITIVE.read(negativeLength()));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Types.STRING_ARRAY.read(negativeLength()));
    }

    @Test
    void testValidArraysStillRead() {
        final ByteBuf buf = Unpooled.buffer();
        Types.LONG_ARRAY_PRIMITIVE.write(buf, new long[]{1, 2, 3});
        Assertions.assertArrayEquals(new long[]{1, 2, 3}, Types.LONG_ARRAY_PRIMITIVE.read(buf));

        Types.INT_ARRAY_PRIMITIVE.write(buf, new int[]{1, 2, 3});
        Assertions.assertArrayEquals(new int[]{1, 2, 3}, Types.INT_ARRAY_PRIMITIVE.read(buf));

        Types.VAR_INT_ARRAY_PRIMITIVE.write(buf, new int[]{1, 2, 3});
        Assertions.assertArrayEquals(new int[]{1, 2, 3}, Types.VAR_INT_ARRAY_PRIMITIVE.read(buf));
    }

    @Test
    void testOversizedPaletteIsNotPreAllocated() {
        final ByteBuf buf = Unpooled.buffer();
        buf.writeByte(4); // Bits per block, so at most 16 palette entries
        Types.VAR_INT.writePrimitive(buf, Integer.MAX_VALUE); // Palette length

        // The length is only an allocation hint - reading must fail on the empty buffer instead of
        // allocating an array for Integer.MAX_VALUE entries, which would throw an OutOfMemoryError
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> Types.CHUNK_SECTION1_16.read(buf));
    }

    @Test
    void testVarIntDoesNotConsumeSixthByte() {
        final ByteBuf buf = Unpooled.buffer();
        buf.writeBytes(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x01});

        Assertions.assertThrows(RuntimeException.class, () -> Types.VAR_INT.readPrimitive(buf));
        Assertions.assertEquals(5, buf.readerIndex(), "The sixth byte must not be read, its shift would wrap around");
    }

    private static ByteBuf negativeLength() {
        final ByteBuf buf = Unpooled.buffer();
        Types.VAR_INT.writePrimitive(buf, -1);
        buf.writeBytes(new byte[64]);
        return buf;
    }
}
