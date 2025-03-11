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
package com.viaversion.viaversion.api.type.types;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.Either;
import io.netty.buffer.ByteBuf;

// Yuck - only use when necessary
public final class EitherType<T, V> extends Type<Either<T, V>> {
    private final Type<T> leftType;
    private final Type<V> rightType;

    public EitherType(final Type<T> leftType, final Type<V> rightType) {
        super(Either.class);
        this.leftType = leftType;
        this.rightType = rightType;
    }

    @Override
    public Either<T, V> read(final ByteBuf buffer) {
        return read(buffer, this.leftType, this.rightType);
    }

    @Override
    public void write(final ByteBuf buffer, final Either<T, V> value) {
        write(buffer, value, this.leftType, this.rightType);
    }

    public static <X, Y> Either<X, Y> read(final ByteBuf buf, final Type<X> leftType, final Type<Y> rightType) {
        if (buf.readBoolean()) {
            return Either.left(leftType.read(buf));
        } else {
            return Either.right(rightType.read(buf));
        }
    }

    public static <X, Y> void write(final ByteBuf buf, final Either<X, Y> value, final Type<X> leftType, final Type<Y> rightType) {
        if (value.isLeft()) {
            buf.writeBoolean(true);
            leftType.write(buf, value.left());
        } else {
            buf.writeBoolean(false);
            rightType.write(buf, value.right());
        }
    }
}
