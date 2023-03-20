/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2023 ViaVersion and contributors
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

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface EntityTracker {

    /**
     * User connection the tracker belongs to.
     *
     * @return user connection
     */
    UserConnection user();

    /**
     * Tracks an entity.
     *
     * @param id   entity id
     * @param type entity type
     */
    void addEntity(int id, EntityType type);

    /**
     * Returns whether the entity is currently tracked.
     *
     * @param id entity id
     * @return whether the entity is tracked
     */
    boolean hasEntity(int id);

    /**
     * Returns the tracked entity for the given entity id if present.
     *
     * @param entityId entity id
     * @return tracked entity if tracked
     */
    @Nullable TrackedEntity entity(int entityId);

    /**
     * Entity type of the entity if tracked.
     * This returning null does not necessarily mean no entity by the id exists.
     *
     * @param id entity id
     * @return entity type of the entity if tracked
     */
    @Nullable EntityType entityType(int id);

    /**
     * Untracks an entity.
     *
     * @param id entity id
     */
    void removeEntity(int id);

    /**
     * Clears stored entity types and data.
     */
    void clearEntities();

    /**
     * Returns the stored entity data if an entity with the id is tracked, else null.
     * If no data has been initialized yet, it will be done and returned by this method.
     *
     * @param id entity id
     * @return stored entity data if an entity with the id is tracked, else null
     * @throws IllegalArgumentException if entitiy data storage has not been enabled via the implementation
     */
    @Nullable StoredEntityData entityData(int id);

    /**
     * Returns stored entity data if it has previously been initialized by {@link #entityData(int)}, else null.
     *
     * @param id entity id
     * @return stored entity data if it has previously been initialized by {@link #entityData(int)}
     * @throws IllegalArgumentException if entitiy data storage has not been enabled via the implementation
     */
    @Nullable StoredEntityData entityDataIfPresent(int id);

    /**
     * Returns the client entity id or -1 if unset.
     *
     * @return client entity id or -1 if unset
     */
    int clientEntityId();

    /**
     * Sets the client entity id.
     *
     * @param clientEntityId client entity id
     */
    void setClientEntityId(int clientEntityId);

    /**
     * Returns the current world section height (block height / 16).
     * This is always 16 for sub 1.17 worlds.
     *
     * @return current world section height
     */
    int currentWorldSectionHeight();

    /**
     * Sets the current world section height.
     *
     * @param currentWorldSectionHeight world section height
     */
    void setCurrentWorldSectionHeight(int currentWorldSectionHeight);

    /**
     * Returns the minimum y of the current player world.
     *
     * @return minimum y of the current world
     */
    int currentMinY();

    /**
     * Sets the minimum y of the current player world.
     *
     * @param currentMinY minimum y of the current world
     */
    void setCurrentMinY(int currentMinY);

    /**
     * Returns the name of the world the player is currently in.
     *
     * @return world name of the current world
     */
    @Nullable String currentWorld();

    /**
     * Sets the name of the world the player is currently in.
     *
     * @param currentWorld name of the current world
     */
    void setCurrentWorld(String currentWorld);

    int biomesSent();

    void setBiomesSent(int biomesSent);

    EntityType playerType();

    @Nullable DimensionData dimensionData(String dimension);

    void setDimensions(Map<String, DimensionData> dimensions);

    /**
     * Adds the client player entity to the tracker.
     * If the client entity has not been set yet, this will return false.
     *
     * @return whether the client has been tracked
     */
    boolean trackClientEntity();
}
