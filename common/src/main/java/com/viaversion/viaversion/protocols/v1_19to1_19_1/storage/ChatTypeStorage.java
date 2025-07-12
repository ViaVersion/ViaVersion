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
package com.viaversion.viaversion.protocols.v1_19to1_19_1.storage;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.Protocol1_18_2To1_19;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ChatTypeStorage implements StorableObject {

    private final Int2ObjectMap<CompoundTag> chatTypes = new Int2ObjectOpenHashMap<>();

    public @Nullable CompoundTag chatType(final int id) {
        return chatTypes.isEmpty() ? Protocol1_18_2To1_19.MAPPINGS.chatType(id) : chatTypes.get(id);
    }

    public void addChatType(final int id, final CompoundTag chatType) {
        chatTypes.put(id, chatType);
    }

    public void clear() {
        chatTypes.clear();
    }
}
