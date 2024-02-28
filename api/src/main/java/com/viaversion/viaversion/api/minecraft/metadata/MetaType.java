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
package com.viaversion.viaversion.api.minecraft.metadata;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.type.Type;

public interface MetaType {

    /**
     * Get the write/read type
     *
     * @return Type instance
     */
    Type type();

    /**
     * Get type id from the specific MetaDataType
     *
     * @return Type id as an integer
     */
    int typeId();

    static MetaType create(final int typeId, final Type<?> type) {
        return new MetaTypeImpl(typeId, type);
    }

    final class MetaTypeImpl implements MetaType {
        private final int typeId;
        private final Type<?> type;

        MetaTypeImpl(final int typeId, final Type<?> type) {
            Preconditions.checkNotNull(type);
            this.typeId = typeId;
            this.type = type;
        }

        @Override
        public int typeId() {
            return typeId;
        }

        @Override
        public Type<?> type() {
            return type;
        }

        @Override
        public String toString() {
            return "MetaType{" +
                    "typeId=" + typeId +
                    ", type=" + type +
                    '}';
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final MetaTypeImpl metaType = (MetaTypeImpl) o;
            if (typeId != metaType.typeId) return false;
            return type.equals(metaType.type);
        }

        @Override
        public int hashCode() {
            int result = typeId;
            result = 31 * result + type.hashCode();
            return result;
        }
    }
}
