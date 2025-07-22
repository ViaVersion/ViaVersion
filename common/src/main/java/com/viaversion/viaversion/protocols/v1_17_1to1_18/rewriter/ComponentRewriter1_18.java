/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_17_1to1_18.rewriter;

import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.protocols.v1_17to1_17_1.packet.ClientboundPackets1_17_1;
import com.viaversion.viaversion.rewriter.text.JsonNBTComponentRewriter;
import java.util.HashMap;
import java.util.Map;

public final class ComponentRewriter1_18 extends JsonNBTComponentRewriter<ClientboundPackets1_17_1> {

    private final Map<String, String> mappings = new HashMap<>();

    public ComponentRewriter1_18(final Protocol<ClientboundPackets1_17_1, ?, ?, ?> protocol) {
        super(protocol, ReadType.JSON);

        mappings.put("commands.scoreboard.objectives.add.longName", "Objective names cannot be longer than %s characters");
        mappings.put("commands.team.add.longName", "Team names cannot be longer than %s characters");

        mappings.put("biome.minecraft.snowy_mountains", "Snowy Mountains");
        mappings.put("biome.minecraft.wooded_mountains", "Wooded Mountains");
        mappings.put("biome.minecraft.jungle_edge", "Jungle Edge");
        mappings.put("biome.minecraft.dark_forest_hills", "Dark Forest Hills");
        mappings.put("biome.minecraft.modified_wooded_badlands_plateau", "Modified Wooded Badlands Plateau");
        mappings.put("biome.minecraft.snowy_tundra", "Snowy Tundra");
        mappings.put("biome.minecraft.mountain_edge", "Mountain Edge");
        mappings.put("biome.minecraft.modified_badlands_plateau", "Modified Badlands Plateau");
        mappings.put("biome.minecraft.mushroom_field_shore", "Mushroom Field Shore");
        mappings.put("biome.minecraft.tall_birch_forest", "Tall Birch Forest");
        mappings.put("biome.minecraft.wooded_hills", "Wooded Hills");
        mappings.put("biome.minecraft.taiga_hills", "Taiga Hills");
        mappings.put("biome.minecraft.snowy_taiga_mountains", "Snowy Taiga Mountains");
        mappings.put("biome.minecraft.deep_warm_ocean", "Deep Warm Ocean");
        mappings.put("biome.minecraft.swamp_hills", "Swamp Hills");
        mappings.put("biome.minecraft.giant_tree_taiga_hills", "Giant Tree Taiga Hills");
        mappings.put("biome.minecraft.tall_birch_hills", "Tall Birch Hills");
        mappings.put("biome.minecraft.bamboo_jungle_hills", "Bamboo Jungle Hills");
        mappings.put("biome.minecraft.jungle_hills", "Jungle Hills");
        mappings.put("biome.minecraft.gravelly_mountains", "Gravelly Mountains");
        mappings.put("biome.minecraft.wooded_badlands_plateau", "Wooded Badlands Plateau");
        mappings.put("biome.minecraft.shattered_savanna", "Shattered Savanna");
        mappings.put("biome.minecraft.desert_hills", "Desert Hills");
        mappings.put("biome.minecraft.badlands_plateau", "Badlands Plateau");
        mappings.put("biome.minecraft.modified_jungle_edge", "Modified Jungle Edge");
        mappings.put("biome.minecraft.giant_spruce_taiga", "Giant Spruce Taiga");
        mappings.put("biome.minecraft.stone_shore", "Stone Shore");
        mappings.put("biome.minecraft.mountains", "Mountains");
        mappings.put("biome.minecraft.giant_tree_taiga", "Giant Tree Taiga");
        mappings.put("biome.minecraft.snowy_taiga_hills", "Snowy Taiga Hills");
        mappings.put("biome.minecraft.modified_gravelly_mountains", "Gravelly Mountains+");
        mappings.put("biome.minecraft.taiga_mountains", "Taiga Mountains");
        mappings.put("biome.minecraft.desert_lakes", "Desert Lakes");
        mappings.put("biome.minecraft.shattered_savanna_plateau", "Shattered Savanna Plateau");
        mappings.put("biome.minecraft.modified_jungle", "Modified Jungle");
        mappings.put("biome.minecraft.birch_forest_hills", "Birch Forest Hills");
        mappings.put("biome.minecraft.giant_spruce_taiga_hills", "Giant Spruce Taiga Hills");
    }

    @Override
    protected void handleTranslate(final JsonObject object, final String translate) {
        String mappedTranslation = mappings.get(translate);
        if (mappedTranslation != null) {
            object.addProperty("translate", mappedTranslation);
        }
    }
}
