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
package com.viaversion.viaversion.api.minecraft.data;

import com.viaversion.viaversion.util.Copyable;
import com.viaversion.viaversion.util.IdHolder;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface StructuredData<T> extends IdHolder, Copyable {

    /**
     * Returns filled structured data, equivalent to an Optional with a value in vanilla.
     *
     * @param key   serializer key
     * @param value value
     * @param id    serializer id
     * @param <T>   serializer type
     * @return filled structured data
     */
    static <T> StructuredData<T> of(final StructuredDataKey<T> key, final T value, final int id) {
        return new FilledStructuredData<>(key, value, id);
    }

    /**
     * Returns empty structured data, equivalent to an empty Optional in vanilla.
     *
     * @param key serializer key
     * @param id  serializer id
     * @return empty structured data
     */
    static <T> StructuredData<T> empty(final StructuredDataKey<T> key, final int id) {
        return new EmptyStructuredData<>(key, id);
    }

    @Nullable
    T value();

    void setValue(final T value);

    void setId(final int id);

    StructuredDataKey<T> key();

    @Override
    StructuredData<T> copy();

    /**
     * Returns whether the structured data is present. Even if true, the value may be null.
     *
     * @return true if the structured data is present
     */
    default boolean isPresent() {
        return !isEmpty();
    }

    /**
     * Returns whether the structured data is empty. Not to be confused with a null value.
     *
     * @return true if the structured data is empty
     */
    boolean isEmpty();

    void write(final ByteBuf buffer);
}
