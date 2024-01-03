/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
package com.viaversion.viaversion.util;

import java.util.UUID;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class UUIDUtil {

    public static UUID fromIntArray(final int[] parts) {
        if (parts.length != 4) {
            return new UUID(0, 0);
        }
        return new UUID((long) parts[0] << 32 | (parts[1] & 0xFFFFFFFFL), (long) parts[2] << 32 | (parts[3] & 0xFFFFFFFFL));
    }

    public static int[] toIntArray(final UUID uuid) {
        return toIntArray(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
    }

    public static int[] toIntArray(final long msb, final long lsb) {
        return new int[]{(int) (msb >> 32), (int) msb, (int) (lsb >> 32), (int) lsb};
    }

    public static @Nullable UUID parseUUID(final String uuidString) {
        try {
            return UUID.fromString(uuidString);
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }
}
