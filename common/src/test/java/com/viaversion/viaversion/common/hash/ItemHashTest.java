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
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.GlobalBlockPosition;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.codec.CodecContext;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.api.minecraft.item.data.BannerPattern;
import com.viaversion.viaversion.api.minecraft.item.data.BannerPatternLayer;
import com.viaversion.viaversion.api.minecraft.item.data.CustomModelData1_21_4;
import com.viaversion.viaversion.api.minecraft.item.data.EnumTypes;
import com.viaversion.viaversion.api.minecraft.item.data.Equippable;
import com.viaversion.viaversion.api.minecraft.item.data.LodestoneTracker;
import com.viaversion.viaversion.api.minecraft.item.data.UseCooldown;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.codec.CodecRegistryContext;
import com.viaversion.viaversion.codec.hash.HashFunction;
import com.viaversion.viaversion.codec.hash.HashOps;
import com.viaversion.viaversion.common.PlatformTestBase;
import com.viaversion.viaversion.protocols.v1_21_6to1_21_7.Protocol1_21_6To1_21_7;
import com.viaversion.viaversion.rewriter.RegistryDataRewriter;
import com.viaversion.viaversion.util.KeyMappings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ItemHashTest extends PlatformTestBase {

    private static CodecRegistryContext context;
    private static Protocol<?, ?, ?, ?> protocol;
    private static HashOps hasher;

    @BeforeAll
    static void loadContext() {
        protocol = Via.getManager().getProtocolManager().getProtocol(Protocol1_21_6To1_21_7.class);
        context = new CodecRegistryContext(null, CodecContext.RegistryAccess.of(protocol), false);
        hasher = new HashOps(context, HashFunction.CRC32C);
    }

    @BeforeEach
    void resetHasher() {
        hasher.reset();
    }

    @Test
    void testNumberAndKey() {
        final UseCooldown useCooldown = new UseCooldown(0.5f, ":stick");
        hasher.write(UseCooldown.TYPE, useCooldown);
        Assertions.assertEquals(-524579578, hasher.hash(), "use_cooldown hash mismatch");
    }

    @Test
    void testCompoundTag() {
        hasher.write(Types.COMPOUND_TAG, createCompoundTag());
        Assertions.assertEquals(-859008863, hasher.hash(), "CompoundTag hash mismatch");
    }

    @Test
    void testArrays() {
        hasher.write(CustomModelData1_21_4.TYPE, new CustomModelData1_21_4(new float[]{1f}, new boolean[0], new String[0], new int[0]));
        Assertions.assertEquals(2007278159, hasher.hash(), "custom_model_Data hash mismatch");
    }

    @Test
    void testNestedMap() {
        hasher.write(LodestoneTracker.TYPE, new LodestoneTracker(new GlobalBlockPosition("wow", 5, 6, 7), true));
        Assertions.assertEquals(1876575779, hasher.hash(), "lodestone_tracker hash mismatch");
    }

    @Test
    void testMapInList() {
        final StructuredItem useRemainder = new StructuredItem(1, 1);
        useRemainder.dataContainer().setIdLookup(protocol, false);
        useRemainder.dataContainer().set(StructuredDataKey.ENCHANTMENT_GLINT_OVERRIDE, true);
        hasher.write(VersionedTypes.V1_21_6.item(), useRemainder);
        Assertions.assertEquals(-95388252, hasher.hash(), "use_remainder hash mismatch");
    }

    @Test
    void testMapInList2() {
        hasher.write(VersionedTypes.V1_21_6.structuredDataKeys.container.type(), new Item[]{new StructuredItem(1, 1)});
        Assertions.assertEquals(231516551, hasher.hash(), "container hash mismatch");
    }

    @Test
    void testContainerWithEmptyItem() {
        hasher.write(VersionedTypes.V1_21_6.structuredDataKeys.container.type(), new Item[]{StructuredItem.empty(), new StructuredItem(1, 1)});
        Assertions.assertEquals(1506540737, hasher.hash(), "container hash mismatch");
    }

    @Test
    void testInlinedList() {
        final HolderSet singleEntrySet = HolderSet.of(new int[]{0});
        hasher.write(Equippable.TYPE1_21_6, new Equippable(0, Holder.of(0), null, null, singleEntrySet, true, true, true, false, false, Holder.of(0)));
        Assertions.assertEquals(-1789860417, hasher.hash(), "equippable hash mismatch");
    }

    @Test
    void testRegistryHolderFull() {
        hasher.write(BannerPatternLayer.ARRAY_TYPE, new BannerPatternLayer[]{new BannerPatternLayer(Holder.of(new BannerPattern("bricks", "some_key")), EnumTypes.DYE_COLOR.idFromName("cyan"))});
        Assertions.assertEquals(-2006113844, hasher.hash(), "banner_pattern hash mismatch");
    }

    @Test
    void testRegistryHolderId() {
        ((RegistryDataRewriter) protocol.getRegistryDataRewriter()).registryKeyMappings().put("banner_pattern", new KeyMappings("base"));
        hasher.write(BannerPatternLayer.ARRAY_TYPE, new BannerPatternLayer[]{new BannerPatternLayer(Holder.of(0), 0)});
        Assertions.assertEquals(606382024, hasher.hash(), "banner_pattern hash mismatch");
    }

    private CompoundTag createCompoundTag() {
        final CompoundTag tag = new CompoundTag();
        tag.putInt("id", 1);
        tag.putString("type", "minecraft:stick");
        tag.put("wow", new CompoundTag());
        final ListTag<FloatTag> list = new ListTag<>(FloatTag.class);
        list.add(new FloatTag(0.3f));
        tag.put("list", list);
        tag.put("emptylist", new ListTag<>(CompoundTag.class));
        return tag;
    }
}
