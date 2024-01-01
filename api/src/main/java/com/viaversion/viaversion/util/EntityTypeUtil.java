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
package com.viaversion.viaversion.util;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.protocol.Protocol;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class EntityTypeUtil {

    private static final EntityType[] EMPTY_ARRAY = new EntityType[0];

    /**
     * Returns an ordered array with each index representing the actual entity id.
     *
     * @param values entity types
     * @return ordered array with each index representing the actual entity id
     */
    public static EntityType[] toOrderedArray(final EntityType[] values) {
        final List<EntityType> types = new ArrayList<>();
        for (final EntityType type : values) {
            if (type.getId() != -1) {
                types.add(type);
            }
        }

        types.sort(Comparator.comparingInt(EntityType::getId));
        return types.toArray(EMPTY_ARRAY);
    }

    /**
     * Sets entity type ids based on the protocol's mapping data and fills the given typesToFill array with the index corresponding to the id.
     *
     * @param values      full enum values
     * @param typesToFill yet unfilled array to be filled with types ordered by id
     * @param protocol    protocol to get entity types from
     * @param idSetter    function to set the internal entity id
     * @param <T>         entity type
     */
    public static <T extends EntityType> void initialize(final T[] values, final EntityType[] typesToFill, final Protocol<?, ?, ?, ?> protocol, final EntityIdSetter<T> idSetter) {
        for (final T type : values) {
            if (type.isAbstractType()) {
                continue;
            }

            final int id = protocol.getMappingData().getEntityMappings().mappedId(type.identifier());
            Preconditions.checkArgument(id != -1, "Entity type %s has no id", type.identifier());
            idSetter.setId(type, id);
            typesToFill[id] = type;
        }
    }

    public static EntityType[] createSizedArray(final EntityType[] values) {
        int count = 0;
        for (final EntityType type : values) {
            if (!type.isAbstractType()) {
                count++;
            }
        }
        return new EntityType[count];
    }

    /**
     * Returns the entity type from id, or the given fallback if out of bounds.
     *
     * @param values   sorted entity type array
     * @param typeId   entity type id
     * @param fallback fallback/base entity type
     * @return entity type from id
     */
    public static EntityType getTypeFromId(final EntityType[] values, final int typeId, final EntityType fallback) {
        final EntityType type;
        if (typeId < 0 || typeId >= values.length || (type = values[typeId]) == null) {
            Via.getPlatform().getLogger().severe("Could not find " + fallback.getClass().getSimpleName() + " type id " + typeId);
            return fallback;
        }
        return type;
    }

    @FunctionalInterface
    public interface EntityIdSetter<T extends EntityType> {

        void setId(T entityType, int id);
    }
}
