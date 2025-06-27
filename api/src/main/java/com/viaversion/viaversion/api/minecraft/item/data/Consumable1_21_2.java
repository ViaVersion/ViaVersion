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
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.util.Copyable;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;

public record Consumable1_21_2(float consumeSeconds, int animationType, Holder<SoundEvent> sound,
                               boolean hasConsumeParticles,
                               ConsumeEffect<?>[] consumeEffects) implements Copyable, Rewritable {

    public static final Type<?>[] EFFECT_TYPES = {
        ApplyStatusEffects.TYPE,
        Types.HOLDER_SET, // remove effects
        Types.EMPTY, // clear all effects
        Types.FLOAT, // teleport randomly
        Types.SOUND_EVENT // play sound
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
