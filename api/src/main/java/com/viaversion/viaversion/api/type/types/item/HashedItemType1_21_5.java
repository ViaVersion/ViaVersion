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

import com.viaversion.viaversion.api.minecraft.item.HashedItem;
import com.viaversion.viaversion.api.minecraft.item.HashedStructuredItem;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Limit;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class HashedItemType1_21_5 extends Type<HashedItem> {

    public HashedItemType1_21_5() {
        super(HashedItem.class);
    }

    @Override
    public HashedItem read(final ByteBuf buffer) {
        if (!buffer.readBoolean()) {
            return HashedStructuredItem.empty();
        }

        final int id = Types.VAR_INT.readPrimitive(buffer);
        final int amount = Types.VAR_INT.readPrimitive(buffer);

        final int addedComponentsSize = Limit.max(Types.VAR_INT.readPrimitive(buffer), 256);
        final Int2IntMap dataHashes = new Int2IntOpenHashMap(addedComponentsSize);
        for (int i = 0; i < addedComponentsSize; i++) {
            final int dataType = Types.VAR_INT.readPrimitive(buffer);
            final int hash = Types.INT.readPrimitive(buffer);
            dataHashes.put(dataType, hash);
        }

        final int removedComponentsSize = Limit.max(Types.VAR_INT.readPrimitive(buffer), 256);
        final IntSet removedData = new IntOpenHashSet(removedComponentsSize);
        for (int i = 0; i < removedComponentsSize; i++) {
            final int dataType = Types.VAR_INT.readPrimitive(buffer);
            removedData.add(dataType);
        }
        return new HashedStructuredItem(id, amount, dataHashes, removedData);
    }

    @Override
    public void write(final ByteBuf buffer, final HashedItem value) {
        if (value.isEmpty()) {
            buffer.writeBoolean(false);
            return;
        }

        buffer.writeBoolean(true);
        Types.VAR_INT.writePrimitive(buffer, value.identifier());
        Types.VAR_INT.writePrimitive(buffer, value.amount());

        Types.VAR_INT.writePrimitive(buffer, value.dataHashesById().size());
        for (final Int2IntMap.Entry entry : value.dataHashesById().int2IntEntrySet()) {
            Types.VAR_INT.writePrimitive(buffer, entry.getIntKey());
            Types.INT.writePrimitive(buffer, entry.getIntValue());
        }

        Types.VAR_INT.writePrimitive(buffer, value.removedDataIds().size());
        for (final int removedDataId : value.removedDataIds()) {
            Types.VAR_INT.writePrimitive(buffer, removedDataId);
        }
    }
}
