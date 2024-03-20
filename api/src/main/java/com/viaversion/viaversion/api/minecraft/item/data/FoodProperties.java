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
import io.netty.buffer.ByteBuf;

public final class FoodProperties {

    public static final Type<FoodProperties> TYPE = new Type<FoodProperties>(FoodProperties.class) {
        @Override
        public FoodProperties read(final ByteBuf buffer) throws Exception {
            final int nutrition = Type.VAR_INT.readPrimitive(buffer);
            final float saturationModifier = buffer.readFloat();
            final boolean canAlwaysEat = buffer.readBoolean();
            final float eatSeconds = buffer.readFloat();
            final FoodEffect[] possibleEffects = FoodEffect.ARRAY_TYPE.read(buffer);
            return new FoodProperties(nutrition, saturationModifier, canAlwaysEat, eatSeconds, possibleEffects);
        }

        @Override
        public void write(final ByteBuf buffer, final FoodProperties value) throws Exception {
            Type.VAR_INT.writePrimitive(buffer, value.nutrition);
            buffer.writeFloat(value.saturationModifier);
            buffer.writeBoolean(value.canAlwaysEat);
            buffer.writeFloat(value.eatSeconds);
            FoodEffect.ARRAY_TYPE.write(buffer, value.possibleEffects);
        }
    };

    private final int nutrition;
    private final float saturationModifier;
    private final boolean canAlwaysEat;
    private final float eatSeconds;
    private final FoodEffect[] possibleEffects;

    public FoodProperties(final int nutrition, final float saturationModifier, final boolean canAlwaysEat, final float eatSeconds, final FoodEffect[] possibleEffects) {
        this.nutrition = nutrition;
        this.saturationModifier = saturationModifier;
        this.canAlwaysEat = canAlwaysEat;
        this.eatSeconds = eatSeconds;
        this.possibleEffects = possibleEffects;
    }

    public int nutrition() {
        return nutrition;
    }

    public float saturationModifier() {
        return saturationModifier;
    }

    public boolean canAlwaysEat() {
        return canAlwaysEat;
    }

    public float eatSeconds() {
        return eatSeconds;
    }

    public FoodEffect[] possibleEffects() {
        return possibleEffects;
    }
}
