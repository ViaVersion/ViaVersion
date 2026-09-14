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

public final class Limit {

    public static int max(final int value, final int max) {
        if (value > max) {
            throw new IllegalArgumentException("Value " + value + " is higher than the maximum " + max);
        }
        return value;
    }

    /**
     * Limits a length read from a buffer to be used as the initial capacity of a collection or array.
     * <p>
     * Unlike {@link #max(int, int)}, this does not throw on larger values: the full length is still read and is
     * bound by the readable bytes of the buffer. It only stops a malformed length from causing an arbitrarily
     * large allocation before a single element has been read.
     *
     * @param length length read from a buffer
     * @param max    highest capacity to pre-allocate
     * @return the length clamped to the range [0, max]
     */
    public static int initialCapacity(final int length, final int max) {
        return MathUtil.clamp(length, 0, max);
    }
}
