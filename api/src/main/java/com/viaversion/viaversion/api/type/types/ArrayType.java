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

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ArrayType<T> extends Type<T[]> {
    private final Type<T> elementType;
    private final int maxLength;

    public ArrayType(Type<T> type) {
        this(type, -1);
    }

    public ArrayType(Type<T> type, int maxLength) {
        //noinspection unchecked
        super(type.getTypeName() + " Array", (Class<T[]>) getArrayClass(type.getOutputClass()));
        this.elementType = type;
        this.maxLength = maxLength;
    }

    public static Class<?> getArrayClass(Class<?> componentType) {
        // Should only happen once per class init.
        return Array.newInstance(componentType, 0).getClass();
    }

    @Override
    public T[] read(ByteBuf buffer) {
        int amount = Types.VAR_INT.readPrimitive(buffer);
        if (maxLength != -1 && amount > maxLength) {
            throw new IllegalArgumentException("Array length " + amount + " is longer than maximum " + maxLength);
        }

        return amount < Short.MAX_VALUE ? readArray(buffer, amount) : readList(buffer, amount);
    }

    private T[] readArray(ByteBuf buffer, int length) {
        T[] array = createArray(length);
        for (int i = 0; i < length; i++) {
            array[i] = elementType.read(buffer);
        }
        return array;
    }

    private T[] readList(ByteBuf buffer, int length) {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            list.add(elementType.read(buffer));
        }
        return list.toArray(createArray(0));
    }

    private T[] createArray(int length) {
        //noinspection unchecked
        return (T[]) Array.newInstance(elementType.getOutputClass(), length);
    }

    @Override
    public void write(ByteBuf buffer, T[] object) {
        if (maxLength != -1 && object.length > maxLength) {
            throw new IllegalArgumentException("Array length " + object.length + " is longer than maximum " + maxLength);
        }

        Types.VAR_INT.writePrimitive(buffer, object.length);
        for (T o : object) {
            elementType.write(buffer, o);
        }
    }

    @Override
    public void write(final Ops ops, final T[] value) {
        ops.writeList(list -> {
            for (T element : value) {
                list.write(elementType, element);
            }
        });
    }
}
