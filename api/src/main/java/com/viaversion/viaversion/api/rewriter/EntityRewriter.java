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
package com.viaversion.viaversion.api.rewriter;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.protocol.Protocol;
import java.util.List;

public interface EntityRewriter<T extends Protocol> extends Rewriter<T> {

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
     * @return EntityType from id
     */
    default EntityType objectTypeFromId(int type) {
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
     * Handles and transforms metadata of an entity.
     *
     * @param entityId     entity id
     * @param metadataList full, mutable list of metadata
     * @param connection   user connection
     */
    void handleMetadata(int entityId, List<Metadata> metadataList, UserConnection connection);

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
