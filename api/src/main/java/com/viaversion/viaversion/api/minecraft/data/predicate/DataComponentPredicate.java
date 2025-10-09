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
package com.viaversion.viaversion.api.minecraft.data.predicate;

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import io.netty.buffer.ByteBuf;

public record DataComponentPredicate(PredicateType type, Tag predicate) {

    public DataComponentPredicate(final int predicateType, final Tag predicate) {
        this(PredicateType.ofPredicateType(predicateType), predicate);
    }

    public static final Type<DataComponentPredicate> TYPE1_21_5 = new Type<>(DataComponentPredicate.class) {
        @Override
        public DataComponentPredicate read(final ByteBuf buffer) {
            final int id = Types.VAR_INT.readPrimitive(buffer);
            final Tag predicate = Types.TAG.read(buffer);
            return new DataComponentPredicate(id, predicate);
        }

        @Override
        public void write(final ByteBuf buffer, final DataComponentPredicate value) {
            Types.VAR_INT.writePrimitive(buffer, value.type().id());
            Types.TAG.write(buffer, value.predicate());
        }
    };
    public static final Type<DataComponentPredicate[]> ARRAY_TYPE1_21_5 = new ArrayType<>(TYPE1_21_5, 64);

    public static final Type<DataComponentPredicate> TYPE1_21_11 = new Type<>(DataComponentPredicate.class) {
        @Override
        public DataComponentPredicate read(final ByteBuf buffer) {
            final boolean isPredicateType = Types.BOOLEAN.read(buffer);
            final PredicateType type = new PredicateType(Types.VAR_INT.readPrimitive(buffer), isPredicateType);
            final Tag predicate = Types.TAG.read(buffer);
            return new DataComponentPredicate(type, predicate);
        }

        @Override
        public void write(final ByteBuf buffer, final DataComponentPredicate value) {
            Types.BOOLEAN.write(buffer, value.type().isPredicateType());
            Types.VAR_INT.writePrimitive(buffer, value.type().id());
            Types.TAG.write(buffer, value.predicate());
        }
    };
    public static final Type<DataComponentPredicate[]> ARRAY_TYPE1_21_11 = new ArrayType<>(TYPE1_21_11, 64);

    public record PredicateType(int id, boolean isPredicateType) {

        public static PredicateType ofPredicateType(final int id) {
            return new PredicateType(id, true);
        }

        public static PredicateType ofDataComponentType(final int id) {
            return new PredicateType(id, false);
        }
    }
}
