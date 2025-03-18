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

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.predicate.DataComponentMatchers;
import com.viaversion.viaversion.api.minecraft.data.predicate.DataComponentMatchers.DataComponentMatchersType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.util.Copyable;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import org.checkerframework.checker.nullness.qual.Nullable;

public record BlockPredicate(@Nullable HolderSet holderSet, StatePropertyMatcher @Nullable [] propertyMatchers,
                             @Nullable CompoundTag tag, DataComponentMatchers dataMatchers) implements Copyable {

    public BlockPredicate(@Nullable final HolderSet holderSet, final StatePropertyMatcher @Nullable [] propertyMatchers, @Nullable final CompoundTag tag) {
        this(holderSet, propertyMatchers, tag, null);
    }

    public static final Type<BlockPredicate> TYPE1_20_5 = new Type<>(BlockPredicate.class) {
        @Override
        public BlockPredicate read(final ByteBuf buffer) {
            final HolderSet holders = Types.OPTIONAL_HOLDER_SET.read(buffer);
            final StatePropertyMatcher[] propertyMatchers = buffer.readBoolean() ? StatePropertyMatcher.ARRAY_TYPE.read(buffer) : null;
            final CompoundTag tag = Types.OPTIONAL_COMPOUND_TAG.read(buffer);
            return new BlockPredicate(holders, propertyMatchers, tag, null);
        }

        @Override
        public void write(final ByteBuf buffer, final BlockPredicate value) {
            Types.OPTIONAL_HOLDER_SET.write(buffer, value.holderSet);

            buffer.writeBoolean(value.propertyMatchers != null);
            if (value.propertyMatchers != null) {
                StatePropertyMatcher.ARRAY_TYPE.write(buffer, value.propertyMatchers);
            }

            Types.OPTIONAL_COMPOUND_TAG.write(buffer, value.tag);
        }
    };
    public static final Type<BlockPredicate[]> ARRAY_TYPE1_20_5 = new ArrayType<>(TYPE1_20_5);

    public static final class BlockPredicateType1_21_5 extends Type<BlockPredicate> {
        private final Type<DataComponentMatchers> matchersType;

        public BlockPredicateType1_21_5(final Type<StructuredData<?>[]> dataArrayType) {
            super(BlockPredicate.class);
            this.matchersType = new DataComponentMatchersType(dataArrayType);
        }

        @Override
        public BlockPredicate read(final ByteBuf buffer) {
            final HolderSet holders = Types.OPTIONAL_HOLDER_SET.read(buffer);
            final StatePropertyMatcher[] propertyMatchers = buffer.readBoolean() ? StatePropertyMatcher.ARRAY_TYPE.read(buffer) : null;
            final CompoundTag tag = Types.OPTIONAL_COMPOUND_TAG.read(buffer);
            final DataComponentMatchers matchers = matchersType.read(buffer);
            return new BlockPredicate(holders, propertyMatchers, tag, matchers);
        }

        @Override
        public void write(final ByteBuf buffer, final BlockPredicate value) {
            Types.OPTIONAL_HOLDER_SET.write(buffer, value.holderSet);

            buffer.writeBoolean(value.propertyMatchers != null);
            if (value.propertyMatchers != null) {
                StatePropertyMatcher.ARRAY_TYPE.write(buffer, value.propertyMatchers);
            }

            Types.OPTIONAL_COMPOUND_TAG.write(buffer, value.tag);
            matchersType.write(buffer, value.dataMatchers);
        }
    }

    public BlockPredicate rewrite(final Int2IntFunction blockIdRewriter) {
        if (holderSet == null || holderSet.hasTagKey()) {
            return this;
        }

        final HolderSet updatedHolders = holderSet.rewrite(blockIdRewriter);
        return new BlockPredicate(updatedHolders, propertyMatchers, tag);
    }

    @Override
    public BlockPredicate copy() {
        return new BlockPredicate(holderSet, copy(propertyMatchers), tag == null ? null : tag.copy());
    }
}
