/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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
package us.myles.ViaVersion.api.type;


import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.google.gson.JsonElement;
import us.myles.ViaVersion.api.minecraft.BlockChangeRecord;
import us.myles.ViaVersion.api.minecraft.EulerAngle;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.minecraft.Vector;
import us.myles.ViaVersion.api.minecraft.VillagerData;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.type.types.ArrayType;
import us.myles.ViaVersion.api.type.types.BooleanType;
import us.myles.ViaVersion.api.type.types.ByteArrayType;
import us.myles.ViaVersion.api.type.types.ByteType;
import us.myles.ViaVersion.api.type.types.ComponentType;
import us.myles.ViaVersion.api.type.types.DoubleType;
import us.myles.ViaVersion.api.type.types.FloatType;
import us.myles.ViaVersion.api.type.types.IntType;
import us.myles.ViaVersion.api.type.types.LongArrayType;
import us.myles.ViaVersion.api.type.types.LongType;
import us.myles.ViaVersion.api.type.types.RemainingBytesType;
import us.myles.ViaVersion.api.type.types.ShortType;
import us.myles.ViaVersion.api.type.types.StringType;
import us.myles.ViaVersion.api.type.types.UUIDIntArrayType;
import us.myles.ViaVersion.api.type.types.UUIDType;
import us.myles.ViaVersion.api.type.types.UnsignedByteType;
import us.myles.ViaVersion.api.type.types.UnsignedShortType;
import us.myles.ViaVersion.api.type.types.VarIntArrayType;
import us.myles.ViaVersion.api.type.types.VarIntType;
import us.myles.ViaVersion.api.type.types.VarLongType;
import us.myles.ViaVersion.api.type.types.VoidType;
import us.myles.ViaVersion.api.type.types.minecraft.BlockChangeRecordType;
import us.myles.ViaVersion.api.type.types.minecraft.EulerAngleType;
import us.myles.ViaVersion.api.type.types.minecraft.FlatItemArrayType;
import us.myles.ViaVersion.api.type.types.minecraft.FlatItemType;
import us.myles.ViaVersion.api.type.types.minecraft.FlatVarIntItemArrayType;
import us.myles.ViaVersion.api.type.types.minecraft.FlatVarIntItemType;
import us.myles.ViaVersion.api.type.types.minecraft.ItemArrayType;
import us.myles.ViaVersion.api.type.types.minecraft.ItemType;
import us.myles.ViaVersion.api.type.types.minecraft.NBTType;
import us.myles.ViaVersion.api.type.types.minecraft.OptPosition1_14Type;
import us.myles.ViaVersion.api.type.types.minecraft.OptPositionType;
import us.myles.ViaVersion.api.type.types.minecraft.OptUUIDType;
import us.myles.ViaVersion.api.type.types.minecraft.OptionalComponentType;
import us.myles.ViaVersion.api.type.types.minecraft.OptionalVarIntType;
import us.myles.ViaVersion.api.type.types.minecraft.Position1_14Type;
import us.myles.ViaVersion.api.type.types.minecraft.PositionType;
import us.myles.ViaVersion.api.type.types.minecraft.VarLongBlockChangeRecordType;
import us.myles.ViaVersion.api.type.types.minecraft.VectorType;
import us.myles.ViaVersion.api.type.types.minecraft.VillagerDataType;

import java.util.UUID;

public abstract class Type<T> implements ByteBufReader<T>, ByteBufWriter<T> {
    /* Defined Types */
    public static final Type<Byte> BYTE = new ByteType();
    /**
     * @deprecated unreasonable overhead, use BYTE_ARRAY_PRIMITIVE
     */
    @Deprecated
    public static final Type<Byte[]> BYTE_ARRAY = new ArrayType<>(Type.BYTE);
    public static final Type<byte[]> BYTE_ARRAY_PRIMITIVE = new ByteArrayType();

    public static final Type<byte[]> REMAINING_BYTES = new RemainingBytesType();

    public static final Type<Short> UNSIGNED_BYTE = new UnsignedByteType();
    /**
     * @deprecated unreasonable overhead
     */
    @Deprecated
    public static final Type<Short[]> UNSIGNED_BYTE_ARRAY = new ArrayType<>(Type.UNSIGNED_BYTE);

    public static final Type<Boolean> BOOLEAN = new BooleanType();
    /**
     * @deprecated unreasonable overhead
     */
    @Deprecated
    public static final Type<Boolean[]> BOOLEAN_ARRAY = new ArrayType<>(Type.BOOLEAN);
    /* Number Types */
    public static final Type<Integer> INT = new IntType();
    /**
     * @deprecated unreasonable overhead
     */
    @Deprecated
    public static final Type<Integer[]> INT_ARRAY = new ArrayType<>(Type.INT);

    public static final DoubleType DOUBLE = new DoubleType();
    /**
     * @deprecated unreasonable overhead
     */
    @Deprecated
    public static final Type<Double[]> DOUBLE_ARRAY = new ArrayType<>(Type.DOUBLE);

    public static final LongType LONG = new LongType();
    /**
     * @deprecated unreasonable overhead
     */
    @Deprecated
    public static final Type<Long[]> LONG_ARRAY = new ArrayType<>(Type.LONG);
    public static final Type<long[]> LONG_ARRAY_PRIMITIVE = new LongArrayType();

