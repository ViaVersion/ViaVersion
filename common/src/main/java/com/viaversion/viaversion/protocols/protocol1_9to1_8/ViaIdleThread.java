/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_9to1_8;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocol.packet.PacketWrapperImpl;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.providers.MovementTransmitterProvider;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.storage.MovementTracker;

public class ViaIdleThread implements Runnable {
    public static final short ID = (short) "via".hashCode();

    public static void onReceiveMainThreadPing(UserConnection info) {
        MovementTracker movementTracker = info.get(MovementTracker.class);
        if (movementTracker == null) return;

        if (movementTracker.canSendIdle() && info.getChannel().isOpen()) {
            Via.getManager().getProviders().get(MovementTransmitterProvider.class).sendPlayer(info);
        }
    }

    public void sendPing(UserConnection info) {
        PacketWrapper wrapper = new PacketWrapperImpl(ClientboundPackets1_9.WINDOW_CONFIRMATION.getId(), null, info);
        wrapper.write(Type.UNSIGNED_BYTE, (short) 0); // inv id
        wrapper.write(Type.SHORT, ID); // confirm id
        wrapper.write(Type.BOOLEAN, false); // not accepted, returns a packet
        try {
            wrapper.send(Protocol1_9To1_8.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        for (UserConnection info : Via.getManager().getConnectionManager().getConnections()) {
            ProtocolInfo protocolInfo = info.getProtocolInfo();
            if (protocolInfo == null
                    || protocolInfo.getState() != State.PLAY
                    || !protocolInfo.getPipeline().contains(Protocol1_9To1_8.class)) continue;

            sendPing(info);
        }
    }
}
