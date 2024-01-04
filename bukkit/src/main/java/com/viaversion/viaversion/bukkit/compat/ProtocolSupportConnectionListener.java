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
package com.viaversion.viaversion.bukkit.compat;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.bukkit.util.NMSUtil;
import java.lang.reflect.Method;
import protocolsupport.api.Connection;

final class ProtocolSupportConnectionListener extends Connection.PacketListener {

    static final Method ADD_PACKET_LISTENER_METHOD;
    private static final Class<?> HANDSHAKE_PACKET_CLASS;
    private static final Method GET_VERSION_METHOD;
    private static final Method SET_VERSION_METHOD;
    private static final Method REMOVE_PACKET_LISTENER_METHOD;
    private static final Method GET_LATEST_METHOD;
    private static final Object PROTOCOL_VERSION_MINECRAFT_FUTURE;
    private static final Object PROTOCOL_TYPE_PC;

    static {
        try {
            HANDSHAKE_PACKET_CLASS = NMSUtil.nms(
                    "PacketHandshakingInSetProtocol",
                    "net.minecraft.network.protocol.handshake.PacketHandshakingInSetProtocol"
            );

            final Class<?> connectionImplClass = Class.forName("protocolsupport.protocol.ConnectionImpl");
            final Class<?> connectionClass = Class.forName("protocolsupport.api.Connection");
            final Class<?> packetListenerClass = Class.forName("protocolsupport.api.Connection$PacketListener");
            final Class<?> protocolVersionClass = Class.forName("protocolsupport.api.ProtocolVersion");
            final Class<?> protocolTypeClass = Class.forName("protocolsupport.api.ProtocolType");
            GET_VERSION_METHOD = connectionClass.getDeclaredMethod("getVersion");
            SET_VERSION_METHOD = connectionImplClass.getDeclaredMethod("setVersion", protocolVersionClass);
            PROTOCOL_VERSION_MINECRAFT_FUTURE = protocolVersionClass.getDeclaredField("MINECRAFT_FUTURE").get(null);
            GET_LATEST_METHOD = protocolVersionClass.getDeclaredMethod("getLatest", protocolTypeClass);
            PROTOCOL_TYPE_PC = protocolTypeClass.getDeclaredField("PC").get(null);
            ADD_PACKET_LISTENER_METHOD = connectionClass.getDeclaredMethod("addPacketListener", packetListenerClass);
            REMOVE_PACKET_LISTENER_METHOD = connectionClass.getDeclaredMethod("removePacketListener", packetListenerClass);
        } catch (final ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private final Object connection;

    ProtocolSupportConnectionListener(final Object connection) {
        this.connection = connection;
    }

    @Override
    public void onPacketReceiving(final Connection.PacketListener.PacketEvent event) {
        try {
            // Check if we are getting handshake packet.
            if (HANDSHAKE_PACKET_CLASS.isInstance(event.getPacket()) && GET_VERSION_METHOD.invoke(connection) == PROTOCOL_VERSION_MINECRAFT_FUTURE) {
                final Object packet = event.getPacket();
                final int protocolVersion = (int) HANDSHAKE_PACKET_CLASS.getDeclaredMethod(ProtocolSupportCompat.handshakeVersionMethod().methodName()).invoke(packet);

                // ViaVersion has at this point already spoofed the connectionversion. (Since it is higher up the pipeline)
                // If via has put the protoVersion to the server we can spoof ProtocolSupport's version.
                if (protocolVersion == Via.getAPI().getServerVersion().lowestSupportedVersion()) {
                    SET_VERSION_METHOD.invoke(connection, GET_LATEST_METHOD.invoke(null, PROTOCOL_TYPE_PC));
                }
            }
            // Id version is not serverversion viaversion will not spoof. ProtocolSupport will handle the rest.
            // In any case, remove the packet listener and wrap up.
            REMOVE_PACKET_LISTENER_METHOD.invoke(connection, this);
        } catch (final ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
