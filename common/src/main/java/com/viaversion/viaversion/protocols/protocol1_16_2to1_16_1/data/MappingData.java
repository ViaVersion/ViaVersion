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
package com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.data;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.data.MappingDataLoader;
import com.viaversion.viaversion.util.TagUtil;
import java.util.HashMap;
import java.util.Map;

public class MappingData extends MappingDataBase {
    private final Map<String, CompoundTag> dimensionDataMap = new HashMap<>();
    private CompoundTag dimensionRegistry;

    public MappingData() {
        super("1.16", "1.16.2");
    }

    @Override
    public void loadExtras(final CompoundTag data) {
        dimensionRegistry = MappingDataLoader.INSTANCE.loadNBTFromFile("dimension-registry-1.16.2.nbt");

        // Data of each dimension
        final ListTag<CompoundTag> dimensions = TagUtil.getRegistryEntries(dimensionRegistry, "dimension_type");
        for (final CompoundTag dimension : dimensions) {
            // Copy with an empty name
            final CompoundTag dimensionData = dimension.getCompoundTag("element").copy();
            dimensionDataMap.put(dimension.getStringTag("name").getValue(), dimensionData);
        }
    }

    public Map<String, CompoundTag> getDimensionDataMap() {
        return dimensionDataMap;
    }

    public CompoundTag getDimensionRegistry() {
        return dimensionRegistry.copy();
    }
}
