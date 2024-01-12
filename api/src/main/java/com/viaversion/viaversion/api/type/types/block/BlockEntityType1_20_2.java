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

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntityImpl;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

public class BlockEntityType1_20_2 extends Type<BlockEntity> {

    public BlockEntityType1_20_2() {
        super(BlockEntity.class);
    }

    @Override
    public BlockEntity read(final ByteBuf buffer) throws Exception {
        final byte xz = buffer.readByte();
        final short y = buffer.readShort();
        final int typeId = Type.VAR_INT.readPrimitive(buffer);
        final CompoundTag tag = Type.COMPOUND_TAG.read(buffer);
        return new BlockEntityImpl(xz, y, typeId, tag);
    }

    @Override
    public void write(final ByteBuf buffer, final BlockEntity entity) throws Exception {
        buffer.writeByte(entity.packedXZ());
        buffer.writeShort(entity.y());
        Type.VAR_INT.writePrimitive(buffer, entity.typeId());
        Type.COMPOUND_TAG.write(buffer, entity.tag());
    }
}
