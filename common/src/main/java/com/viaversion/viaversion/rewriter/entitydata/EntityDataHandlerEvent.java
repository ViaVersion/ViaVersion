/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.rewriter.entitydata;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.TrackedEntity;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface EntityDataHandlerEvent {

    /**
     * Returns the user connection the entity data is sent to.
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
     * Returns the entity type of the entity data belongs to if tracked.
     *
     * @return entity type of the entity if tracked, else null
     */
    default @Nullable EntityType entityType() {
        return trackedEntity() != null ? trackedEntity().entityType() : null;
    }

    /**
     * Returns the entity data index.
     *
     * @return return entity data index
     */
    default int index() {
        return data().id();
    }

    /**
     * Sets the entity data index.
     *
     * @param index new entity data index
     */
    default void setIndex(int index) {
        data().setId(index);
    }

    /**
     * Returns the entity data.
     *
     * @return return entity data
     */
    EntityData data();

    /**
     * Prevents other handlers from being called with this entity data entry and removes it from the list.
     */
    void cancel();

    /**
     * Returns whether this entity data entry should be removed.
     *
     * @return true if cancelled/removed
     */
    boolean cancelled();

    /**
     * Returns entity data by the given index if present.
     *
     * @param index entity data index
     * @return entity data if present, else null
     */
    @Nullable EntityData dataAtIndex(int index);

    /**
     * Returns an immutable entity data view.
     * This list is not sorted or indexed by the actual entity data indexes.
     *
     * @return immutable entity data list
     * @see #dataAtIndex(int)
     * @see #cancel()
     * @see #createExtraData(EntityData)
     */
    List<EntityData> dataList();

    /**
     * Returns additionally created entity data.
     * May be null; use {@link #createExtraData(EntityData)} for adding entity data.
     *
     * @return additionally created entity data if present
     */
    @Nullable List<EntityData> extraData();

    /**
     * Returns whether additionally created entity data will be added.
     *
     * @return true if additionally created entity data is present
     */
    default boolean hasExtraData() {
        return extraData() != null;
    }

    /**
     * Adds the given entity data to the entity data list.
     * This entity data will not be passed through handlers of the current loop.
     *
     * @param entityData entity data
     */
    void createExtraData(EntityData entityData);
}
