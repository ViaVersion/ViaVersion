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
package com.viaversion.viaversion.data.entity;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.NumberTag;
import com.viaversion.viaversion.api.data.entity.DimensionData;

public final class DimensionDataImpl implements DimensionData {

    private final int id;
    private final int minY;
    private final int height;

    public DimensionDataImpl(final int id, final int minY, final int height) {
        this.id = id;
        this.minY = minY;
        this.height = height;
    }

    public DimensionDataImpl(final int id, final CompoundTag dimensionData) {
        this.id = id;
        final NumberTag height = dimensionData.getNumberTag("height");
        if (height == null) {
            throw new IllegalArgumentException("height missing in dimension data: " + dimensionData);
        }
        this.height = height.asInt();

        final NumberTag minY = dimensionData.getNumberTag("min_y");
        if (minY == null) {
            throw new IllegalArgumentException("min_y missing in dimension data: " + dimensionData);
        }
        this.minY = minY.asInt();
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public int minY() {
        return minY;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public String toString() {
        return "DimensionData{" +
                "minY=" + minY +
                ", height=" + height +
                '}';
    }
}
