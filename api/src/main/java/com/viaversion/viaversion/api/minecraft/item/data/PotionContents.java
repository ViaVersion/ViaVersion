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
import com.viaversion.viaversion.util.Copyable;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public record PotionContents(@Nullable Integer potion, @Nullable Integer customColor, PotionEffect[] customEffects,
                             @Nullable String customName) implements Copyable {

    public PotionContents(final @Nullable Integer potion, final @Nullable Integer customColor, final PotionEffect[] customEffects) {
        this(potion, customColor, customEffects, null);
    }

    public static final Type<PotionContents> TYPE1_20_5 = new Type<>(PotionContents.class) {
        @Override
        public PotionContents read(final ByteBuf buffer) {
            final Integer potion = buffer.readBoolean() ? Types.VAR_INT.readPrimitive(buffer) : null;
            final Integer customColor = buffer.readBoolean() ? buffer.readInt() : null;
            final PotionEffect[] customEffects = PotionEffect.ARRAY_TYPE.read(buffer);
            return new PotionContents(potion, customColor, customEffects, null);
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

    public static final Type<PotionContents> TYPE1_21_2 = new Type<>(PotionContents.class) {
        @Override
        public PotionContents read(final ByteBuf buffer) {
            final Integer potion = buffer.readBoolean() ? Types.VAR_INT.readPrimitive(buffer) : null;
            final Integer customColor = buffer.readBoolean() ? buffer.readInt() : null;
            final PotionEffect[] customEffects = PotionEffect.ARRAY_TYPE.read(buffer);
            final String customName = Types.OPTIONAL_STRING.read(buffer);
            return new PotionContents(potion, customColor, customEffects, customName);
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
            Types.OPTIONAL_STRING.write(buffer, value.customName);
        }

        @Override
        public void write(final Ops ops, final PotionContents value) {
            ops.writeMap(map -> map
                .writeOptional("potion", EnumTypes.POTION, value.potion)
                .writeOptional("custom_color", Types.INT, value.customColor)
                .writeOptional("custom_effects", PotionEffect.ARRAY_TYPE, value.customEffects, new PotionEffect[0])
                .writeOptional("custom_name", Types.STRING, value.customName));
        }
    };

    @Override
    public PotionContents copy() {
        return new PotionContents(potion, customColor, Copyable.copy(customEffects), customName);
    }
}