    public static final FloatType FLOAT = new FloatType();
    /**
     * @deprecated unreasonable overhead
     */
    @Deprecated
    public static final Type<Float[]> FLOAT_ARRAY = new ArrayType<>(Type.FLOAT);

    public static final ShortType SHORT = new ShortType();
    /**
     * @deprecated unreasonable overhead
     */
    @Deprecated
    public static final Type<Short[]> SHORT_ARRAY = new ArrayType<>(Type.SHORT);

    public static final Type<Integer> UNSIGNED_SHORT = new UnsignedShortType();
    /**
     * @deprecated unreasonable overhead
     */
    @Deprecated
    public static final Type<Integer[]> UNSIGNED_SHORT_ARRAY = new ArrayType<>(Type.UNSIGNED_SHORT);
    /* Other Types */
    public static final Type<JsonElement> COMPONENT = new ComponentType();
    public static final Type<String> STRING = new StringType();
    public static final Type<String[]> STRING_ARRAY = new ArrayType<>(Type.STRING);

    public static final Type<UUID> UUID = new UUIDType();
    public static final Type<UUID> UUID_INT_ARRAY = new UUIDIntArrayType();
    public static final Type<UUID[]> UUID_ARRAY = new ArrayType<>(Type.UUID);
    /* Variable Types */
    public static final VarIntType VAR_INT = new VarIntType();
    /**
     * @deprecated unreasonable overhead, use VAR_INT_ARRAY_PRIMITIVE
     */
    @Deprecated
    public static final Type<Integer[]> VAR_INT_ARRAY = new ArrayType<>(Type.VAR_INT);
    public static final Type<int[]> VAR_INT_ARRAY_PRIMITIVE = new VarIntArrayType();
    public static final Type<Integer> OPTIONAL_VAR_INT = new OptionalVarIntType();
    public static final VarLongType VAR_LONG = new VarLongType();
    /**
     * @deprecated unreasonable overhead
     */
    @Deprecated
    public static final Type<Long[]> VAR_LONG_ARRAY = new ArrayType<>(Type.VAR_LONG);
    /* Special Types */
    public static final Type<Void> NOTHING = new VoidType(); // This is purely used for remapping.
    /* MC Types */
    public static final Type<Position> POSITION = new PositionType();
    public static final Type<Position> POSITION1_14 = new Position1_14Type();
    public static final Type<EulerAngle> ROTATION = new EulerAngleType();
    public static final Type<Vector> VECTOR = new VectorType();
    public static final Type<CompoundTag> NBT = new NBTType();
    public static final Type<CompoundTag[]> NBT_ARRAY = new ArrayType<>(Type.NBT);

    public static final Type<UUID> OPTIONAL_UUID = new OptUUIDType();
    public static final Type<JsonElement> OPTIONAL_COMPONENT = new OptionalComponentType();
    public static final Type<Position> OPTIONAL_POSITION = new OptPositionType();
    public static final Type<Position> OPTIONAL_POSITION_1_14 = new OptPosition1_14Type();

    public static final Type<Item> ITEM = new ItemType();
    public static final Type<Item[]> ITEM_ARRAY = new ItemArrayType();

    public static final Type<BlockChangeRecord> BLOCK_CHANGE_RECORD = new BlockChangeRecordType();
    public static final Type<BlockChangeRecord[]> BLOCK_CHANGE_RECORD_ARRAY = new ArrayType<>(Type.BLOCK_CHANGE_RECORD);

    public static final Type<BlockChangeRecord> VAR_LONG_BLOCK_CHANGE_RECORD = new VarLongBlockChangeRecordType();
    public static final Type<BlockChangeRecord[]> VAR_LONG_BLOCK_CHANGE_RECORD_ARRAY = new ArrayType<>(Type.VAR_LONG_BLOCK_CHANGE_RECORD);

    public static final Type<VillagerData> VILLAGER_DATA = new VillagerDataType();

    /* 1.13 Flat Item (no data) */
    public static final Type<Item> FLAT_ITEM = new FlatItemType();
    public static final Type<Item> FLAT_VAR_INT_ITEM = new FlatVarIntItemType();
    public static final Type<Item[]> FLAT_ITEM_ARRAY = new FlatItemArrayType();
    public static final Type<Item[]> FLAT_VAR_INT_ITEM_ARRAY = new FlatVarIntItemArrayType();
    public static final Type<Item[]> FLAT_ITEM_ARRAY_VAR_INT = new ArrayType<>(FLAT_ITEM);
    public static final Type<Item[]> FLAT_VAR_INT_ITEM_ARRAY_VAR_INT = new ArrayType<>(FLAT_VAR_INT_ITEM);

    /* Actual Class */

    private final Class<? super T> outputClass;
    private final String typeName;

    public Type(Class<? super T> outputClass) {
        this(outputClass.getSimpleName(), outputClass);
    }

    public Type(String typeName, Class<? super T> outputClass) {
        this.outputClass = outputClass;
        this.typeName = typeName;
    }

    public Class<? super T> getOutputClass() {
        return outputClass;
    }

    public String getTypeName() {
        return typeName;
    }

    public Class<? extends Type> getBaseClass() {
        return this.getClass();
    }

    @Override
    public String toString() {
        return "Type|" + typeName;
    }
}
