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
package com.viaversion.viaversion.api.minecraft.entities;

import org.checkerframework.checker.nullness.qual.Nullable;

public interface EntityType {

    /**
     * Returns the entity id.
     *
     * @return entity id
     * @throws IllegalStateException if ids have not been loaded yet
     */
    int getId();

    /**
     * Returns the parent entity type if present.
     *
     * @return parent entity type if present
     */
    @Nullable EntityType getParent();

    /**
     * Returns the entity type name, not necessarily matching the Vanilla type name.
     *
     * @return entity type name
     */
    String name();

    /**
     * Returns the entity's Vanilla identifier.
     *
     * @return entity identifier
     * @throws IllegalArgumentException if {@link #isAbstractType()} returns true
     */
    String identifier();

    /**
     * Returns whether the type does not represent an actual entity, e.g. animal or monster.
     *
     * @return whether the type does not represent an actual entity
     */
    boolean isAbstractType();

    default boolean is(EntityType type) {
        return this == type;
    }

    /**
     * Returns whether the current type is equal to the given type, or has it as a parent type.
     *
     * @param type entity type to check against
     * @return true if the current type is equal to the given type, or has it as a parent type
     */
    default boolean isOrHasParent(EntityType type) {
        EntityType parent = this;

        do {
            if (parent == type) {
                return true;
            }

            parent = parent.getParent();
        } while (parent != null);

        return false;
    }
}
