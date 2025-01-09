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

import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

// Very similar to normal items (and just results in an item), except it allows non-positive amounts and has id/amount swapped because ???
public final class ItemCostType1_20_5 extends Type<Item> {

    private static final StructuredData<?>[] EMPTY_DATA_ARRAY = new StructuredData[0];
    private final Type<StructuredData<?>[]> dataArrayType;

    public ItemCostType1_20_5(final Type<StructuredData<?>[]> dataArrayType) {
        super(Item.class);
        this.dataArrayType = dataArrayType;
    }

    @Override
    public Item read(final ByteBuf buffer) {
        final int id = Types.VAR_INT.readPrimitive(buffer);
        final int amount = Types.VAR_INT.readPrimitive(buffer);
        final StructuredData<?>[] dataArray = dataArrayType.read(buffer);
        return new StructuredItem(id, amount, new StructuredDataContainer(dataArray));
    }

    @Override
    public void write(final ByteBuf buffer, final Item object) {
        Types.VAR_INT.writePrimitive(buffer, object.identifier());
        Types.VAR_INT.writePrimitive(buffer, object.amount());
        dataArrayType.write(buffer, object.dataContainer().data().values().toArray(EMPTY_DATA_ARRAY));
    }

    public static final class OptionalItemCostType extends OptionalType<Item> {

        public OptionalItemCostType(final Type<Item> type) {
            super(type);
        }
    }
}
