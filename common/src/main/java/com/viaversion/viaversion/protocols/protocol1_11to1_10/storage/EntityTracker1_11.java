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
package com.viaversion.viaversion.protocols.protocol1_11to1_10.storage;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_11.EntityType;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class EntityTracker1_11 extends EntityTrackerBase {
    private final IntSet holograms = new IntOpenHashSet();

    public EntityTracker1_11(UserConnection user) {
        super(user, EntityType.PLAYER);
    }

    @Override
    public void removeEntity(int entityId) {
        super.removeEntity(entityId);

        removeHologram(entityId);
    }

    public boolean addHologram(int entId) {
        return holograms.add(entId);
    }

    public boolean isHologram(int entId) {
        return holograms.contains(entId);
    }

    public void removeHologram(int entId) {
        holograms.remove(entId);
    }
}
