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
package com.viaversion.viaversion.protocols.v1_8to1_9.provider;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.providers.Provider;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ServerboundPackets1_8;
import com.viaversion.viaversion.protocols.v1_8to1_9.storage.MovementTracker;
import java.util.logging.Level;

public class MovementTransmitterProvider implements Provider {

    public void sendPlayer(UserConnection userConnection) {
        userConnection.getChannel().eventLoop().execute(() -> {
            if (userConnection.getProtocolInfo().getClientState() != State.PLAY || !userConnection.getEntityTracker(Protocol1_8To1_9.class).hasClientEntityId()) {
                return;
            }

            final MovementTracker movementTracker = userConnection.get(MovementTracker.class);
            movementTracker.incrementIdlePacket();

            try {
                final PacketWrapper playerMovement = PacketWrapper.create(ServerboundPackets1_8.MOVE_PLAYER_STATUS_ONLY, userConnection);
                playerMovement.write(Types.BOOLEAN, movementTracker.isGround()); // on ground
                playerMovement.sendToServer(Protocol1_8To1_9.class);
            } catch (Throwable e) {
                Via.getPlatform().getLogger().log(Level.WARNING, "Failed to send player movement packet", e);
            }
        });
    }
}
