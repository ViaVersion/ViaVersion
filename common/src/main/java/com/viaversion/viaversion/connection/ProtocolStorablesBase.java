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
package com.viaversion.viaversion.connection;

import com.viaversion.viaversion.api.connection.ProtocolStorables;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.data.item.ItemHasher;
import com.viaversion.viaversion.api.minecraft.ClientWorld;

public class ProtocolStorablesBase implements ProtocolStorables {

    private EntityTracker tracker;
    private ClientWorld clientWorld;
    private ItemHasher itemHasher;

    @Override
    public EntityTracker entityTracker() {
        return tracker;
    }

    @Override
    public void setEntityTracker(final EntityTracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public ClientWorld clientWorld() {
        return clientWorld;
    }

    @Override
    public void setClientWorld(final ClientWorld clientWorld) {
        this.clientWorld = clientWorld;
    }

    @Override
    public ItemHasher itemHasher() {
        return itemHasher;
    }

    @Override
    public void setItemHasher(final ItemHasher itemHasher) {
        this.itemHasher = itemHasher;
    }
}
