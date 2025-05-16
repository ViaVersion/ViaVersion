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

import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.util.Copyable;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public record FoodProperties1_20_5(int nutrition, float saturationModifier, boolean canAlwaysEat, float eatSeconds,
                                   @Nullable Item usingConvertsTo, FoodEffect[] possibleEffects) implements Copyable {

    public static final Type<FoodProperties1_20_5> TYPE1_20_5 = new Type<>(FoodProperties1_20_5.class) {
        @Override
        public FoodProperties1_20_5 read(final ByteBuf buffer) {
            final int nutrition = Types.VAR_INT.readPrimitive(buffer);
            final float saturationModifier = buffer.readFloat();
            final boolean canAlwaysEat = buffer.readBoolean();
            final float eatSeconds = buffer.readFloat();
            final FoodEffect[] possibleEffects = FoodEffect.ARRAY_TYPE.read(buffer);
            return new FoodProperties1_20_5(nutrition, saturationModifier, canAlwaysEat, eatSeconds, null, possibleEffects);
        }

        @Override
        public void write(final ByteBuf buffer, final FoodProperties1_20_5 value) {
            Types.VAR_INT.writePrimitive(buffer, value.nutrition);
            buffer.writeFloat(value.saturationModifier);
            buffer.writeBoolean(value.canAlwaysEat);
            buffer.writeFloat(value.eatSeconds);
            FoodEffect.ARRAY_TYPE.write(buffer, value.possibleEffects);
        }
    };
    public static final Type<FoodProperties1_20_5> TYPE1_21 = new Type<>(FoodProperties1_20_5.class) {
        @Override
        public FoodProperties1_20_5 read(final ByteBuf buffer) {
            final int nutrition = Types.VAR_INT.readPrimitive(buffer);
            final float saturationModifier = buffer.readFloat();
            final boolean canAlwaysEat = buffer.readBoolean();
            final float eatSeconds = buffer.readFloat();
            final Item usingConvertsTo = VersionedTypes.V1_21.optionalItem.read(buffer);
            final FoodEffect[] possibleEffects = FoodEffect.ARRAY_TYPE.read(buffer);
            return new FoodProperties1_20_5(nutrition, saturationModifier, canAlwaysEat, eatSeconds, usingConvertsTo, possibleEffects);
        }

        @Override
        public void write(final ByteBuf buffer, final FoodProperties1_20_5 value) {
            Types.VAR_INT.writePrimitive(buffer, value.nutrition);
            buffer.writeFloat(value.saturationModifier);
            buffer.writeBoolean(value.canAlwaysEat);
            buffer.writeFloat(value.eatSeconds);
            VersionedTypes.V1_21.optionalItem.write(buffer, value.usingConvertsTo);
            FoodEffect.ARRAY_TYPE.write(buffer, value.possibleEffects);
        }
    };

    @Override
    public FoodProperties1_20_5 copy() {
        return new FoodProperties1_20_5(nutrition, saturationModifier, canAlwaysEat, eatSeconds, usingConvertsTo == null ? null : usingConvertsTo.copy(), Copyable.copy(possibleEffects));
    }

    public record FoodEffect(PotionEffect effect, float probability) {

        public static final Type<FoodEffect> TYPE = new Type<>(FoodEffect.class) {
            @Override
            public FoodEffect read(final ByteBuf buffer) {
                final PotionEffect effect = PotionEffect.TYPE.read(buffer);
                final float probability = buffer.readFloat();
                return new FoodEffect(effect, probability);
            }

            @Override
            public void write(final ByteBuf buffer, final FoodEffect value) {
                PotionEffect.TYPE.write(buffer, value.effect);
                buffer.writeFloat(value.probability);
            }
        };
        public static final Type<FoodEffect[]> ARRAY_TYPE = new ArrayType<>(TYPE);
    }
}
