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
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.util.Copyable;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;

public record AdventureModePredicate(BlockPredicate[] predicates,
                                     boolean showInTooltip) implements Copyable, Rewritable {

    public AdventureModePredicate(final BlockPredicate[] predicates) {
        this(predicates, true);
    }

    public static final Type<AdventureModePredicate> TYPE1_20_5 = new Type<>(AdventureModePredicate.class) {
        @Override
        public AdventureModePredicate read(final ByteBuf buffer) {
            final BlockPredicate[] predicates = BlockPredicate.ARRAY_TYPE1_20_5.read(buffer);
            final boolean showInTooltip = buffer.readBoolean();
            return new AdventureModePredicate(predicates, showInTooltip);
        }

        @Override
        public void write(final ByteBuf buffer, final AdventureModePredicate value) {
            BlockPredicate.ARRAY_TYPE1_20_5.write(buffer, value.predicates);
            buffer.writeBoolean(value.showInTooltip);
        }
    };

    public static final class AdventureModePredicateType1_21_5 extends Type<AdventureModePredicate> {
        private final Type<BlockPredicate[]> blockPredicateType;

        public AdventureModePredicateType1_21_5(final Type<StructuredData<?>[]> dataArrayType) {
            super(AdventureModePredicate.class);
            this.blockPredicateType = new ArrayType<>(new BlockPredicate.BlockPredicateType1_21_5(dataArrayType));
        }

        @Override
        public AdventureModePredicate read(final ByteBuf buffer) {
            final BlockPredicate[] predicates = blockPredicateType.read(buffer);
            return new AdventureModePredicate(predicates);
        }

        @Override
        public void write(final ByteBuf buffer, final AdventureModePredicate value) {
            blockPredicateType.write(buffer, value.predicates);
        }
    }

    @Override
    public AdventureModePredicate rewrite(final UserConnection connection, final Protocol<?, ?, ?, ?> protocol, final boolean clientbound) {
        final BlockPredicate[] predicates = new BlockPredicate[this.predicates.length];
        for (int i = 0; i < predicates.length; i++) {
            predicates[i] = this.predicates[i].rewrite(connection, protocol, clientbound);
        }
        return new AdventureModePredicate(predicates, showInTooltip);
    }

    @Override
    public AdventureModePredicate copy() {
        return new AdventureModePredicate(Copyable.copy(predicates), showInTooltip);
    }
}
