/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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

public class CompactArrayUtil {
    //Oh no
    private static final int[] MAGIC = {
            -1, -1, 0, Integer.MIN_VALUE, 0, 0, 1431655765, 1431655765, 0, Integer.MIN_VALUE,
            0, 1, 858993459, 858993459, 0, 715827882, 715827882, 0, 613566756, 613566756,
            0, Integer.MIN_VALUE, 0, 2, 477218588, 477218588, 0, 429496729, 429496729, 0,
            390451572, 390451572, 0, 357913941, 357913941, 0, 330382099, 330382099, 0, 306783378,
            306783378, 0, 286331153, 286331153, 0, Integer.MIN_VALUE, 0, 3, 252645135, 252645135,
            0, 238609294, 238609294, 0, 226050910, 226050910, 0, 214748364, 214748364, 0,
            204522252, 204522252, 0, 195225786, 195225786, 0, 186737708, 186737708, 0, 178956970,
            178956970, 0, 171798691, 171798691, 0, 165191049, 165191049, 0, 159072862, 159072862,
            0, 153391689, 153391689, 0, 148102320, 148102320, 0, 143165576, 143165576, 0,
            138547332, 138547332, 0, Integer.MIN_VALUE, 0, 4, 130150524, 130150524, 0, 126322567,
            126322567, 0, 122713351, 122713351, 0, 119304647, 119304647, 0, 116080197, 116080197,
            0, 113025455, 113025455, 0, 110127366, 110127366, 0, 107374182, 107374182, 0,
            104755299, 104755299, 0, 102261126, 102261126, 0, 99882960, 99882960, 0, 97612893,
            97612893, 0, 95443717, 95443717, 0, 93368854, 93368854, 0, 91382282, 91382282,
            0, 89478485, 89478485, 0, 87652393, 87652393, 0, 85899345, 85899345, 0,
            84215045, 84215045, 0, 82595524, 82595524, 0, 81037118, 81037118, 0, 79536431,
            79536431, 0, 78090314, 78090314, 0, 76695844, 76695844, 0, 75350303, 75350303,
            0, 74051160, 74051160, 0, 72796055, 72796055, 0, 71582788, 71582788, 0,
            70409299, 70409299, 0, 69273666, 69273666, 0, 68174084, 68174084, 0, Integer.MIN_VALUE,
            0, 5};

    private CompactArrayUtil() {
        throw new AssertionError();
    }

    public static long[] createCompactArrayWithPadding(int bitsPerEntry, int entries, IntToLongFunction valueGetter) {
        long maxEntryValue = (1L << bitsPerEntry) - 1;
        char valuesPerLong = (char) (64 / bitsPerEntry);
        int magicIndex = 3 * (valuesPerLong - 1);
        long divideMul = Integer.toUnsignedLong(MAGIC[magicIndex]);
        long divideAdd = Integer.toUnsignedLong(MAGIC[magicIndex + 1]);
        int divideShift = MAGIC[magicIndex + 2];
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
        int magicIndex = 3 * (valuesPerLong - 1);
        long divideMul = Integer.toUnsignedLong(MAGIC[magicIndex]);
        long divideAdd = Integer.toUnsignedLong(MAGIC[magicIndex + 1]);
        int divideShift = MAGIC[magicIndex + 2];

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
