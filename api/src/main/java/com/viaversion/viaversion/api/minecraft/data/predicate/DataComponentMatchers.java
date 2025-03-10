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

import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

public record DataComponentMatchers(StructuredData<?>[] exactPredicates, DataComponentPredicate[] predicates) {

    public static final class DataComponentMatchersType extends Type<DataComponentMatchers> {
        private final Type<StructuredData<?>[]> dataArrayType;

        public DataComponentMatchersType(final Type<StructuredData<?>[]> dataArrayType) {
            super(DataComponentMatchers.class);
            this.dataArrayType = dataArrayType;
        }

        @Override
        public DataComponentMatchers read(final ByteBuf buffer) {
            final StructuredData<?>[] exactPredicates = dataArrayType.read(buffer);
            final DataComponentPredicate[] partialPredicates = DataComponentPredicate.ARRAY_TYPE.read(buffer);
            return new DataComponentMatchers(exactPredicates, partialPredicates);
        }

        @Override
        public void write(final ByteBuf buffer, final DataComponentMatchers value) {
            dataArrayType.write(buffer, value.exactPredicates());
            DataComponentPredicate.ARRAY_TYPE.write(buffer, value.predicates());
        }
    }
}
