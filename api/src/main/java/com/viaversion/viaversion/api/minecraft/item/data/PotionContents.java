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
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public record PotionContents(@Nullable Integer potion, @Nullable Integer customColor, PotionEffect[] customEffects) {

    public static final Type<PotionContents> TYPE = new Type<>(PotionContents.class) {
        @Override
        public PotionContents read(final ByteBuf buffer) {
            final Integer potion = buffer.readBoolean() ? Types.VAR_INT.readPrimitive(buffer) : null;
            final Integer customColor = buffer.readBoolean() ? buffer.readInt() : null;
            final PotionEffect[] customEffects = PotionEffect.ARRAY_TYPE.read(buffer);
            return new PotionContents(potion, customColor, customEffects);
        }

        @Override
        public void write(final ByteBuf buffer, final PotionContents value) {
            buffer.writeBoolean(value.potion != null);
            if (value.potion != null) {
                Types.VAR_INT.writePrimitive(buffer, value.potion);
            }

            buffer.writeBoolean(value.customColor != null);
            if (value.customColor != null) {
                buffer.writeInt(value.customColor);
            }

            PotionEffect.ARRAY_TYPE.write(buffer, value.customEffects);
        }
    };
}
