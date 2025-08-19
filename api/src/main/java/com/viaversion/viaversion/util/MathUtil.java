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

public final class MathUtil {

    /**
     * Miniscule value below which positive floats are considered zero
     */
    public static final float EPSILON = 1.0E-5F;

    /**
     * Returns the ceiled integer value of the given float.
     *
     * @param value float value to ceil
     * @return ceiled integer value
     */
    public static int ceil(final float value) {
        final int intValue = (int) value;
        return value > intValue ? intValue + 1 : intValue;
    }

    /**
     * Returns the ceiled log to the base of 2 for the given number.
     *
     * @param i positive number to ceillog
     * @return ceiled log2 of the given number
     */
    public static int ceilLog2(final int i) {
        return i > 0 ? 32 - Integer.numberOfLeadingZeros(i - 1) : 0;
    }

    public static long ceilLong(final double d) {
        final long l = (long) d;
        return d > l ? l + 1 : l;
    }

    /**
     * Returns the clamped number within the given range.
     *
     * @param i   number to clamp
     * @param min minimum value
     * @param max maximum value
     * @return clamped number
     */
    public static int clamp(final int i, final int min, final int max) {
        if (i < min) {
            return min;
        }
        return i > max ? max : i;
    }

    public static double clamp(final double d, final double min, final double max) {
        if (d < min) {
            return min;
        }
        return d > max ? max : d;
    }
}
