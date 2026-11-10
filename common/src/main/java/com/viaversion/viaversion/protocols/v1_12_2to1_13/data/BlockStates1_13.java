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
package com.viaversion.viaversion.protocols.v1_12_2to1_13.data;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ObjIntConsumer;

public final class BlockStates1_13 {

    /**
     * Visits every block state key (e.g. {@code chest[facing=north,type=single,waterlogged=true]})
     * together with its block state id, decoded from the given blockstates-1.13.nbt data.
     *
     * @param data     root tag of blockstates-1.13.nbt
     * @param consumer consumer called with each block state key and its id, in id order
     */
    public static void forEach(CompoundTag data, ObjIntConsumer<String> consumer) {
        ListTag<CompoundTag> propertyTable = data.getListTag("properties", CompoundTag.class);
        int blockStateId = 0;
        for (CompoundTag blockTag : data.getListTag("blockstates", CompoundTag.class)) {
            String name = blockTag.getString("name");
            IntArrayTag propertiesTag = blockTag.getIntArrayTag("properties");
            if (propertiesTag == null) {
                consumer.accept(name, blockStateId++);
                continue;
            }

            // Construct the full string from the properties
            List<String> states = List.of("");
            for (int propertyIndex : propertiesTag.getValue()) {
                CompoundTag property = propertyTable.get(propertyIndex);
                String propertyName = property.getString("name");
                List<String> combined = new ArrayList<>();
                for (String state : states) {
                    for (StringTag value : property.getListTag("values", StringTag.class)) {
                        String extendedState = state.isEmpty()
                            ? propertyName + "=" + value.getValue()
                            : state + "," + propertyName + "=" + value.getValue();
                        combined.add(extendedState);
                    }
                }
                states = combined;
            }
            for (String state : states) {
                consumer.accept(name + "[" + state + "]", blockStateId++);
            }
        }
    }
}
