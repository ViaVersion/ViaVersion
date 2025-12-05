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
package com.viaversion.viaversion.protocols.v1_21_9to1_21_11.data;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.data.MappingDataLoader;

public final class MappingData1_21_11 extends MappingDataBase {

    private CompoundTag timelineRegistry;

    public MappingData1_21_11() {
        super("1.21.9", "1.21.11");
    }

    @Override
    protected void loadExtras(final CompoundTag data) {
        timelineRegistry = MappingDataLoader.INSTANCE.loadNBTFromFile("timeline-registry-1.21.11.nbt");
    }

    public CompoundTag timelineRegistry() {
        return timelineRegistry;
    }
}
