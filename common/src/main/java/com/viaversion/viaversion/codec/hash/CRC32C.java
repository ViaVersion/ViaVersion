/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.codec.hash;

import sun.misc.Unsafe;
import java.lang.reflect.Field;
import java.nio.ByteOrder;

final class CRC32C implements HashFunction {

    private static final int REVERSED_CRC32C_POLY = 0x82F63B78; // Integer.reverse(0x1EDC6F41)

    private static final Unsafe UNSAFE;
    private static final int BYTE_BASE;
    private static final boolean LITTLE_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;
    private static final boolean ADDRESS_64;

    private static final int[][] byteTables = new int[8][256];
    private static final int[] byteTable0 = byteTables[0];
    private static final int[] byteTable1 = byteTables[1];
    private static final int[] byteTable2 = byteTables[2];
    private static final int[] byteTable3 = byteTables[3];
    private static final int[] byteTable4 = byteTables[4];
    private static final int[] byteTable5 = byteTables[5];
    private static final int[] byteTable6 = byteTables[6];
    private static final int[] byteTable7 = byteTables[7];
    private static final int[] byteTable;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe) f.get(null);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
        BYTE_BASE = UNSAFE.arrayBaseOffset(byte[].class);
        ADDRESS_64 = UNSAFE.addressSize() == 8;

        for (int index = 0; index < 256; index++) {
            int r = index;
            for (int i = 0; i < 8; i++) {
                if ((r & 1) != 0) {
                    r = (r >>> 1) ^ REVERSED_CRC32C_POLY;
                } else {
                    r >>>= 1;
                }
            }
            byteTables[0][index] = r;
        }
        for (int index = 0; index < 256; index++) {
            int r = byteTables[0][index];
            for (int k = 1; k < 8; k++) {
                r = byteTables[0][r & 0xFF] ^ (r >>> 8);
                byteTables[k][index] = r;
            }
        }
        if (LITTLE_ENDIAN) {
            byteTable = byteTables[0];
        } else {
            byteTable = new int[byteTable0.length];
            System.arraycopy(byteTable0, 0, byteTable, 0, byteTable0.length);
            for (int[] table : byteTables) {
                for (int index = 0; index < table.length; index++) {
                    table[index] = Integer.reverseBytes(table[index]);
                }
            }
        }
    }

    @Override
    public int hashBytes(final byte[] data, final int length) {
        int crc = 0xFFFFFFFF;
        int off = 0;

        if (length - off >= 8) {
            int alignLength = (8 - ((BYTE_BASE + off) & 0x7)) & 0x7;
            for (int alignEnd = off + alignLength; off < alignEnd; off++) {
                crc = (crc >>> 8) ^ byteTable[(crc ^ data[off]) & 0xFF];
            }

            if (!LITTLE_ENDIAN) {
                crc = Integer.reverseBytes(crc);
            }

            for (; off < (length - 8); off += 8) {
                int firstHalf;
                int secondHalf;
                if (!ADDRESS_64) {
                    firstHalf = UNSAFE.getInt(data, (long) BYTE_BASE + off);
                    secondHalf = UNSAFE.getInt(data, (long) BYTE_BASE + off + 4);
                } else {
                    long value = UNSAFE.getLong(data, (long) BYTE_BASE + off);
                    if (LITTLE_ENDIAN) {
                        firstHalf = (int) value;
                        secondHalf = (int) (value >>> 32);
                    } else {
                        firstHalf = (int) (value >>> 32);
                        secondHalf = (int) value;
                    }
                }
                crc ^= firstHalf;
                if (LITTLE_ENDIAN) {
                    crc = byteTable7[crc & 0xFF]
                        ^ byteTable6[(crc >>> 8) & 0xFF]
                        ^ byteTable5[(crc >>> 16) & 0xFF]
                        ^ byteTable4[crc >>> 24]
                        ^ byteTable3[secondHalf & 0xFF]
                        ^ byteTable2[(secondHalf >>> 8) & 0xFF]
                        ^ byteTable1[(secondHalf >>> 16) & 0xFF]
                        ^ byteTable0[secondHalf >>> 24];
                } else {
                    crc = byteTable0[secondHalf & 0xFF]
                        ^ byteTable1[(secondHalf >>> 8) & 0xFF]
                        ^ byteTable2[(secondHalf >>> 16) & 0xFF]
                        ^ byteTable3[secondHalf >>> 24]
                        ^ byteTable4[crc & 0xFF]
                        ^ byteTable5[(crc >>> 8) & 0xFF]
                        ^ byteTable6[(crc >>> 16) & 0xFF]
                        ^ byteTable7[crc >>> 24];
                }
            }

            if (!LITTLE_ENDIAN) {
                crc = Integer.reverseBytes(crc);
            }
        }

        for (; off < length; off++) {
            crc = (crc >>> 8) ^ byteTable[(crc ^ data[off]) & 0xFF];
        }

        return ~crc;
    }
}
