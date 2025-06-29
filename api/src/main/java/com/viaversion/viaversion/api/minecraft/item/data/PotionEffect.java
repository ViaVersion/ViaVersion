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
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import io.netty.buffer.ByteBuf;

public record PotionEffect(int effect, PotionEffectData effectData) {

    public static final Type<PotionEffect> TYPE = new Type<>(PotionEffect.class) {
        @Override
        public PotionEffect read(final ByteBuf buffer) {
            final int effect = Types.VAR_INT.readPrimitive(buffer);
            final PotionEffectData effectData = PotionEffectData.TYPE.read(buffer);
            return new PotionEffect(effect, effectData);
        }

        @Override
        public void write(final ByteBuf buffer, final PotionEffect value) {
            Types.VAR_INT.writePrimitive(buffer, value.effect);
            PotionEffectData.TYPE.write(buffer, value.effectData);
        }

        @Override
        public void write(final Ops ops, final PotionEffect value) {
            ops.writeMap(map -> map
                .write("id", EnumTypes.MOB_EFFECT, value.effect)
                .writeInlinedMap(PotionEffectData.TYPE, value.effectData));
        }
    };
    public static final Type<PotionEffect[]> ARRAY_TYPE = new ArrayType<>(TYPE);

}
