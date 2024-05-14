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
package com.viaversion.viaversion.protocols.v1_20_5to1_21.data;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.data.MappingDataLoader;

public final class MappingData extends MappingDataBase {

    private ListTag<CompoundTag> enchantments;

    public MappingData() {
        super("1.20.5", "1.21");
    }

    @Override
    protected void loadExtras(final CompoundTag data) {
        final CompoundTag extraMappings = MappingDataLoader.INSTANCE.loadNBT("enchantments-1.21.nbt");
        enchantments = extraMappings.getListTag("entries", CompoundTag.class);
    }

    public CompoundTag enchantment(final int id) {
        return enchantments.get(id).copy();
    }
}
