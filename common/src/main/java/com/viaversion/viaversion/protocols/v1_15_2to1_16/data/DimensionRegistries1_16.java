/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_15_2to1_16.data;

import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.FloatTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.LongTag;
import com.viaversion.nbt.tag.StringTag;
import java.util.Arrays;

public class DimensionRegistries1_16 {

    private static final CompoundTag DIMENSIONS_TAG = new CompoundTag();
    private static final String[] WORLD_NAMES = {"minecraft:overworld", "minecraft:the_nether", "minecraft:the_end"};

    static {
        ListTag<CompoundTag> list = new ListTag<>(CompoundTag.class);
        list.add(createOverworldEntry());
        list.add(createOverworldCavesEntry());
        list.add(createNetherEntry());
        list.add(createEndEntry());
        DIMENSIONS_TAG.put("dimension", list);
    }

    public static CompoundTag getDimensionsTag() {
        return DIMENSIONS_TAG.copy();
    }

    public static String[] getWorldNames() {
        return Arrays.copyOf(WORLD_NAMES, WORLD_NAMES.length);
    }

    private static CompoundTag createOverworldEntry() {
        CompoundTag tag = new CompoundTag();
        tag.put("name", new StringTag("minecraft:overworld"));
        tag.put("has_ceiling", new ByteTag((byte) 0));
        addSharedOverwaldEntries(tag);
        return tag;
    }

    private static CompoundTag createOverworldCavesEntry() {
        CompoundTag tag = new CompoundTag();
        tag.put("name", new StringTag("minecraft:overworld_caves"));
        tag.put("has_ceiling", new ByteTag((byte) 1));
        addSharedOverwaldEntries(tag);
        return tag;
    }

    private static void addSharedOverwaldEntries(CompoundTag tag) {
        tag.put("piglin_safe", new ByteTag((byte) 0));
        tag.put("natural", new ByteTag((byte) 1));
        tag.put("ambient_light", new FloatTag(0));
        tag.put("infiniburn", new StringTag("minecraft:infiniburn_overworld"));
        tag.put("respawn_anchor_works", new ByteTag((byte) 0));
        tag.put("has_skylight", new ByteTag((byte) 1));
        tag.put("bed_works", new ByteTag((byte) 1));
        tag.put("has_raids", new ByteTag((byte) 1));
        tag.put("logical_height", new IntTag(256));
        tag.put("shrunk", new ByteTag((byte) 0));
        tag.put("ultrawarm", new ByteTag((byte) 0));
    }

    private static CompoundTag createNetherEntry() {
        CompoundTag tag = new CompoundTag();
        tag.put("piglin_safe", new ByteTag((byte) 1));
        tag.put("natural", new ByteTag((byte) 0));
        tag.put("ambient_light", new FloatTag(0.1F));
        tag.put("infiniburn", new StringTag("minecraft:infiniburn_nether"));
        tag.put("respawn_anchor_works", new ByteTag((byte) 1));
        tag.put("has_skylight", new ByteTag((byte) 0));
        tag.put("bed_works", new ByteTag((byte) 0));
        tag.put("fixed_time", new LongTag(18000));
        tag.put("has_raids", new ByteTag((byte) 0));
        tag.put("name", new StringTag("minecraft:the_nether"));
        tag.put("logical_height", new IntTag(128));
        tag.put("shrunk", new ByteTag((byte) 1));
        tag.put("ultrawarm", new ByteTag((byte) 1));
        tag.put("has_ceiling", new ByteTag((byte) 1));
        return tag;
    }

    private static CompoundTag createEndEntry() {
        CompoundTag tag = new CompoundTag();
        tag.put("piglin_safe", new ByteTag((byte) 0));
        tag.put("natural", new ByteTag((byte) 0));
        tag.put("ambient_light", new FloatTag(0));
        tag.put("infiniburn", new StringTag("minecraft:infiniburn_end"));
        tag.put("respawn_anchor_works", new ByteTag((byte) 0));
        tag.put("has_skylight", new ByteTag((byte) 0));
        tag.put("bed_works", new ByteTag((byte) 0));
        tag.put("fixed_time", new LongTag(6000));
        tag.put("has_raids", new ByteTag((byte) 1));
        tag.put("name", new StringTag("minecraft:the_end"));
        tag.put("logical_height", new IntTag(256));
        tag.put("shrunk", new ByteTag((byte) 0));
        tag.put("ultrawarm", new ByteTag((byte) 0));
        tag.put("has_ceiling", new ByteTag((byte) 0));
        return tag;
    }

}
