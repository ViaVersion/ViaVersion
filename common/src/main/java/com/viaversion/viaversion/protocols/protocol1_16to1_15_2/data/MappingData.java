/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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
import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.data.MappingDataBase;

public class MappingData extends MappingDataBase {
    private final BiMap<String, String> attributeMappings = HashBiMap.create();

    public MappingData() {
        super("1.15", "1.16", true);
    }

    @Override
    protected void loadExtras(JsonObject oldMappings, JsonObject newMappings, JsonObject diffMappings) {
        attributeMappings.put("generic.maxHealth", "minecraft:generic.max_health");
        attributeMappings.put("zombie.spawnReinforcements", "minecraft:zombie.spawn_reinforcements");
        attributeMappings.put("horse.jumpStrength", "minecraft:horse.jump_strength");
        attributeMappings.put("generic.followRange", "minecraft:generic.follow_range");
        attributeMappings.put("generic.knockbackResistance", "minecraft:generic.knockback_resistance");
        attributeMappings.put("generic.movementSpeed", "minecraft:generic.movement_speed");
        attributeMappings.put("generic.flyingSpeed", "minecraft:generic.flying_speed");
        attributeMappings.put("generic.attackDamage", "minecraft:generic.attack_damage");
        attributeMappings.put("generic.attackKnockback", "minecraft:generic.attack_knockback");
        attributeMappings.put("generic.attackSpeed", "minecraft:generic.attack_speed");
        attributeMappings.put("generic.armorToughness", "minecraft:generic.armor_toughness");
    }

    public BiMap<String, String> getAttributeMappings() {
        return attributeMappings;
    }
}
