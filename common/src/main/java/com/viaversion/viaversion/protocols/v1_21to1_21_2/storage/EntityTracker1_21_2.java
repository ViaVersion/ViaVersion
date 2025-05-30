/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_21to1_21_2.storage;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_2;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class EntityTracker1_21_2 extends EntityTrackerBase {

    private final Int2ObjectMap<BoatEntity> boats = new Int2ObjectOpenHashMap<>();
    private double playerMaxHealthAttributeValue = 20F;

    public EntityTracker1_21_2(final UserConnection connection) {
        super(connection, EntityTypes1_21_2.PLAYER);
    }

    public BoatEntity trackBoatEntity(final int entityId, final UUID uuid, final int data) {
        final BoatEntity entity = new BoatEntity(uuid, data);
        boats.put(entityId, entity);
        return entity;
    }

    public BoatEntity trackedBoatEntity(final int entityId) {
        return boats.get(entityId);
    }

    @Override
    public void removeEntity(final int id) {
        super.removeEntity(id);
        boats.remove(id);
    }

    public void updateBoatType(final int entityId, final EntityType type) {
        final BoatEntity entity = boats.get(entityId);
        removeEntity(entityId);
        boats.put(entityId, entity);
        addEntity(entityId, type);
    }

    public double playerMaxHealthAttributeValue() {
        return this.playerMaxHealthAttributeValue;
    }

    public void setPlayerMaxHealthAttributeValue(final double playerMaxHealthAttributeValue) {
        this.playerMaxHealthAttributeValue = playerMaxHealthAttributeValue;
    }

    public static class BoatEntity {

        private final List<EntityData> entityData = new ArrayList<>();

        private final UUID uuid;
        private final int data;

        private double x;
        private double y;
        private double z;

        private float yaw;
        private float pitch;

        private int[] passengers;

        public BoatEntity(final UUID uuid, final int data) {
            this.uuid = uuid;
            this.data = data;
        }

        public void setPosition(final double x, final double y, final double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public void setRotation(final float yaw, final float pitch) {
            this.yaw = yaw;
            this.pitch = pitch;
        }

        public void setPassengers(final int[] passengers) {
            this.passengers = passengers;
        }

        public UUID uuid() {
            return uuid;
        }

        public int data() {
            return data;
        }

        public double x() {
            return x;
        }

        public double y() {
            return y;
        }

        public double z() {
            return z;
        }

        public float yaw() {
            return yaw;
        }

        public float pitch() {
            return pitch;
        }

        public List<EntityData> entityData() {
            return entityData;
        }

        public int[] passengers() {
            return passengers;
        }
    }

}
