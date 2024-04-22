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
import org.checkerframework.checker.nullness.qual.Nullable;

public final class Enchantments1_20_5 {

    public static final KeyMappings ENCHANTMENTS = new KeyMappings(
        "protection",
        "fire_protection",
        "feather_falling",
        "blast_protection",
        "projectile_protection",
        "respiration",
        "aqua_affinity",
        "thorns",
        "depth_strider",
        "frost_walker",
        "binding_curse",
        "soul_speed",
        "swift_sneak",
        "sharpness",
        "smite",
        "bane_of_arthropods",
        "knockback",
        "fire_aspect",
        "looting",
        "sweeping_edge",
        "efficiency",
        "silk_touch",
        "unbreaking",
        "fortune",
        "power",
        "punch",
        "flame",
        "infinity",
        "luck_of_the_sea",
        "lure",
        "loyalty",
        "impaling",
        "riptide",
        "channeling",
        "multishot",
        "quick_charge",
        "piercing",
        "density",
        "breach",
        "wind_burst",
        "mending",
        "vanishing_curse"
    );

    public static @Nullable String idToKey(final int id) {
        return ENCHANTMENTS.idToKey(id);
    }

    public static int keyToId(final String enchantment) {
        return ENCHANTMENTS.keyToId(enchantment);
    }
}
