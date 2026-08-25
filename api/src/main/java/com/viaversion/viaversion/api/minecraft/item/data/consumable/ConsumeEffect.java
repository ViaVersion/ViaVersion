/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.api.minecraft.item.data.consumable;

import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.minecraft.item.data.EnumTypes;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.api.type.types.misc.HolderSetType;
import com.viaversion.viaversion.api.type.types.misc.HolderType;
import io.netty.buffer.ByteBuf;

public record ConsumeEffect<T>(int id, Type<T> type, T value) {

    private static final HolderSetType REMOVE_EFFECTS_TYPE = new HolderSetType() {
        @Override
        public void write(final Ops ops, final HolderSet value) {
            ops.writeMap(map -> map.write("effects", new HolderSetType(EnumTypes.MOB_EFFECT), value));
        }
    };
    private static final Type<Float> TELEPORT_RANDOMLY_TYPE1_21_2 = new Type<>(Float.class) {
        @Override
        public void write(final ByteBuf buffer, final Float value) {
            Types.FLOAT.writePrimitive(buffer, value);
        }

        @Override
        public Float read(final ByteBuf buffer) {
            return Types.FLOAT.readPrimitive(buffer);
        }

        @Override
        public void write(final Ops ops, final Float value) {
            ops.writeMap(map -> map.write("diameter", Types.FLOAT, value, 16F));
        }
    };
    public static final Type<TeleportRandomlyConsumeEffect> TELEPORT_RANDOMLY_TYPE26_3 = new Type<>(TeleportRandomlyConsumeEffect.class) {
        @Override
        public void write(final ByteBuf buffer, final TeleportRandomlyConsumeEffect value) {
            Types.FLOAT.writePrimitive(buffer, value.diameter());
            Types.BOOLEAN.write(buffer, value.directionalParticles());
        }

        @Override
        public TeleportRandomlyConsumeEffect read(final ByteBuf buffer) {
            final float diameter = Types.FLOAT.readPrimitive(buffer);
            final boolean directionalParticles = Types.BOOLEAN.read(buffer);
            return new TeleportRandomlyConsumeEffect(diameter, directionalParticles);
        }

        @Override
        public void write(final Ops ops, final TeleportRandomlyConsumeEffect value) {
            ops.writeMap(map -> map
                .write("diameter", Types.FLOAT, value.diameter(), 16F)
                .write("directional_particles", Types.BOOLEAN, value.directionalParticles(), true)
            );
        }
    };
    private static final HolderType<SoundEvent> PLAY_SOUND_TYPE = new HolderType<>(MappingData.MappingType.SOUND) {
        @Override
        public SoundEvent readDirect(final ByteBuf buffer) {
            return Types.SOUND_EVENT.readDirect(buffer);
        }

        @Override
        public void writeDirect(final ByteBuf buffer, final SoundEvent value) {
            Types.SOUND_EVENT.writeDirect(buffer, value);
        }

        @Override
        public void write(final Ops ops, final Holder<SoundEvent> value) {
            ops.writeMap(map -> map.write("sound", Types.SOUND_EVENT, value));
        }
    };

    public static final Type<?>[] EFFECT_TYPES1_21_2 = {
        ApplyStatusEffects.TYPE,
        REMOVE_EFFECTS_TYPE,
        Types.EMPTY, // clear all effects
        TELEPORT_RANDOMLY_TYPE1_21_2,
        PLAY_SOUND_TYPE
    };
    public static final Type<?>[] EFFECT_TYPES26_3 = {
        ApplyStatusEffects.TYPE,
        REMOVE_EFFECTS_TYPE,
        Types.EMPTY, // clear all effects
        TELEPORT_RANDOMLY_TYPE26_3,
        PLAY_SOUND_TYPE
    };

    public static final Type<ConsumeEffect<?>> TYPE1_21_2 = new ConsumeEffectType(EFFECT_TYPES1_21_2);
    public static final Type<ConsumeEffect<?>[]> ARRAY_TYPE1_21_2 = new ArrayType<>(TYPE1_21_2);
    public static final Type<ConsumeEffect<?>> TYPE26_3 = new ConsumeEffectType(EFFECT_TYPES26_3);
    public static final Type<ConsumeEffect<?>[]> ARRAY_TYPE26_3 = new ArrayType<>(TYPE26_3);

    static <T> ConsumeEffect<T> of(final int id, final Type<T> type, final Object value) {
        //noinspection unchecked
        return new ConsumeEffect<>(id, type, (T) value);
    }

    void writeValue(final ByteBuf buf) {
        this.type.write(buf, this.value);
    }

    private static final class ConsumeEffectType extends Type<ConsumeEffect<?>> {

        private final Type<?>[] types;

        private ConsumeEffectType(final Type<?>[] types) {
            super(ConsumeEffect.class);
            this.types = types;
        }

        @Override
        public ConsumeEffect<?> read(final ByteBuf buffer) {
            // Oh no...
            final int effectType = Types.VAR_INT.readPrimitive(buffer);
            final Type<?> type = types[effectType];
            final Object value = type.read(buffer);
            return ConsumeEffect.of(effectType, type, value);
        }

        @Override
        public void write(final ByteBuf buffer, final ConsumeEffect<?> value) {
            Types.VAR_INT.writePrimitive(buffer, value.id);
            value.writeValue(buffer);
        }

        @Override
        public void write(final Ops ops, final ConsumeEffect<?> value) {
            writeGeneric(ops, value);
        }

        private <E> void writeGeneric(final Ops ops, final ConsumeEffect<E> value) {
            ops.writeMap(map -> map
                .write("type", EnumTypes.CONSUME_EFFECT, value.id)
                .writeInlinedMap(value.type, value.value));
        }
    }
}
