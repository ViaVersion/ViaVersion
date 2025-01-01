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
package com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data;

import com.viaversion.viaversion.util.KeyMappings;
import org.checkerframework.checker.nullness.qual.Nullable;

// Same as 1.20.5
public final class Instruments1_20_3 {

    private static final KeyMappings MAPPINGS = new KeyMappings(
        "ponder_goat_horn",
        "sing_goat_horn",
        "seek_goat_horn",
        "feel_goat_horn",
        "admire_goat_horn",
        "call_goat_horn",
        "yearn_goat_horn",
        "dream_goat_horn"
    );

    public static @Nullable String idToKey(final int id) {
        return MAPPINGS.idToKey(id);
    }

    public static int keyToId(final String name) {
        return MAPPINGS.keyToId(name);
    }
}
