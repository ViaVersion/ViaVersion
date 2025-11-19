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
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public record PiercingWeapon(boolean dealsKnockback, boolean dismounts,
                             @Nullable Holder<SoundEvent> sound, @Nullable Holder<SoundEvent> hitSound) {

    public static final Type<PiercingWeapon> TYPE = new Type<>(PiercingWeapon.class) {
        @Override
        public PiercingWeapon read(final ByteBuf buffer) {
            final boolean dealsKnockback = Types.BOOLEAN.read(buffer);
            final boolean dismounts = Types.BOOLEAN.read(buffer);
            final Holder<SoundEvent> sound = Types.OPTIONAL_SOUND_EVENT.read(buffer);
            final Holder<SoundEvent> hitSound = Types.OPTIONAL_SOUND_EVENT.read(buffer);
            return new PiercingWeapon(dealsKnockback, dismounts, sound, hitSound);
        }

        @Override
        public void write(final ByteBuf buffer, final PiercingWeapon value) {
            Types.BOOLEAN.write(buffer, value.dealsKnockback);
            Types.BOOLEAN.write(buffer, value.dismounts);
            Types.OPTIONAL_SOUND_EVENT.write(buffer, value.sound);
            Types.OPTIONAL_SOUND_EVENT.write(buffer, value.hitSound);
        }

        @Override
        public void write(final Ops ops, final PiercingWeapon value) {
            ops.writeMap(map -> map
                .writeOptional("deals_knockback", Types.BOOLEAN, value.dealsKnockback, true)
                .writeOptional("dismounts", Types.BOOLEAN, value.dismounts, false)
                .writeOptional("sound", Types.SOUND_EVENT, value.sound)
                .writeOptional("hit_sound", Types.SOUND_EVENT, value.hitSound)
            );
        }
    };
}
