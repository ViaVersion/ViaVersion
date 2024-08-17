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

package com.viaversion.viaversion.data.entity;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.ClientEntityIdChangeListener;
import com.viaversion.viaversion.api.data.entity.DimensionData;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.data.entity.StoredEntityData;
import com.viaversion.viaversion.api.data.entity.TrackedEntity;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.util.Key;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Collections;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public class EntityTrackerBase implements EntityTracker, ClientEntityIdChangeListener {
    protected final Int2ObjectMap<TrackedEntity> entities = new Int2ObjectOpenHashMap<>();
    private final UserConnection connection;
    private final EntityType playerType;
    private int clientEntityId = -1;
    private int currentWorldSectionHeight = -1;
    private int currentMinY;
    private String currentWorld;
    private int biomesSent = -1;
    private Map<String, DimensionData> dimensions = Collections.emptyMap();

    public EntityTrackerBase(UserConnection connection, @Nullable EntityType playerType) {
        this.connection = connection;
        this.playerType = playerType;
    }

    @Override
    public UserConnection user() {
        return connection;
    }

    @Override
    public void addEntity(int id, EntityType type) {
        entities.put(id, new TrackedEntityImpl(type));
    }

    @Override
    public boolean hasEntity(int id) {
        return entities.containsKey(id);
    }

    @Override
    public @Nullable TrackedEntity entity(final int entityId) {
        return entities.get(entityId);
    }

    @Override
    public @Nullable EntityType entityType(int id) {
        final TrackedEntity entity = entities.get(id);
        return entity != null ? entity.entityType() : null;
    }

    @Override
    public @Nullable StoredEntityData entityData(int id) {
        final TrackedEntity entity = entities.get(id);
        return entity != null ? entity.data() : null;
    }

    @Override
    public @Nullable StoredEntityData entityDataIfPresent(int id) {
        final TrackedEntity entity = entities.get(id);
        return entity != null && entity.hasData() ? entity.data() : null;
    }

    //TODO Soft memory leak: Remove entities on respawn in protocols prior to 1.18 (1.16+ only when the worldname is different)
    @Override
    public void removeEntity(int id) {
        entities.remove(id);
    }

    @Override
    public void clearEntities() {
        entities.clear();
    }

    @Override
    public int clientEntityId() {
        return clientEntityId;
    }

    @Override
    public void setClientEntityId(int clientEntityId) {
        Preconditions.checkNotNull(playerType);
        final TrackedEntity oldEntity;
        if (this.clientEntityId != -1 && (oldEntity = entities.remove(this.clientEntityId)) != null) {
            entities.put(clientEntityId, oldEntity);
        } else {
            entities.put(clientEntityId, new TrackedEntityImpl(playerType));
        }

        this.clientEntityId = clientEntityId;
    }

    @Override
    public boolean trackClientEntity() {
        if (clientEntityId != -1) {
            entities.put(clientEntityId, new TrackedEntityImpl(playerType));
            return true;
        }
        return false;
    }

    @Override
    public int currentWorldSectionHeight() {
        return currentWorldSectionHeight;
    }

    @Override
    public void setCurrentWorldSectionHeight(int currentWorldSectionHeight) {
        this.currentWorldSectionHeight = currentWorldSectionHeight;
    }

    @Override
    public int currentMinY() {
        return currentMinY;
    }

    @Override
    public void setCurrentMinY(int currentMinY) {
        this.currentMinY = currentMinY;
    }

    @Override
    public @Nullable String currentWorld() {
        return currentWorld;
    }

    @Override
    public void setCurrentWorld(final String currentWorld) {
        this.currentWorld = currentWorld;
    }

    @Override
    public int biomesSent() {
        return biomesSent;
    }

    @Override
    public void setBiomesSent(int biomesSent) {
        this.biomesSent = biomesSent;
    }

    @Override
    public EntityType playerType() {
        return playerType;
    }

    @Override
    public @Nullable DimensionData dimensionData(String dimension) {
        return dimensions.get(Key.stripMinecraftNamespace(dimension));
    }

    @Override
    public @Nullable DimensionData dimensionData(int dimensionId) {
        return dimensions.values().stream().filter(data -> data.id() == dimensionId).findFirst().orElse(null); // TODO Store as array as well
    }

    @Override
    public void setDimensions(Map<String, DimensionData> dimensions) {
        this.dimensions = dimensions;
    }
}
