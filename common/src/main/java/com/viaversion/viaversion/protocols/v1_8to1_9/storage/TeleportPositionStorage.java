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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.protocols.v1_8to1_9.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.minecraft.Vector3d;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Positions of the teleports most recently sent to the client.
 * Since 1.21.2, clients keep their own position when receiving a teleport while riding and reply
 * with that instead of the teleport position. A 1.8 server expects the reply at the teleport
 * position, treats anything else as an illegal move, and endlessly corrects the player.
 * See https://github.com/ViaVersion/ViaVersion/issues/4748
 */
public class TeleportPositionStorage implements StorableObject {
    private static final int MAX_PENDING_POSITIONS = 64;

    private final Deque<Vector3d> pendingPositions = new ArrayDeque<>();

    public void addPendingPosition(Vector3d position) {
        if (this.pendingPositions.size() >= MAX_PENDING_POSITIONS) {
            this.pendingPositions.poll();
        }
        this.pendingPositions.add(position);
    }

    /**
     * Returns and removes the oldest pending teleport position, or null if none is pending.
     */
    public Vector3d pollPendingPosition() {
        return this.pendingPositions.poll();
    }
}
