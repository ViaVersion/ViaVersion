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
package com.viaversion.viaversion.protocols.v1_18_2to1_19.provider;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.platform.providers.Provider;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.Protocol1_18_2To1_19;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.packet.ClientboundPackets1_19;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.storage.SequenceStorage;

public class AckSequenceProvider implements Provider {

    public void handleSequence(final UserConnection connection, final BlockPosition position, final int sequence) {
        if (sequence <= 0) return; // Does not need to be acked

        if (position == null) {
            // Acknowledge immediately if the position isn't known
            final PacketWrapper ackPacket = PacketWrapper.create(ClientboundPackets1_19.BLOCK_CHANGED_ACK, connection);
            ackPacket.write(Types.VAR_INT, sequence);
            ackPacket.send(Protocol1_18_2To1_19.class);
        } else {
            // Store sequence to be acknowledged when the block change is received
            connection.get(SequenceStorage.class).addPendingBlockChange(position, sequence);
        }
    }

    public void handleBlockChange(final UserConnection connection, final BlockPosition position) {
        final int sequence = connection.get(SequenceStorage.class).removePendingBlockChange(position);
        if (sequence > 0) { // Acknowledge if pending sequence was found
            final PacketWrapper ackPacket = PacketWrapper.create(ClientboundPackets1_19.BLOCK_CHANGED_ACK, connection);
            ackPacket.write(Types.VAR_INT, sequence);
            ackPacket.scheduleSend(Protocol1_18_2To1_19.class);
        }
    }

}
