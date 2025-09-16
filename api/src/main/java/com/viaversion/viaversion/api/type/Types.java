/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.viaversion.viaversion.api.type;

import com.google.gson.JsonElement;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.minecraft.BlockChangeRecord;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.ChunkPosition;
import com.viaversion.viaversion.api.minecraft.EulerAngle;
import com.viaversion.viaversion.api.minecraft.GameProfile;
import com.viaversion.viaversion.api.minecraft.GlobalBlockPosition;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.PlayerMessageSignature;
import com.viaversion.viaversion.api.minecraft.ProfileKey;
import com.viaversion.viaversion.api.minecraft.Quaternion;
import com.viaversion.viaversion.api.minecraft.RegistryEntry;
import com.viaversion.viaversion.api.minecraft.ResolvableProfile;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.Vector;
import com.viaversion.viaversion.api.minecraft.Vector3d;
import com.viaversion.viaversion.api.minecraft.Vector3f;
import com.viaversion.viaversion.api.minecraft.VillagerData;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.Heightmap;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.item.HashedItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.data.Enchantments;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.api.type.types.BitSetType;
import com.viaversion.viaversion.api.type.types.BooleanArrayType;
import com.viaversion.viaversion.api.type.types.BooleanType;
import com.viaversion.viaversion.api.type.types.ByteArrayType;
import com.viaversion.viaversion.api.type.types.ByteType;
import com.viaversion.viaversion.api.type.types.ComponentType;
import com.viaversion.viaversion.api.type.types.DoubleType;
import com.viaversion.viaversion.api.type.types.EmptyType;
import com.viaversion.viaversion.api.type.types.FloatArrayType;
import com.viaversion.viaversion.api.type.types.FloatType;
import com.viaversion.viaversion.api.type.types.IntArrayType;
import com.viaversion.viaversion.api.type.types.IntType;
import com.viaversion.viaversion.api.type.types.LongArrayType;
import com.viaversion.viaversion.api.type.types.LongType;
import com.viaversion.viaversion.api.type.types.OptionalVarIntType;
import com.viaversion.viaversion.api.type.types.RegistryEntryType;
import com.viaversion.viaversion.api.type.types.RemainingBytesType;
import com.viaversion.viaversion.api.type.types.ShortByteArrayType;
import com.viaversion.viaversion.api.type.types.ShortType;
import com.viaversion.viaversion.api.type.types.StringType;
import com.viaversion.viaversion.api.type.types.UUIDType;
import com.viaversion.viaversion.api.type.types.UnsignedByteType;
import com.viaversion.viaversion.api.type.types.UnsignedIntType;
import com.viaversion.viaversion.api.type.types.UnsignedShortType;
import com.viaversion.viaversion.api.type.types.VarIntArrayType;
import com.viaversion.viaversion.api.type.types.VarIntType;
import com.viaversion.viaversion.api.type.types.VarLongType;
import com.viaversion.viaversion.api.type.types.block.BlockChangeRecordType;
import com.viaversion.viaversion.api.type.types.block.BlockEntityType1_18;
import com.viaversion.viaversion.api.type.types.block.BlockEntityType1_20_2;
import com.viaversion.viaversion.api.type.types.block.VarLongBlockChangeRecordType;
import com.viaversion.viaversion.api.type.types.chunk.ChunkSectionType1_13;
import com.viaversion.viaversion.api.type.types.chunk.ChunkSectionType1_16;
import com.viaversion.viaversion.api.type.types.chunk.ChunkSectionType1_8;
import com.viaversion.viaversion.api.type.types.chunk.ChunkSectionType1_9;
import com.viaversion.viaversion.api.type.types.chunk.HeightmapType;
import com.viaversion.viaversion.api.type.types.entitydata.EntityDataListType;
import com.viaversion.viaversion.api.type.types.entitydata.EntityDataType1_12;
import com.viaversion.viaversion.api.type.types.entitydata.EntityDataType1_8;
import com.viaversion.viaversion.api.type.types.entitydata.EntityDataType1_9;
import com.viaversion.viaversion.api.type.types.item.HashedItemType1_21_5;
import com.viaversion.viaversion.api.type.types.item.ItemShortArrayType1_13;
import com.viaversion.viaversion.api.type.types.item.ItemShortArrayType1_13_2;
import com.viaversion.viaversion.api.type.types.item.ItemShortArrayType1_8;
import com.viaversion.viaversion.api.type.types.item.ItemType1_13;
import com.viaversion.viaversion.api.type.types.item.ItemType1_13_2;
import com.viaversion.viaversion.api.type.types.item.ItemType1_20_2;
import com.viaversion.viaversion.api.type.types.item.ItemType1_8;
import com.viaversion.viaversion.api.type.types.math.BlockPositionType1_14;
import com.viaversion.viaversion.api.type.types.math.BlockPositionType1_8;
import com.viaversion.viaversion.api.type.types.math.ChunkPositionType;
import com.viaversion.viaversion.api.type.types.math.EulerAngleType;
import com.viaversion.viaversion.api.type.types.math.GlobalBlockPositionType;
import com.viaversion.viaversion.api.type.types.math.MovementVectorType;
import com.viaversion.viaversion.api.type.types.math.QuaternionType;
import com.viaversion.viaversion.api.type.types.math.Vector3fType;
import com.viaversion.viaversion.api.type.types.math.VectorType;
import com.viaversion.viaversion.api.type.types.misc.CompoundTagType;
import com.viaversion.viaversion.api.type.types.misc.GameProfileType;
import com.viaversion.viaversion.api.type.types.misc.HolderSetType;
import com.viaversion.viaversion.api.type.types.misc.HolderType;
import com.viaversion.viaversion.api.type.types.misc.KeyType;
import com.viaversion.viaversion.api.type.types.misc.LengthPrefixedTagType;
import com.viaversion.viaversion.api.type.types.misc.NamedCompoundTagType;
import com.viaversion.viaversion.api.type.types.misc.PlayerMessageSignatureType;
import com.viaversion.viaversion.api.type.types.misc.ProfileKeyType;
import com.viaversion.viaversion.api.type.types.misc.ProfilePropertyType;
import com.viaversion.viaversion.api.type.types.misc.SoundEventType;
import com.viaversion.viaversion.api.type.types.misc.ResolvableProfileType;
import com.viaversion.viaversion.api.type.types.misc.TagKeyType;
import com.viaversion.viaversion.api.type.types.misc.TagType;
import com.viaversion.viaversion.api.type.types.misc.TextComponentTagType;
import com.viaversion.viaversion.api.type.types.misc.VillagerDataType;
import com.viaversion.viaversion.api.type.types.version.Types1_20_5;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.Unit;
import java.util.List;
import java.util.UUID;

