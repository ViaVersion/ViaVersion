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

import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public record KineticWeapon(int contactCooldownTicks, int delayTicks, @Nullable Condition dismountConditions,
                            @Nullable Condition knockbackConditions, @Nullable Condition damageConditions,
                            float forwardMovement, float damageMultiplier,
                            @Nullable Holder<SoundEvent> sound, @Nullable Holder<SoundEvent> hitSound) {

    public static final Type<KineticWeapon> TYPE = new Type<>(KineticWeapon.class) {
        @Override
        public KineticWeapon read(final ByteBuf buffer) {
            final int contactCooldownTicks = Types.VAR_INT.readPrimitive(buffer);
            final int delayTicks = Types.VAR_INT.readPrimitive(buffer);
            final Condition dismountConditions = Condition.OPTIONAL_TYPE.read(buffer);
            final Condition knockbackConditions = Condition.OPTIONAL_TYPE.read(buffer);
            final Condition damageConditions = Condition.OPTIONAL_TYPE.read(buffer);
            final float forwardMovement = Types.FLOAT.readPrimitive(buffer);
            final float damageMultiplier = Types.FLOAT.readPrimitive(buffer);
            final Holder<SoundEvent> sound = Types.OPTIONAL_SOUND_EVENT.read(buffer);
            final Holder<SoundEvent> hitSound = Types.OPTIONAL_SOUND_EVENT.read(buffer);
            return new KineticWeapon(contactCooldownTicks, delayTicks, dismountConditions, knockbackConditions, damageConditions, forwardMovement, damageMultiplier, sound, hitSound);
        }

        @Override
        public void write(final ByteBuf buffer, final KineticWeapon value) {
            Types.VAR_INT.writePrimitive(buffer, value.contactCooldownTicks);
            Types.VAR_INT.writePrimitive(buffer, value.delayTicks);
            Condition.OPTIONAL_TYPE.write(buffer, value.dismountConditions);
            Condition.OPTIONAL_TYPE.write(buffer, value.knockbackConditions);
            Condition.OPTIONAL_TYPE.write(buffer, value.damageConditions);
            Types.FLOAT.writePrimitive(buffer, value.forwardMovement);
            Types.FLOAT.writePrimitive(buffer, value.damageMultiplier);
            Types.OPTIONAL_SOUND_EVENT.write(buffer, value.sound);
            Types.OPTIONAL_SOUND_EVENT.write(buffer, value.hitSound);
        }

        @Override
        public void write(final Ops ops, final KineticWeapon value) {
            ops.writeMap(map -> map
                .writeOptional("contact_cooldown_ticks", Types.INT, value.contactCooldownTicks, 10)
                .writeOptional("delay_ticks", Types.INT, value.delayTicks, 0)
                .writeOptional("dismount_conditions", Condition.TYPE, value.dismountConditions)
                .writeOptional("knockback_conditions", Condition.TYPE, value.knockbackConditions)
                .writeOptional("damage_conditions", Condition.TYPE, value.damageConditions)
                .writeOptional("forward_movement", Types.FLOAT, value.forwardMovement, 0F)
                .writeOptional("damage_multiplier", Types.FLOAT, value.damageMultiplier, 1F)
                .writeOptional("sound", Types.SOUND_EVENT, value.sound)
                .writeOptional("hit_sound", Types.SOUND_EVENT, value.hitSound)
            );
        }
    };

    public record Condition(int maxDurationTicks, float minSpeed, float minRelativeSpeed) {

        public static final Type<Condition> TYPE = new Type<>(Condition.class) {
            @Override
            public Condition read(final ByteBuf buffer) {
                final int maxDurationTicks = Types.VAR_INT.readPrimitive(buffer);
                final float minSpeed = Types.FLOAT.readPrimitive(buffer);
                final float minRelativeSpeed = Types.FLOAT.readPrimitive(buffer);
                return new Condition(maxDurationTicks, minSpeed, minRelativeSpeed);
            }

            @Override
            public void write(final ByteBuf buffer, final Condition value) {
                Types.VAR_INT.writePrimitive(buffer, value.maxDurationTicks);
                Types.FLOAT.writePrimitive(buffer, value.minSpeed);
                Types.FLOAT.writePrimitive(buffer, value.minRelativeSpeed);
            }

            @Override
            public void write(final Ops ops, final Condition value) {
                ops.writeMap(map -> map
                    .write("max_duration_ticks", Types.INT, value.maxDurationTicks)
                    .writeOptional("min_speed", Types.FLOAT, value.minSpeed, 0F)
                    .writeOptional("min_relative_speed", Types.FLOAT, value.minRelativeSpeed, 0F)
                );
            }
        };
        public static final Type<Condition> OPTIONAL_TYPE = new OptionalType<>(TYPE) {
        };
    }
}
