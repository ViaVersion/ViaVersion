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
package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.EitherType;
import com.viaversion.viaversion.util.Either;
import io.netty.buffer.ByteBuf;

/**
 * Both values are either a constant or the id of a context provider registry entry.
 */
public record Fuel(Either<Integer, String> amount, Either<Float, String> speedMultiplier) {

    private static final Type<Either<Integer, String>> INT_TYPE = new EitherType<>(Types.INT, Types.STRING);
    private static final Type<Either<Float, String>> FLOAT_TYPE = new EitherType<>(Types.FLOAT, Types.STRING);

    public static final Type<Fuel> TYPE = new Type<>(Fuel.class) {
        @Override
        public Fuel read(final ByteBuf buffer) {
            return new Fuel(INT_TYPE.read(buffer), FLOAT_TYPE.read(buffer));
        }

        @Override
        public void write(final ByteBuf buffer, final Fuel value) {
            INT_TYPE.write(buffer, value.amount());
            FLOAT_TYPE.write(buffer, value.speedMultiplier());
        }
    };
}
