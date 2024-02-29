/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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

import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ItemType1_20_5 extends Type<Item> {

    private final Type<StructuredData<?>> dataType;

    public ItemType1_20_5(final Type<StructuredData<?>> itemDataType) {
        super(Item.class);
        this.dataType = itemDataType;
    }

    @Override
    public @Nullable Item read(final ByteBuf buffer) throws Exception {
        final byte amount = buffer.readByte();
        if (amount <= 0) {
            return null;
        }

        final int id = Type.VAR_INT.readPrimitive(buffer);
        final Int2ObjectMap<Optional<StructuredData<?>>> data = readData(buffer);
        return new StructuredItem(id, amount, new StructuredDataContainer(data));
    }

    private Int2ObjectMap<Optional<StructuredData<?>>> readData(final ByteBuf buffer) throws Exception {
        final int valuesSize = Type.VAR_INT.readPrimitive(buffer);
        final int markersSize = Type.VAR_INT.readPrimitive(buffer);
        if (valuesSize == 0 && markersSize == 0) {
            return new Int2ObjectOpenHashMap<>();
        }

        final Int2ObjectMap<Optional<StructuredData<?>>> map = new Int2ObjectOpenHashMap<>(valuesSize + markersSize);
        for (int i = 0; i < valuesSize; i++) {
            final StructuredData<?> value = dataType.read(buffer);
            map.put(value.id(), Optional.of(value));
        }

        for (int i = 0; i < markersSize; i++) {
            final int key = Type.VAR_INT.readPrimitive(buffer);
            map.put(key, Optional.empty());
        }
        return map;
    }

    @Override
    public void write(final ByteBuf buffer, @Nullable final Item object) throws Exception {
        if (object == null) {
            buffer.writeByte(0);
            return;
        }

        buffer.writeByte(object.amount());
        Type.VAR_INT.writePrimitive(buffer, object.identifier());

        final Int2ObjectMap<Optional<StructuredData<?>>> data = object.structuredData().data();
        int valuesSize = 0;
        int markersSize = 0;
        for (final Int2ObjectMap.Entry<Optional<StructuredData<?>>> entry : data.int2ObjectEntrySet()) {
            if (entry.getValue().isPresent()) {
                valuesSize++;
            } else {
                markersSize++;
            }
        }

        Type.VAR_INT.writePrimitive(buffer, valuesSize);
        Type.VAR_INT.writePrimitive(buffer, markersSize);

        for (final Int2ObjectMap.Entry<Optional<StructuredData<?>>> entry : data.int2ObjectEntrySet()) {
            if (entry.getValue().isPresent()) {
                dataType.write(buffer, entry.getValue().get());
            }
        }
        for (final Int2ObjectMap.Entry<Optional<StructuredData<?>>> entry : data.int2ObjectEntrySet()) {
            if (!entry.getValue().isPresent()) {
                Type.VAR_INT.writePrimitive(buffer, entry.getIntKey());
            }
        }
    }
}
