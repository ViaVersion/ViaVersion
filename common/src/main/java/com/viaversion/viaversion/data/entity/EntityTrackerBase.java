/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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

package com.viaversion.viaversion.data.entity;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.ClientEntityIdChangeListener;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.data.entity.StoredEntityData;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityTrackerBase implements EntityTracker, ClientEntityIdChangeListener {
    private final Map<Integer, EntityType> entityTypes = new ConcurrentHashMap<>();
    private final Map<Integer, StoredEntityData> entityData;
    private final UserConnection connection;
    private final EntityType playerType;
    private int clientEntityId = -1;
    private int currentWorldSectionHeight = 16;
    private int currentMinY;

    public EntityTrackerBase(UserConnection connection, @Nullable EntityType playerType) {
        this(connection, playerType, false);
    }

    public EntityTrackerBase(UserConnection connection, @Nullable EntityType playerType, boolean storesEntityData) {
        this.connection = connection;
        this.playerType = playerType;
        this.entityData = storesEntityData ? new ConcurrentHashMap<>() : null;
    }

    @Override
    public UserConnection user() {
        return connection;
    }

    @Override
    public void addEntity(int id, EntityType type) {
        entityTypes.put(id, type);
    }

    @Override
    public boolean hasEntity(int id) {
        return entityTypes.containsKey(id);
    }

    @Override
    public @Nullable EntityType entityType(int id) {
        return entityTypes.get(id);
    }

    @Override
    public @Nullable StoredEntityData entityData(int id) {
        EntityType type = entityType(id);
        return type != null ? entityData.computeIfAbsent(id, s -> new StoredEntityImpl(type)) : null;
    }

    @Override
    public @Nullable StoredEntityData entityDataIfPresent(int id) {
        return entityData.get(id);
    }

    @Override
    public void removeEntity(int id) {
        entityTypes.remove(id);
        if (entityData != null) {
            entityData.remove(id);
        }
    }

    @Override
    public int clientEntityId() {
        return clientEntityId;
    }

    @Override
    public void setClientEntityId(int clientEntityId) {
        Preconditions.checkNotNull(playerType);
        entityTypes.put(clientEntityId, playerType);
        if (this.clientEntityId != -1 && entityData != null) {
            StoredEntityData data = entityData.remove(this.clientEntityId);
            if (data != null) {
                entityData.put(clientEntityId, data);
            }
        }

        this.clientEntityId = clientEntityId;
    }

    /**
     * @return amount of chunk sections of the current world (block height / 16)
     */
    @Override
    public int currentWorldSectionHeight() {
        return currentWorldSectionHeight;
    }

    @Override
    public void setCurrentWorldSectionHeight(int currentWorldSectionHeight) {
        this.currentWorldSectionHeight = currentWorldSectionHeight;
    }

    /**
     * @return absolute minimum y coordinate of the current world
     */
    @Override
    public int currentMinY() {
        return currentMinY;
    }

    @Override
    public void setCurrentMinY(int currentMinY) {
        this.currentMinY = currentMinY;
    }
}
