/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2023 ViaVersion and contributors
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
package com.viaversion.viaversion.protocol;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.debug.DebugHandler;
import com.viaversion.viaversion.api.platform.ViaPlatform;
import com.viaversion.viaversion.api.protocol.AbstractSimpleProtocol;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.ProtocolPipeline;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ProtocolPipelineImpl extends AbstractSimpleProtocol implements ProtocolPipeline {
    private final UserConnection userConnection;
    /**
     * Protocol list ordered from client to server transforation with the base protocols at the end.
     */
    private final List<Protocol> protocolList = new CopyOnWriteArrayList<>();
    private final Set<Class<? extends Protocol>> protocolSet = new HashSet<>();
    private List<Protocol> reversedProtocolList = new CopyOnWriteArrayList<>();
    private int baseProtocols;

    public ProtocolPipelineImpl(UserConnection userConnection) {
        this.userConnection = userConnection;
        userConnection.getProtocolInfo().setPipeline(this);
        registerPackets(); // Not registered as a standard "protocol", so we have to call the method manually
    }

    @Override
    protected void registerPackets() {
        // This is a pipeline so we register basic pipes
        final Protocol<?, ?, ?, ?> baseProtocol = Via.getManager().getProtocolManager().getBaseProtocol();
        protocolList.add(baseProtocol);
        reversedProtocolList.add(baseProtocol);
        protocolSet.add(baseProtocol.getClass());
        baseProtocols++;
    }

    @Override
    public void init(UserConnection userConnection) {
        throw new UnsupportedOperationException("ProtocolPipeline can only be initialized once");
    }

    @Override
    public synchronized void add(final Protocol protocol) {
        if (protocol.isBaseProtocol()) {
            // Add base protocol on top of previous ones
            protocolList.add(baseProtocols, protocol);
            reversedProtocolList.add(baseProtocols, protocol);
            baseProtocols++;
        } else {
            protocolList.add(protocol);
            reversedProtocolList.add(0, protocol);
        }

        protocolSet.add(protocol.getClass());
        protocol.init(userConnection);
    }

    @Override
    public synchronized void add(final Collection<Protocol> protocols) {
        protocolList.addAll(protocols);
        for (final Protocol protocol : protocols) {
            protocol.init(userConnection);
            protocolSet.add(protocol.getClass());
        }

        refreshReversedList();
    }

    private synchronized void refreshReversedList() {
        final List<Protocol> protocols = new ArrayList<>(protocolList.subList(0, this.baseProtocols));
        final List<Protocol> additionalProtocols = new ArrayList<>(protocolList.subList(this.baseProtocols, protocolList.size()));
        Collections.reverse(additionalProtocols);
        protocols.addAll(additionalProtocols);
        reversedProtocolList = new CopyOnWriteArrayList<>(protocols);
    }

    @Override
    public void transform(Direction direction, State state, PacketWrapper packetWrapper) throws Exception {
        int originalID = packetWrapper.getId();

        DebugHandler debugHandler = Via.getManager().debugHandler();
        if (debugHandler.enabled() && !debugHandler.logPostPacketTransform() && debugHandler.shouldLog(packetWrapper, direction)) {
            logPacket(direction, state, packetWrapper, originalID);
        }

        // Apply protocols
        packetWrapper.apply(direction, state, 0, protocolListFor(direction));
        super.transform(direction, state, packetWrapper);

        if (debugHandler.enabled() && debugHandler.logPostPacketTransform() && debugHandler.shouldLog(packetWrapper, direction)) {
            logPacket(direction, state, packetWrapper, originalID);
        }
    }

    private List<Protocol> protocolListFor(final Direction direction) {
        return Collections.unmodifiableList(direction == Direction.SERVERBOUND ? protocolList : reversedProtocolList);
    }

    private void logPacket(Direction direction, State state, PacketWrapper packetWrapper, int originalID) {
        // Debug packet
        int clientProtocol = userConnection.getProtocolInfo().getProtocolVersion();
        ViaPlatform<?> platform = Via.getPlatform();

        String actualUsername = packetWrapper.user().getProtocolInfo().getUsername();
        String username = actualUsername != null ? actualUsername + " " : "";

        platform.getLogger().log(Level.INFO, "{0}{1} {2}: {3} ({4}) -> {5} ({6}) [{7}] {8}",
                new Object[]{
                        username,
                        direction,
                        state,
                        originalID,
                        AbstractSimpleProtocol.toNiceHex(originalID),
                        packetWrapper.getId(),
                        AbstractSimpleProtocol.toNiceHex(packetWrapper.getId()),
                        Integer.toString(clientProtocol),
                        packetWrapper
                });
    }

    @Override
    public boolean contains(Class<? extends Protocol> protocolClass) {
        return protocolSet.contains(protocolClass);
    }

    @Override
    public @Nullable <P extends Protocol> P getProtocol(Class<P> pipeClass) {
        for (Protocol protocol : protocolList) {
            if (protocol.getClass() == pipeClass) {
                return (P) protocol;
            }
        }
        return null;
    }

    @Override
    public List<Protocol> pipes() {
        return Collections.unmodifiableList(protocolList);
    }

    @Override
    public List<Protocol> reversedPipes() {
        return Collections.unmodifiableList(reversedProtocolList);
    }

    @Override
    public boolean hasNonBaseProtocols() {
        for (Protocol protocol : protocolList) {
            if (!protocol.isBaseProtocol()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void cleanPipes() {
        registerPackets();
    }
}
