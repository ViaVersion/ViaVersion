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

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.minecraft.item.data.PotionEffect;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

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
