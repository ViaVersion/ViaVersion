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

public class Triple<A, B, C> {
    private final A first;
    private final B second;
    private final C third;

    public Triple(@Nullable A first, @Nullable B second, @Nullable C third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public @Nullable A first() {
        return first;
    }

    public @Nullable B second() {
        return second;
    }

    public @Nullable C third() {
        return third;
    }

    @Deprecated/*(forRemoval = true)*/
    public @Nullable A getFirst() {
        return first;
    }

    @Deprecated/*(forRemoval = true)*/
    public @Nullable B getSecond() {
        return second;
    }

    @Deprecated/*(forRemoval = true)*/
    public @Nullable C getThird() {
        return third;
    }

    @Override
    public String toString() {
        return "Triple{" + first + ", " + second + ", " + third + '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Triple<?, ?, ?> triple = (Triple<?, ?, ?>) o;
        if (!Objects.equals(first, triple.first)) return false;
        if (!Objects.equals(second, triple.second)) return false;
        return Objects.equals(third, triple.third);
    }

    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        result = 31 * result + (third != null ? third.hashCode() : 0);
        return result;
    }
}
