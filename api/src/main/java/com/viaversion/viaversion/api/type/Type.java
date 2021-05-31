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
package com.viaversion.viaversion.api.type;


import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.google.gson.JsonElement;
import com.viaversion.viaversion.api.minecraft.BlockChangeRecord;
import com.viaversion.viaversion.api.minecraft.EulerAngle;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.Vector;
import com.viaversion.viaversion.api.minecraft.VillagerData;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.api.type.types.BooleanType;
import com.viaversion.viaversion.api.type.types.ByteArrayType;
import com.viaversion.viaversion.api.type.types.ByteType;
import com.viaversion.viaversion.api.type.types.ComponentType;
import com.viaversion.viaversion.api.type.types.DoubleType;
import com.viaversion.viaversion.api.type.types.FloatType;
import com.viaversion.viaversion.api.type.types.IntType;
import com.viaversion.viaversion.api.type.types.LongArrayType;
import com.viaversion.viaversion.api.type.types.LongType;
import com.viaversion.viaversion.api.type.types.RemainingBytesType;
import com.viaversion.viaversion.api.type.types.ShortByteArrayType;
import com.viaversion.viaversion.api.type.types.ShortType;
import com.viaversion.viaversion.api.type.types.StringType;
import com.viaversion.viaversion.api.type.types.UUIDIntArrayType;
import com.viaversion.viaversion.api.type.types.UUIDType;
import com.viaversion.viaversion.api.type.types.UnsignedByteType;
import com.viaversion.viaversion.api.type.types.UnsignedShortType;
import com.viaversion.viaversion.api.type.types.VarIntArrayType;
import com.viaversion.viaversion.api.type.types.VarIntType;
import com.viaversion.viaversion.api.type.types.VarLongType;
import com.viaversion.viaversion.api.type.types.VoidType;
import com.viaversion.viaversion.api.type.types.minecraft.BlockChangeRecordType;
import com.viaversion.viaversion.api.type.types.minecraft.EulerAngleType;
import com.viaversion.viaversion.api.type.types.minecraft.FlatItemArrayType;
import com.viaversion.viaversion.api.type.types.minecraft.FlatItemType;
import com.viaversion.viaversion.api.type.types.minecraft.FlatVarIntItemArrayType;
import com.viaversion.viaversion.api.type.types.minecraft.FlatVarIntItemType;
import com.viaversion.viaversion.api.type.types.minecraft.ItemArrayType;
import com.viaversion.viaversion.api.type.types.minecraft.ItemType;
import com.viaversion.viaversion.api.type.types.minecraft.NBTType;
import com.viaversion.viaversion.api.type.types.minecraft.OptPosition1_14Type;
import com.viaversion.viaversion.api.type.types.minecraft.OptPositionType;
import com.viaversion.viaversion.api.type.types.minecraft.OptUUIDType;
import com.viaversion.viaversion.api.type.types.minecraft.OptionalComponentType;
import com.viaversion.viaversion.api.type.types.minecraft.OptionalVarIntType;
import com.viaversion.viaversion.api.type.types.minecraft.Position1_14Type;
import com.viaversion.viaversion.api.type.types.minecraft.PositionType;
import com.viaversion.viaversion.api.type.types.minecraft.VarLongBlockChangeRecordType;
import com.viaversion.viaversion.api.type.types.minecraft.VectorType;
import com.viaversion.viaversion.api.type.types.minecraft.VillagerDataType;

import java.util.UUID;

/**
 * Type for buffer reading and writing.
 *
 * @param <T> read/written type
 */
public abstract class Type<T> implements ByteBufReader<T>, ByteBufWriter<T> {

    public static final ByteType BYTE = new ByteType();
    public static final UnsignedByteType UNSIGNED_BYTE = new UnsignedByteType();
    public static final Type<byte[]> BYTE_ARRAY_PRIMITIVE = new ByteArrayType();
    public static final Type<byte[]> SHORT_BYTE_ARRAY = new ShortByteArrayType();
    public static final Type<byte[]> REMAINING_BYTES = new RemainingBytesType();

    public static final ShortType SHORT = new ShortType();
    public static final UnsignedShortType UNSIGNED_SHORT = new UnsignedShortType();

    public static final IntType INT = new IntType();
    public static final FloatType FLOAT = new FloatType();
    public static final DoubleType DOUBLE = new DoubleType();

    public static final LongType LONG = new LongType();
    public static final Type<long[]> LONG_ARRAY_PRIMITIVE = new LongArrayType();

    public static final BooleanType BOOLEAN = new BooleanType();

    /* Other Types */
    public static final Type<JsonElement> COMPONENT = new ComponentType();
    public static final Type<JsonElement> OPTIONAL_COMPONENT = new OptionalComponentType();

