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
package com.viaversion.viaversion.api.type.types.item;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Map;

public class ItemType1_20_5 extends Type<Item> {

    private final StructuredDataTypeBase dataType;

    public ItemType1_20_5(final StructuredDataTypeBase dataType) {
        super(Item.class);
        this.dataType = dataType;
    }

    @Override
    public Item read(final ByteBuf buffer) {
        final int amount = Types.VAR_INT.readPrimitive(buffer);
        if (amount <= 0) {
            return StructuredItem.empty();
        }

        final int id = Types.VAR_INT.readPrimitive(buffer);
        final Map<StructuredDataKey<?>, StructuredData<?>> data = readData(buffer);
        return new StructuredItem(id, amount, new StructuredDataContainer(data));
    }

    private Map<StructuredDataKey<?>, StructuredData<?>> readData(final ByteBuf buffer) {
        final int valuesSize = Types.VAR_INT.readPrimitive(buffer);
        final int markersSize = Types.VAR_INT.readPrimitive(buffer);
        if (valuesSize == 0 && markersSize == 0) {
            return new Reference2ObjectOpenHashMap<>(0);
        }

        final Map<StructuredDataKey<?>, StructuredData<?>> map = new Reference2ObjectOpenHashMap<>(Math.min(valuesSize + markersSize, 128));
        for (int i = 0; i < valuesSize; i++) {
            final StructuredData<?> value = dataType.read(buffer);
            final StructuredDataKey<?> key = dataType.key(value.id());
            Preconditions.checkNotNull(key, "No data component serializer found for %s", value);
            map.put(key, value);
        }

        for (int i = 0; i < markersSize; i++) {
            final int id = Types.VAR_INT.readPrimitive(buffer);
            final StructuredDataKey<?> key = dataType.key(id);
            Preconditions.checkNotNull(key, "No data component serializer found for empty id %s", id);
            map.put(key, StructuredData.empty(key, id));
        }
        return map;
    }

    @Override
    public void write(final ByteBuf buffer, final Item object) {
        if (object.isEmpty()) {
            Types.VAR_INT.writePrimitive(buffer, 0);
            return;
        }

        Types.VAR_INT.writePrimitive(buffer, object.amount());
        Types.VAR_INT.writePrimitive(buffer, object.identifier());

        final Map<StructuredDataKey<?>, StructuredData<?>> data = object.dataContainer().data();
        int valuesSize = 0;
        int markersSize = 0;
        for (final StructuredData<?> value : data.values()) {
            if (value.isPresent()) {
                valuesSize++;
            } else {
                markersSize++;
            }
        }

        Types.VAR_INT.writePrimitive(buffer, valuesSize);
        Types.VAR_INT.writePrimitive(buffer, markersSize);

        for (final StructuredData<?> value : data.values()) {
            if (value.isPresent()) {
                dataType.write(buffer, value);
            }
        }
        for (final StructuredData<?> value : data.values()) {
            if (value.isEmpty()) {
                Types.VAR_INT.writePrimitive(buffer, value.id());
            }
        }
    }

    public final class OptionalItemType extends OptionalType<Item> {

        public OptionalItemType() {
            super(ItemType1_20_5.this);
        }
    }
}
