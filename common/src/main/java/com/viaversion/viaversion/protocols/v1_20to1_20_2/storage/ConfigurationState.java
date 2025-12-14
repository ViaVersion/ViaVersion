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
package com.viaversion.viaversion.protocols.v1_20to1_20_2.storage;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ServerboundPackets1_19_4;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.Protocol1_20To1_20_2;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ConfigurationState implements StorableObject {

    private static final QueuedPacket[] EMPTY_PACKET_ARRAY = new QueuedPacket[0];
    private final List<QueuedPacket> packetQueue = new ArrayList<>();
    private BridgePhase bridgePhase = BridgePhase.NONE;
    private QueuedPacket joinGamePacket;
    private boolean queuedJoinGame;
    private CompoundTag lastDimensionRegistry;
    private ClientInformation clientInformation;

    public BridgePhase bridgePhase() {
        return bridgePhase;
    }

    public void setBridgePhase(final BridgePhase bridgePhase) {
        this.bridgePhase = bridgePhase;
    }

    public @Nullable CompoundTag lastDimensionRegistry() {
        return lastDimensionRegistry;
    }

    /**
     * Sets the last dimension registry and returns whether it differs from the previously stored one.
     *
     * @param dimensionRegistry dimension registry to set
     * @return whether the dimension registry differs from the previously stored one
     */
    public boolean setLastDimensionRegistry(final CompoundTag dimensionRegistry) {
        final boolean equals = Objects.equals(this.lastDimensionRegistry, dimensionRegistry);
        this.lastDimensionRegistry = dimensionRegistry;
        return !equals;
    }

    public void setClientInformation(final ClientInformation clientInformation) {
        this.clientInformation = clientInformation;
    }

    public void addPacketToQueue(final PacketWrapper wrapper, final boolean clientbound) {
        packetQueue.add(toQueuedPacket(wrapper, clientbound, false));
    }

    private QueuedPacket toQueuedPacket(final PacketWrapper wrapper, final boolean clientbound, final boolean skipCurrentPipeline) {
        // Caching packet buffers is cursed, copy to heap buffers to make sure we don't start leaking in dumb cases
        final ByteBuf copy = Unpooled.buffer();
        final PacketType packetType = wrapper.getPacketType();
        final int packetId = wrapper.getId();
        // Don't write the packet id to the buffer
        //noinspection deprecation
        wrapper.setId(-1);
        wrapper.writeToBuffer(copy);
        return new QueuedPacket(copy, clientbound, packetType, packetId, skipCurrentPipeline);
    }

    public void setJoinGamePacket(final PacketWrapper wrapper) {
        this.joinGamePacket = toQueuedPacket(wrapper, true, true);
        queuedJoinGame = true;
    }

    @Override
    public void onRemove() {
        for (final QueuedPacket packet : packetQueue) {
            packet.buf().release();
        }
        if (joinGamePacket != null) {
            joinGamePacket.buf().release();
        }
    }

    public void sendQueuedPackets(final UserConnection connection) {
        final boolean hasJoinGamePacket = joinGamePacket != null;
        if (hasJoinGamePacket) {
            packetQueue.add(0, joinGamePacket);
            joinGamePacket = null;
        }

        final PacketWrapper clientInformationPacket = clientInformationPacket(connection);
        if (clientInformationPacket != null) {
            packetQueue.add(hasJoinGamePacket ? 1 : 0, toQueuedPacket(clientInformationPacket, false, true));
        }

        final QueuedPacket[] queuedPackets = packetQueue.toArray(EMPTY_PACKET_ARRAY);
        packetQueue.clear();

        for (final QueuedPacket packet : queuedPackets) {
            final PacketWrapper queuedWrapper;
            try {
                if (packet.packetType() != null) {
                    queuedWrapper = PacketWrapper.create(packet.packetType(), packet.buf(), connection);
                } else {
                    //noinspection deprecation
                    queuedWrapper = PacketWrapper.create(packet.packetId(), packet.buf(), connection);
                }

                if (packet.clientbound()) {
                    queuedWrapper.send(Protocol1_20To1_20_2.class, packet.skipCurrentPipeline());
                } else {
                    queuedWrapper.sendToServer(Protocol1_20To1_20_2.class, packet.skipCurrentPipeline());
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
        NONE, PROFILE_SENT, CONFIGURATION, REENTERING_CONFIGURATION
    }

    public @Nullable PacketWrapper clientInformationPacket(final UserConnection connection) {
        if (clientInformation == null) {
            // Should never be null, but we also shouldn't error
            return null;
        }

        final PacketWrapper settingsPacket = PacketWrapper.create(ServerboundPackets1_19_4.CLIENT_INFORMATION, connection);
        settingsPacket.write(Types.STRING, clientInformation.language);
        settingsPacket.write(Types.BYTE, clientInformation.viewDistance);
        settingsPacket.write(Types.VAR_INT, clientInformation.chatVisibility);
        settingsPacket.write(Types.BOOLEAN, clientInformation.showChatColors);
        settingsPacket.write(Types.UNSIGNED_BYTE, clientInformation.modelCustomization);
        settingsPacket.write(Types.VAR_INT, clientInformation.mainHand);
        settingsPacket.write(Types.BOOLEAN, clientInformation.textFiltering);
        settingsPacket.write(Types.BOOLEAN, clientInformation.allowListing);
        return settingsPacket;
    }

    public static final class QueuedPacket {
        private final ByteBuf buf;
        private final boolean clientbound;
        private final PacketType packetType;
        private final int packetId;
        private final boolean skipCurrentPipeline;

        private QueuedPacket(final ByteBuf buf, final boolean clientbound, @Nullable final PacketType packetType,
                             final int packetId, final boolean skipCurrentPipeline) {
            this.buf = buf;
            this.clientbound = clientbound;
            this.packetType = packetType;
            this.packetId = packetId;
            this.skipCurrentPipeline = skipCurrentPipeline;
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

        public boolean skipCurrentPipeline() {
            return skipCurrentPipeline;
        }
    }

    public static final class ClientInformation {
        private final String language;
        private final byte viewDistance;
        private final int chatVisibility;
        private final boolean showChatColors;
        private final short modelCustomization;
        private final int mainHand;
        private final boolean textFiltering;
        private final boolean allowListing;

        public ClientInformation(final String language, final byte viewDistance, final int chatVisibility,
                                 final boolean showChatColors, final short modelCustomization, final int mainHand,
                                 final boolean textFiltering, final boolean allowListing) {
            this.language = language;
            this.viewDistance = viewDistance;
            this.chatVisibility = chatVisibility;
            this.showChatColors = showChatColors;
            this.modelCustomization = modelCustomization;
            this.mainHand = mainHand;
            this.textFiltering = textFiltering;
            this.allowListing = allowListing;
        }
    }
}