    public static final Type<String> STRING = new StringType();
    public static final Type<String[]> STRING_ARRAY = new ArrayType<>(Type.STRING);

    public static final Type<UUID> UUID = new UUIDType();
    public static final Type<UUID> OPTIONAL_UUID = new OptUUIDType();
    public static final Type<UUID> UUID_INT_ARRAY = new UUIDIntArrayType();
    public static final Type<UUID[]> UUID_ARRAY = new ArrayType<>(Type.UUID);

    public static final VarIntType VAR_INT = new VarIntType();
    public static final OptionalVarIntType OPTIONAL_VAR_INT = new OptionalVarIntType();
    public static final Type<int[]> VAR_INT_ARRAY_PRIMITIVE = new VarIntArrayType();
    public static final VarLongType VAR_LONG = new VarLongType();

    /* Boxed number array types */
    @Deprecated
    public static final Type<Byte[]> BYTE_ARRAY = new ArrayType<>(Type.BYTE);
    @Deprecated
    public static final Type<Short[]> UNSIGNED_BYTE_ARRAY = new ArrayType<>(Type.UNSIGNED_BYTE);
    @Deprecated
    public static final Type<Boolean[]> BOOLEAN_ARRAY = new ArrayType<>(Type.BOOLEAN);
    @Deprecated
    public static final Type<Integer[]> INT_ARRAY = new ArrayType<>(Type.INT);
    @Deprecated
    public static final Type<Short[]> SHORT_ARRAY = new ArrayType<>(Type.SHORT);
    @Deprecated
    public static final Type<Integer[]> UNSIGNED_SHORT_ARRAY = new ArrayType<>(Type.UNSIGNED_SHORT);
    @Deprecated
    public static final Type<Double[]> DOUBLE_ARRAY = new ArrayType<>(Type.DOUBLE);
    @Deprecated
    public static final Type<Long[]> LONG_ARRAY = new ArrayType<>(Type.LONG);
    @Deprecated
    public static final Type<Float[]> FLOAT_ARRAY = new ArrayType<>(Type.FLOAT);
    @Deprecated
    public static final Type<Integer[]> VAR_INT_ARRAY = new ArrayType<>(Type.VAR_INT);
    @Deprecated
    public static final Type<Long[]> VAR_LONG_ARRAY = new ArrayType<>(Type.VAR_LONG);

    /* Special Types */
    public static final VoidType NOTHING = new VoidType(); // This is purely used for remapping.

    /* MC Types */
    public static final Type<Position> POSITION = new PositionType();
    public static final Type<Position> POSITION1_14 = new Position1_14Type();
    public static final Type<EulerAngle> ROTATION = new EulerAngleType();
    public static final Type<Vector> VECTOR = new VectorType();
    public static final Type<CompoundTag> NBT = new NBTType();
    public static final Type<CompoundTag[]> NBT_ARRAY = new ArrayType<>(Type.NBT);

    public static final Type<Position> OPTIONAL_POSITION = new OptPositionType();
    public static final Type<Position> OPTIONAL_POSITION_1_14 = new OptPosition1_14Type();

    public static final Type<BlockChangeRecord> BLOCK_CHANGE_RECORD = new BlockChangeRecordType();
    public static final Type<BlockChangeRecord[]> BLOCK_CHANGE_RECORD_ARRAY = new ArrayType<>(Type.BLOCK_CHANGE_RECORD);

    public static final Type<BlockChangeRecord> VAR_LONG_BLOCK_CHANGE_RECORD = new VarLongBlockChangeRecordType();
    public static final Type<BlockChangeRecord[]> VAR_LONG_BLOCK_CHANGE_RECORD_ARRAY = new ArrayType<>(Type.VAR_LONG_BLOCK_CHANGE_RECORD);

    public static final Type<VillagerData> VILLAGER_DATA = new VillagerDataType();

    public static final Type<Item> ITEM = new ItemType();
    public static final Type<Item[]> ITEM_ARRAY = new ItemArrayType();

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

    protected Type(Class<? super T> outputClass) {
        this(outputClass.getSimpleName(), outputClass);
    }

    protected Type(String typeName, Class<? super T> outputClass) {
        this.outputClass = outputClass;
        this.typeName = typeName;
    }

    /**
     * Returns the output class type.
     *
     * @return output class type
     */
    public Class<? super T> getOutputClass() {
        return outputClass;
    }

    /**
     * Returns the type name.
     *
     * @return type name
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * Returns the base class, useful when the output class is insufficient for type comparison.
     * One such case are types with {{@link java.util.List}} as their output type.
     *
     * @return base class
     */
    public Class<? extends Type> getBaseClass() {
        return this.getClass();
    }

    @Override
    public String toString() {
        return "Type|" + typeName;
    }
}
