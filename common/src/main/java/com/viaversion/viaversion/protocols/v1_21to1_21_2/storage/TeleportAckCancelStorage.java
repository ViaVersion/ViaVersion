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

import com.viaversion.viaversion.api.connection.StorableObject;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class TeleportAckCancelStorage implements StorableObject {

    private final IntSet cancelTeleportIds = new IntOpenHashSet();
    private boolean cancelNextPlayerPositionPacket;

    public boolean checkShouldCancelTeleportAck(final int teleportId) {
        final boolean shouldCancel = this.cancelTeleportIds.remove(teleportId);
        if (shouldCancel) {
            this.cancelNextPlayerPositionPacket = true;
        }
        return shouldCancel;
    }

    public boolean checkShouldCancelPlayerPositionPacket() {
        if (this.cancelNextPlayerPositionPacket) {
            this.cancelNextPlayerPositionPacket = false;
            return true;
        } else {
            return false;
        }
    }

    public void cancelTeleportId(final int teleportId) {
        this.cancelTeleportIds.add(teleportId);
    }

}
