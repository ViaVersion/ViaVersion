/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2023 ViaVersion and contributors
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

import com.google.gson.*;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class GsonUtil {
    private static final Gson GSON = new GsonBuilder().create();

    /**
     * Returns google's Gson magic.
     *
     * @return gson instance
     */
    public static Gson getGson() {
        return GSON;
    }

    /**
     * Convert a json element to a sorted string.<br>
     * If the {@code comparator} is null, {@link Comparator#naturalOrder()} will be used.
     *
     * @param element    The element to convert
     * @param comparator The comparator to use
     * @return The sorted string
     */
    public static String toSortedString(@Nullable final JsonElement element, @Nullable final Comparator<String> comparator) {
        if (element == null) {
            return null;
        } else if (comparator != null) {
            return sort(element, comparator).toString();
        } else {
            return sort(element, Comparator.naturalOrder()).toString();
        }
    }

    /**
     * Sort a json element.
     *
     * @param element    The element to sort
     * @param comparator The comparator to use
     * @return The sorted element
     */
    public static JsonElement sort(@Nullable final JsonElement element, final Comparator<String> comparator) {
        if (element == null) {
            return null;
        } else if (element.isJsonArray()) {
            final JsonArray array = element.getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                array.set(i, sort(array.get(i), comparator));
            }
            return array;
        } else if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();
            final JsonObject sorted = new JsonObject();
            final List<String> keys = new ArrayList<>(object.keySet());
            keys.sort(comparator);
            for (String key : keys) {
                sorted.add(key, sort(object.get(key), comparator));
            }
            return sorted;
        } else {
            return element;
        }
    }

}
