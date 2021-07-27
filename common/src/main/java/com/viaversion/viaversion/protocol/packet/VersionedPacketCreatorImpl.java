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
package com.viaversion.viaversion.protocol.packet;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.ProtocolPathEntry;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.packet.VersionedPacketCreator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class VersionedPacketCreatorImpl<C extends ClientboundPacketType, S extends ServerboundPacketType> implements VersionedPacketCreator<C, S> {

    private final int inputProtocolVersion;
    private final Class<C> clientboundPacketsClass;
    private final Class<S> serverboundPacketsClass;

    public VersionedPacketCreatorImpl(ProtocolVersion inputVersion, Class<C> clientboundPacketsClass, Class<S> serverboundPacketsClass) {
        Preconditions.checkNotNull(inputVersion);
        Preconditions.checkNotNull(clientboundPacketsClass);
        Preconditions.checkNotNull(serverboundPacketsClass);
        this.inputProtocolVersion = inputVersion.getVersion();
        this.clientboundPacketsClass = clientboundPacketsClass;
        this.serverboundPacketsClass = serverboundPacketsClass;
    }

    @Override
    public boolean send(UserConnection connection, C packetType, Consumer<PacketWrapper> packetWriter) throws Exception {
        Preconditions.checkArgument(packetType.getClass() == clientboundPacketsClass);
        return createAndSend(connection, packetType, packetWriter, true);
    }

    @Override
    public boolean send(UserConnection connection, S packetType, Consumer<PacketWrapper> packetWriter) throws Exception {
        Preconditions.checkArgument(packetType.getClass() == serverboundPacketsClass);
        return createAndSend(connection, packetType, packetWriter, true);
    }

    @Override
    public boolean scheduleSend(UserConnection connection, C packetType, Consumer<PacketWrapper> packetWriter) throws Exception {
        Preconditions.checkArgument(packetType.getClass() == clientboundPacketsClass);
        return createAndSend(connection, packetType, packetWriter, false);
    }

    @Override
    public boolean scheduleSend(UserConnection connection, S packetType, Consumer<PacketWrapper> packetWriter) throws Exception {
        Preconditions.checkArgument(packetType.getClass() == serverboundPacketsClass);
        return createAndSend(connection, packetType, packetWriter, false);
    }

    @Override
    public @Nullable PacketWrapper transform(UserConnection connection, C packetType, Consumer<PacketWrapper> packetWriter) throws Exception {
        Preconditions.checkArgument(packetType.getClass() == clientboundPacketsClass);
        PacketWrapper packet = createAndTransform(connection, packetType, packetWriter);
        return packet.isCancelled() ? null : packet;
    }

    @Override
    public @Nullable PacketWrapper transform(UserConnection connection, S packetType, Consumer<PacketWrapper> packetWriter) throws Exception {
        Preconditions.checkArgument(packetType.getClass() == serverboundPacketsClass);
        PacketWrapper packet = createAndTransform(connection, packetType, packetWriter);
        return packet.isCancelled() ? null : packet;
    }

    private boolean createAndSend(UserConnection connection, PacketType packetType, Consumer<PacketWrapper> packetWriter, boolean currentThread) throws Exception {
        PacketWrapper packet = createAndTransform(connection, packetType, packetWriter);
        if (!packet.isCancelled()) {
            return false;
        }

        if (currentThread) {
            if (packetType.direction() == Direction.CLIENTBOUND) {
                packet.sendRaw();
            } else {
                packet.sendToServerRaw();
            }
        } else {
            if (packetType.direction() == Direction.CLIENTBOUND) {
                packet.scheduleSendRaw();
            } else {
                packet.scheduleSendToServerRaw();
            }
        }
        return true;
    }

    private PacketWrapper createAndTransform(UserConnection connection, PacketType packetType, Consumer<PacketWrapper> packetWriter) throws Exception {
        // If clientbound: Constructor given inputProtocolVersion → Client version
        // If serverbound: Constructor given inputProtocolVersion → Server version
        boolean clientbound = packetType.direction() == Direction.CLIENTBOUND;
        int serverProtocolVersion = clientbound ? this.inputProtocolVersion : connection.getProtocolInfo().getServerProtocolVersion();
        int clientProtocolVersion = clientbound ? connection.getProtocolInfo().getProtocolVersion() : this.inputProtocolVersion;

        // Construct protocol pipeline
        List<ProtocolPathEntry> path = Via.getManager().getProtocolManager().getProtocolPath(clientProtocolVersion, serverProtocolVersion);
        List<Protocol> protocolList = null;
        if (path != null) {
            protocolList = new ArrayList<>(path.size());
            for (ProtocolPathEntry entry : path) {
                protocolList.add(entry.getProtocol());
            }
        } else if (serverProtocolVersion != clientProtocolVersion) {
            throw new RuntimeException("No protocol path between client version " + clientProtocolVersion + " and server version " + serverProtocolVersion);
        }

        PacketWrapper packet = PacketWrapper.create(packetType, connection);
        packetWriter.accept(packet);
        if (protocolList != null) {
            // Reset reader and apply pipeline
            packet.resetReader();

            try {
                packet.apply(packetType.direction(), State.PLAY, 0, protocolList, clientbound);
            } catch (Exception e) {
                throw new Exception("Exception trying to transform packet between client version " + clientProtocolVersion
                        + " and server version " + serverProtocolVersion + ". Are you sure you used the correct input version and packet write types?", e);
            }
        }
        return packet;
    }
}
