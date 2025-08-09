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
package com.viaversion.viaversion.protocols.base.v1_7;

import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import com.viaversion.viaversion.protocols.base.packet.BaseClientboundPacket;
import com.viaversion.viaversion.protocols.base.packet.BasePacketTypesProvider;
import com.viaversion.viaversion.protocols.base.packet.BaseServerboundPacket;
import com.viaversion.viaversion.util.ChatColorUtil;
import com.viaversion.viaversion.util.ComponentUtil;
import io.netty.channel.ChannelFuture;
import java.util.logging.Level;

public class ServerboundBaseProtocol1_7 extends AbstractProtocol<BaseClientboundPacket, BaseClientboundPacket, BaseServerboundPacket, BaseServerboundPacket> {

    public ServerboundBaseProtocol1_7() {
        super(BaseClientboundPacket.class, BaseClientboundPacket.class, BaseServerboundPacket.class, BaseServerboundPacket.class);
    }

    @Override
    protected void registerPackets() {
        // State tracking
        registerServerbound(ServerboundLoginPackets.LOGIN_ACKNOWLEDGED, wrapper -> {
            final ProtocolInfo info = wrapper.user().getProtocolInfo();
            info.setState(State.CONFIGURATION);
        });

        // Handle blocked version disconnect
        registerServerbound(ServerboundLoginPackets.HELLO, wrapper -> {
            final UserConnection user = wrapper.user();
            final ProtocolVersion protocol = user.getProtocolInfo().protocolVersion();
            if (Via.getConfig().blockedProtocolVersions().contains(protocol)) {
                if (!user.getChannel().isOpen() || !user.shouldApplyBlockProtocol()) {
                    return;
                }

                wrapper.cancel(); // cancel current

                final String disconnectMessage = ChatColorUtil.translateAlternateColorCodes(Via.getConfig().getBlockedDisconnectMsg());
                final PacketWrapper disconnectPacket = PacketWrapper.create(ClientboundLoginPackets.LOGIN_DISCONNECT, user);

                final JsonObject object = ComponentUtil.plainToJson(disconnectMessage);
                if (protocol.olderThanOrEqualTo(ProtocolVersion.v1_8)) {
                    disconnectPacket.write(Types.STRING, object.toString());
                } else {
                    disconnectPacket.write(Types.COMPONENT, object);
                }

                // Send and close
                final ChannelFuture future = disconnectPacket.sendFutureRaw();
                future.addListener(f -> user.getChannel().close());

                if (Via.getConfig().logBlockedJoins()) {
                    Via.getPlatform().getLogger().log(Level.INFO, "Blocked join due to unsupported version from " + user.getChannel().remoteAddress() + " (" + protocol.getName() + ")");
                }
            }
        });
    }

    @Override
    public boolean isBaseProtocol() {
        return true;
    }

    @Override
    protected PacketTypesProvider<BaseClientboundPacket, BaseClientboundPacket, BaseServerboundPacket, BaseServerboundPacket> createPacketTypesProvider() {
        return BasePacketTypesProvider.INSTANCE;
    }
}
