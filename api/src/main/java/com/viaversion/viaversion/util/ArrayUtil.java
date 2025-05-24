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
package com.viaversion.viaversion.util;

import java.util.Arrays;

/**
 * For type safety and effort reasons, buffer types use arrays instead of lists.
 * <p>
 * This class includes simple methods to work with these arrays in case they need to be modified
 * (obviously being more expensive due to the required array copies for every modification).
 */
public final class ArrayUtil {

    public static <T> T[] add(final T[] array, final T element) {
        final int length = array.length;
        final T[] newArray = Arrays.copyOf(array, length + 1);
        newArray[length] = element;
        return newArray;
    }

    @SafeVarargs
    public static <T> T[] add(final T[] array, final T... elements) {
        final int length = array.length;
        final T[] newArray = Arrays.copyOf(array, length + elements.length);
        System.arraycopy(elements, 0, newArray, length, elements.length);
        return newArray;
    }

    public static <T> T[] remove(final T[] array, final int index) {
        final T[] newArray = Arrays.copyOf(array, array.length - 1);
        System.arraycopy(array, index + 1, newArray, index, newArray.length - index);
        return newArray;
    }

    public static Float[] boxedArray(final float[] array) {
        final Float[] boxedArray = new Float[array.length];
        for (int i = 0; i < array.length; i++) {
            boxedArray[i] = array[i];
        }
        return boxedArray;
    }

    public static Integer[] boxedArray(final int[] array) {
        final Integer[] boxedArray = new Integer[array.length];
        for (int i = 0; i < array.length; i++) {
            boxedArray[i] = array[i];
        }
        return boxedArray;
    }

    public static Boolean[] boxedArray(final boolean[] array) {
        final Boolean[] boxedArray = new Boolean[array.length];
        for (int i = 0; i < array.length; i++) {
            boxedArray[i] = array[i];
        }
        return boxedArray;
    }
}
