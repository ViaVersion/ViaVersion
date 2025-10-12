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

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import io.netty.buffer.ByteBuf;
import java.util.function.Function;

public class TransformingType<F, T> extends Type<T> {
    private final Type<F> from;
    private final Function<F, T> mapFunction;
    private final Function<T, F> reverseFunction;

    protected TransformingType(
        final Type<F> from, final Class<T> outputClass,
        final Function<F, T> mapFunction, final Function<T, F> reverseFunction
    ) {
        super(from.getTypeName(), outputClass);
        this.from = from;
        this.mapFunction = mapFunction;
        this.reverseFunction = reverseFunction;
    }

    /**
     * Returns a type that transforms the input to a separate output type.
     * Useful if you have wrapper classes of a single object.
     *
     * @param from            type to map from
     * @param outputClass     output class
     * @param mapFunction     function to map from F to T
     * @param reverseFunction function to map from T to F
     * @param <F>             from type
     * @param <T>             to type
     * @return transforming type
     */
    public static <F, T> Type<T> of(
        final Type<F> from, final Class<T> outputClass,
        final Function<F, T> mapFunction, final Function<T, F> reverseFunction
    ) {
        return new TransformingType<>(from, outputClass, mapFunction, reverseFunction);
    }

    @Override
    public T read(final ByteBuf buffer) {
        return mapFunction.apply(from.read(buffer));
    }

    @Override
    public void write(final ByteBuf buffer, final T value) {
        from.write(buffer, reverseFunction.apply(value));
    }

    @Override
    public void write(final Ops ops, final T value) {
        from.write(ops, reverseFunction.apply(value));
    }
}
