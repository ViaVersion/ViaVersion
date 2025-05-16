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
package com.viaversion.viaversion.api.type.types.version;

import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.version.VersionedStructuredDataKeys;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.entitydata.types.AbstractEntityDataTypes;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.api.type.types.entitydata.EntityDataListType;
import com.viaversion.viaversion.api.type.types.entitydata.EntityDataType;
import com.viaversion.viaversion.api.type.types.item.ItemCostType1_20_5;
import com.viaversion.viaversion.api.type.types.item.ItemType1_20_5;
import com.viaversion.viaversion.api.type.types.item.LengthPrefixedStructuredDataType;
import com.viaversion.viaversion.api.type.types.item.StructuredDataType;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import java.util.List;
import java.util.function.Function;

public class Types1_20_5<K extends VersionedStructuredDataKeys, E extends AbstractEntityDataTypes> implements VersionedTypesHolder {

    public final StructuredDataType structuredData = new StructuredDataType();
    public final LengthPrefixedStructuredDataType lengthPrefixedStructuredData = new LengthPrefixedStructuredDataType(structuredData);
    public final Type<StructuredData<?>[]> structuredDataArray = new ArrayType<>(structuredData);
    public final Type<Item> item = new ItemType1_20_5(structuredData);
    public final Type<Item> lengthPrefixedItem = new ItemType1_20_5(lengthPrefixedStructuredData);
    public final Type<Item[]> itemArray = new ArrayType<>(item);
    public final Type<Item> itemCost = new ItemCostType1_20_5(structuredDataArray);
    public final Type<Item> optionalItemCost = new ItemCostType1_20_5.OptionalItemCostType(itemCost);
    public final K structuredDataKeys;

    public final ParticleType particle = new ParticleType();
    public final ArrayType<Particle> particles = new ArrayType<>(particle);
    public final E entityDataTypes;
    public final Type<EntityData> entityData;
    public final Type<List<EntityData>> entityDataList;

    public Types1_20_5(final Function<Types1_20_5<?, ?>, K> keysSupplier, final Function<Types1_20_5<?, ?>, E> entityDataTypesSupplier) {
        this.structuredDataKeys = keysSupplier.apply(this);
        this.entityDataTypes = entityDataTypesSupplier.apply(this);
        this.entityData = new EntityDataType(entityDataTypes);
        this.entityDataList = new EntityDataListType(entityData);
    }

    @Override
    public Type<Item> item() {
        return item;
    }

    @Override
    public Type<Item[]> itemArray() {
        return itemArray;
    }

    @Override
    public Type<Item> itemCost() {
        return itemCost;
    }

    @Override
    public Type<Item> optionalItemCost() {
        return optionalItemCost;
    }

    @Override
    public Type<Item> lengthPrefixedItem() {
        return lengthPrefixedItem;
    }

    @Override
    public StructuredDataType structuredData() {
        return structuredData;
    }

    @Override
    public Type<StructuredData<?>[]> structuredDataArray() {
        return structuredDataArray;
    }

    @Override
    public K structuredDataKeys() {
        return structuredDataKeys;
    }

    @Override
    public ParticleType particle() {
        return particle;
    }

    @Override
    public ArrayType<Particle> particles() {
        return particles;
    }

    @Override
    public E entityDataTypes() {
        return entityDataTypes;
    }

    @Override
    public Type<List<EntityData>> entityDataList() {
        return entityDataList;
    }
}
