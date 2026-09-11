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
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.Copyable;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.viaversion.viaversion.util.Rewritable.rewriteItem;

public record PotDecorations26_3(@Nullable Item back, @Nullable Item left, @Nullable Item right,
                                 @Nullable Item front) implements Copyable, Rewritable {

    public static final class PotDecorationsType extends Type<PotDecorations26_3> {

        private final Type<Item> itemTemplateType;

        public PotDecorationsType(final Type<Item> itemTemplateType) {
            super(PotDecorations26_3.class);
            this.itemTemplateType = itemTemplateType;
        }

        @Override
        public void write(final ByteBuf buffer, final PotDecorations26_3 value) {
            itemTemplateType.write(buffer, value.back());
            itemTemplateType.write(buffer, value.left());
            itemTemplateType.write(buffer, value.right());
            itemTemplateType.write(buffer, value.front());
        }

        @Override
        public PotDecorations26_3 read(final ByteBuf buffer) {
            final Item back = itemTemplateType.read(buffer);
            final Item left = itemTemplateType.read(buffer);
            final Item right = itemTemplateType.read(buffer);
            final Item front = itemTemplateType.read(buffer);
            return new PotDecorations26_3(back, left, right, front);
        }

        @Override
        public void write(final Ops ops, final PotDecorations26_3 value) {
            ops.writeMap(map -> map
                .writeOptional("back", itemTemplateType, value.back())
                .writeOptional("left", itemTemplateType, value.left())
                .writeOptional("right", itemTemplateType, value.right())
                .writeOptional("front", itemTemplateType, value.front())
            );
        }
    }

    @Override
    public PotDecorations26_3 rewrite(final UserConnection connection, final Protocol<?, ?, ?, ?> protocol, final boolean clientbound) {
        return new PotDecorations26_3(
            rewriteItem(connection, protocol, clientbound, back),
            rewriteItem(connection, protocol, clientbound, left),
            rewriteItem(connection, protocol, clientbound, right),
            rewriteItem(connection, protocol, clientbound, front)
        );
    }

    @Override
    public PotDecorations26_3 copy() {
        return new PotDecorations26_3(
            back == null ? null : back.copy(),
            left == null ? null : left.copy(),
            right == null ? null : right.copy(),
            front == null ? null : front.copy()
        );
    }
}
