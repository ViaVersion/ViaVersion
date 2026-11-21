/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v26_2to26_3.data;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.data.MappingDataLoader;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public final class MappingData26_3 extends MappingDataBase {

    private static final String[] BREWING_INPUTS = {
        "potion",
        "splash_potion",
        "lingering_potion",
        "glass_bottle"
    };
    private static final String[] BREWING_REAGENTS = {
        "blaze_powder",
        "breeze_rod",
        "cobweb",
        "dragon_breath",
        "fermented_spider_eye",
        "ghast_tear",
        "glistering_melon_slice",
        "glowstone_dust",
        "golden_carrot",
        "gunpowder",
        "magma_cream",
        "nether_wart",
        "phantom_membrane",
        "pufferfish",
        "rabbit_foot",
        "redstone",
        "slime_block",
        "spider_eye",
        "stone",
        "sugar",
        "turtle_helmet"
    };

    private CompoundTag blockTransformerRegistry;
    private int[] brewingInputIds;
    private int[] brewingReagentIds;

    public MappingData26_3() {
        super("26.2", "26.3");
    }

    @Override
    protected void loadExtras(final CompoundTag data) {
        blockTransformerRegistry = MappingDataLoader.INSTANCE.loadNBTFromFile("block-transformer-registry-26.3.nbt");

        brewingInputIds = itemIds(BREWING_INPUTS);
        brewingReagentIds = itemIds(BREWING_REAGENTS);
    }

    private int[] itemIds(final String[] identifiers) {
        final IntList ids = new IntArrayList(identifiers.length);
        for (final String identifier : identifiers) {
            final int id = getFullItemMappings().mappedId(identifier);
            if (id != -1) {
                ids.add(id);
            }
        }
        return ids.toIntArray();
    }

    public CompoundTag blockTransformerRegistry() {
        return blockTransformerRegistry;
    }

    public int[] brewingInputIds() {
        return brewingInputIds;
    }

    public int[] brewingReagentIds() {
        return brewingReagentIds;
    }
}
