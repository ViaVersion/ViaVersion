/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
package com.viaversion.viaversion.api.type.types.block;

import com.viaversion.viaversion.api.minecraft.BlockChangeRecord;
import com.viaversion.viaversion.api.minecraft.BlockChangeRecord1_16_2;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

public class VarLongBlockChangeRecordType extends Type<BlockChangeRecord> {

    public VarLongBlockChangeRecordType() {
        super(BlockChangeRecord.class);
    }

    @Override
    public BlockChangeRecord read(ByteBuf buffer) throws Exception {
        long data = Type.VAR_LONG.readPrimitive(buffer);
        short position = (short) (data & 0xFFFL);
        return new BlockChangeRecord1_16_2(position >>> 8 & 0xF, position & 0xF, position >>> 4 & 0xF, (int) (data >>> 12));
    }

    @Override
    public void write(ByteBuf buffer, BlockChangeRecord object) throws Exception {
        short position = (short) (object.getSectionX() << 8 | object.getSectionZ() << 4 | object.getSectionY());
        Type.VAR_LONG.writePrimitive(buffer, (long) object.getBlockId() << 12 | position);
    }
}
