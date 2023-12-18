/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2023 ViaVersion and contributors
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

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * String to/from int ID mappings for 1.20.3 attributes.
 */
public final class AttributeMappings {

    private static final String[] ATTRIBUTES = {
            "generic.armor",
            "generic.armor_toughness",
            "generic.attack_damage",
            "generic.attack_knockback",
            "generic.attack_speed",
            "generic.flying_speed",
            "generic.follow_range",
            "horse.jump_strength",
            "generic.knockback_resistance",
            "generic.luck",
            "generic.max_absorption",
            "generic.max_health",
            "generic.movement_speed",
            "zombie.spawn_reinforcements"
    };
    private static final Object2IntMap<String> STRING_TO_ID = new Object2IntOpenHashMap<>();

    static {
        for (int i = 0; i < ATTRIBUTES.length; i++) {
            STRING_TO_ID.put(ATTRIBUTES[i], i);
        }
    }

    public static @Nullable String attribute(final int id) {
        return id >= 0 && id < ATTRIBUTES.length ? ATTRIBUTES[id] : null;
    }

    public static int id(final String attribute) {
        return STRING_TO_ID.getOrDefault(attribute, -1);
    }
}
