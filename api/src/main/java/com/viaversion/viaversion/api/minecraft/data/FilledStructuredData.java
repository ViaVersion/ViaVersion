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

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.util.ArrayUtil;
import com.viaversion.viaversion.util.Copyable;
import io.netty.buffer.ByteBuf;
import java.util.Objects;

final class FilledStructuredData<T> implements StructuredData<T> {

    private final StructuredDataKey<T> key;
    private T value;
    private int id;

    FilledStructuredData(final StructuredDataKey<T> key, final T value, final int id) {
        Preconditions.checkNotNull(key);
        this.key = key;
        this.value = value;
        this.id = id;
    }

    @Override
    public void setValue(final T value) {
        this.value = value;
    }

    @Override
    public void write(final ByteBuf buffer) {
        key.type().write(buffer, value);
    }

    @Override
    public void setId(final int id) {
        this.id = id;
    }

    @Override
    public StructuredDataKey<T> key() {
        return key;
    }

    @Override
    public StructuredData<T> copy() {
        return new FilledStructuredData<>(this.key, Copyable.copy(this.value), this.id);
    }

    @Override
    public T value() {
        return value;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final FilledStructuredData<?> that = (FilledStructuredData<?>) o;
        if (id != that.id) return false;
        if (!key.equals(that.key)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + id;
        return result;
    }

    @Override
    public String toString() {
        return "FilledStructuredData{" +
            "key=" + key +
            ", value=" + ArrayUtil.toString(value) +
            ", id=" + id +
            '}';
    }
}
