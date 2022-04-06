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
package com.viaversion.viaversion.api.type.types.minecraft;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntArrayTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.api.minecraft.GlobalPosition;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

public class OptionalGlobalPositionType extends Type<GlobalPosition> {

    public OptionalGlobalPositionType() {
        super(GlobalPosition.class);
    }

    @Override
    public GlobalPosition read(ByteBuf buffer) throws Exception {
        if (buffer.readBoolean()) {
            // ♨︎_♨︎
            final CompoundTag compound = Type.NBT.read(buffer);
            final String dimension = (String) compound.get("dimension").getValue();
            final IntArrayTag positionFields = compound.get("pos");
            return new GlobalPosition(dimension, positionFields.getValue(0), positionFields.getValue(1), positionFields.getValue(2));
        }
        return null;
    }

    @Override
    public void write(ByteBuf buffer, GlobalPosition object) throws Exception {
        buffer.writeBoolean(object != null);
        if (object != null) {
            final CompoundTag compound = new CompoundTag();
            compound.put("dimension", new StringTag(object.dimension()));
            final int[] positionFields = {object.x(), object.y(), object.z()};
            compound.put("pos", new IntArrayTag(positionFields));
            Type.NBT.write(buffer, compound);
        }
    }
}
