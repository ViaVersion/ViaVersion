/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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

final class CRC32C implements HashFunction {

    private static final int[] CRC32C_TABLE = new int[256];

    static {
        for (int i = 0; i < 256; i++) {
            int crc = i;
            for (int j = 0; j < 8; j++) {
                if ((crc & 1) == 1) {
                    crc = (crc >>> 1) ^ 0x82F63B78;
                } else {
                    crc >>>= 1;
                }
            }
            CRC32C_TABLE[i] = crc;
        }
    }

    @Override
    public int hashBytes(final byte[] data, final int length) {
        int crc = ~0;
        for (int i = 0; i < length; i++) {
            final byte b = data[i];
            final int index = (crc ^ b) & 0xFF;
            crc = (crc >>> 8) ^ CRC32C_TABLE[index];
        }
        return ~crc;
    }
}
