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

public final class NestingLimiter {

    private static final ThreadLocal<int[]> STATE = ThreadLocal.withInitial(() -> new int[]{0, -1}); // Current depth, max depth

    /**
     * Sets the max depth for reads on the current thread.
     *
     * @param maxDepth max depth, or -1 for no limit
     * @return the previous max depth, to be set again once done
     */
    public static int setMaxDepth(final int maxDepth) {
        final int[] state = STATE.get();
        final int previous = state[1];
        state[1] = maxDepth;
        return previous;
    }

    public static void increment() {
        final int[] state = STATE.get();
        if (state[1] != -1 && state[0] >= state[1]) {
            throw new IllegalArgumentException("Data is nested too deeply");
        }
        state[0]++;
    }

    public static void decrement() {
        STATE.get()[0]--;
    }
}
