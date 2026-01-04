/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.util;

import com.viaversion.nbt.tag.Tag;
import java.lang.reflect.Array;

public interface Copyable {

    static <T> T copy(final T object) {
        if (object == null) {
            return null;
        } else if (object instanceof Tag tag) {
            return (T) tag.copy();
        } else if (object instanceof Copyable copyable) {
            return (T) copyable.copy();
        } else if (object.getClass().isArray()) {
            final Class<?> componentType = object.getClass().getComponentType();
            final int length = Array.getLength(object);
            final Object copy = Array.newInstance(componentType, length);
            if (componentType.isPrimitive()) {
                for (int i = 0; i < length; i++) {
                    Array.set(copy, i, Array.get(object, i));
                }
            } else {
                // See if we need to copy elements, too
                for (int i = 0; i < length; i++) {
                    Array.set(copy, i, copy(Array.get(object, i)));
                }
            }
            return (T) copy;
        } else {
            return object;
        }
    }

    Object copy();

}
