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
package com.viaversion.viaversion.common.hash;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.FloatTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.viaversion.api.minecraft.GlobalBlockPosition;
import com.viaversion.viaversion.api.minecraft.codec.CodecContext;
import com.viaversion.viaversion.api.minecraft.item.data.CustomModelData1_21_4;
import com.viaversion.viaversion.api.minecraft.item.data.LodestoneTracker;
import com.viaversion.viaversion.api.minecraft.item.data.UseCooldown;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.codec.CodecRegistryContext;
import com.viaversion.viaversion.codec.hash.HashFunction;
import com.viaversion.viaversion.codec.hash.HashOps;
import com.viaversion.viaversion.util.SerializerVersion;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ItemHashTest {

    private static final CodecRegistryContext CONTEXT = new CodecRegistryContext(null, SerializerVersion.V1_21_5, SerializerVersion.V1_21_5, CodecContext.RegistryAccess.of(List.of(), null), true);

    @Test
    void testDataHashes() {
        final HashOps hasher = new HashOps(CONTEXT, HashFunction.CRC32C);

        final UseCooldown useCooldown = new UseCooldown(0.5f, ":stick");
        hasher.write(UseCooldown.TYPE, useCooldown);
        Assertions.assertEquals(-524579578, hasher.hash(), "UseCooldown hash mismatch");

        hasher.reset();
        hasher.write(Types.COMPOUND_TAG, createCompoundTag());
        Assertions.assertEquals(-859008863, hasher.hash(), "CompoundTag hash mismatch");

        hasher.reset();
        hasher.write(LodestoneTracker.TYPE, new LodestoneTracker(new GlobalBlockPosition("wow", 5, 6, 7), true));
        Assertions.assertEquals(1876575779, hasher.hash(), "LodestoneTracker hash mismatch");

        hasher.reset();
        hasher.write(CustomModelData1_21_4.TYPE, new CustomModelData1_21_4(new float[]{1f}, new boolean[0], new String[0], new int[0]));
        Assertions.assertEquals(2007278159, hasher.hash(), "CustomModelData hash mismatch");
    }

    private CompoundTag createCompoundTag() {
        final CompoundTag tag = new CompoundTag();
        tag.putInt("id", 1);
        tag.putString("type", "minecraft:stick");
        tag.put("wow", new CompoundTag());
        final ListTag<FloatTag> list = new ListTag<>(FloatTag.class);
        list.add(new FloatTag(0.3f));
        tag.put("list", list);
        tag.put("emptylist", new ListTag<>());
        return tag;
    }
}
