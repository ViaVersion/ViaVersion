/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.Copyable;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Sherds are full item templates in their own slot now, where they used to be a list of item ids.
 */
public record PotDecorations26_3(@Nullable Item back, @Nullable Item left, @Nullable Item right, @Nullable Item front) implements Copyable, Rewritable {

    public static Type<PotDecorations26_3> type(final Type<Item> optionalItemTemplate) {
        return new Type<>(PotDecorations26_3.class) {
            @Override
            public PotDecorations26_3 read(final ByteBuf buffer) {
                return new PotDecorations26_3(optionalItemTemplate.read(buffer), optionalItemTemplate.read(buffer),
                    optionalItemTemplate.read(buffer), optionalItemTemplate.read(buffer));
            }

            @Override
            public void write(final ByteBuf buffer, final PotDecorations26_3 value) {
                optionalItemTemplate.write(buffer, value.back());
                optionalItemTemplate.write(buffer, value.left());
                optionalItemTemplate.write(buffer, value.right());
                optionalItemTemplate.write(buffer, value.front());
            }
        };
    }

    @Override
    public PotDecorations26_3 rewrite(final UserConnection connection, final Protocol<?, ?, ?, ?> protocol, final boolean clientbound) {
        return new PotDecorations26_3(rewriteSherd(protocol, clientbound, back), rewriteSherd(protocol, clientbound, left),
            rewriteSherd(protocol, clientbound, right), rewriteSherd(protocol, clientbound, front));
    }

    private static @Nullable Item rewriteSherd(final Protocol<?, ?, ?, ?> protocol, final boolean clientbound, @Nullable final Item sherd) {
        if (sherd == null) {
            return null;
        }
        final Item copy = sherd.copy();
        copy.setIdentifier(Rewritable.rewriteItem(protocol, clientbound, sherd.identifier()));
        return copy;
    }

    @Override
    public PotDecorations26_3 copy() {
        return new PotDecorations26_3(Copyable.copy(back), Copyable.copy(left), Copyable.copy(right), Copyable.copy(front));
    }
}
