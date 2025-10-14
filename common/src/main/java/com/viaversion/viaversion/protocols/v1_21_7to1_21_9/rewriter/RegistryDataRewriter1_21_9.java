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
package com.viaversion.viaversion.protocols.v1_21_7to1_21_9.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.RegistryEntry;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.storage.DimensionScaleStorage;
import com.viaversion.viaversion.rewriter.RegistryDataRewriter;
import com.viaversion.viaversion.util.Key;

public final class RegistryDataRewriter1_21_9 extends RegistryDataRewriter {

    public RegistryDataRewriter1_21_9(final Protocol<?, ?, ?, ?> protocol) {
        super(protocol);
    }

    @Override
    protected void handleParticleData(final CompoundTag particleData) {
        final String type = particleData.getString("type");
        if (type != null && Key.stripMinecraftNamespace(type).equals("flash")) {
            particleData.putInt("color", -1);
        }
        super.handleParticleData(particleData);
    }

    @Override
    public void trackDimensionAndBiomes(final UserConnection connection, final String registryKey, final RegistryEntry[] entries) {
        super.trackDimensionAndBiomes(connection, registryKey, entries);
        if (!registryKey.equals("dimension_type")) {
            return;
        }

        final DimensionScaleStorage dimensionScaleStorage = connection.get(DimensionScaleStorage.class);
        for (int i = 0; i < entries.length; i++) {
            final RegistryEntry entry = entries[i];
            final CompoundTag dimension = (CompoundTag) entry.tag();
            if (dimension == null) {
                continue;
            }

            final double coordinateScale = dimension.getDouble("coordinate_scale", 1);
            dimensionScaleStorage.setScale(i, coordinateScale);
        }
    }
}
