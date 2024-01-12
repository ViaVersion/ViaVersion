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

import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ItemTypeTest {
    @Test
    public void testEmptyItemRead() throws Exception {
        // Test empty item read
        Assertions.assertNull(Type.ITEM1_8.read(Unpooled.wrappedBuffer(new byte[]{-1, -1})));
        Assertions.assertNull(Type.ITEM1_13.read(Unpooled.wrappedBuffer(new byte[]{-1, -1})));
        Assertions.assertNull(Type.ITEM1_13_2.read(Unpooled.wrappedBuffer(new byte[]{0})));
    }

    @Test
    public void testNormalItemRead() throws Exception {

        // Test item read
        Assertions.assertEquals(
                new DataItem(Short.MAX_VALUE, (byte) -128, (short) 257, null),
                Type.ITEM1_8.read(Unpooled.wrappedBuffer(new byte[]{
                        127, -1,
                        -128,
                        1, 1,
                        0
                }))
        );
        Assertions.assertEquals(
                new DataItem(420, (byte) 53, (short) 0, null),
                Type.ITEM1_13.read(Unpooled.wrappedBuffer(new byte[]{
                        1, (byte) 164,
                        53,
                        0
                }))
        );
        Assertions.assertEquals(
                new DataItem(268435456, (byte) 127, (short) 0, null),
                Type.ITEM1_13_2.read(Unpooled.wrappedBuffer(new byte[]{
                        1,
                        -128, -128, -128, -128, 1,
                        127,
                        0
                }))
        );
    }

    @Test
    public void testEmptyItemWrite() throws Exception {
        ByteBuf buf = Unpooled.buffer();

        // Test item empty write
        Type.ITEM1_8.write(buf, null);
        Assertions.assertArrayEquals(toBytes(buf), new byte[]{-1, -1});
        Type.ITEM1_13.write(buf, null);
        Assertions.assertArrayEquals(toBytes(buf), new byte[]{-1, -1});
        Type.ITEM1_13_2.write(buf, null);
        Assertions.assertArrayEquals(toBytes(buf), new byte[]{0});
    }

    @Test
    public void testNormalItemWrite() throws Exception {
        ByteBuf buf = Unpooled.buffer();

        // Test item write
        Type.ITEM1_8.write(buf, new DataItem(Short.MAX_VALUE, (byte) -128, (short) 257, null));
        Assertions.assertArrayEquals(toBytes(buf), new byte[]{
                127, -1,
                -128,
                1, 1,
                0
        });
        Type.ITEM1_13.write(buf, new DataItem(420, (byte) 53, (short) 0, null));
        Assertions.assertArrayEquals(toBytes(buf), new byte[]{
                1, (byte) 164,
                53,
                0
        });
        Type.ITEM1_13_2.write(buf, new DataItem(268435456, (byte) 127, (short) 0, null));
        Assertions.assertArrayEquals(toBytes(buf), new byte[]{
                1,
                -128, -128, -128, -128, 1,
                127,
                0
        });
    }

    private byte[] toBytes(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        return bytes;
    }
}
