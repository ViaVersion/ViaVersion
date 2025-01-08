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
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;

public record ToolProperties(ToolRule[] rules, float defaultMiningSpeed, int damagePerBlock, boolean canDestroyBlocksInCreative) {

    public ToolProperties(final ToolRule[] rules, final float defaultMiningSpeed, final int damagePerBlock) {
        this(rules, defaultMiningSpeed, damagePerBlock, false);
    }

    public static final Type<ToolProperties> TYPE1_20_5 = new Type<>(ToolProperties.class) {
        @Override
        public ToolProperties read(final ByteBuf buffer) {
            final ToolRule[] rules = ToolRule.ARRAY_TYPE.read(buffer);
            final float defaultMiningSpeed = buffer.readFloat();
            final int damagePerBlock = Types.VAR_INT.readPrimitive(buffer);
            return new ToolProperties(rules, defaultMiningSpeed, damagePerBlock, true);
        }

        @Override
        public void write(final ByteBuf buffer, final ToolProperties value) {
            ToolRule.ARRAY_TYPE.write(buffer, value.rules());
            buffer.writeFloat(value.defaultMiningSpeed());
            Types.VAR_INT.writePrimitive(buffer, value.damagePerBlock());
        }
    };
    public static final Type<ToolProperties> TYPE1_21_5 = new Type<>(ToolProperties.class) {
        @Override
        public ToolProperties read(final ByteBuf buffer) {
            final ToolRule[] rules = ToolRule.ARRAY_TYPE.read(buffer);
            final float defaultMiningSpeed = buffer.readFloat();
            final int damagePerBlock = Types.VAR_INT.readPrimitive(buffer);
            final boolean canDestroyBlocksInCreative = buffer.readBoolean();
            return new ToolProperties(rules, defaultMiningSpeed, damagePerBlock, canDestroyBlocksInCreative);
        }

        @Override
        public void write(final ByteBuf buffer, final ToolProperties value) {
            ToolRule.ARRAY_TYPE.write(buffer, value.rules());
            buffer.writeFloat(value.defaultMiningSpeed());
            Types.VAR_INT.writePrimitive(buffer, value.damagePerBlock());
            buffer.writeBoolean(value.canDestroyBlocksInCreative());
        }
    };

    public ToolProperties rewrite(final Int2IntFunction blockIdRewriter) {
        final ToolRule[] rules = new ToolRule[this.rules.length];
        for (int i = 0; i < rules.length; i++) {
            rules[i] = this.rules[i].rewrite(blockIdRewriter);
        }
        return new ToolProperties(rules, defaultMiningSpeed, damagePerBlock, canDestroyBlocksInCreative);
    }
}
