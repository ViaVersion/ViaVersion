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
import com.viaversion.viaversion.api.protocol.packet.PacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationState implements StorableObject {

    private final List<QueuedPacket> packetQueue = new ArrayList<>();
    private BridgePhase bridgePhase = BridgePhase.NONE;

    public BridgePhase bridgePhase() {
        return bridgePhase;
    }

    public void setBridgePhase(final BridgePhase bridgePhase) {
        this.bridgePhase = bridgePhase;
    }

    public void addPacketToQueue(final PacketWrapper wrapper, final boolean clientbound) throws Exception {
        // Caching packet buffers is cursed, copy to heap buffers to make sure we don't start leaking in dumb cases
        final ByteBuf copy = Unpooled.buffer();
        wrapper.writeToBuffer(copy);
        packetQueue.add(new QueuedPacket(copy, clientbound, wrapper.getPacketType(), wrapper.getId()));
    }

    public List<QueuedPacket> packetQueue() {
        return packetQueue;
    }

    public void reset() {
        packetQueue.clear();
        bridgePhase = BridgePhase.NONE;
    }

    @Override
    public boolean clearOnServerSwitch() {
        return false; // This might be bad
    }

    public enum BridgePhase {
        NONE, PROFILE_SENT, CONFIGURATION
    }

    public static final class QueuedPacket {
        private final ByteBuf buf;
        private final boolean clientbound;
        private final PacketType packetType;
        private final int packetId;

        private QueuedPacket(final ByteBuf buf, final boolean clientbound, final PacketType packetType, final int packetId) {
            this.buf = buf;
            this.clientbound = clientbound;
            this.packetType = packetType;
            this.packetId = packetId;
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
    }
}
