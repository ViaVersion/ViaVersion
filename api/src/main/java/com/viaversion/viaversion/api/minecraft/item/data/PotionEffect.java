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

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.ArrayType;
import io.netty.buffer.ByteBuf;

public final class PotionEffect {

    public static final Type<PotionEffect> TYPE = new Type<PotionEffect>(PotionEffect.class) {
        @Override
        public PotionEffect read(final ByteBuf buffer) throws Exception {
            final int effect = Type.VAR_INT.readPrimitive(buffer);
            final PotionEffectData effectData = PotionEffectData.TYPE.read(buffer);
            return new PotionEffect(effect, effectData);
        }

        @Override
        public void write(final ByteBuf buffer, final PotionEffect value) throws Exception {
            Type.VAR_INT.writePrimitive(buffer, value.effect);
            PotionEffectData.TYPE.write(buffer, value.effectData);
        }
    };
    public static final Type<PotionEffect[]> ARRAY_TYPE = new ArrayType<>(TYPE);

    private final int effect;
    private final PotionEffectData effectData;

    public PotionEffect(final int effect, final PotionEffectData effectData) {
        this.effect = effect;
        this.effectData = effectData;
    }

    public int effect() {
        return effect;
    }

    public PotionEffectData effectData() {
        return effectData;
    }
}
