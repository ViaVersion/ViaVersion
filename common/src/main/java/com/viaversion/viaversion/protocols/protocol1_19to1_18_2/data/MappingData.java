/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2023 ViaVersion and contributors
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
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.data.MappingDataLoader;
import com.viaversion.viaversion.api.minecraft.nbt.BinaryTagIO;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.IOException;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class MappingData extends MappingDataBase {

    private final Int2ObjectMap<CompoundTag> defaultChatTypes = new Int2ObjectOpenHashMap<>();

    public MappingData() {
        super("1.18", "1.19");
    }

    @Override
    protected void loadExtras(final CompoundTag daata) {
        try {
            // TODO Read uncompressed file
            final ListTag chatTypes = BinaryTagIO.readCompressedInputStream(MappingDataLoader.getResource("chat-types-1.19.nbt")).get("values");
            for (final Tag chatType : chatTypes) {
                final CompoundTag chatTypeCompound = (CompoundTag) chatType;
                final NumberTag idTag = chatTypeCompound.get("id");
                defaultChatTypes.put(idTag.asInt(), chatTypeCompound);
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public @Nullable CompoundTag chatType(final int id) {
        return defaultChatTypes.get(id);
    }
}
