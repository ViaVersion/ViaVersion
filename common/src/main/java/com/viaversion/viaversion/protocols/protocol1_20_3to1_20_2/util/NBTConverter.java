/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2023 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.util;

import com.github.steveice10.opennbt.tag.builtin.ByteArrayTag;
import com.github.steveice10.opennbt.tag.builtin.ByteTag;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.DoubleTag;
import com.github.steveice10.opennbt.tag.builtin.FloatTag;
import com.github.steveice10.opennbt.tag.builtin.IntArrayTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.LongArrayTag;
import com.github.steveice10.opennbt.tag.builtin.LongTag;
import com.github.steveice10.opennbt.tag.builtin.NumberTag;
import com.github.steveice10.opennbt.tag.builtin.ShortTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import java.util.Map;
import net.lenni0451.mcstructs.nbt.INbtTag;
import org.checkerframework.checker.nullness.qual.Nullable;

final class NBTConverter {

    public static @Nullable Tag mcStructsToVia(@Nullable final INbtTag nbtTag) {
        if (nbtTag == null) {
            return null;
        } else if (nbtTag.isByteTag()) {
            return new ByteTag(nbtTag.asByteTag().getValue());
        } else if (nbtTag.isShortTag()) {
            return new ShortTag(nbtTag.asShortTag().getValue());
        } else if (nbtTag.isIntTag()) {
            return new IntTag(nbtTag.asIntTag().getValue());
        } else if (nbtTag.isLongTag()) {
            return new LongTag(nbtTag.asLongTag().getValue());
        } else if (nbtTag.isFloatTag()) {
            return new FloatTag(nbtTag.asFloatTag().getValue());
        } else if (nbtTag.isDoubleTag()) {
            return new DoubleTag(nbtTag.asDoubleTag().getValue());
        } else if (nbtTag.isByteArrayTag()) {
            return new ByteArrayTag(nbtTag.asByteArrayTag().getValue());
        } else if (nbtTag.isStringTag()) {
            return new StringTag(nbtTag.asStringTag().getValue());
        } else if (nbtTag.isListTag()) {
            final ListTag list = new ListTag();
            for (final INbtTag t : nbtTag.asListTag().getValue()) {
                list.add(mcStructsToVia(t));
            }
            return list;
        } else if (nbtTag.isCompoundTag()) {
            final Map<String, INbtTag> values = nbtTag.asCompoundTag().getValue();
            final CompoundTag compound = new CompoundTag();
            for (final Map.Entry<String, INbtTag> entry : values.entrySet()) {
                compound.put(entry.getKey(), mcStructsToVia(entry.getValue()));
            }
            return compound;
        } else if (nbtTag.isIntArrayTag()) {
            return new IntArrayTag(nbtTag.asIntArrayTag().getValue());
        } else if (nbtTag.isLongArrayTag()) {
            return new LongArrayTag(nbtTag.asLongArrayTag().getValue());
        } else {
            throw new IllegalArgumentException("Unsupported tag type: " + nbtTag.getClass().getName());
        }
    }

    public static @Nullable INbtTag viaToMcStructs(@Nullable final Tag tag) {
        if (tag == null) {
            return null;
        } else if (tag instanceof ByteTag) {
            return new net.lenni0451.mcstructs.nbt.tags.ByteTag(((NumberTag) tag).asByte());
        } else if (tag instanceof ShortTag) {
            return new net.lenni0451.mcstructs.nbt.tags.ShortTag(((NumberTag) tag).asShort());
        } else if (tag instanceof IntTag) {
            return new net.lenni0451.mcstructs.nbt.tags.IntTag(((NumberTag) tag).asInt());
        } else if (tag instanceof LongTag) {
            return new net.lenni0451.mcstructs.nbt.tags.LongTag(((NumberTag) tag).asLong());
        } else if (tag instanceof FloatTag) {
            return new net.lenni0451.mcstructs.nbt.tags.FloatTag(((NumberTag) tag).asFloat());
        } else if (tag instanceof DoubleTag) {
            return new net.lenni0451.mcstructs.nbt.tags.DoubleTag(((NumberTag) tag).asDouble());
        } else if (tag instanceof ByteArrayTag) {
            return new net.lenni0451.mcstructs.nbt.tags.ByteArrayTag(((ByteArrayTag) tag).getValue());
        } else if (tag instanceof StringTag) {
            return new net.lenni0451.mcstructs.nbt.tags.StringTag(((StringTag) tag).getValue());
        } else if (tag instanceof ListTag) {
            final net.lenni0451.mcstructs.nbt.tags.ListTag<INbtTag> list = new net.lenni0451.mcstructs.nbt.tags.ListTag<>();
            for (final Tag t : ((ListTag) tag).getValue()) {
                list.add(viaToMcStructs(t));
            }
            return list;
        } else if (tag instanceof CompoundTag) {
            final Map<String, Tag> values = ((CompoundTag) tag).getValue();
            final net.lenni0451.mcstructs.nbt.tags.CompoundTag compound = new net.lenni0451.mcstructs.nbt.tags.CompoundTag();
            for (final Map.Entry<String, Tag> entry : values.entrySet()) {
                compound.add(entry.getKey(), viaToMcStructs(entry.getValue()));
            }
            return compound;
        } else if (tag instanceof IntArrayTag) {
            return new net.lenni0451.mcstructs.nbt.tags.IntArrayTag(((IntArrayTag) tag).getValue());
        } else if (tag instanceof LongArrayTag) {
            return new net.lenni0451.mcstructs.nbt.tags.LongArrayTag(((LongArrayTag) tag).getValue());
        } else {
            throw new IllegalArgumentException("Unsupported tag type: " + tag.getClass().getName());
        }
    }
}