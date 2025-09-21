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
package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.util.Copyable;
import io.netty.buffer.ByteBuf;

public record Bee(EntityData entityData, int ticksInHive, int minTicksInHive) implements Copyable {

    public Bee(final CompoundTag entityData, final int ticksInHive, final int minTicksInHive) {
        this(new EntityData(-1, entityData), ticksInHive, minTicksInHive);
    }

    public static final Type<Bee> TYPE1_20_5 = new Type<>(Bee.class) {
        @Override
        public Bee read(final ByteBuf buffer) {
            final CompoundTag entityData = Types.COMPOUND_TAG.read(buffer);
            final int ticksInHive = Types.VAR_INT.readPrimitive(buffer);
            final int minTicksInHive = Types.VAR_INT.readPrimitive(buffer);
            return new Bee(new EntityData(-1, entityData), ticksInHive, minTicksInHive);
        }

        @Override
        public void write(final ByteBuf buffer, final Bee value) {
            Types.COMPOUND_TAG.write(buffer, value.entityData.tag());
            Types.VAR_INT.writePrimitive(buffer, value.ticksInHive);
            Types.VAR_INT.writePrimitive(buffer, value.minTicksInHive);
        }

        @Override
        public void write(final Ops ops, final Bee value) {
            ops.writeMap(map -> map
                .writeOptional("entity_data", Types.COMPOUND_TAG, value.entityData.tag(), new CompoundTag())
                .write("ticks_in_hive", Types.INT, value.ticksInHive)
                .write("min_ticks_in_hive", Types.INT, value.minTicksInHive));
        }
    };
    public static final Type<Bee[]> ARRAY_TYPE1_20_5 = new ArrayType<>(TYPE1_20_5);

    public static final Type<Bee> TYPE1_21_9 = new Type<>(Bee.class) {
        @Override
        public Bee read(final ByteBuf buffer) {
            final EntityData entityData = EntityData.TYPE.read(buffer);
            final int ticksInHive = Types.VAR_INT.readPrimitive(buffer);
            final int minTicksInHive = Types.VAR_INT.readPrimitive(buffer);
            return new Bee(entityData, ticksInHive, minTicksInHive);
        }

        @Override
        public void write(final ByteBuf buffer, final Bee value) {
            EntityData.TYPE.write(buffer, value.entityData);
            Types.VAR_INT.writePrimitive(buffer, value.ticksInHive);
            Types.VAR_INT.writePrimitive(buffer, value.minTicksInHive);
        }

        @Override
        public void write(final Ops ops, final Bee value) {
            ops.writeMap(map -> map
                .write("entity_data", EntityData.TYPE, value.entityData)
                .write("ticks_in_hive", Types.INT, value.ticksInHive)
                .write("min_ticks_in_hive", Types.INT, value.minTicksInHive));
        }
    };
    public static final Type<Bee[]> ARRAY_TYPE1_21_9 = new ArrayType<>(TYPE1_21_9);

    @Override
    public Bee copy() {
        return new Bee(entityData.copy(), ticksInHive, minTicksInHive);
    }
}