/**
 * Holds the mostly stable {@link Type} instances.
 * Others may be held separately or object classes themselves, e.g. {@link Types1_20_5} or {@link Enchantments}
 */
public final class Types {

    public static final Type<Unit> EMPTY = new EmptyType();

    public static final ByteType BYTE = new ByteType();
    public static final UnsignedByteType UNSIGNED_BYTE = new UnsignedByteType();
    public static final Type<byte[]> BYTE_ARRAY_PRIMITIVE = new ByteArrayType();
    public static final Type<byte[]> OPTIONAL_BYTE_ARRAY_PRIMITIVE = new ByteArrayType.OptionalByteArrayType();
    public static final Type<byte[]> SHORT_BYTE_ARRAY = new ShortByteArrayType();
    public static final Type<byte[]> REMAINING_BYTES = new RemainingBytesType();

    public static final ShortType SHORT = new ShortType();
    public static final UnsignedShortType UNSIGNED_SHORT = new UnsignedShortType();

    public static final IntType INT = new IntType();
    public static final Type<int[]> INT_ARRAY_PRIMITIVE = new IntArrayType();
    public static final UnsignedIntType UNSIGNED_INT = new UnsignedIntType();

    public static final FloatType FLOAT = new FloatType();
    public static final FloatType.OptionalFloatType OPTIONAL_FLOAT = new FloatType.OptionalFloatType();
    public static final Type<float[]> FLOAT_ARRAY_PRIMITIVE = new FloatArrayType();

    public static final DoubleType DOUBLE = new DoubleType();

    public static final LongType LONG = new LongType();
    public static final Type<Long> OPTIONAL_LONG = new LongType.OptionalLongType();
    public static final Type<long[]> LONG_ARRAY_PRIMITIVE = new LongArrayType();

    public static final BooleanType BOOLEAN = new BooleanType();
    public static final BooleanType.OptionalBooleanType OPTIONAL_BOOLEAN = new BooleanType.OptionalBooleanType();
    public static final Type<boolean[]> BOOLEAN_ARRAY_PRIMITIVE = new BooleanArrayType();

    /* Other Types */
    public static final Type<JsonElement> COMPONENT = new ComponentType();
    public static final Type<JsonElement> OPTIONAL_COMPONENT = new ComponentType.OptionalComponentType();

    public static final Type<String> STRING = new StringType();
    public static final Type<String> OPTIONAL_STRING = new StringType.OptionalStringType();
    public static final Type<String[]> STRING_ARRAY = new ArrayType<>(Types.STRING);

