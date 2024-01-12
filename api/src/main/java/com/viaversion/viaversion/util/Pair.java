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
package com.viaversion.viaversion.util;

import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

public class Pair<X, Y> {
    private final X key;
    private Y value;

    public Pair(@Nullable X key, @Nullable Y value) {
        this.key = key;
        this.value = value;
    }

    public @Nullable X key() {
        return key;
    }

    public @Nullable Y value() {
        return value;
    }

    @Deprecated/*(forRemoval = true)*/
    public @Nullable X getKey() {
        return key;
    }

    @Deprecated/*(forRemoval = true)*/
    public @Nullable Y getValue() {
        return value;
    }

    /**
     * @deprecated don't count on this continuing to be mutable
     */
    @Deprecated/*(forRemoval = true)*/
    public void setValue(@Nullable Y value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Pair{" + key + ", " + value + '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        if (!Objects.equals(key, pair.key)) return false;
        return Objects.equals(value, pair.value);
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
