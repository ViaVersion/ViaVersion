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
package com.viaversion.viaversion.protocols.base;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.ProtocolManager;
import com.viaversion.viaversion.api.protocol.ProtocolPathEntry;
import com.viaversion.viaversion.api.protocol.ProtocolPipeline;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.protocol.version.VersionProvider;
import com.viaversion.viaversion.api.protocol.version.VersionType;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.exception.CancelException;
import com.viaversion.viaversion.exception.InformativeException;
import com.viaversion.viaversion.protocol.SpecialProtocolVersion;
import com.viaversion.viaversion.protocol.version.BaseVersionProvider;
import com.viaversion.viaversion.protocols.base.packet.BaseClientboundPacket;
import com.viaversion.viaversion.protocols.base.packet.BasePacketTypesProvider;
import com.viaversion.viaversion.protocols.base.packet.BaseServerboundPacket;
import java.util.ArrayList;
import java.util.List;

/**
 * Initial base protocol which is kept always in the pipeline.
 * <p>
 * State tracking for configuration state is done via {@link AbstractProtocol#registerConfigurationChangeHandlers()}
 */
public class InitialBaseProtocol extends AbstractProtocol<BaseClientboundPacket, BaseClientboundPacket, BaseServerboundPacket, BaseServerboundPacket> {

    private static final int STATUS_INTENT = 1;
    private static final int LOGIN_INTENT = 2;
    private static final int TRANSFER_INTENT = 3;

    public InitialBaseProtocol() {
        super(BaseClientboundPacket.class, BaseClientboundPacket.class, BaseServerboundPacket.class, BaseServerboundPacket.class);
    }

    @Override
    protected void registerPackets() {
        // Setup protocol pipeline + track initial state
        registerServerbound(ServerboundHandshakePackets.CLIENT_INTENTION, wrapper -> {
            int protocolVersion = wrapper.passthrough(Types.VAR_INT);
            wrapper.passthrough(Types.STRING); // Server Address
            wrapper.passthrough(Types.UNSIGNED_SHORT); // Server Port
            int state = wrapper.passthrough(Types.VAR_INT);

            VersionProvider versionProvider = Via.getManager().getProviders().get(VersionProvider.class);
            // Ensure the server has a version provider
            if (versionProvider == null) {
                wrapper.user().setActive(false);
                return;
            }

            ProtocolInfo info = wrapper.user().getProtocolInfo();
            info.setProtocolVersion(ProtocolVersion.getProtocol(protocolVersion));

            ProtocolVersion clientVersion = versionProvider.getClientProtocol(wrapper.user());
            if (clientVersion != null) {
                info.setProtocolVersion(clientVersion);
            }

            // Choose the pipe
            ProtocolVersion serverProtocol;
            try {
                serverProtocol = versionProvider.getClosestServerProtocol(wrapper.user());
            } catch (final Exception e) {
                throw new RuntimeException("Error getting server protocol", e);
            }

            info.setServerProtocolVersion(serverProtocol);

            ProtocolManager protocolManager = Via.getManager().getProtocolManager();
            List<ProtocolPathEntry> protocolPath = protocolManager.getProtocolPath(info.protocolVersion(), serverProtocol);

            ProtocolPipeline pipeline = info.getPipeline();

            // Save manually added protocols for later
            List<Protocol> alreadyAdded = new ArrayList<>(pipeline.pipes());

            // Special versions might compare equal to normal versions and would the normal lookup,
            // platforms can use a SpecialProtocolVersion or need to manually handle their base protocols.
            ProtocolVersion clientboundBaseProtocolVersion = null;
            if (serverProtocol.getVersionType() != VersionType.SPECIAL) {
                clientboundBaseProtocolVersion = serverProtocol;
            } else if (serverProtocol instanceof SpecialProtocolVersion version) {
                clientboundBaseProtocolVersion = version.getBaseProtocolVersion();
            }
            // Add base protocols
            for (final Protocol protocol : protocolManager.getBaseProtocols(info.protocolVersion(), clientboundBaseProtocolVersion)) {
                pipeline.add(protocol);
            }

            // Add other protocols
            if (protocolPath != null) {
                List<Protocol> protocols = new ArrayList<>(protocolPath.size());
                for (ProtocolPathEntry entry : protocolPath) {
                    protocols.add(entry.protocol());

                    // Ensure mapping data has already been loaded
                    protocolManager.completeMappingDataLoading(entry.protocol().getClass());
                }

                // Add protocols to pipeline
                pipeline.add(protocols);

                // Set the original snapshot version if present
                wrapper.set(Types.VAR_INT, 0, serverProtocol.getOriginalVersion());
            }

            if (state == TRANSFER_INTENT && serverProtocol.olderThan(ProtocolVersion.v1_20_5)) {
                wrapper.set(Types.VAR_INT, 1, LOGIN_INTENT);
            }

            // Send client intention into the pipeline in case protocols down the line need to transform it
            try {
                final List<Protocol> protocols = new ArrayList<>(pipeline.pipes());
                protocols.removeAll(alreadyAdded); // Skip all manually added protocols to prevent double handling
                wrapper.resetReader();
                wrapper.apply(Direction.SERVERBOUND, State.HANDSHAKE, protocols);
            } catch (CancelException e) {
                wrapper.cancel();
            }

            if (Via.getManager().isDebug()) {
                Via.getPlatform().getLogger().info("User connected with protocol: " + info.protocolVersion() + " and serverProtocol: " + info.serverProtocolVersion());
                Via.getPlatform().getLogger().info("Protocol pipeline: " + pipeline.pipes());
            }

            // Set initial state
            if (state == STATUS_INTENT) {
                info.setState(State.STATUS);
            } else if (state == LOGIN_INTENT || state == TRANSFER_INTENT) {
                info.setState(State.LOGIN);
            }
        });
    }

    @Override
    public boolean isBaseProtocol() {
        return true;
    }

    @Override
    public void register(ViaProviders providers) {
        providers.register(VersionProvider.class, new BaseVersionProvider());
    }

    @Override
    public void transform(Direction direction, State state, PacketWrapper packetWrapper) throws InformativeException, CancelException {
        super.transform(direction, state, packetWrapper);
        if (direction == Direction.SERVERBOUND && state == State.HANDSHAKE) {
            // Disable if it isn't a handshake packet.
            if (packetWrapper.getId() != 0) {
                packetWrapper.user().setActive(false);
            }
        }
    }

    @Override
    protected PacketTypesProvider<BaseClientboundPacket, BaseClientboundPacket, BaseServerboundPacket, BaseServerboundPacket> createPacketTypesProvider() {
        return BasePacketTypesProvider.INSTANCE;
    }
}
