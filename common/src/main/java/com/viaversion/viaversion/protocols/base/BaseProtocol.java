/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
import com.viaversion.viaversion.api.protocol.ProtocolPathEntry;
import com.viaversion.viaversion.api.protocol.ProtocolPipeline;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.protocol.version.VersionProvider;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.base.packet.BaseClientboundPacket;
import com.viaversion.viaversion.protocols.base.packet.BasePacketTypesProvider;
import com.viaversion.viaversion.protocols.base.packet.BaseServerboundPacket;
import java.util.ArrayList;
import java.util.List;

public class BaseProtocol extends AbstractProtocol<BaseClientboundPacket, BaseClientboundPacket, BaseServerboundPacket, BaseServerboundPacket> {

    private static final int STATUS_INTENT = 1;
    private static final int LOGIN_INTENT = 2;
    private static final int TRANSFER_INTENT = 3;

    public BaseProtocol() {
        super(BaseClientboundPacket.class, BaseClientboundPacket.class, BaseServerboundPacket.class, BaseServerboundPacket.class);
    }

    @Override
    protected void registerPackets() {
        // Handshake Packet
        registerServerbound(ServerboundHandshakePackets.CLIENT_INTENTION, wrapper -> {
            int protocolVersion = wrapper.passthrough(Type.VAR_INT);
            wrapper.passthrough(Type.STRING); // Server Address
            wrapper.passthrough(Type.UNSIGNED_SHORT); // Server Port
            int state = wrapper.passthrough(Type.VAR_INT);

            ProtocolInfo info = wrapper.user().getProtocolInfo();
            info.setProtocolVersion(protocolVersion);
            // Ensure the server has a version provider
            VersionProvider versionProvider = Via.getManager().getProviders().get(VersionProvider.class);
            if (versionProvider == null) {
                wrapper.user().setActive(false);
                return;
            }

            // Choose the pipe
            int serverProtocol = versionProvider.getClosestServerProtocol(wrapper.user());
            info.setServerProtocolVersion(serverProtocol);
            List<ProtocolPathEntry> protocolPath = null;

            // Only allow newer clients (or 1.9.2 on 1.9.4 server if the server supports it)
            if (info.getProtocolVersion() >= serverProtocol || Via.getPlatform().isOldClientsAllowed()) {
                protocolPath = Via.getManager().getProtocolManager().getProtocolPath(info.getProtocolVersion(), serverProtocol);
            }

            ProtocolPipeline pipeline = wrapper.user().getProtocolInfo().getPipeline();
            if (protocolPath != null) {
                List<Protocol> protocols = new ArrayList<>(protocolPath.size());
                for (ProtocolPathEntry entry : protocolPath) {
                    protocols.add(entry.protocol());

                    // Ensure mapping data has already been loaded
                    Via.getManager().getProtocolManager().completeMappingDataLoading(entry.protocol().getClass());
                }

                // Add protocols to pipeline
                pipeline.add(protocols);

                // Set the original snapshot version if present
                ProtocolVersion protocol = ProtocolVersion.getProtocol(serverProtocol);
                wrapper.set(Type.VAR_INT, 0, protocol.getOriginalVersion());
            }

            // Add Base Protocol
            pipeline.add(Via.getManager().getProtocolManager().getBaseProtocol(serverProtocol));

            if (Via.getManager().isDebug()) {
                Via.getPlatform().getLogger().info("User connected with protocol: " + info.getProtocolVersion() + " and serverProtocol: " + info.getServerProtocolVersion());
                Via.getPlatform().getLogger().info("Protocol pipeline: " + pipeline.pipes());
            }

            if (state == STATUS_INTENT) {
                info.setState(State.STATUS);
            } else if (state == LOGIN_INTENT) {
                info.setState(State.LOGIN);
            } else if (state == TRANSFER_INTENT) {
                info.setState(State.LOGIN);

                if (serverProtocol < ProtocolVersion.v1_20_5.getVersion()) {
                    wrapper.set(Type.VAR_INT, 1, LOGIN_INTENT);
                }
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
    public void transform(Direction direction, State state, PacketWrapper packetWrapper) throws Exception {
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
