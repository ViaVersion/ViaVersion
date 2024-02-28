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
package com.viaversion.viaversion.api.minecraft.data;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.IdHolder;
import com.viaversion.viaversion.util.Unit;
import io.netty.buffer.ByteBuf;

public final class StructuredData<T> implements IdHolder {

    private final Type<T> type;
    private T value;
    private int id;

    public StructuredData(final Type<T> type, final T value, final int id) {
        this.type = type;
        this.value = value;
        this.id = id;
    }

    public static StructuredData<?> empty(final int id) {
        // Indicates empty structures, whereas an empty optional is used to remove default values
        return new StructuredData<>(Type.UNIT, Unit.INSTANCE, id);
    }

    public boolean isEmpty() {
        return type == Type.UNIT;
    }

    public void setValue(final T value) {
        if (value != null && !type.getOutputClass().isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException("Item data type and value are incompatible. Type=" + type
                + ", value=" + value + " (" + value.getClass().getSimpleName() + ")");
        }
        this.value = value;
    }

    public void write(final ByteBuf buffer) throws Exception {
        type.write(buffer, value);
    }

    public void setId(final int id) {
        this.id = id;
    }

    public Type<T> type() {
        return type;
    }

    public T value() {
        return value;
    }

    @Override
    public int id() {
        return id;
    }
}
