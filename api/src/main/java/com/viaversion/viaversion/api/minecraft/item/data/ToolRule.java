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
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public record ToolRule(HolderSet blocks, @Nullable Float speed,
                       @Nullable Boolean correctForDrops) implements Rewritable {

    public static final Type<ToolRule> TYPE = new Type<>(ToolRule.class) {
        @Override
        public ToolRule read(final ByteBuf buffer) {
            final HolderSet blocks = Types.HOLDER_SET.read(buffer);
            final Float speed = Types.OPTIONAL_FLOAT.read(buffer);
            final Boolean correctForDrops = Types.OPTIONAL_BOOLEAN.read(buffer);
            return new ToolRule(blocks, speed, correctForDrops);
        }

        @Override
        public void write(final ByteBuf buffer, final ToolRule value) {
            Types.HOLDER_SET.write(buffer, value.blocks);
            Types.OPTIONAL_FLOAT.write(buffer, value.speed);
            Types.OPTIONAL_BOOLEAN.write(buffer, value.correctForDrops);
        }
    };
    public static final Type<ToolRule[]> ARRAY_TYPE = new ArrayType<>(TYPE);

    @Override
    public ToolRule rewrite(final UserConnection connection, final Protocol<?, ?, ?, ?> protocol, final boolean clientbound) {
        return blocks.hasIds() ? new ToolRule(blocks.rewrite(Rewritable.blockRewriteFunction(protocol, clientbound)), speed, correctForDrops) : this;
    }
}
