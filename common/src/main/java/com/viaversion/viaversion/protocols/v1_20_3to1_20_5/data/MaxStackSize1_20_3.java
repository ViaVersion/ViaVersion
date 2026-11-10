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
package com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

public final class MaxStackSize1_20_3 {

    private static final Int2IntMap MAPPING = new Int2IntOpenHashMap();

    static {
        fill(521, 537, 1);
        fill(764, 790, 1);
        MAPPING.put(793, 1);
        MAPPING.put(795, 1);
        MAPPING.put(797, 1);
        fill(814, 843, 1);
        MAPPING.put(846, 1);
        MAPPING.put(853, 1);
        fill(854, 876, 1);
        fill(883, 905, 16);
        fill(906, 908, 1);
        MAPPING.put(909, 16);
        fill(911, 917, 1);
        MAPPING.put(924, 16);
        MAPPING.put(927, 1);
        MAPPING.put(928, 1);
        MAPPING.put(930, 1);
        fill(960, 976, 1);
        MAPPING.put(980, 1);
        MAPPING.put(990, 16);
        MAPPING.put(995, 1);
        MAPPING.put(1085, 1);
        MAPPING.put(1086, 16);
        MAPPING.put(1107, 1);
        MAPPING.put(1113, 1);
        MAPPING.put(1116, 16);
        fill(1117, 1120, 1);
        MAPPING.put(1123, 1);
        fill(1126, 1141, 16);
        MAPPING.put(1149, 1);
        MAPPING.put(1151, 1);
        fill(1154, 1156, 1);
        fill(1159, 1176, 1);
        MAPPING.put(1178, 1);
        MAPPING.put(1182, 1);
        MAPPING.put(1183, 1);
        fill(1185, 1191, 1);
        MAPPING.put(1212, 16);
        MAPPING.put(1256, 1);
    }

    public static int getMaxStackSize(final int identifier) {
        return MAPPING.getOrDefault(identifier, 64);
    }

    private static void fill(final int start, final int end, final int value) {
        for (int i = start; i <= end; i++) {
            MAPPING.put(i, value);
        }
    }

}
