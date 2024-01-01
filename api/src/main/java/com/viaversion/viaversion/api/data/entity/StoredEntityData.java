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
import org.checkerframework.checker.nullness.qual.Nullable;

public interface StoredEntityData {

    /**
     * Returns the entity type of the stored entity.
     *
     * @return entity type
     */
    EntityType type();

    /**
     * Checks if the storage contains an object of the given type.
     *
     * @param objectClass object class to check
     * @return whether an object of the given type is in the storage
     */
    boolean has(Class<?> objectClass);

    /**
     * Returns an object from the storage if present.
     *
     * @param objectClass class of the object to get
     * @param <T>         object type
     * @return object if present
     */
    @Nullable <T> T get(Class<T> objectClass);

    /**
     * Removes and returns an object from the storage if present.
     *
     * @param objectClass class of the object to remove
     * @param <T>         object type
     * @return removed object if present
     */
    @Nullable <T> T remove(Class<T> objectClass);

    /**
     * Stores an object based on its class.
     *
     * @param object object to store
     */
    void put(Object object);
}