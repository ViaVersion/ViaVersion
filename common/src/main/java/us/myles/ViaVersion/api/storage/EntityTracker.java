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
package us.myles.ViaVersion.api.storage;

import org.jetbrains.annotations.Nullable;
import us.myles.ViaVersion.api.data.ExternalJoinGameListener;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.EntityType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class EntityTracker extends StoredObject implements ExternalJoinGameListener {
    private final Map<Integer, EntityType> clientEntityTypes = new ConcurrentHashMap<>();
    private final EntityType playerType;
    private int clientEntityId;

    protected EntityTracker(UserConnection user, EntityType playerType) {
        super(user);
        this.playerType = playerType;
    }

    public void removeEntity(int entityId) {
        clientEntityTypes.remove(entityId);
    }

    public void addEntity(int entityId, EntityType type) {
        clientEntityTypes.put(entityId, type);
    }

    public boolean hasEntity(int entityId) {
        return clientEntityTypes.containsKey(entityId);
    }

    @Nullable
    public EntityType getEntity(int entityId) {
        return clientEntityTypes.get(entityId);
    }

    @Override
    public void onExternalJoinGame(int playerEntityId) {
        clientEntityId = playerEntityId;
        clientEntityTypes.put(playerEntityId, playerType);
    }

    public int getClientEntityId() {
        return clientEntityId;
    }

    public void setClientEntityId(int clientEntityId) {
        this.clientEntityId = clientEntityId;
    }
}
