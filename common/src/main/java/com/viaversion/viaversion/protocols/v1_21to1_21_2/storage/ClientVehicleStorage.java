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

public final class ClientVehicleStorage implements StorableObject {

    private final int vehicleId;

    private float sidewaysMovement;
    private float forwardMovement;
    private byte flags;

    public ClientVehicleStorage(final int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public void storeMovement(final float sidewaysMovement, final float forwardMovement, final byte flags) {
        this.sidewaysMovement = sidewaysMovement;
        this.forwardMovement = forwardMovement;
        this.flags = flags;
    }

    public int vehicleId() {
        return vehicleId;
    }

    public float sidewaysMovement() {
        return sidewaysMovement;
    }

    public float forwardMovement() {
        return forwardMovement;
    }

    public byte flags() {
        return flags;
    }

}
