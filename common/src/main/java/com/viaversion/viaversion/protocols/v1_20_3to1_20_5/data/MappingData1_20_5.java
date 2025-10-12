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
package com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.data.MappingDataLoader;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.Protocol1_20_3To1_20_5;
import com.viaversion.viaversion.util.KeyMappings;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MappingData1_20_5 extends MappingDataBase {

    private final Object2ObjectMap<String, CompoundTag> damageTypes = new Object2ObjectOpenHashMap<>();

    public MappingData1_20_5() {
        super("1.20.3", "1.20.5");
    }

    @Override
    protected void loadExtras(final CompoundTag data) {
        super.loadExtras(data);

        final CompoundTag damageTypes = MappingDataLoader.INSTANCE.loadNBT("damage-types-1.20.3.nbt");
        for (final String key : damageTypes.keySet()) {
            this.damageTypes.put(key, damageTypes.getCompoundTag(key));
        }
    }

    public int blockId(final String name) {
        return Protocol1_20_3To1_20_5.MAPPINGS.getFullBlockMappings().id(name);
    }

    public @Nullable String blockName(final int id) {
        return Protocol1_20_3To1_20_5.MAPPINGS.getFullBlockMappings().identifier(id);
    }

    public int soundId(final String name) {
        return Protocol1_20_3To1_20_5.MAPPINGS.getFullSoundMappings().id(name);
    }

    public @Nullable String soundName(final int id) {
        return Protocol1_20_3To1_20_5.MAPPINGS.getFullSoundMappings().identifier(id);
    }

    public CompoundTag damageType(final String key) {
        return damageTypes.get(key).copy();
    }

    public ObjectSet<String> damageKeys() {
        return damageTypes.keySet();
    }
}
