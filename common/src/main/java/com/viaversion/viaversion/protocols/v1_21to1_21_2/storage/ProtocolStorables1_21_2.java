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

import com.viaversion.viaversion.connection.ProtocolStorablesBase;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ProtocolStorables1_21_2 extends ProtocolStorablesBase {

    private final BundleStateTracker bundleStateTracker = new BundleStateTracker();
    private final TeleportAckCancelStorage teleportAckCancelStorage = new TeleportAckCancelStorage();
    private GroundFlagTracker groundFlagTracker = new GroundFlagTracker();
    private LastExplosionPowerStorage lastExplosionPowerStorage;
    private PlayerPositionStorage playerPositionStorage;
    private ChunkLoadTracker chunkLoadTracker;
    private ClientVehicleStorage clientVehicleStorage;

    public BundleStateTracker bundleStateTracker() {
        return bundleStateTracker;
    }

    public GroundFlagTracker groundFlagTracker() {
        return groundFlagTracker;
    }

    public void setGroundFlagTracker(final GroundFlagTracker groundFlagTracker) {
        this.groundFlagTracker = groundFlagTracker;
    }

    public TeleportAckCancelStorage teleportAckCancelStorage() {
        return teleportAckCancelStorage;
    }

    public @Nullable LastExplosionPowerStorage lastExplosionPowerStorage() {
        return lastExplosionPowerStorage;
    }

    public void setLastExplosionPowerStorage(final LastExplosionPowerStorage lastExplosionPowerStorage) {
        this.lastExplosionPowerStorage = lastExplosionPowerStorage;
    }

    public @Nullable PlayerPositionStorage playerPositionStorage() {
        return playerPositionStorage;
    }

    public void setPlayerPositionStorage(final PlayerPositionStorage playerPositionStorage) {
        this.playerPositionStorage = playerPositionStorage;
    }

    public @Nullable ChunkLoadTracker chunkLoadTracker() {
        return chunkLoadTracker;
    }

    public void setChunkLoadTracker(final ChunkLoadTracker chunkLoadTracker) {
        this.chunkLoadTracker = chunkLoadTracker;
    }

    public @Nullable ClientVehicleStorage clientVehicleStorage() {
        return clientVehicleStorage;
    }

    public void setClientVehicleStorage(final @Nullable ClientVehicleStorage clientVehicleStorage) {
        this.clientVehicleStorage = clientVehicleStorage;
    }

}
