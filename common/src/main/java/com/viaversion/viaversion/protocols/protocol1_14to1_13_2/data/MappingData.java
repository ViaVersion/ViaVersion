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
package com.viaversion.viaversion.protocols.protocol1_14to1_13_2.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.data.MappingDataLoader;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.HashMap;
import java.util.Map;

public class MappingData extends MappingDataBase {
    private IntSet motionBlocking;
    private IntSet nonFullBlocks;

    public MappingData() {
        super("1.13.2", "1.14");
    }

    @Override
    public void loadExtras(JsonObject oldMappings, JsonObject newMappings, JsonObject diffMappings) {
        JsonObject blockStates = newMappings.getAsJsonObject("blockstates");
        Map<String, Integer> blockStateMap = new HashMap<>(blockStates.entrySet().size());
        for (Map.Entry<String, JsonElement> entry : blockStates.entrySet()) {
            blockStateMap.put(entry.getValue().getAsString(), Integer.parseInt(entry.getKey()));
        }

        JsonObject heightMapData = MappingDataLoader.loadData("heightMapData-1.14.json");
        JsonArray motionBlocking = heightMapData.getAsJsonArray("MOTION_BLOCKING");
        this.motionBlocking = new IntOpenHashSet(motionBlocking.size(), 1F);
        for (JsonElement blockState : motionBlocking) {
            String key = blockState.getAsString();
            Integer id = blockStateMap.get(key);
            if (id == null) {
                Via.getPlatform().getLogger().warning("Unknown blockstate " + key + " :(");
            } else {
                this.motionBlocking.add(id.intValue());
            }
        }

        if (Via.getConfig().isNonFullBlockLightFix()) {
            nonFullBlocks = new IntOpenHashSet(1611, 1F);
            for (Map.Entry<String, JsonElement> blockstates : oldMappings.getAsJsonObject("blockstates").entrySet()) {
                final String state = blockstates.getValue().getAsString();
                if (state.contains("_slab") || state.contains("_stairs") || state.contains("_wall[")) {
                    nonFullBlocks.add(blockStateMappings.getNewId(Integer.parseInt(blockstates.getKey())));
                }
            }
            nonFullBlocks.add(blockStateMappings.getNewId(8163)); // grass path
            for (int i = 3060; i <= 3067; i++) { // farmland
                nonFullBlocks.add(blockStateMappings.getNewId(i));
            }
        }
    }

    public IntSet getMotionBlocking() {
        return motionBlocking;
    }

    public IntSet getNonFullBlocks() {
        return nonFullBlocks;
    }
}
