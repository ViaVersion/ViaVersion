/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.rewriter.meta;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.TrackedEntity;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface MetaHandlerEvent {

    /**
     * Returns the user connection the metadata is sent to.
     *
     * @return user connection
     */
    UserConnection user();

    /**
     * Returns the entity id of the entity.
     *
     * @return entity id
     */
    int entityId();

    /**
     * Returns the tracked entity if present.
     *
     * @return tracked entity if present, else null
     */
    @Nullable TrackedEntity trackedEntity();

    /**
     * Returns the entity type of the entity the metadata belongs to if tracked.
     *
     * @return entity type of the entity if tracked, else null
     */
    default @Nullable EntityType entityType() {
        return trackedEntity() != null ? trackedEntity().entityType() : null;
    }

    /**
     * Returns the metadata index.
     *
     * @return return meta index
     */
    default int index() {
        return meta().id();
    }

    /**
     * Sets the metadata index.
     *
     * @param index new metadata index
     */
    default void setIndex(int index) {
        meta().setId(index);
    }

    /**
     * Returns the metadata.
     *
     * @return return metadata
     */
    Metadata meta();

    /**
     * Prevents other handlers from being called with this metadata entry and removes it from the list.
     */
    void cancel();

    /**
     * Returns whether this metadata entry should be removed.
     *
     * @return true if cancelled/removed
     */
    boolean cancelled();

    /**
     * Returns metadata by the given index if present.
     *
     * @param index metadata index
     * @return metadata if present, else null
     */
    @Nullable Metadata metaAtIndex(int index);

    /**
     * Returns an immutable metadata view.
     * This list is not sorted or indexed by the actual metadata indexes.
     *
     * @return immutable metadata list
     * @see #metaAtIndex(int)
     * @see #cancel()
     * @see #createExtraMeta(Metadata)
     */
    List<Metadata> metadataList();

    /**
     * Returns additionally created metadata.
     * May be null; use {@link #createExtraMeta(Metadata)} for adding metadata.
     *
     * @return additionally created metadata if present
     */
    @Nullable List<Metadata> extraMeta();

    /**
     * Returns whether additionally created metadata will be added.
     *
     * @return true if additionally created metadata is present
     */
    default boolean hasExtraMeta() {
        return extraMeta() != null;
    }

    /**
     * Adds the given metadata to the metadata list.
     * This metadata will not be passed through handlers of the current loop.
     *
     * @param metadata metadata
     */
    void createExtraMeta(Metadata metadata);
}
