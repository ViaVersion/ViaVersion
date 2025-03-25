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
package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Copyable;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;

public final class PotDecorations implements Copyable {

    public static final Type<PotDecorations> TYPE = new Type<>(PotDecorations.class) {
        @Override
        public PotDecorations read(final ByteBuf buffer) {
            return new PotDecorations(Types.VAR_INT_ARRAY_PRIMITIVE.read(buffer));
        }

        @Override
        public void write(final ByteBuf buffer, final PotDecorations value) {
            Types.VAR_INT_ARRAY_PRIMITIVE.write(buffer, value.itemIds());
        }
    };

    private final int[] itemIds;

    public PotDecorations(final int[] itemIds) {
        this.itemIds = itemIds;
    }

    public PotDecorations(final int backItem, final int leftItem, final int rightItem, final int frontItem) {
        this.itemIds = new int[]{backItem, leftItem, rightItem, frontItem};
    }

    public int[] itemIds() {
        return itemIds;
    }

    public int backItem() {
        return item(0);
    }

    public int leftItem() {
        return item(1);
    }

    public int rightItem() {
        return item(2);
    }

    public int frontItem() {
        return item(3);
    }

    private int item(final int index) {
        return index < 0 || index >= itemIds.length ? -1 : itemIds[index];
    }

    public PotDecorations rewrite(final Int2IntFunction idRewriteFunction) {
        final int[] newItems = new int[itemIds.length];
        for (int i = 0; i < itemIds.length; i++) {
            newItems[i] = idRewriteFunction.applyAsInt(itemIds[i]);
        }
        return new PotDecorations(newItems);
    }

    @Override
    public PotDecorations copy() {
        return new PotDecorations(copy(itemIds));
    }
}
