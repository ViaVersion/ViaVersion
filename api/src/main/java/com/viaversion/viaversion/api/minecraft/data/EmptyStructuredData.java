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

import io.netty.buffer.ByteBuf;

final class EmptyStructuredData<T> implements StructuredData<T> {

    private final StructuredDataKey<T> key;
    private int id;

    EmptyStructuredData(final StructuredDataKey<T> key, final int id) {
        this.key = key;
        this.id = id;
    }

    @Override
    public void setValue(final T value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(final ByteBuf buffer) {
    }

    @Override
    public void setId(final int id) {
        this.id = id;
    }

    @Override
    public StructuredDataKey<T> key() {
        return this.key;
    }

    @Override
    public StructuredData<T> copy() {
        return new EmptyStructuredData<>(this.key, this.id);
    }

    @Override
    public T value() {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public int id() {
        return this.id;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final EmptyStructuredData<?> that = (EmptyStructuredData<?>) o;
        if (id != that.id) return false;
        return key.equals(that.key);
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + id;
        return result;
    }

    @Override
    public String toString() {
        return "EmptyStructuredData{" +
            "key=" + key +
            ", id=" + id +
            '}';
    }
}
