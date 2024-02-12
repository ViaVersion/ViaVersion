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

import com.viaversion.viaversion.util.Key;
import java.util.HashMap;
import java.util.Map;

/*
    CHANGED ENTITY NAMES IN 1.13

    commandblock_minecart => command_block_minecart
    ender_crystal => end_crystal
    evocation_fangs => evoker_fangs
    evocation_illager => evoker
    eye_of_ender_signal => eye_of_ender
    fireworks_rocket => firework_rocket
    illusion_illager => illusioner
    snowman => snow_golem
    villager_golem => iron_golem
    vindication_illager => vindicator
    xp_bottle => experience_bottle
    xp_orb => experience_orb
 */
public class EntityNameRewriter {
    private static final Map<String, String> entityNames = new HashMap<>();

    static {
        /*
            CHANGED NAMES IN 1.13
         */
        reg("commandblock_minecart", "command_block_minecart");
        reg("ender_crystal", "end_crystal");
        reg("evocation_fangs", "evoker_fangs");
        reg("evocation_illager", "evoker");
        reg("eye_of_ender_signal", "eye_of_ender");
        reg("fireworks_rocket", "firework_rocket");
        reg("illusion_illager", "illusioner");
        reg("snowman", "snow_golem");
        reg("villager_golem", "iron_golem");
        reg("vindication_illager", "vindicator");
        reg("xp_bottle", "experience_bottle");
        reg("xp_orb", "experience_orb");
    }


    private static void reg(String past, String future) {
        entityNames.put(Key.namespaced(past), Key.namespaced(future));
    }

    public static String rewrite(String entName) {
        String entityName = entityNames.get(entName);
        if (entityName != null) {
            return entityName;
        }
        entityName = entityNames.get(Key.namespaced(entName));
        if (entityName != null) {
            return entityName;
        } else
            return entName;
    }
}