    public static final Type<UUID> UUID = new UUIDType();
    public static final Type<UUID> OPTIONAL_UUID = new UUIDType.OptionalUUIDType();
    public static final Type<UUID[]> UUID_ARRAY = new ArrayType<>(Types.UUID);

    public static final VarIntType VAR_INT = new VarIntType();
    public static final OptionalVarIntType OPTIONAL_VAR_INT = new OptionalVarIntType();
    public static final Type<int[]> VAR_INT_ARRAY_PRIMITIVE = new VarIntArrayType();
    public static final VarLongType VAR_LONG = new VarLongType();

    /* MC Types */
    public static final Type<byte[]> SERVERBOUND_CUSTOM_PAYLOAD_DATA = new RemainingBytesType(Short.MAX_VALUE);

    public static final Type<Key> RESOURCE_LOCATION = new KeyType();
    public static final Type<Key> OPTIONAL_RESOURCE_LOCATION = new KeyType.OptionalKeyType();
    public static final Type<Key[]> RESOURCE_LOCATION_ARRAY = new ArrayType<>(RESOURCE_LOCATION);
    public static final Type<Key> TAG_KEY = new TagKeyType();

    public static final Type<BlockPosition> BLOCK_POSITION1_8 = new BlockPositionType1_8();
    public static final Type<BlockPosition> OPTIONAL_POSITION1_8 = new BlockPositionType1_8.OptionalBlockPositionType();
    public static final Type<BlockPosition> BLOCK_POSITION1_14 = new BlockPositionType1_14();
    public static final Type<BlockPosition> OPTIONAL_POSITION_1_14 = new BlockPositionType1_14.OptionalBlockPositionType();
    public static final Type<EulerAngle> ROTATIONS = new EulerAngleType();
    public static final Type<Vector> VECTOR = new VectorType();
    public static final Type<Vector3f> VECTOR3F = new Vector3fType();
    public static final Type<Quaternion> QUATERNION = new QuaternionType();
    public static final Type<Vector3d> MOVEMENT_VECTOR = new MovementVectorType();

    public static final Type<CompoundTag> NAMED_COMPOUND_TAG = new NamedCompoundTagType();
    public static final Type<CompoundTag> OPTIONAL_NAMED_COMPOUND_TAG = new NamedCompoundTagType.OptionalNamedCompoundTagType();
    public static final Type<CompoundTag[]> NAMED_COMPOUND_TAG_ARRAY = new ArrayType<>(Types.NAMED_COMPOUND_TAG);
    public static final Type<CompoundTag> COMPOUND_TAG = new CompoundTagType();
    public static final Type<CompoundTag> OPTIONAL_COMPOUND_TAG = new CompoundTagType.OptionalCompoundTagType();
    public static final Type<Tag> TAG = new TagType();
    public static final Type<Tag[]> TAG_ARRAY = new ArrayType<>(TAG);
    public static final Type<Tag> OPTIONAL_TAG = new TagType.OptionalTagType();
    public static final Type<Tag> TEXT_COMPONENT_TAG = new TextComponentTagType(); // only strictly needed for hashing
    public static final Type<Tag> CUSTOM_CLICK_ACTION_TAG = new LengthPrefixedTagType(65536);

    public static final Type<GlobalBlockPosition> GLOBAL_POSITION = new GlobalBlockPositionType();
    public static final Type<GlobalBlockPosition> OPTIONAL_GLOBAL_POSITION = new GlobalBlockPositionType.OptionalGlobalPositionType();
    public static final Type<ChunkPosition> CHUNK_POSITION = new ChunkPositionType();

    public static final Type<ChunkSection> CHUNK_SECTION1_8 = new ChunkSectionType1_8();
    public static final Type<ChunkSection> CHUNK_SECTION1_9 = new ChunkSectionType1_9();
    public static final Type<ChunkSection> CHUNK_SECTION1_13 = new ChunkSectionType1_13();
    public static final Type<ChunkSection> CHUNK_SECTION1_16 = new ChunkSectionType1_16();

    public static final Type<BlockEntity> BLOCK_ENTITY1_18 = new BlockEntityType1_18();
    public static final Type<BlockEntity> BLOCK_ENTITY1_20_2 = new BlockEntityType1_20_2();

    public static final Type<BlockChangeRecord> BLOCK_CHANGE = new BlockChangeRecordType();
    public static final Type<BlockChangeRecord[]> BLOCK_CHANGE_ARRAY = new ArrayType<>(Types.BLOCK_CHANGE);
    public static final Type<BlockChangeRecord> VAR_LONG_BLOCK_CHANGE = new VarLongBlockChangeRecordType();
    public static final Type<BlockChangeRecord[]> VAR_LONG_BLOCK_CHANGE_ARRAY = new ArrayType<>(Types.VAR_LONG_BLOCK_CHANGE);

