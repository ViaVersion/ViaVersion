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
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class Filterable<T> {
    private final T raw;
    private final T filtered;

    protected Filterable(final T raw, @Nullable final T filtered) {
        this.raw = raw;
        this.filtered = filtered;
    }

    public T raw() {
        return raw;
    }

    public boolean isFiltered() {
        return filtered != null;
    }

    public @Nullable T filtered() {
        return filtered;
    }

    public T get() {
        return filtered != null ? filtered : raw;
    }

    public abstract static class FilterableType<T, F extends Filterable<T>> extends Type<F> {
        private final Type<T> elementType;
        private final Type<T> optionalElementType;

        protected FilterableType(final Type<T> elementType, final Type<T> optionalElementType, final Class<F> outputClass) {
            super(outputClass);
            this.elementType = elementType;
            this.optionalElementType = optionalElementType;
        }

        @Override
        public F read(final ByteBuf buffer) {
            final T raw = elementType.read(buffer);
            final T filtered = optionalElementType.read(buffer);
            return create(raw, filtered);
        }

        @Override
        public void write(final ByteBuf buffer, final F value) {
            elementType.write(buffer, value.raw());
            optionalElementType.write(buffer, value.filtered());
        }

        @Override
        public void write(final Ops ops, final F value) {
            ops.writeMap(map -> map
                .write("raw", elementType, value.raw())
                .writeOptional("filtered", elementType, value.filtered()));
        }

        protected abstract F create(T raw, T filtered);
    }
}
