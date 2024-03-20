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

public final class FoodEffect {

    public static final Type<FoodEffect> TYPE = new Type<FoodEffect>(FoodEffect.class) {
        @Override
        public FoodEffect read(final ByteBuf buffer) throws Exception {
            final PotionEffect effect = PotionEffect.TYPE.read(buffer);
            final float probability = buffer.readFloat();
            return new FoodEffect(effect, probability);
        }

        @Override
        public void write(final ByteBuf buffer, final FoodEffect value) throws Exception {
            PotionEffect.TYPE.write(buffer, value.effect);
            buffer.writeFloat(value.probability);
        }
    };
    public static final Type<FoodEffect[]> ARRAY_TYPE = new ArrayType<>(TYPE);

    private final PotionEffect effect;
    private final float probability;

    public FoodEffect(final PotionEffect effect, final float probability) {
        this.effect = effect;
        this.probability = probability;
    }

    public PotionEffect effect() {
        return effect;
    }

    public float probability() {
        return probability;
    }
}
