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
package com.viaversion.viaversion.util;

import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Collection;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class KeyMappings {

    private final Object2IntMap<String> keyToId;
    private final String[] keys;

    public KeyMappings(final String... keys) {
        this.keys = keys;
        keyToId = new Object2IntOpenHashMap<>(keys.length);
        keyToId.defaultReturnValue(-1);
        for (int i = 0; i < keys.length; i++) {
            keyToId.put(keys[i], i);
        }
    }

    public KeyMappings(final Collection<String> keys) {
        this(keys.toArray(new String[0]));
    }

    public KeyMappings(final ListTag<StringTag> keys) {
        this(keys.getValue().stream().map(StringTag::getValue).toArray(String[]::new));
    }

    public @Nullable String idToKey(final int id) {
        if (id < 0 || id >= keys.length) {
            return null;
        }
        return keys[id];
    }

    public int keyToId(final String identifier) {
        return keyToId.getInt(Key.stripMinecraftNamespace(identifier));
    }

    public String[] keys() {
        return keys;
    }

    public int size() {
        return keys.length;
    }
}
