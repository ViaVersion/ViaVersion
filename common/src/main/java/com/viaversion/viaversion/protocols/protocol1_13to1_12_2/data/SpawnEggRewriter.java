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
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.viaversion.viaversion.util.Key;
import java.util.Optional;

public class SpawnEggRewriter {
    private static final BiMap<String, Integer> spawnEggs = HashBiMap.create();

    static {
        // Class yz.java in 18w14b
        // Register spawn eggs (generated with GT)
        registerSpawnEgg("bat");
        registerSpawnEgg("blaze");
        registerSpawnEgg("cave_spider");
        registerSpawnEgg("chicken");
        registerSpawnEgg("cow");
        registerSpawnEgg("creeper");
        registerSpawnEgg("donkey");
        registerSpawnEgg("elder_guardian");
        registerSpawnEgg("enderman");
        registerSpawnEgg("endermite");
        registerSpawnEgg("evocation_illager");
        registerSpawnEgg("ghast");
        registerSpawnEgg("guardian");
        registerSpawnEgg("horse");
        registerSpawnEgg("husk");
        registerSpawnEgg("llama");
        registerSpawnEgg("magma_cube");
        registerSpawnEgg("mooshroom");
        registerSpawnEgg("mule");
        registerSpawnEgg("ocelot");

        registerSpawnEgg("parrot");
        registerSpawnEgg("pig");
        registerSpawnEgg("polar_bear");
        registerSpawnEgg("rabbit");
        registerSpawnEgg("sheep");
        registerSpawnEgg("shulker");
        registerSpawnEgg("silverfish");
        registerSpawnEgg("skeleton");
        registerSpawnEgg("skeleton_horse");
        registerSpawnEgg("slime");
        registerSpawnEgg("spider");
        registerSpawnEgg("squid");
        registerSpawnEgg("stray");
        registerSpawnEgg("vex");
        registerSpawnEgg("villager");
        registerSpawnEgg("vindication_illager");
        registerSpawnEgg("witch");
        registerSpawnEgg("wither_skeleton");
        registerSpawnEgg("wolf");
        registerSpawnEgg("zombie");
        registerSpawnEgg("zombie_horse");
        registerSpawnEgg("zombie_pigman");
        registerSpawnEgg("zombie_villager");
    }

    private static void registerSpawnEgg(String name) {
        spawnEggs.put(Key.namespaced(name), spawnEggs.size());
    }

    // Make it a non-existing block id
    public static int getSpawnEggId(String entityIdentifier) {
        // Fallback to bat
        if (!spawnEggs.containsKey(entityIdentifier))
            //return 25100288;
            return -1;
        return (383 << 16 | (spawnEggs.get(entityIdentifier) & 0xFFFF));
    }

    public static Optional<String> getEntityId(int spawnEggId) {
        if (spawnEggId >> 16 != 383) return Optional.empty();
        return Optional.ofNullable(spawnEggs.inverse().get(spawnEggId & 0xFFFF));
    }
}
