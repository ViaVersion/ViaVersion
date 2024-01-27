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
package com.viaversion.viaversion.protocols.base.packet;

import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypeMap;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.base.ClientboundStatusPackets;
import com.viaversion.viaversion.protocols.base.ServerboundHandshakePackets;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import com.viaversion.viaversion.protocols.base.ServerboundStatusPackets;
import java.util.EnumMap;
import java.util.Map;

public final class BasePacketTypesProvider implements PacketTypesProvider<BaseClientboundPacket, BaseClientboundPacket, BaseServerboundPacket, BaseServerboundPacket> {

    public static final PacketTypesProvider<BaseClientboundPacket, BaseClientboundPacket, BaseServerboundPacket, BaseServerboundPacket> INSTANCE = new BasePacketTypesProvider();
    private final Map<State, PacketTypeMap<BaseClientboundPacket>> clientboundPacketTypes = new EnumMap<>(State.class);
    private final Map<State, PacketTypeMap<BaseServerboundPacket>> serverboundPacketTypes = new EnumMap<>(State.class);

    private BasePacketTypesProvider() {
        clientboundPacketTypes.put(State.STATUS, PacketTypeMap.of(ClientboundStatusPackets.class));
        clientboundPacketTypes.put(State.LOGIN, PacketTypeMap.of(ClientboundLoginPackets.class));
        serverboundPacketTypes.put(State.STATUS, PacketTypeMap.of(ServerboundStatusPackets.class));
        serverboundPacketTypes.put(State.HANDSHAKE, PacketTypeMap.of(ServerboundHandshakePackets.class));
        serverboundPacketTypes.put(State.LOGIN, PacketTypeMap.of(ServerboundLoginPackets.class));
    }

    @Override
    public Map<State, PacketTypeMap<BaseClientboundPacket>> unmappedClientboundPacketTypes() {
        return clientboundPacketTypes;
    }

    @Override
    public Map<State, PacketTypeMap<BaseServerboundPacket>> unmappedServerboundPacketTypes() {
        return serverboundPacketTypes;
    }

    @Override
    public Map<State, PacketTypeMap<BaseClientboundPacket>> mappedClientboundPacketTypes() {
        return unmappedClientboundPacketTypes();
    }

    @Override
    public Map<State, PacketTypeMap<BaseServerboundPacket>> mappedServerboundPacketTypes() {
        return unmappedServerboundPacketTypes();
    }
}