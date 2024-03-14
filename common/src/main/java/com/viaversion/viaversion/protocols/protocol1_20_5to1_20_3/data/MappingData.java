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
package com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.data;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.data.MappingDataLoader;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.List;

public class MappingData extends MappingDataBase {

    private final Object2IntMap<String> byId = new Object2IntOpenHashMap<>();
    private final List<String> itemIds = new ArrayList<>();

    public MappingData() {
        super("1.20.3", "1.20.5");
    }

    @Override
    protected void loadExtras(final CompoundTag data) {
        super.loadExtras(data);

        final ListTag<StringTag> items = MappingDataLoader.loadNBT("itemIds-1.20.3.nbt").getListTag("items", StringTag.class);
        for (int i = 0; i < items.size(); i++) {
            final StringTag tag = items.get(i);
            itemIds.add(tag.getValue());
            byId.put(tag.getValue(), i);
        }
        byId.defaultReturnValue(-1);
    }

    public int itemId(final String name) {
        return byId.getInt(name);
    }

    public String itemName(final int id) {
        return itemIds.get(id);
    }
}