    public static final Type<VillagerData> VILLAGER_DATA = new VillagerDataType();

    public static final Type<GameProfile> GAME_PROFILE = new GameProfileType();
    public static final Type<ResolvableProfile> RESOLVABLE_PROFILE = new ResolvableProfileType();
    public static final Type<GameProfile.Property> PROFILE_PROPERTY = new ProfilePropertyType();
    public static final Type<GameProfile.Property[]> PROFILE_PROPERTY_ARRAY = new ArrayType<>(PROFILE_PROPERTY);
    public static final Type<ProfileKey> PROFILE_KEY = new ProfileKeyType();
    public static final Type<ProfileKey> OPTIONAL_PROFILE_KEY = new ProfileKeyType.OptionalProfileKeyType();

    public static final Type<PlayerMessageSignature> PLAYER_MESSAGE_SIGNATURE = new PlayerMessageSignatureType();
    public static final Type<PlayerMessageSignature> OPTIONAL_PLAYER_MESSAGE_SIGNATURE = new PlayerMessageSignatureType.OptionalPlayerMessageSignatureType();
    public static final Type<PlayerMessageSignature[]> PLAYER_MESSAGE_SIGNATURE_ARRAY = new ArrayType<>(PLAYER_MESSAGE_SIGNATURE);

    public static final BitSetType PROFILE_ACTIONS_ENUM1_19_3 = new BitSetType(6);
    public static final BitSetType PROFILE_ACTIONS_ENUM1_21_2 = new BitSetType(7);
    public static final BitSetType PROFILE_ACTIONS_ENUM1_21_4 = new BitSetType(8);

    public static final ByteArrayType SIGNATURE_BYTES = new ByteArrayType(256);
    public static final BitSetType ACKNOWLEDGED_BIT_SET = new BitSetType(20);
    public static final ByteArrayType.OptionalByteArrayType OPTIONAL_SIGNATURE_BYTES = new ByteArrayType.OptionalByteArrayType(256);

    public static final Type<RegistryEntry> REGISTRY_ENTRY = new RegistryEntryType();
    public static final Type<RegistryEntry[]> REGISTRY_ENTRY_ARRAY = new ArrayType<>(REGISTRY_ENTRY);

    public static final Type<HolderSet> HOLDER_SET = new HolderSetType();
    public static final Type<HolderSet> OPTIONAL_HOLDER_SET = new HolderSetType.OptionalHolderSetType();

    public static final HolderType<SoundEvent> SOUND_EVENT = new SoundEventType();
    public static final HolderType<SoundEvent> OPTIONAL_SOUND_EVENT = new SoundEventType.OptionalSoundEventType();

    public static final Type<Heightmap> HEIGHTMAP = new HeightmapType();
    public static final Type<Heightmap[]> HEIGHTMAP_ARRAY = new ArrayType<>(HEIGHTMAP);

    public static final Type<EntityData> ENTITY_DATA1_8 = new EntityDataType1_8();
    public static final Type<EntityData> ENTITY_DATA1_9 = new EntityDataType1_9();
    public static final Type<EntityData> ENTITY_DATA1_12 = new EntityDataType1_12();

    public static final Type<List<EntityData>> ENTITY_DATA_LIST1_8 = new EntityDataListType(ENTITY_DATA1_8);
    public static final Type<List<EntityData>> ENTITY_DATA_LIST1_9 = new EntityDataListType(ENTITY_DATA1_9);
    public static final Type<List<EntityData>> ENTITY_DATA_LIST1_12 = new EntityDataListType(ENTITY_DATA1_12);

    // No longer used past 1.20.2
    public static final Type<Item> ITEM1_8 = new ItemType1_8();
    public static final Type<Item> ITEM1_13 = new ItemType1_13();
    public static final Type<Item> ITEM1_13_2 = new ItemType1_13_2();
    public static final Type<Item> ITEM1_20_2 = new ItemType1_20_2();
    public static final Type<Item[]> ITEM1_8_SHORT_ARRAY = new ItemShortArrayType1_8();
    public static final Type<Item[]> ITEM1_13_SHORT_ARRAY = new ItemShortArrayType1_13();
    public static final Type<Item[]> ITEM1_13_2_SHORT_ARRAY = new ItemShortArrayType1_13_2();
    public static final Type<Item[]> ITEM1_13_ARRAY = new ArrayType<>(ITEM1_13);
    public static final Type<Item[]> ITEM1_13_2_ARRAY = new ArrayType<>(ITEM1_13_2);
    public static final Type<Item[]> ITEM1_20_2_ARRAY = new ArrayType<>(ITEM1_20_2);

    public static final Type<HashedItem> HASHED_ITEM = new HashedItemType1_21_5();
}
