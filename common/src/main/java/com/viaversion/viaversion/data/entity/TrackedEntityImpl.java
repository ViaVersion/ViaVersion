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

import com.viaversion.viaversion.api.data.entity.StoredEntityData;
import com.viaversion.viaversion.api.data.entity.TrackedEntity;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;

public final class TrackedEntityImpl implements TrackedEntity {
    private final EntityType entityType;
    private StoredEntityData data;
    private boolean sentMetadata;

    public TrackedEntityImpl(final EntityType entityType) {
        this.entityType = entityType;
    }

    @Override
    public EntityType entityType() {
        return entityType;
    }

    @Override
    public StoredEntityData data() {
        if (data == null) {
            data = new StoredEntityDataImpl(entityType);
        }
        return data;
    }

    @Override
    public boolean hasData() {
        return data != null;
    }

    @Override
    public boolean hasSentMetadata() {
        return sentMetadata;
    }

    @Override
    public void sentMetadata(final boolean sentMetadata) {
        this.sentMetadata = sentMetadata;
    }

    @Override
    public String toString() {
        return "TrackedEntityImpl{" +
                "entityType=" + entityType +
                ", data=" + data +
                ", sentMetadata=" + sentMetadata +
                '}';
    }
}