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
package com.viaversion.viaversion.protocols.v1_16_4to1_17.storage;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_17;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public final class EntityTracker1_17 extends EntityTrackerBase {

    private final IntSet dyingEntities = new IntOpenHashSet();

    public EntityTracker1_17(final UserConnection user) {
        super(user, EntityTypes1_17.PLAYER);
    }

    public void addDyingEntity(final int entityId) {
        dyingEntities.add(entityId);
    }

    public boolean removeDyingEntity(final int entityId) {
        return dyingEntities.remove(entityId);
    }

    @Override
    public void clearEntities() {
        super.clearEntities();
        dyingEntities.clear();
    }

    @Override
    public void clear() {
        super.clear();
        dyingEntities.clear();
    }

}
