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

import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.KeyMappings;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class Attributes1_20_5 {

    private static final KeyMappings ATTRIBUTES = new KeyMappings(
        "generic.armor",
        "generic.armor_toughness",
        "generic.attack_damage",
        "generic.attack_knockback",
        "generic.attack_speed",
        "player.block_break_speed",
        "player.block_interaction_range",
        "player.entity_interaction_range",
        "generic.fall_damage_multiplier",
        "generic.flying_speed",
        "generic.follow_range",
        "generic.gravity",
        "generic.jump_strength",
        "generic.knockback_resistance",
        "generic.luck",
        "generic.max_absorption",
        "generic.max_health",
        "generic.movement_speed",
        "generic.safe_fall_distance",
        "generic.scale",
        "zombie.spawn_reinforcements",
        "generic.step_height"
    );

    public static @Nullable String idToKey(final int id) {
        return ATTRIBUTES.idToKey(id);
    }

    public static int keyToId(String attribute) {
        if (Key.stripMinecraftNamespace(attribute).equals("horse.jump_strength")) {
            attribute = "generic.jump_strength";
        }
        return ATTRIBUTES.keyToId(attribute);
    }
}
