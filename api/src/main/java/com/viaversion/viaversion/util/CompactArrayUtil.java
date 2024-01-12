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

import java.util.function.IntToLongFunction;

public final class CompactArrayUtil {
    private static final long[] RECIPROCAL_MULT_AND_ADD = {
            0xffffffffL, 0x00000000, 0x55555555, 0x00000000, 0x33333333, 0x2aaaaaaa, 0x24924924, 0x00000000,
            0x1c71c71c, 0x19999999, 0x1745d174, 0x15555555, 0x13b13b13, 0x12492492, 0x11111111, 0x00000000,
            0xf0f0f0f, 0xe38e38e, 0xd79435e, 0xccccccc, 0xc30c30c, 0xba2e8ba, 0xb21642c, 0xaaaaaaa,
            0xa3d70a3, 0x9d89d89, 0x97b425e, 0x9249249, 0x8d3dcb0, 0x8888888, 0x8421084, 0x00000000,
            0x7c1f07c, 0x7878787, 0x7507507, 0x71c71c7, 0x6eb3e45, 0x6bca1af, 0x6906906, 0x6666666,
            0x63e7063, 0x6186186, 0x5f417d0, 0x5d1745d, 0x5b05b05, 0x590b216, 0x572620a, 0x5555555,
            0x5397829, 0x51eb851, 0x5050505, 0x4ec4ec4, 0x4d4873e, 0x4bda12f, 0x4a7904a, 0x4924924,
            0x47dc11f, 0x469ee58, 0x456c797, 0x4444444, 0x4325c53, 0x4210842, 0x4104104, 0x00000000
    };
    // Incrementally shift with each 0x80000000/0x00000000 tuple
    private static final int[] RECIPROCAL_RIGHT_SHIFT = {
            0, 0, 0, 1, 0, 0, 0, 2,
            0, 0, 0, 0, 0, 0, 0, 3,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 4,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 5
    };

    private CompactArrayUtil() {
        throw new AssertionError();
    }

    public static long[] createCompactArrayWithPadding(int bitsPerEntry, int entries, IntToLongFunction valueGetter) {
        long maxEntryValue = (1L << bitsPerEntry) - 1;
        char valuesPerLong = (char) (64 / bitsPerEntry);
        int magicIndex = valuesPerLong - 1;
        long divideAdd = RECIPROCAL_MULT_AND_ADD[magicIndex];
        long divideMul = divideAdd != 0 ? divideAdd : 0x80000000L; // Special case for the 0x80000000/0x00000000 tuple
        int divideShift = RECIPROCAL_RIGHT_SHIFT[magicIndex];
        int size = (entries + valuesPerLong - 1) / valuesPerLong;

        long[] data = new long[size];

        for (int i = 0; i < entries; i++) {
            long value = valueGetter.applyAsLong(i);
            int cellIndex = (int) (i * divideMul + divideAdd >> 32L >> divideShift);
            int bitIndex = (i - cellIndex * valuesPerLong) * bitsPerEntry;
            data[cellIndex] = data[cellIndex] & ~(maxEntryValue << bitIndex) | (value & maxEntryValue) << bitIndex;
        }

        return data;
    }

    public static void iterateCompactArrayWithPadding(int bitsPerEntry, int entries, long[] data, BiIntConsumer consumer) {
        long maxEntryValue = (1L << bitsPerEntry) - 1;
        char valuesPerLong = (char) (64 / bitsPerEntry);
        int magicIndex = valuesPerLong - 1;
        long divideAdd = RECIPROCAL_MULT_AND_ADD[magicIndex];
        long divideMul = divideAdd != 0 ? divideAdd : 0x80000000L;
        int divideShift = RECIPROCAL_RIGHT_SHIFT[magicIndex];

        for (int i = 0; i < entries; i++) {
            int cellIndex = (int) (i * divideMul + divideAdd >> 32L >> divideShift);
            int bitIndex = (i - cellIndex * valuesPerLong) * bitsPerEntry;
            int value = (int) (data[cellIndex] >> bitIndex & maxEntryValue);
            consumer.consume(i, value);
        }
    }

    public static long[] createCompactArray(int bitsPerEntry, int entries, IntToLongFunction valueGetter) {
        long maxEntryValue = (1L << bitsPerEntry) - 1;
        long[] data = new long[(int) Math.ceil(entries * bitsPerEntry / 64.0)];
        for (int i = 0; i < entries; i++) {
            long value = valueGetter.applyAsLong(i);
            int bitIndex = i * bitsPerEntry;
            int startIndex = bitIndex / 64;
            int endIndex = ((i + 1) * bitsPerEntry - 1) / 64;
            int startBitSubIndex = bitIndex % 64;
            data[startIndex] = data[startIndex] & ~(maxEntryValue << startBitSubIndex) | (value & maxEntryValue) << startBitSubIndex;
            if (startIndex != endIndex) {
                int endBitSubIndex = 64 - startBitSubIndex;
                data[endIndex] = data[endIndex] >>> endBitSubIndex << endBitSubIndex | (value & maxEntryValue) >> endBitSubIndex;
            }
        }
        return data;
    }

    public static void iterateCompactArray(int bitsPerEntry, int entries, long[] data, BiIntConsumer consumer) {
        long maxEntryValue = (1L << bitsPerEntry) - 1;
        for (int i = 0; i < entries; i++) {
            int bitIndex = i * bitsPerEntry;
            int startIndex = bitIndex / 64;
            int endIndex = ((i + 1) * bitsPerEntry - 1) / 64;
            int startBitSubIndex = bitIndex % 64;
            int value;
            if (startIndex == endIndex) {
                value = (int) (data[startIndex] >>> startBitSubIndex & maxEntryValue);
            } else {
                int endBitSubIndex = 64 - startBitSubIndex;
                value = (int) ((data[startIndex] >>> startBitSubIndex | data[endIndex] << endBitSubIndex) & maxEntryValue);
            }
            consumer.consume(i, value);
        }
    }
}
