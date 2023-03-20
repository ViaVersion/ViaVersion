/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2023 ViaVersion and contributors
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

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.api.data.MappingDataBase;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public final class MappingData extends MappingDataBase {

    private final Object2IntMap<String> blockEntityIds = new Object2IntOpenHashMap<>();

    public MappingData() {
        super("1.17", "1.18");
        blockEntityIds.defaultReturnValue(-1);
    }

    @Override
    protected void loadExtras(final CompoundTag data) {
        final String[] blockEntities = blockEntities();
        for (int id = 0; id < blockEntities.length; id++) {
            blockEntityIds.put(blockEntities[id], id);
        }
    }

    public Object2IntMap<String> blockEntityIds() {
        return blockEntityIds;
    }

    private String[] blockEntities() {
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
