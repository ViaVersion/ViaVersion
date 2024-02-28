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
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class StoredEntityDataImpl implements StoredEntityData {
    private final Map<Class<?>, Object> storedObjects = new HashMap<>();
    private final EntityType type;

    public StoredEntityDataImpl(EntityType type) {
        this.type = type;
    }

    @Override
    public EntityType type() {
        return type;
    }

    @Override
    public @Nullable <T> T get(Class<T> objectClass) {
        //noinspection unchecked
        return (T) storedObjects.get(objectClass);
    }

    @Override
    public <T> @Nullable T remove(Class<T> objectClass) {
        //noinspection unchecked
        return (T) storedObjects.remove(objectClass);
    }

    @Override
    public boolean has(Class<?> objectClass) {
        return storedObjects.containsKey(objectClass);
    }

    @Override
    public void put(Object object) {
        storedObjects.put(object.getClass(), object);
    }
}
