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
package com.viaversion.viaversion.protocols.v1_19_3to1_19_4.data;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.data.MappingDataLoader;

public final class MappingData1_19_4 extends MappingDataBase {

    private CompoundTag damageTypesRegistry;

    public MappingData1_19_4() {
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
