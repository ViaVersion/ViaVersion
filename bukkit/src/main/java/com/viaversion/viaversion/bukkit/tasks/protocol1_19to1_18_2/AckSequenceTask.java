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
package com.viaversion.viaversion.bukkit.tasks.protocol1_19to1_18_2;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.ClientboundPackets1_19;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.Protocol1_19To1_18_2;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.storage.SequenceStorage;
import java.util.logging.Level;

public final class AckSequenceTask implements Runnable {

    private final UserConnection connection;
    private final SequenceStorage sequenceStorage;

    public AckSequenceTask(final UserConnection connection, final SequenceStorage sequenceStorage) {
        this.connection = connection;
        this.sequenceStorage = sequenceStorage;
    }

    @Override
    public void run() {
        final int sequence = sequenceStorage.setSequenceId(-1);
        try {
            final PacketWrapper ackPacket = PacketWrapper.create(ClientboundPackets1_19.BLOCK_CHANGED_ACK, connection);
            ackPacket.write(Type.VAR_INT, sequence);
            ackPacket.scheduleSend(Protocol1_19To1_18_2.class);
        } catch (final Exception e) {
            Via.getPlatform().getLogger().log(Level.WARNING, "Failed to send block changed ack packet", e);
        }
    }
}
