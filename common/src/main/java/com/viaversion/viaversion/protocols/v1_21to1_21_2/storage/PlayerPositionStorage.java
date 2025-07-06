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
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.Protocol1_21To1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class PlayerPositionStorage implements StorableObject {

    private final IntSet pendingPongs = new IntOpenHashSet();
    private boolean captureNextPlayerPositionPacket;
    private PlayerPosition playerPosition;

    public void sendPing(final UserConnection connection, final int id) {
        if (!this.pendingPongs.add(id)) {
            throw new IllegalStateException("Pong already pending for id " + id);
        }
        final PacketWrapper ping = PacketWrapper.create(ClientboundPackets1_21_2.PING, connection);
        ping.write(Types.INT, id); // id
        ping.send(Protocol1_21To1_21_2.class);
    }

    public boolean checkPong(final int id) {
        if (this.pendingPongs.remove(id)) {
            this.reset(); // Ensure we don't have any leftover state
            this.captureNextPlayerPositionPacket = true;
            return true;
        } else {
            return false;
        }
    }

    public boolean checkCaptureNextPlayerPositionPacket() {
        if (this.captureNextPlayerPositionPacket) {
            this.captureNextPlayerPositionPacket = false;
            return true;
        } else { // Packet order was wrong
            this.reset();
            return false;
        }
    }

    public void setPlayerPosition(final PlayerPosition playerPosition) {
        this.playerPosition = playerPosition;
    }

    public boolean checkHasPlayerPosition() {
        if (this.playerPosition != null) {
            return true;
        } else { // Packet order was wrong (ACCEPT_TELEPORTATION before MOVE_PLAYER_POS_ROT packet)
            this.reset();
            return false;
        }
    }

    public void sendMovePlayerPosRot(final UserConnection user) {
        final PacketWrapper movePlayerPosRot = PacketWrapper.create(ServerboundPackets1_20_5.MOVE_PLAYER_POS_ROT, user);
        movePlayerPosRot.write(Types.DOUBLE, this.playerPosition.x); // X
        movePlayerPosRot.write(Types.DOUBLE, this.playerPosition.y); // Y
        movePlayerPosRot.write(Types.DOUBLE, this.playerPosition.z); // Z
        movePlayerPosRot.write(Types.FLOAT, this.playerPosition.yaw); // Yaw
        movePlayerPosRot.write(Types.FLOAT, this.playerPosition.pitch); // Pitch
        movePlayerPosRot.write(Types.BOOLEAN, this.playerPosition.onGround); // On Ground
        movePlayerPosRot.sendToServer(Protocol1_21To1_21_2.class);
        this.reset();
    }

    public void reset() {
        this.captureNextPlayerPositionPacket = false;
        this.playerPosition = null;
    }

    public record PlayerPosition(double x, double y, double z, float yaw, float pitch, boolean onGround) {
    }

}
