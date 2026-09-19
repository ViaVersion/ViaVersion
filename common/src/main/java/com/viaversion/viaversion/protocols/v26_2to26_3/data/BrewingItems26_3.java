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

import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.protocols.v26_2to26_3.Protocol26_2To26_3;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public final class BrewingItems26_3 {

    private static final String[] INPUTS = {
        "potion",
        "splash_potion",
        "lingering_potion"
    };
    private static final String[] REAGENTS = {
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

    public static int[] inputIds() {
        return ids(INPUTS);
    }

    public static int[] reagentIds() {
        return ids(REAGENTS);
    }

    private static int[] ids(final String[] identifiers) {
        final FullMappings mappings = Protocol26_2To26_3.MAPPINGS.getFullItemMappings();
        final IntList ids = new IntArrayList(identifiers.length);
        for (final String identifier : identifiers) {
            final int id = mappings.mappedId(identifier);
            if (id != -1) {
                ids.add(id);
            }
        }
        return ids.toIntArray();
    }
}
