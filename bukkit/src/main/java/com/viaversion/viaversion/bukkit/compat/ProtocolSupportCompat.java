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

import com.viaversion.viaversion.ViaVersionPlugin;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.bukkit.util.NMSUtil;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class ProtocolSupportCompat {

    public static void registerPSConnectListener(ViaVersionPlugin plugin) {
        Via.getPlatform().getLogger().info("Registering ProtocolSupport compat connection listener");
        try {
            //noinspection unchecked
            Class<? extends Event> connectionOpenEvent = (Class<? extends Event>) Class.forName("protocolsupport.api.events.ConnectionOpenEvent");
            Bukkit.getPluginManager().registerEvent(connectionOpenEvent, new Listener() {
            }, EventPriority.HIGH, (listener, event) -> {
                try {
                    Object connection = event.getClass().getMethod("getConnection").invoke(event);
                    ProtocolSupportConnectionListener connectListener = new ProtocolSupportConnectionListener(connection);
                    ProtocolSupportConnectionListener.ADD_PACKET_LISTENER_METHOD.invoke(connection, connectListener);
                } catch (ReflectiveOperationException e) {
                    Via.getPlatform().getLogger().log(Level.WARNING, "Error when handling ProtocolSupport event", e);
                }
            }, plugin);
        } catch (ClassNotFoundException e) {
            Via.getPlatform().getLogger().log(Level.WARNING, "Unable to register ProtocolSupport listener", e);
        }
    }

    public static boolean isMultiplatformPS() {
        try {
            Class.forName("protocolsupport.zplatform.impl.spigot.network.pipeline.SpigotPacketEncoder");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    static HandshakeProtocolType handshakeVersionMethod() {
        Class<?> clazz = null;
        // Check for the mapped method
        try {
            clazz = NMSUtil.nms(
                    "PacketHandshakingInSetProtocol",
                    "net.minecraft.network.protocol.handshake.PacketHandshakingInSetProtocol"
            );
            clazz.getMethod("getProtocolVersion");
            return HandshakeProtocolType.MAPPED;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException ignored) {
        }

        // Check for obfuscated b/c methods
        try {
            if (clazz.getMethod("b").getReturnType() == int.class) {
                return HandshakeProtocolType.OBFUSCATED_B;
            } else if (clazz.getMethod("c").getReturnType() == int.class) {
                return HandshakeProtocolType.OBFUSCATED_C;
            }
            throw new UnsupportedOperationException("Protocol version method not found in " + clazz.getSimpleName());
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    enum HandshakeProtocolType {

        MAPPED("getProtocolVersion"),
        OBFUSCATED_B("b"),
        OBFUSCATED_C("c");

        private final String methodName;

        HandshakeProtocolType(String methodName) {
            this.methodName = methodName;
        }

        public String methodName() {
            return methodName;
        }
    }
}
