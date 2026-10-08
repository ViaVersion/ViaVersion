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

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ArrayTypeTest {

    private static final Type<?>[] ARRAY_TYPES = {
        Types.BYTE_ARRAY_PRIMITIVE,
        Types.BOOLEAN_ARRAY_PRIMITIVE,
        Types.VAR_INT_ARRAY_PRIMITIVE,
        Types.INT_ARRAY_PRIMITIVE,
        Types.FLOAT_ARRAY_PRIMITIVE,
        Types.LONG_ARRAY_PRIMITIVE
    };

    @Test
    void testNegativeLengthRejected() {
        for (final Type<?> type : ARRAY_TYPES) {
            final ByteBuf buf = Unpooled.buffer();
            Types.VAR_INT.writePrimitive(buf, -1);
            Assertions.assertThrows(IllegalArgumentException.class, () -> type.read(buf),
                type.getTypeName() + " accepted a negative length");
        }
    }

    @Test
    void testLengthBeyondBufferRejected() {
        for (final Type<?> type : ARRAY_TYPES) {
            final ByteBuf buf = Unpooled.buffer();
            Types.VAR_INT.writePrimitive(buf, Integer.MAX_VALUE);
            Assertions.assertThrows(IllegalArgumentException.class, () -> type.read(buf),
                type.getTypeName() + " accepted a length the buffer cannot hold");
        }
    }

    @Test
    void testLengthOverflowingElementWidthRejected() {
        // Lengths picked so that length * element width wraps to zero or a negative number.
        // Multiplying before the bounds check made isReadable accept these, and the allocation
        // that followed was sized from the unwrapped length: several gigabytes from a 5 byte varint.
        assertRejects(Types.INT_ARRAY_PRIMITIVE, 1 << 30);
        assertRejects(Types.FLOAT_ARRAY_PRIMITIVE, 1 << 30);
        assertRejects(Types.LONG_ARRAY_PRIMITIVE, 1 << 28);
    }

    private static void assertRejects(final Type<?> type, final int length) {
        final ByteBuf buf = Unpooled.buffer();
        Types.VAR_INT.writePrimitive(buf, length);
        Assertions.assertThrows(IllegalArgumentException.class, () -> type.read(buf),
            type.getTypeName() + " accepted length " + length);
    }
}
