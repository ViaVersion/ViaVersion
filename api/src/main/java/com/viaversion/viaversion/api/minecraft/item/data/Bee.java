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
package com.viaversion.viaversion.api.minecraft.item.data;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.ArrayType;
import io.netty.buffer.ByteBuf;

public final class Bee {

    public static final Type<Bee> TYPE = new Type<Bee>(Bee.class) {
        @Override
        public Bee read(final ByteBuf buffer) throws Exception {
            final CompoundTag entityData = Type.COMPOUND_TAG.read(buffer);
            final int ticksInHive = Type.VAR_INT.readPrimitive(buffer);
            final int minTicksInHive = Type.VAR_INT.readPrimitive(buffer);
            return new Bee(entityData, ticksInHive, minTicksInHive);
        }

        @Override
        public void write(final ByteBuf buffer, final Bee value) throws Exception {
            Type.COMPOUND_TAG.write(buffer, value.entityData);
            Type.VAR_INT.writePrimitive(buffer, value.ticksInHive);
            Type.VAR_INT.writePrimitive(buffer, value.minTicksInHive);
        }
    };
    public static final Type<Bee[]> ARRAY_TYPE = new ArrayType<>(TYPE);

    private final CompoundTag entityData;
    private final int ticksInHive;
    private final int minTicksInHive;

    public Bee(CompoundTag entityData, int ticksInHive, int minTicksInHive) {
        this.entityData = entityData;
        this.ticksInHive = ticksInHive;
        this.minTicksInHive = minTicksInHive;
    }

    public CompoundTag entityData() {
        return entityData;
    }

    public int ticksInHive() {
        return ticksInHive;
    }

    public int minTicksInHive() {
        return minTicksInHive;
    }
}
