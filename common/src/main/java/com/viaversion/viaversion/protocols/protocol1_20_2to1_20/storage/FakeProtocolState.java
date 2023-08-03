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
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.protocol.packet.PacketWrapperImpl;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

public class FakeProtocolState implements StorableObject {

    private final List<QueuedPacket> packetQueue = new ArrayList<>();
    private boolean configurationState;
    private boolean gameProfileSent;

    public boolean isConfigurationState() {
        return configurationState;
    }

    public void setConfigurationState(final boolean configurationState) {
        this.configurationState = configurationState;
    }

    public void addPacketToQueue(final PacketWrapper wrapper, final boolean clientbound) {
        packetQueue.add(new QueuedPacket(((PacketWrapperImpl) wrapper).getInputBuffer().copy(), clientbound)); // TODO
    }

    public List<QueuedPacket> packetQueue() {
        return packetQueue;
    }

    public boolean hasGameProfileBeenSent() {
        return gameProfileSent;
    }

    public void setGameProfileSent(final boolean gameProfileSent) {
        this.gameProfileSent = gameProfileSent;
    }

    @Override
    public boolean clearOnServerSwitch() {
        return false;
    }

    public static final class QueuedPacket {
        private final ByteBuf buf;
        private final boolean clientbound;

        private QueuedPacket(final ByteBuf buf, final boolean clientbound) {
            this.buf = buf;
            this.clientbound = clientbound;
        }

        public ByteBuf buf() {
            return buf;
        }

        public boolean clientbound() {
            return clientbound;
        }
    }
}
