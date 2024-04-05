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
package com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.data;

import com.viaversion.viaversion.util.KeyMappings;

public final class MapDecorations1_20_5 {

    private static final KeyMappings MAP_DECORATIONS = new KeyMappings(
        "player",
        "frame",
        "red_marker",
        "blue_marker",
        "target_x",
        "target_point",
        "player_off_map",
        "player_off_limits",
        "mansion",
        "monument",
        "banner_white",
        "banner_orange",
        "banner_magenta",
        "banner_light_blue",
        "banner_yellow",
        "banner_lime",
        "banner_pink",
        "banner_gray",
        "banner_light_gray",
        "banner_cyan",
        "banner_purple",
        "banner_blue",
        "banner_brown",
        "banner_green",
        "banner_red",
        "banner_black",
        "red_x",
        "village_desert",
        "village_plains",
        "village_savanna",
        "village_snowy",
        "village_taiga",
        "jungle_temple",
        "swamp_hut",
        "trial_chambers"
    );

    public static String idToKey(final int index) {
        return index < 0 || index >= MAP_DECORATIONS.size() ? "player" : MAP_DECORATIONS.idToKey(index);
    }

    public static int keyToId(final String key) {
        return MAP_DECORATIONS.keyToId(key);
    }
}
