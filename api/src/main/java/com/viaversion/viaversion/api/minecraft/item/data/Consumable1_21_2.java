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

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.api.type.types.misc.HolderSetType;
import com.viaversion.viaversion.api.type.types.misc.HolderType;
import com.viaversion.viaversion.util.Copyable;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;

public record Consumable1_21_2(float consumeSeconds, int animationType, Holder<SoundEvent> sound,
                               boolean hasConsumeParticles,
                               ConsumeEffect<?>[] consumeEffects) implements Copyable, Rewritable {

    private static final HolderSetType REMOVE_EFFECTS_TYPE = new HolderSetType() {
        @Override
        public void write(final Ops ops, final HolderSet value) {
            ops.writeMap(map -> map.write("effects", new HolderSetType(EnumTypes.MOB_EFFECT), value));
        }
    };
    private static final Type<Float> TELEPORT_RANDOMLY_TYPE = new Type<>(Float.class) {
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
            ops.writeMap(map -> map.write("diameter", Types.FLOAT, 16F));
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

    public static final Type<?>[] EFFECT_TYPES = {
        ApplyStatusEffects.TYPE,
        REMOVE_EFFECTS_TYPE,
        Types.EMPTY, // clear all effects
        TELEPORT_RANDOMLY_TYPE,
        PLAY_SOUND_TYPE
    };

    public static final Type<Consumable1_21_2> TYPE = new Type<>(Consumable1_21_2.class) {
        @Override
        public Consumable1_21_2 read(final ByteBuf buffer) {
            final float consumeSeconds = buffer.readFloat();
            final int animationType = Types.VAR_INT.readPrimitive(buffer);
            final Holder<SoundEvent> sound = Types.SOUND_EVENT.read(buffer);
            final boolean hasConsumeParticles = buffer.readBoolean();
            final ConsumeEffect<?>[] consumeEffects = ConsumeEffect.ARRAY_TYPE.read(buffer);
            return new Consumable1_21_2(consumeSeconds, animationType, sound, hasConsumeParticles, consumeEffects);
        }

        @Override
        public void write(final ByteBuf buffer, final Consumable1_21_2 value) {
            buffer.writeFloat(value.consumeSeconds);
            Types.VAR_INT.writePrimitive(buffer, value.animationType);
            Types.SOUND_EVENT.write(buffer, value.sound);
            buffer.writeBoolean(value.hasConsumeParticles);
            ConsumeEffect.ARRAY_TYPE.write(buffer, value.consumeEffects);
        }

        @Override
        public void write(final Ops ops, final Consumable1_21_2 value) {
            final Holder<SoundEvent> defaultSound = Holder.of(ops.context().registryAccess().id(MappingData.MappingType.SOUND, "entity.generic.eat"));
            ops.writeMap(map -> map
                .writeOptional("consume_seconds", Types.FLOAT, value.consumeSeconds, 1.6F)
                .writeOptional("animation", EnumTypes.ITEM_USE_ANIMATION, value.animationType, 1)
                .writeOptional("sound", Types.SOUND_EVENT, value.sound, defaultSound)
                .writeOptional("has_consume_particles", Types.BOOLEAN, value.hasConsumeParticles, true)
                .writeOptional("consume_effects", ConsumeEffect.ARRAY_TYPE, value.consumeEffects, new ConsumeEffect<?>[0]));
        }
    };

    public record ConsumeEffect<T>(int id, Type<T> type, T value) {

        public static final Type<ConsumeEffect<?>> TYPE = new Type<>(ConsumeEffect.class) {
            @Override
            public ConsumeEffect<?> read(final ByteBuf buffer) {
                // Oh no...
                final int effectType = Types.VAR_INT.readPrimitive(buffer);
                final Type<?> type = EFFECT_TYPES[effectType];
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
        };
        public static final Type<ConsumeEffect<?>[]> ARRAY_TYPE = new ArrayType<>(TYPE);

        static <T> ConsumeEffect<T> of(final int id, final Type<T> type, final Object value) {
            //noinspection unchecked
            return new ConsumeEffect<>(id, type, (T) value);
        }

        void writeValue(final ByteBuf buf) {
            this.type.write(buf, this.value);
        }
    }

    public record ApplyStatusEffects(PotionEffect[] effects, float probability) {

        public static final Type<ApplyStatusEffects> TYPE = new Type<>(ApplyStatusEffects.class) {
            @Override
            public ApplyStatusEffects read(final ByteBuf buffer) {
                final PotionEffect[] effects = PotionEffect.ARRAY_TYPE.read(buffer);
                final float probability = buffer.readFloat();
                return new ApplyStatusEffects(effects, probability);
            }

            @Override
            public void write(final ByteBuf buffer, final ApplyStatusEffects value) {
                PotionEffect.ARRAY_TYPE.write(buffer, value.effects);
                buffer.writeFloat(value.probability);
            }

            @Override
            public void write(final Ops ops, final ApplyStatusEffects value) {
                ops.writeMap(map -> map
                    .write("effects", PotionEffect.ARRAY_TYPE, value.effects)
                    .writeOptional("probability", Types.FLOAT, value.probability, 1F));
            }
        };
    }

    @Override
    public Consumable1_21_2 rewrite(final UserConnection connection, final Protocol<?, ?, ?, ?> protocol, final boolean clientbound) {
        final Holder<SoundEvent> soundHolder = SoundEvent.rewriteHolder(this.sound, Rewritable.soundRewriteFunction(protocol, clientbound));
        return soundHolder == this.sound ? this : new Consumable1_21_2(consumeSeconds, animationType, soundHolder, hasConsumeParticles, consumeEffects);
    }

    @Override
    public Consumable1_21_2 copy() {
        return new Consumable1_21_2(consumeSeconds, animationType, sound, hasConsumeParticles, Copyable.copy(consumeEffects));
    }
}
