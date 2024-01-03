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
package com.viaversion.viaversion.protocols.protocol1_19to1_18_2.storage;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.api.connection.StorableObject;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class DimensionRegistryStorage implements StorableObject {

    private Map<CompoundTag, String> dimensions;

    public @Nullable String dimensionKey(final CompoundTag dimensionData) {
        return dimensions.get(dimensionData); // HMMMMMMMMMMM
    }

    public void setDimensions(final Map<CompoundTag, String> dimensions) {
        this.dimensions = dimensions;
    }

    public Map<CompoundTag, String> dimensions() {
        return dimensions;
    }

    @Override
    public boolean clearOnServerSwitch() {
        return false;
    }
}
