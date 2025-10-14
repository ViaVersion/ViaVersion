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

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.TransformingType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Copyable;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;

public record PotDecorations(int[] itemIds) implements Copyable, Rewritable {

    public static final Type<PotDecorations> TYPE = new TransformingType<>(Types.VAR_INT_ARRAY_PRIMITIVE, PotDecorations.class, PotDecorations::new, PotDecorations::itemIds) {
        @Override
        public void write(final Ops ops, final PotDecorations value) {
            ops.writeList(list -> {
                for (final int itemId : value.itemIds) {
                    list.write(Types.RESOURCE_LOCATION, ops.context().registryAccess().item(itemId));
                }
            });
        }
    };

    public PotDecorations(final int backItem, final int leftItem, final int rightItem, final int frontItem) {
        this(new int[]{backItem, leftItem, rightItem, frontItem});
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

    @Override
    public PotDecorations rewrite(final UserConnection connection, final Protocol<?, ?, ?, ?> protocol, final boolean clientbound) {
        final int[] newItems = new int[itemIds.length];
        for (int i = 0; i < itemIds.length; i++) {
            newItems[i] = Rewritable.rewriteItem(protocol, clientbound, itemIds[i]);
        }
        return new PotDecorations(newItems);
    }

    @Override
    public PotDecorations copy() {
        return new PotDecorations(Copyable.copy(itemIds));
    }
}
