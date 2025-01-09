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
package com.viaversion.viaversion.api.minecraft.entitydata;

import com.google.common.base.Preconditions;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class EntityData {
    private int id;
    private EntityDataType dataType;
    private Object value;

    /**
     * Creates a new entity data instance.
     *
     * @param id       data index
     * @param dataType data type
     * @param value    value if present
     * @throws IllegalArgumentException if the value and dataType are incompatible
     */
    public EntityData(int id, EntityDataType dataType, @Nullable Object value) {
        this.id = id;
        this.dataType = dataType;
        this.value = checkValue(dataType, value);
    }

    public int id() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public EntityDataType dataType() {
        return dataType;
    }

    /**
     * Sets the entity data type if compatible with the current value.
     *
     * @param dataType entity data type
     * @throws IllegalArgumentException if the entity data type and current value are incompatible
     * @see #setTypeAndValue(EntityDataType, Object)
     */
    public void setDataType(EntityDataType dataType) {
        checkValue(dataType, this.value);
        this.dataType = dataType;
    }

    public @Nullable <T> T value() {
        return (T) value;
    }

    public @Nullable Object getValue() {
        return value;
    }

    /**
     * Sets the entity data value if compatible with the current data type.
     *
     * @param value value
     * @throws IllegalArgumentException if the value and current dataType are incompatible
     * @see #setTypeAndValue(EntityDataType, Object)
     */
    public void setValue(@Nullable Object value) {
        this.value = checkValue(this.dataType, value);
    }

    /**
     * Sets entity data type and value.
     *
     * @param dataType entity data type
     * @param value    value
     * @throws IllegalArgumentException if the value and dataType are incompatible
     */
    public void setTypeAndValue(EntityDataType dataType, @Nullable Object value) {
        this.value = checkValue(dataType, value);
        this.dataType = dataType;
    }

    private Object checkValue(EntityDataType dataType, @Nullable Object value) {
        Preconditions.checkNotNull(dataType);
        if (value != null && !dataType.type().getOutputClass().isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException("Entity data value and dataType are incompatible. Type=" + dataType
                + ", value=" + value + " (" + value.getClass().getSimpleName() + ")");
        }
        return value;
    }

    @Deprecated
    public void setDataTypeUnsafe(EntityDataType type) {
        this.dataType = type;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityData entityData = (EntityData) o;
        if (id != entityData.id) return false;
        if (dataType != entityData.dataType) return false;
        return Objects.equals(value, entityData.value);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + dataType.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EntityData{" +
            "id=" + id +
            ", dataType=" + dataType +
            ", value=" + value +
            '}';
    }
}
