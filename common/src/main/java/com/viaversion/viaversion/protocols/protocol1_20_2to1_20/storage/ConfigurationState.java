/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2023 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_20_2to1_20.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.Protocol1_20_2To1_20;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationState implements StorableObject {

    private final List<QueuedPacket> packetQueue = new ArrayList<>();
    private BridgePhase bridgePhase = BridgePhase.NONE;
    private QueuedPacket joinGamePacket;
    private boolean queuedJoinGame;

    public BridgePhase bridgePhase() {
        return bridgePhase;
    }

    public void setBridgePhase(final BridgePhase bridgePhase) {
        this.bridgePhase = bridgePhase;
    }

    public void addPacketToQueue(final PacketWrapper wrapper, final boolean clientbound) throws Exception {
        packetQueue.add(toQueuedPacket(wrapper, clientbound, false));
    }

    private QueuedPacket toQueuedPacket(final PacketWrapper wrapper, final boolean clientbound, final boolean skip1_20_2Pipeline) throws Exception {
        // Caching packet buffers is cursed, copy to heap buffers to make sure we don't start leaking in dumb cases
        final ByteBuf copy = Unpooled.buffer();
        final PacketType packetType = wrapper.getPacketType();
        final int packetId = wrapper.getId();
        // Don't write the packet id to the buffer
        //noinspection deprecation
        wrapper.setId(-1);
        wrapper.writeToBuffer(copy);
        return new QueuedPacket(copy, clientbound, packetType, packetId, skip1_20_2Pipeline);
    }

    public void setJoinGamePacket(final PacketWrapper wrapper) throws Exception {
        this.joinGamePacket = toQueuedPacket(wrapper, true, true);
        queuedJoinGame = true;
    }

    @Override
    public boolean clearOnServerSwitch() {
        return false; // This might be bad
    }

    public void sendQueuedPackets(final UserConnection connection) throws Exception {
        if (joinGamePacket != null) {
            packetQueue.add(0, joinGamePacket);
            joinGamePacket = null;
        }

        final ConfigurationState.QueuedPacket[] queuedPackets = packetQueue.toArray(new ConfigurationState.QueuedPacket[0]);
        packetQueue.clear();

        for (final ConfigurationState.QueuedPacket packet : queuedPackets) {
            final PacketWrapper queuedWrapper;
            try {
                if (packet.packetType() != null) {
                    queuedWrapper = PacketWrapper.create(packet.packetType(), packet.buf(), connection);
                } else {
                    //noinspection deprecation
                    queuedWrapper = PacketWrapper.create(packet.packetId(), packet.buf(), connection);
                }

                if (packet.clientbound()) {
                    queuedWrapper.send(Protocol1_20_2To1_20.class, packet.skip1_20_2Pipeline());
                } else {
                    queuedWrapper.sendToServer(Protocol1_20_2To1_20.class, packet.skip1_20_2Pipeline());
                }
            } finally {
                packet.buf().release();
            }
        }
    }

    public void clear() {
        packetQueue.clear();
        bridgePhase = BridgePhase.NONE;
        queuedJoinGame = false;
    }

    public boolean queuedOrSentJoinGame() {
        return queuedJoinGame;
    }

    public enum BridgePhase {
        NONE, PROFILE_SENT, CONFIGURATION
    }

    public static final class QueuedPacket {
        private final ByteBuf buf;
        private final boolean clientbound;
        private final PacketType packetType;
        private final int packetId;
        private final boolean skip1_20_2Pipeline;

        private QueuedPacket(final ByteBuf buf, final boolean clientbound, final PacketType packetType,
                             final int packetId, final boolean skip1_20_2Pipeline) {
            this.buf = buf;
            this.clientbound = clientbound;
            this.packetType = packetType;
            this.packetId = packetId;
            this.skip1_20_2Pipeline = skip1_20_2Pipeline;
        }

        public ByteBuf buf() {
            return buf;
        }

        public boolean clientbound() {
            return clientbound;
        }

        public int packetId() {
            return packetId;
        }

        public @Nullable PacketType packetType() {
            return packetType;
        }

        public boolean skip1_20_2Pipeline() {
            return skip1_20_2Pipeline;
        }

        @Override
        public String toString() {
            return "QueuedPacket{" +
                    "buf=" + buf +
                    ", clientbound=" + clientbound +
                    ", packetType=" + packetType +
                    ", packetId=" + packetId +
                    ", skip1_20_2Pipeline=" + skip1_20_2Pipeline +
                    '}';
        }
    }
}
