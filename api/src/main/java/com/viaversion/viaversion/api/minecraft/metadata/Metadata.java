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
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class Metadata {
    private int id;
    private MetaType metaType;
    private Object value;

    /**
     * Creates a new metadata instance.
     *
     * @param id       metadata index
     * @param metaType metadata type
     * @param value    value if present
     * @throws IllegalArgumentException if the value and metaType are incompatible
     */
    public Metadata(int id, MetaType metaType, @Nullable Object value) {
        this.id = id;
        this.metaType = metaType;
        this.value = checkValue(metaType, value);
    }

    public int id() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public MetaType metaType() {
        return metaType;
    }

    /**
     * Sets the metadata type if compatible with the current value.
     *
     * @param metaType metadata type
     * @throws IllegalArgumentException if the metadata type and current value are incompatible
     * @see #setTypeAndValue(MetaType, Object)
     */
    public void setMetaType(MetaType metaType) {
        checkValue(metaType, this.value);
        this.metaType = metaType;
    }

    public @Nullable <T> T value() {
        return (T) value;
    }

    public @Nullable Object getValue() {
        return value;
    }

    /**
     * Sets the metadata value if compatible with the current meta type.
     *
     * @param value value
     * @throws IllegalArgumentException if the value and current metaType are incompatible
     * @see #setTypeAndValue(MetaType, Object)
     */
    public void setValue(@Nullable Object value) {
        this.value = checkValue(this.metaType, value);
    }

    /**
     * Sets metadata type and value.
     *
     * @param metaType metadata type
     * @param value    value
     * @throws IllegalArgumentException if the value and metaType are incompatible
     */
    public void setTypeAndValue(MetaType metaType, @Nullable Object value) {
        this.value = checkValue(metaType, value);
        this.metaType = metaType;
    }

    private Object checkValue(MetaType metaType, @Nullable Object value) {
        Preconditions.checkNotNull(metaType);
        if (value != null && !metaType.type().getOutputClass().isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException("Metadata value and metaType are incompatible. Type=" + metaType
                    + ", value=" + value + " (" + value.getClass().getSimpleName() + ")");
        }
        return value;
    }

    @Deprecated
    public void setMetaTypeUnsafe(MetaType type) {
        this.metaType = type;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Metadata metadata = (Metadata) o;
        if (id != metadata.id) return false;
        if (metaType != metadata.metaType) return false;
        return Objects.equals(value, metadata.value);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + metaType.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Metadata{" +
                "id=" + id +
                ", metaType=" + metaType +
                ", value=" + value +
                '}';
    }
}
