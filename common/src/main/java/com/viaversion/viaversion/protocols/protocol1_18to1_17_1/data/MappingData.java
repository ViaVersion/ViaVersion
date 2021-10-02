/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_18to1_17_1.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.data.MappingDataBase;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class MappingData extends MappingDataBase {

    private final Object2IntMap<String> blockEntityIds = new Object2IntOpenHashMap<>();

    public MappingData() {
        super("1.17", "1.18");
        blockEntityIds.defaultReturnValue(-1);
    }

    @Override
    protected void loadExtras(final JsonObject oldMappings, final JsonObject newMappings, @Nullable final JsonObject diffMappings) {
        int i = 0;
        for (final JsonElement element : newMappings.getAsJsonArray("blockentities")) {
            final String id = element.getAsString();
            blockEntityIds.put(id, i++);
        }
    }

    public Object2IntMap<String> blockEntityIds() {
        return blockEntityIds;
    }
}
