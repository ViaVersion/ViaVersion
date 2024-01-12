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
package com.viaversion.viaversion.api.data.entity;

import com.viaversion.viaversion.api.minecraft.entities.EntityType;

public interface TrackedEntity {

    /**
     * Returns the type of the stored entity.
     *
     * @return type of the entity
     */
    EntityType entityType();

    /**
     * Object to hold arbitrary additional data.
     *
     * @return entity data
     */
    StoredEntityData data();

    /**
     * Returns whether the stored entity currently has any additional data.
     *
     * @return whether the stored entity currently has additional data
     */
    boolean hasData();

    /**
     * Returns whether metadata has already been sent at least once for this entity.
     *
     * @return whether metadata has already been sent at least once for this entity
     */
    boolean hasSentMetadata();

    void sentMetadata(boolean sentMetadata);
}