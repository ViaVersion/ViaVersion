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
package com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.data;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.data.MappingDataLoader;

public final class MappingData extends MappingDataBase {

    private CompoundTag damageTypesRegistry;

    public MappingData() {
        super("1.19.3", "1.19.4");
    }

    @Override
    protected void loadExtras(final CompoundTag data) {
        damageTypesRegistry = MappingDataLoader.INSTANCE.loadNBTFromFile("damage-types-1.19.4.nbt");
    }

    public CompoundTag damageTypesRegistry() {
        return damageTypesRegistry.copy();
    }
}
