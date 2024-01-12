/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringTypeTest {
    @Test
    public void testStringWrite() throws Exception {
        // Write
        final ByteBuf buf = Unpooled.buffer();
        Type.STRING.write(buf, "\uD83E\uDDFD"); // Sponge Emoji
        Assertions.assertEquals(ByteBufUtil.hexDump(buf), "04f09fa7bd");
    }

    @Test
    public void testStringRead() throws Exception {
        // Write
        final ByteBuf buf = Unpooled.buffer();
        Type.STRING.write(buf, new String(new char[Short.MAX_VALUE]));
        Assertions.assertEquals(Type.STRING.read(buf), new String(new char[Short.MAX_VALUE]));

        Type.STRING.write(buf, new String(new char[Short.MAX_VALUE]).replace("\0", "a"));
        Assertions.assertEquals(Type.STRING.read(buf), new String(new char[Short.MAX_VALUE]).replace("\0", "a"));

        Type.STRING.write(buf, new String(new char[Short.MAX_VALUE / 2]).replace("\0", "\uD83E\uDDFD"));
        Assertions.assertEquals(Type.STRING.read(buf), new String(new char[Short.MAX_VALUE / 2]).replace("\0", "\uD83E\uDDFD"));
    }

    @Test
    public void testStringReadOverflowException() {
        // Read exception
        final ByteBuf buf = Unpooled.buffer();
        Type.VAR_INT.writePrimitive(buf, (Short.MAX_VALUE + 1) * 4);
        for (int i = 0; i < Short.MAX_VALUE / 2 + 1; i++) {
            buf.writeBytes(new byte[]{0x04, (byte) 0xf0, (byte) 0x9f, (byte) 0xa7, (byte) 0xbd}); // Sponge emoji
        }
        Assertions.assertThrows(IllegalArgumentException.class, () -> Type.STRING.read(buf));
    }

    @Test
    public void testStringWriteOverflowException() {
        // Write exceptions
        final ByteBuf buf = Unpooled.buffer();
        Assertions.assertThrows(IllegalArgumentException.class, () -> Type.STRING.write(buf, new String(new char[Short.MAX_VALUE / 2 + 1]).replace("\0", "\uD83E\uDDFD")));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Type.STRING.write(buf, new String(new char[Short.MAX_VALUE + 1])));
    }
}
