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
package com.viaversion.viaversion.api.minecraft.blockentity;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;

public final class BlockEntityImpl implements BlockEntity {
    private final byte packedXZ;
    private final short y;
    private final int typeId;
    private final CompoundTag tag;

    public BlockEntityImpl(final byte packedXZ, final short y, final int typeId, final CompoundTag tag) {
        this.packedXZ = packedXZ;
        this.y = y;
        this.typeId = typeId;
        this.tag = tag;
    }

    @Override
    public byte packedXZ() {
        return packedXZ;
    }

    @Override
    public short y() {
        return y;
    }

    @Override
    public int typeId() {
        return typeId;
    }

    @Override
    public CompoundTag tag() {
        return tag;
    }

    @Override
    public BlockEntity withTypeId(int typeId) {
        return new BlockEntityImpl(packedXZ, y, typeId, tag);
    }
}
