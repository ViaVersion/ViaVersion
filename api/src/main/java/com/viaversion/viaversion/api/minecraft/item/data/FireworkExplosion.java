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

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.util.Copyable;
import io.netty.buffer.ByteBuf;

public record FireworkExplosion(int shape, int[] colors, int[] fadeColors, boolean hasTrail, boolean hasTwinkle) implements Copyable {
    public static final Type<FireworkExplosion> TYPE = new Type<>(FireworkExplosion.class) {
        @Override
        public FireworkExplosion read(final ByteBuf buffer) {
            final int shape = Types.VAR_INT.readPrimitive(buffer);
            final int[] colors = Types.INT_ARRAY_PRIMITIVE.read(buffer);
            final int[] fadeColors = Types.INT_ARRAY_PRIMITIVE.read(buffer);
            final boolean hasTrail = buffer.readBoolean();
            final boolean hasTwinkle = buffer.readBoolean();
            return new FireworkExplosion(shape, colors, fadeColors, hasTrail, hasTwinkle);
        }

        @Override
        public void write(final ByteBuf buffer, final FireworkExplosion value) {
            Types.VAR_INT.writePrimitive(buffer, value.shape);
            Types.INT_ARRAY_PRIMITIVE.write(buffer, value.colors);
            Types.INT_ARRAY_PRIMITIVE.write(buffer, value.fadeColors);
            buffer.writeBoolean(value.hasTrail);
            buffer.writeBoolean(value.hasTwinkle);
        }
    };
    public static final Type<FireworkExplosion[]> ARRAY_TYPE = new ArrayType<>(TYPE);

    @Override
    public FireworkExplosion copy() {
        return new FireworkExplosion(shape, copy(colors), copy(fadeColors), hasTrail, hasTwinkle);
    }
}
