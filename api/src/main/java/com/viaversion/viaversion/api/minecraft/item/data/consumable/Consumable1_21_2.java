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

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.minecraft.item.data.EnumTypes;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Copyable;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;

public record Consumable1_21_2(float consumeSeconds, int animationType, Holder<SoundEvent> sound,
                               boolean hasConsumeParticles,
                               ConsumeEffect<?>[] consumeEffects) implements Copyable, Rewritable {

    public static final Type<Consumable1_21_2> TYPE1_21_2 = new ConsumableType(ConsumeEffect.ARRAY_TYPE1_21_2);
    public static final Type<Consumable1_21_2> TYPE26_3 = new ConsumableType(ConsumeEffect.ARRAY_TYPE26_3);

    private static final class ConsumableType extends Type<Consumable1_21_2> {

        private final Type<ConsumeEffect<?>[]> consumeEffectType;

        private ConsumableType(final Type<ConsumeEffect<?>[]> consumeEffectType) {
            super(Consumable1_21_2.class);
            this.consumeEffectType = consumeEffectType;
        }

        @Override
        public Consumable1_21_2 read(final ByteBuf buffer) {
            final float consumeSeconds = buffer.readFloat();
            final int animationType = Types.VAR_INT.readPrimitive(buffer);
            final Holder<SoundEvent> sound = Types.SOUND_EVENT.read(buffer);
            final boolean hasConsumeParticles = buffer.readBoolean();
            final ConsumeEffect<?>[] consumeEffects = consumeEffectType.read(buffer);
            return new Consumable1_21_2(consumeSeconds, animationType, sound, hasConsumeParticles, consumeEffects);
        }

        @Override
        public void write(final ByteBuf buffer, final Consumable1_21_2 value) {
            buffer.writeFloat(value.consumeSeconds);
            Types.VAR_INT.writePrimitive(buffer, value.animationType);
            Types.SOUND_EVENT.write(buffer, value.sound);
            buffer.writeBoolean(value.hasConsumeParticles);
            consumeEffectType.write(buffer, value.consumeEffects);
        }

        @Override
        public void write(final Ops ops, final Consumable1_21_2 value) {
            final Holder<SoundEvent> defaultSound = Holder.of(ops.context().registryAccess().id(MappingData.MappingType.SOUND, "entity.generic.eat"));
            ops.writeMap(map -> map
                .writeOptional("consume_seconds", Types.FLOAT, value.consumeSeconds, 1.6F)
                .writeOptional("animation", EnumTypes.ITEM_USE_ANIMATION, value.animationType, 1)
                .writeOptional("sound", Types.SOUND_EVENT, value.sound, defaultSound)
                .writeOptional("has_consume_particles", Types.BOOLEAN, value.hasConsumeParticles, true)
                .writeOptional("consume_effects", consumeEffectType, value.consumeEffects, new ConsumeEffect<?>[0]));
        }
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
