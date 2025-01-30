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
package com.viaversion.viaversion.api.type;

import com.viaversion.viaversion.util.Either;
import io.netty.buffer.ByteBuf;

/**
 * Type for buffer reading and writing.
 *
 * @param <T> read/written type
 * @see Types
 */
public abstract class Type<T> implements ByteBufReader<T>, ByteBufWriter<T> {

    /* Actual Class */
    private final Class<? super T> outputClass;
    private final String typeName;

    protected Type(Class<? super T> outputClass) {
        this(null, outputClass);
    }

    protected Type(String typeName, Class<? super T> outputClass) {
        this.outputClass = outputClass;
        this.typeName = typeName;
    }

    /**
     * Returns the output class type.
     *
     * @return output class type
     */
    public Class<? super T> getOutputClass() {
        return outputClass;
    }

    /**
     * Returns the type name.
     *
     * @return type name
     */
    public String getTypeName() {
        return typeName != null && !typeName.isEmpty() ? typeName : this.getClass().getSimpleName();
    }

    /**
     * Returns the base class, useful when the output class is insufficient for type comparison.
     * One such case are types with {{@link java.util.List}} as their output type.
     *
     * @return base class
     */
    public Class<? extends Type> getBaseClass() {
        return this.getClass();
    }

    @Override
    public String toString() {
        return getTypeName();
    }

    public static <X, Y> Either<X, Y> readEither(final ByteBuf buf, final Type<X> leftType, final Type<Y> rightType) {
        if (buf.readBoolean()) {
            return Either.left(leftType.read(buf));
        } else {
            return Either.right(rightType.read(buf));
        }
    }

    public static <X, Y> void writeEither(final ByteBuf buf, final Either<X, Y> value, final Type<X> leftType, final Type<Y> rightType) {
        if (value.isLeft()) {
            buf.writeBoolean(true);
            leftType.write(buf, value.left());
        } else {
            buf.writeBoolean(false);
            rightType.write(buf, value.right());
        }
    }
}
