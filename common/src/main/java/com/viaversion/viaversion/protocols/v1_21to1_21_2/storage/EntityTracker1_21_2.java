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
package com.viaversion.viaversion.protocols.v1_21to1_21_2.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.TrackedEntity;
import com.viaversion.viaversion.api.minecraft.Quaternion;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_2;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class EntityTracker1_21_2 extends EntityTrackerBase {

    private double playerMaxHealthAttributeValue = 20F;

    public EntityTracker1_21_2(final UserConnection connection) {
        super(connection, EntityTypes1_21_2.PLAYER);
    }

    public BoatEntity trackBoatEntity(final int entityId, final UUID uuid, final int data) {
        final BoatEntity entity = new BoatEntity(uuid, data);
        entity(entityId).put(entity);
        return entity;
    }

    public @Nullable BoatEntity trackedBoatEntity(final int entityId) {
        final TrackedEntity entity = entity(entityId);
        return entity != null ? entity.get(BoatEntity.class) : null;
    }

    public void updateBoatType(final int entityId, final EntityType type) {
        final BoatEntity boatEntity = trackedBoatEntity(entityId);
        removeEntity(entityId);

        final TrackedEntity newEntity = addEntity(entityId, type);
        newEntity.put(boatEntity);
    }

    // Returns the display state (lazy-loaded), or null if not a display.
    public @Nullable DisplayEntity trackedDisplay(final int entityId) {
        final TrackedEntity entity = entity(entityId);
        if (entity == null) {
            return null;
        }
        final EntityType type = entity.entityType();
        if (type == null || !type.isOrHasParent(EntityTypes1_21_2.DISPLAY)) {
            return null;
        }
        DisplayEntity display = entity.get(DisplayEntity.class);
        if (display == null) {
            display = new DisplayEntity();
            entity.put(display);
        }
        return display;
    }

    public double playerMaxHealthAttributeValue() {
        return this.playerMaxHealthAttributeValue;
    }

    public void setPlayerMaxHealthAttributeValue(final double playerMaxHealthAttributeValue) {
        this.playerMaxHealthAttributeValue = playerMaxHealthAttributeValue;
    }

    // Track boats to allow boat type changes from the default type -> respawn as new entity
    public static final class BoatEntity implements StorableObject {

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

        public int @Nullable [] passengers() {
            return passengers;
        }
    }

    // Tracks the state needed to work around the 1.21.2 pitch clamp for a single display entity.
    public static final class DisplayEntity implements StorableObject {

        private static final Quaternion IDENTITY = new Quaternion(0, 0, 0, 1);

        private @Nullable Quaternion leftRotation; // base left_rotation as sent by the server, null = identity
        private Quaternion appliedLeftRotation = IDENTITY; // value last delivered to the client
        private float pitch; // real, unclamped xRot in degrees
        private int billboard; // 0 = FIXED (default)

        public @Nullable Quaternion leftRotation() {
            return leftRotation;
        }

        public Quaternion baseLeftRotation() {
            return leftRotation != null ? leftRotation : IDENTITY;
        }

        public void setLeftRotation(final @Nullable Quaternion leftRotation) {
            this.leftRotation = leftRotation;
        }

        public Quaternion appliedLeftRotation() {
            return appliedLeftRotation;
        }

        public void setAppliedLeftRotation(final Quaternion appliedLeftRotation) {
            this.appliedLeftRotation = appliedLeftRotation;
        }

        public float pitch() {
            return pitch;
        }

        public void setPitch(final float pitch) {
            this.pitch = pitch;
        }

        public void setBillboard(final int billboard) {
            this.billboard = billboard;
        }

        public boolean fixedBillboard() {
            return billboard == 0;
        }
    }

}
