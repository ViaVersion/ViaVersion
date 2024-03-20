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
package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.ArrayType;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ToolRule {

    public static final Type<ToolRule> TYPE = new Type<ToolRule>(ToolRule.class) {
        @Override
        public ToolRule read(final ByteBuf buffer) throws Exception {
            final HolderSet blocks = Type.HOLDER_SET.read(buffer);
            final Float speed = Type.OPTIONAL_FLOAT.read(buffer);
            final Boolean correctForDrops = Type.OPTIONAL_BOOLEAN.read(buffer);
            return new ToolRule(blocks, speed, correctForDrops);
        }

        @Override
        public void write(final ByteBuf buffer, final ToolRule value) throws Exception {
            Type.HOLDER_SET.write(buffer, value.blocks);
            Type.OPTIONAL_FLOAT.write(buffer, value.speed);
            Type.OPTIONAL_BOOLEAN.write(buffer, value.correctForDrops);
        }
    };
    public static final Type<ToolRule[]> ARRAY_TYPE = new ArrayType<>(TYPE);

    private final HolderSet blocks;
    private final Float speed;
    private final Boolean correctForDrops;

    public ToolRule(final HolderSet blocks, @Nullable final Float speed, @Nullable final Boolean correctForDrops) {
        this.blocks = blocks;
        this.speed = speed;
        this.correctForDrops = correctForDrops;
    }

    public HolderSet blocks() {
        return blocks;
    }

    public @Nullable Float speed() {
        return speed;
    }

    public @Nullable Boolean correctForDrops() {
        return correctForDrops;
    }
}
