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

import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class PotionEffectData {

    public static final Type<PotionEffectData> TYPE = new Type<PotionEffectData>(PotionEffectData.class) {
        @Override
        public PotionEffectData read(final ByteBuf buffer) throws Exception {
            final int amplifier = Type.VAR_INT.readPrimitive(buffer);
            final int duration = Type.VAR_INT.readPrimitive(buffer);
            final boolean ambient = buffer.readBoolean();
            final boolean showParticles = buffer.readBoolean();
            final boolean showIcon = buffer.readBoolean();
            final PotionEffectData hiddenEffect = OPTIONAL_TYPE.read(buffer);
            return new PotionEffectData(amplifier, duration, ambient, showParticles, showIcon, hiddenEffect);
        }

        @Override
        public void write(final ByteBuf buffer, final PotionEffectData value) throws Exception {
            Type.VAR_INT.writePrimitive(buffer, value.amplifier);
            Type.VAR_INT.writePrimitive(buffer, value.duration);
            buffer.writeBoolean(value.ambient);
            buffer.writeBoolean(value.showParticles);
            buffer.writeBoolean(value.showIcon);
            OPTIONAL_TYPE.write(buffer, value.hiddenEffect);
        }
    };
    public static final Type<PotionEffectData> OPTIONAL_TYPE = new OptionalType<PotionEffectData>(TYPE) {
    };

    private final int amplifier;
    private final int duration;
    private final boolean ambient;
    private final boolean showParticles;
    private final boolean showIcon;
    private final PotionEffectData hiddenEffect;

    public PotionEffectData(final int amplifier, final int duration, final boolean ambient, final boolean showParticles,
                            final boolean showIcon, @Nullable final PotionEffectData hiddenEffect) {
        this.amplifier = amplifier;
        this.duration = duration;
        this.ambient = ambient;
        this.showParticles = showParticles;
        this.showIcon = showIcon;
        this.hiddenEffect = hiddenEffect;
    }

    public int amplifier() {
        return amplifier;
    }

    public int duration() {
        return duration;
    }

    public boolean ambient() {
        return ambient;
    }

    public boolean showParticles() {
        return showParticles;
    }

    public boolean showIcon() {
        return showIcon;
    }

    public @Nullable PotionEffectData hiddenEffect() {
        return hiddenEffect;
    }
}
