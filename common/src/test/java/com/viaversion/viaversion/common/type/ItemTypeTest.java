/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.viaversion.viaversion.api.type.Type;

public class ItemTypeTest {
    @Test
    public void testEmptyItemRead() throws Exception {
        // Test empty item read
        Assertions.assertNull(Type.ITEM.read(Unpooled.wrappedBuffer(new byte[]{-1, -1})));
        Assertions.assertNull(Type.FLAT_ITEM.read(Unpooled.wrappedBuffer(new byte[]{-1, -1})));
        Assertions.assertNull(Type.FLAT_VAR_INT_ITEM.read(Unpooled.wrappedBuffer(new byte[]{0})));
    }

    @Test
    public void testNormalItemRead() throws Exception {

        // Test item read
        Assertions.assertEquals(
                new DataItem((int) Short.MAX_VALUE, (byte) -128, (short) 257, null),
                Type.ITEM.read(Unpooled.wrappedBuffer(new byte[]{
                        127, -1,
                        -128,
                        1, 1,
                        0
                }))
        );
        Assertions.assertEquals(
                new DataItem(420, (byte) 53, (short) 0, null),
                Type.FLAT_ITEM.read(Unpooled.wrappedBuffer(new byte[]{
                        1, (byte) 164,
                        53,
                        0
                }))
        );
        Assertions.assertEquals(
                new DataItem(268435456, (byte) 127, (short) 0, null),
                Type.FLAT_VAR_INT_ITEM.read(Unpooled.wrappedBuffer(new byte[]{
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
        Type.ITEM.write(buf, null);
        Assertions.assertArrayEquals(toBytes(buf), new byte[]{-1, -1});
        Type.FLAT_ITEM.write(buf, null);
        Assertions.assertArrayEquals(toBytes(buf), new byte[]{-1, -1});
        Type.FLAT_VAR_INT_ITEM.write(buf, null);
        Assertions.assertArrayEquals(toBytes(buf), new byte[]{0});
    }

    @Test
    public void testNormalItemWrite() throws Exception {
        ByteBuf buf = Unpooled.buffer();

        // Test item write
        Type.ITEM.write(buf, new DataItem((int) Short.MAX_VALUE, (byte) -128, (short) 257, null));
        Assertions.assertArrayEquals(toBytes(buf), new byte[]{
                127, -1,
                -128,
                1, 1,
                0
        });
        Type.FLAT_ITEM.write(buf, new DataItem(420, (byte) 53, (short) 0, null));
        Assertions.assertArrayEquals(toBytes(buf), new byte[]{
                1, (byte) 164,
                53,
                0
        });
        Type.FLAT_VAR_INT_ITEM.write(buf, new DataItem(268435456, (byte) 127, (short) 0, null));
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
