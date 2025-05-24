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
package com.viaversion.viaversion.api.type.types.math;

import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

public class BlockPositionType1_14 extends Type<BlockPosition> {
    public BlockPositionType1_14() {
        super(BlockPosition.class);
    }

    @Override
    public BlockPosition read(ByteBuf buffer) {
        long val = buffer.readLong();

        long x = (val >> 38);
        long y = val << 52 >> 52;
        long z = val << 26 >> 38;

        return new BlockPosition((int) x, (int) y, (int) z);
    }

    @Override
    public void write(ByteBuf buffer, BlockPosition object) {
        buffer.writeLong((((long) object.x() & 0x3ffffff) << 38)
            | (object.y() & 0xfff)
            | ((((long) object.z()) & 0x3ffffff) << 12));
    }

    @Override
    public void write(final Ops ops, final BlockPosition value) {
        ops.write(Types.INT_ARRAY_PRIMITIVE, new int[]{value.x(), value.y(), value.z()});
    }

    public static final class OptionalBlockPositionType extends OptionalType<BlockPosition> {

        public OptionalBlockPositionType() {
            super(Types.BLOCK_POSITION1_14);
        }
    }
}
