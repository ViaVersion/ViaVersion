/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
}
