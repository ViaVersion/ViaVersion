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
import com.viaversion.viaversion.util.Either;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public record StatePropertyMatcher(String name, Either<String, RangedMatcher> matcher) {

    public static final Type<StatePropertyMatcher> TYPE = new Type<>(StatePropertyMatcher.class) {
        @Override
        public StatePropertyMatcher read(final ByteBuf buffer) {
            final String name = Types.STRING.read(buffer);
            if (buffer.readBoolean()) {
                final String value = Types.STRING.read(buffer);
                return new StatePropertyMatcher(name, Either.left(value));
            } else {
                final String minValue = Types.OPTIONAL_STRING.read(buffer);
                final String maxValue = Types.OPTIONAL_STRING.read(buffer);
                return new StatePropertyMatcher(name, Either.right(new RangedMatcher(minValue, maxValue)));
            }
        }

        @Override
        public void write(final ByteBuf buffer, final StatePropertyMatcher value) {
            Types.STRING.write(buffer, value.name);
            if (value.matcher.isLeft()) {
                buffer.writeBoolean(true);
                Types.STRING.write(buffer, value.matcher.left());
            } else {
                buffer.writeBoolean(false);
                Types.OPTIONAL_STRING.write(buffer, value.matcher.right().minValue());
                Types.OPTIONAL_STRING.write(buffer, value.matcher.right().maxValue());
            }
        }
    };
    public static final Type<StatePropertyMatcher[]> ARRAY_TYPE = new ArrayType<>(TYPE);

    public record RangedMatcher(@Nullable String minValue, @Nullable String maxValue) {
    }
}
