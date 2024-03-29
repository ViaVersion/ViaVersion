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
package com.viaversion.viaversion.protocols.protocol1_19to1_18_2.data;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.NumberTag;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.data.MappingDataLoader;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class MappingData extends MappingDataBase {

    private final Int2ObjectMap<CompoundTag> defaultChatTypes = new Int2ObjectOpenHashMap<>();
    private CompoundTag chatRegistry;

    public MappingData() {
        super("1.18", "1.19");
    }

    @Override
    protected void loadExtras(final CompoundTag daata) {
        final ListTag<CompoundTag> chatTypes = MappingDataLoader.INSTANCE.loadNBTFromFile("chat-types-1.19.nbt").getListTag("values", CompoundTag.class);
        for (final CompoundTag chatType : chatTypes) {
            final NumberTag idTag = chatType.getNumberTag("id");
            defaultChatTypes.put(idTag.asInt(), chatType);
        }

        chatRegistry = MappingDataLoader.INSTANCE.loadNBTFromFile("chat-registry-1.19.nbt");
    }

    public @Nullable CompoundTag chatType(final int id) {
        return defaultChatTypes.get(id);
    }

    public CompoundTag chatRegistry() {
        return chatRegistry.copy();
    }
}
