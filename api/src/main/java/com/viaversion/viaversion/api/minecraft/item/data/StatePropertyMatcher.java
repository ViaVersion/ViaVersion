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
import com.viaversion.viaversion.util.Either;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class StatePropertyMatcher {

    // TODO Abstract Either reading
    public static final Type<StatePropertyMatcher> TYPE = new Type<StatePropertyMatcher>(StatePropertyMatcher.class) {
        @Override
        public StatePropertyMatcher read(final ByteBuf buffer) throws Exception {
            final String name = Type.STRING.read(buffer);
            if (buffer.readBoolean()) {
                final String value = Type.STRING.read(buffer);
                return new StatePropertyMatcher(name, Either.left(value));
            } else {
                final String minValue = Type.OPTIONAL_STRING.read(buffer);
                final String maxValue = Type.OPTIONAL_STRING.read(buffer);
                return new StatePropertyMatcher(name, Either.right(new RangedMatcher(minValue, maxValue)));
            }
        }

        @Override
        public void write(final ByteBuf buffer, final StatePropertyMatcher value) throws Exception {
            Type.STRING.write(buffer, value.name);
            if (value.matcher.isLeft()) {
                buffer.writeBoolean(true);
                Type.STRING.write(buffer, value.matcher.left());
            } else {
                buffer.writeBoolean(false);
                Type.OPTIONAL_STRING.write(buffer, value.matcher.right().minValue());
                Type.OPTIONAL_STRING.write(buffer, value.matcher.right().maxValue());
            }
        }
    };
    public static final Type<StatePropertyMatcher[]> ARRAY_TYPE = new ArrayType<>(TYPE);

    private final String name;
    private final Either<String, RangedMatcher> matcher;

    public StatePropertyMatcher(final String name, final Either<String, RangedMatcher> matcher) {
        this.name = name;
        this.matcher = matcher;
    }

    public String name() {
        return name;
    }

    public Either<String, RangedMatcher> matcher() {
        return matcher;
    }

    public static final class RangedMatcher {
        private final String minValue;
        private final String maxValue;

        public RangedMatcher(@Nullable final String minValue, @Nullable final String maxValue) {
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        public @Nullable String minValue() {
            return minValue;
        }

        public @Nullable String maxValue() {
            return maxValue;
        }
    }
}
