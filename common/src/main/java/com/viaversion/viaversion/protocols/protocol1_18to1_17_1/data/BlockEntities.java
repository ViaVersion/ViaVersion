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
package com.viaversion.viaversion.protocols.protocol1_18to1_17_1.data;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public final class BlockEntities {
    private static final Object2IntMap<String> BLOCK_ENTITY_IDS = new Object2IntOpenHashMap<>();

    static {
        BLOCK_ENTITY_IDS.defaultReturnValue(-1);
        final String[] blockEntities = blockEntities();
        for (int id = 0; id < blockEntities.length; id++) {
            BLOCK_ENTITY_IDS.put(blockEntities[id], id);
        }
    }

    public static Object2IntMap<String> blockEntityIds() {
        return BLOCK_ENTITY_IDS;
    }

    private static String[] blockEntities() {
        return new String[]{
            "furnace",
            "chest",
            "trapped_chest",
            "ender_chest",
            "jukebox",
            "dispenser",
            "dropper",
            "sign",
            "mob_spawner",
            "piston",
            "brewing_stand",
            "enchanting_table",
            "end_portal",
            "beacon",
            "skull",
            "daylight_detector",
            "hopper",
            "comparator",
            "banner",
            "structure_block",
            "end_gateway",
            "command_block",
            "shulker_box",
            "bed",
            "conduit",
            "barrel",
            "smoker",
            "blast_furnace",
            "lectern",
            "bell",
            "jigsaw",
            "campfire",
            "beehive",
            "sculk_sensor"
        };
    }
}
