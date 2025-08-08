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
package com.viaversion.viaversion.protocol;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.debug.DebugHandler;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.ProtocolPipeline;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.exception.CancelException;
import com.viaversion.viaversion.exception.InformativeException;
import com.viaversion.viaversion.util.ProtocolUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ProtocolPipelineImpl implements ProtocolPipeline {
    private final List<Protocol> protocolList = new ArrayList<>();
    private final Set<Class<? extends Protocol>> protocolSet = new HashSet<>();
    private final UserConnection userConnection;
    private List<Protocol> reversedProtocolList = new ArrayList<>();
    private int baseProtocols;

    public ProtocolPipelineImpl(UserConnection userConnection) {
        this.userConnection = userConnection;
        userConnection.getProtocolInfo().setPipeline(this);
        this.add(Via.getManager().getProtocolManager().getBaseProtocol());
    }

    @Override
    public void add(final Protocol protocol) {
        reversedProtocolList.add(baseProtocols, protocol);
        if (protocol.isBaseProtocol()) {
            // Add base protocol on top of previous ones
            protocolList.add(baseProtocols, protocol);
            baseProtocols++;
        } else {
            protocolList.add(protocol);
        }

        protocolSet.add(protocol.getClass());
        protocol.init(userConnection);
    }

    @Override
    public void add(final Collection<Protocol> protocols) {
        for (final Protocol protocol : protocols) {
            if (protocol.isBaseProtocol()) {
                throw new UnsupportedOperationException("Base protocols cannot be added in bulk");
            }

            protocol.init(userConnection);
            protocolSet.add(protocol.getClass());
        }
        protocolList.addAll(protocols);

        refreshReversedList();
    }

    private void refreshReversedList() {
        final List<Protocol> reversedProtocols = new ArrayList<>(protocolList.size());
        // Add base protocols in regular order first
        for (int i = 0; i < baseProtocols; i++) {
            reversedProtocols.add(protocolList.get(i));
        }

        // Add non-base protocols in reverse order
        for (int i = protocolList.size() - 1; i >= baseProtocols; i--) {
            reversedProtocols.add(protocolList.get(i));
        }
        reversedProtocolList = reversedProtocols;
    }

    @Override
    public void transform(Direction direction, State state, PacketWrapper packetWrapper) throws InformativeException, CancelException {
        int originalID = packetWrapper.getId();

        DebugHandler debugHandler = Via.getManager().debugHandler();
        boolean debug = debugHandler.enabled();
        if (debug && debugHandler.logPrePacketTransform() && debugHandler.shouldLog(packetWrapper, direction)) {
            logPacket(direction, state, packetWrapper, originalID, false);
        }

        // Apply protocols
        packetWrapper.apply(direction, state, protocolListFor(direction));

        if (debug && debugHandler.logPostPacketTransform() && debugHandler.shouldLog(packetWrapper, direction)) {
            logPacket(direction, state, packetWrapper, originalID, true);
        }
    }

    private List<Protocol> protocolListFor(final Direction direction) {
        return direction == Direction.SERVERBOUND ? protocolList : reversedProtocolList;
    }

    private void logPacket(Direction direction, State state, PacketWrapper packetWrapper, int originalID, boolean post) {
        ProtocolInfo protocolInfo = userConnection.getProtocolInfo();
        String actualUsername = protocolInfo.getUsername();
        String username = actualUsername != null ? actualUsername + " " : "";
        Via.getPlatform().getLogger().log(Level.INFO, "{0}: {1}{2} {3}: {4} ({5}) -> {6} ({7}) [{8}] {9}",
            new Object[]{
                post ? "Post" : "Pre",
                username,
                direction,
                state,
                originalID,
                ProtocolUtil.toNiceHex(originalID),
                packetWrapper.getId(),
                ProtocolUtil.toNiceHex(packetWrapper.getId()),
                protocolInfo.protocolVersion().getName(),
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
    public List<Protocol> pipes(@Nullable final Class<? extends Protocol> protocolClass, final boolean skipCurrentPipeline, final Direction direction) {
        final List<Protocol> protocolList = this.protocolListFor(direction);
        final int index = indexOf(protocolClass, skipCurrentPipeline, protocolList);

        final List<Protocol> pipes = new ArrayList<>(baseProtocols + protocolList.size() - index);
        // Always add base protocols to the head
        for (int i = 0, size = Math.min(index, baseProtocols); i < size; i++) {
            pipes.add(protocolList.get(i));
        }

        // Add remaining protocols on top
        for (int i = index, size = protocolList.size(); i < size; i++) {
            pipes.add(protocolList.get(i));
        }
        return pipes;
    }

    private int indexOf(@Nullable Class<? extends Protocol> protocolClass, boolean skipCurrentPipeline, List<Protocol> protocolList) {
        if (protocolClass == null) {
            return 0;
        }

        // Find the index of the given protocol
        int index = -1;
        for (int i = 0; i < protocolList.size(); i++) {
            if (protocolList.get(i).getClass() == protocolClass) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            // The given protocol is not in the pipeline
            throw new NoSuchElementException(protocolClass.getCanonicalName());
        }

        if (skipCurrentPipeline) {
            index = Math.min(index + 1, protocolList.size());
        }
        return index;
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
    public int baseProtocolCount() {
        return baseProtocols;
    }

    @Override
    public boolean hasNonBaseProtocols() {
        return protocolList.size() > baseProtocols;
    }

    @Override
    public void cleanPipes() {
        protocolList.clear();
        reversedProtocolList.clear();
        protocolSet.clear();
        baseProtocols = 0;

        this.add(Via.getManager().getProtocolManager().getBaseProtocol());
    }

    @Override
    public String toString() {
        return "ProtocolPipelineImpl{" +
            "protocolList=" + protocolList +
            '}';
    }
}
