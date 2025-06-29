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

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public record PotionEffectData(int amplifier, int duration, boolean ambient, boolean showParticles,
                               boolean showIcon, @Nullable PotionEffectData hiddenEffect) {

    public static final Type<PotionEffectData> TYPE = new Type<>(PotionEffectData.class) {
        @Override
        public PotionEffectData read(final ByteBuf buffer) {
            final int amplifier = Types.VAR_INT.readPrimitive(buffer);
            final int duration = Types.VAR_INT.readPrimitive(buffer);
            final boolean ambient = buffer.readBoolean();
            final boolean showParticles = buffer.readBoolean();
            final boolean showIcon = buffer.readBoolean();
            final PotionEffectData hiddenEffect = OPTIONAL_TYPE.read(buffer);
            return new PotionEffectData(amplifier, duration, ambient, showParticles, showIcon, hiddenEffect);
        }

        @Override
        public void write(final ByteBuf buffer, final PotionEffectData value) {
            Types.VAR_INT.writePrimitive(buffer, value.amplifier);
            Types.VAR_INT.writePrimitive(buffer, value.duration);
            buffer.writeBoolean(value.ambient);
            buffer.writeBoolean(value.showParticles);
            buffer.writeBoolean(value.showIcon);
            OPTIONAL_TYPE.write(buffer, value.hiddenEffect);
        }

        @Override
        public void write(final Ops ops, final PotionEffectData value) {
            ops.writeMap(map -> map
                .writeOptional("amplifier", Types.UNSIGNED_BYTE, (short) value.amplifier, (short) 0)
                .writeOptional("duration", Types.INT, value.duration, 0)
                .writeOptional("ambient", Types.BOOLEAN, value.ambient, false)
                .writeOptional("show_particles", Types.BOOLEAN, value.showParticles, true)
                .writeOptional("show_icon", Types.BOOLEAN, value.showIcon)
                .writeOptional("hidden_effect", TYPE, value.hiddenEffect));
        }
    };
    public static final Type<PotionEffectData> OPTIONAL_TYPE = new OptionalType<>(TYPE) {
    };
}
