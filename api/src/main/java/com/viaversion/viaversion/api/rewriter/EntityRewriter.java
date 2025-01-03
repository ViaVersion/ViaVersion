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
package com.viaversion.viaversion.api.rewriter;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.protocol.Protocol;
import java.util.List;

public interface EntityRewriter<T extends Protocol<?, ?, ?, ?>> extends Rewriter<T> {

    /**
     * Returns the entity type from the given (mapped) type id.
     *
     * @param type mapped type id
     * @return entity type
     */
    EntityType typeFromId(int type);

    /**
     * Returns the entity type from the given id.
     * From 1.14 and onwards, this is the same exact value as {@link #typeFromId(int)}.
     *
     * @param type entity type id
     * @param data entity data
     * @return EntityType from id
     */
    default EntityType objectTypeFromId(int type, int data) {
        return typeFromId(type);
    }

    /**
     * Returns the mapped entity (or the same if it has not changed).
     *
     * @param id unmapped entity id
     * @return mapped entity id
     */
    int newEntityId(int id);

    /**
     * Returns the mapped entity (or the same if it has not changed).
     *
     * @param identifier unmapped entity identifier
     * @return mapped entity identifier
     */
    String mappedEntityIdentifier(String identifier);

    /**
     * Handles and transforms entity data of an entity.
     *
     * @param entityId     entity id
     * @param dataList full, mutable list of entity data
     * @param connection   user connection
     */
    void handleEntityData(int entityId, List<EntityData> dataList, UserConnection connection);

    /**
     * Returns the entity tracker for the current protocol.
     *
     * @param connection user connection
     * @param <E>        entity tracker type
     * @return entity tracker
     */
    default <E extends EntityTracker> E tracker(UserConnection connection) {
        return connection.getEntityTracker(protocol().getClass());
    }
}
