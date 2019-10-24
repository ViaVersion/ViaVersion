package us.myles.ViaVersion.util;

import java.util.function.IntToLongFunction;

public class CompactArrayUtil {
    private CompactArrayUtil() {
        throw new AssertionError();
    }

    public static long[] createCompactArray(int bitsPerEntry, int entries, IntToLongFunction valueGetter) {
        long maxEntryValue = (1L << bitsPerEntry) - 1;
        long[] data = new long[(int) Math.ceil(entries * bitsPerEntry / 64.0)];
        for (int index = 0; index < entries; index++) {
            long value = valueGetter.applyAsLong(index);
            int bitIndex = index * bitsPerEntry;
            int startIndex = bitIndex / 64;
            int endIndex = ((index + 1) * bitsPerEntry - 1) / 64;
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
            int val;
            if (startIndex == endIndex) {
                val = (int) (data[startIndex] >>> startBitSubIndex & maxEntryValue);
            } else {
                int endBitSubIndex = 64 - startBitSubIndex;
                val = (int) ((data[startIndex] >>> startBitSubIndex | data[endIndex] << endBitSubIndex) & maxEntryValue);
            }
            consumer.consume(i, val);
        }
    }
}
