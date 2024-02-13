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
package com.viaversion.viaversion.protocols.protocol1_16to1_15_2.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public final class AttributeMappings {
    private static final BiMap<String, String> ATTRIBUTE_MAPPINGS = HashBiMap.create();

    static {
        ATTRIBUTE_MAPPINGS.put("generic.maxHealth", "minecraft:generic.max_health");
        ATTRIBUTE_MAPPINGS.put("zombie.spawnReinforcements", "minecraft:zombie.spawn_reinforcements");
        ATTRIBUTE_MAPPINGS.put("horse.jumpStrength", "minecraft:horse.jump_strength");
        ATTRIBUTE_MAPPINGS.put("generic.followRange", "minecraft:generic.follow_range");
        ATTRIBUTE_MAPPINGS.put("generic.knockbackResistance", "minecraft:generic.knockback_resistance");
        ATTRIBUTE_MAPPINGS.put("generic.movementSpeed", "minecraft:generic.movement_speed");
        ATTRIBUTE_MAPPINGS.put("generic.flyingSpeed", "minecraft:generic.flying_speed");
        ATTRIBUTE_MAPPINGS.put("generic.attackDamage", "minecraft:generic.attack_damage");
        ATTRIBUTE_MAPPINGS.put("generic.attackKnockback", "minecraft:generic.attack_knockback");
        ATTRIBUTE_MAPPINGS.put("generic.attackSpeed", "minecraft:generic.attack_speed");
        ATTRIBUTE_MAPPINGS.put("generic.armorToughness", "minecraft:generic.armor_toughness");
    }

    public static BiMap<String, String> attributeIdentifierMappings() {
        return ATTRIBUTE_MAPPINGS;
    }
}
