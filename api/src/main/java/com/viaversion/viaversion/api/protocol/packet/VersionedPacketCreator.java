/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.viaversion.viaversion.api.protocol.packet;

import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Consumer;

/**
 * Utility to send packets from a given base version to or from any client version supported by Via.
 *
 * @param <C> clientbound packet type
 * @param <S> serverbound packet type
 */
public interface VersionedPacketCreator<C extends ClientboundPacketType, S extends ServerboundPacketType> {

    /**
     * Sends a packet to the given user.
     * Returns false if the packet has been cancelled at some point, but does not indicate whether a replacement has been constructed.
     *
     * @param connection   user connection
     * @param packetType   clientbound packet type
     * @param packetWriter consumer filling the packet with data
     * @return whether this packet specifically has been sent, false if cancelled
     * @throws IllegalArgumentException if the packet type is not of the expected clientbound packets class
     * @throws RuntimeException         if no path from the input version to the required client version exists
     * @throws Exception                if an error occurred while constructing the packet or sending it
     */
    boolean send(UserConnection connection, C packetType, Consumer<PacketWrapper> packetWriter) throws Exception;

    /**
     * Sends a packet to the server.
     * Returns false if the packet has been cancelled at some point, but does not indicate whether a replacement has been constructed.
     *
     * @param connection   user connection
     * @param packetType   serverbound packet type
     * @param packetWriter consumer filling the packet with data
     * @return whether this packet specifically has been sent, false if cancelled
     * @throws IllegalArgumentException if the packet type is not of the expected serverbound packets class
     * @throws RuntimeException         if no path from the input version to the required server version exists
     * @throws Exception                if an error occurred while constructing the packet or sending it
     */
    boolean send(UserConnection connection, S packetType, Consumer<PacketWrapper> packetWriter) throws Exception;

    /**
     * Sends a packet to the given user, submitted to the netty event loop.
     * Returns false if the packet has been cancelled at some point, but does not indicate whether a replacement has been constructed.
     *
     * @param connection   user connection
     * @param packetType   clientbound packet type
     * @param packetWriter consumer filling the packet with data
     * @return whether this packet specifically has been sent, false if cancelled
     * @throws IllegalArgumentException if the packet type is not of the expected clientbound packets class
     * @throws RuntimeException         if no path from the input version to the required client version exists
     * @throws Exception                if an error occurred while constructing the packet or sending it
     */
    boolean scheduleSend(UserConnection connection, C packetType, Consumer<PacketWrapper> packetWriter) throws Exception;

    /**
     * Sends a packet to the server, submitted to the netty event loop.
     * Returns false if the packet has been cancelled at some point, but does not indicate whether a replacement has been constructed.
     *
     * @param connection   user connection
     * @param packetType   serverbound packet type
     * @param packetWriter consumer filling the packet with data
     * @return whether this packet specifically has been sent, false if cancelled
     * @throws IllegalArgumentException if the packet type is not of the expected serverbound packets class
     * @throws RuntimeException         if no path from the input version to the required server version exists
     * @throws Exception                if an error occurred while constructing the packet or sending it
     */
    boolean scheduleSend(UserConnection connection, S packetType, Consumer<PacketWrapper> packetWriter) throws Exception;

    /**
     * Transforms a packet to the protocol version of the given connection, or null if cancelled at some point.
     * The target version is given by {@link ProtocolInfo#getProtocolVersion()} with the connection as the receiver.
     *
     * @param connection   user connection
     * @param packetType   clientbound packet type
     * @param packetWriter consumer filling the packet with data
     * @return created and transformed packet wrapper, or null if cancelled at some point
     * @throws IllegalArgumentException if the packet type is not of the expected clientbound packets class
     * @throws RuntimeException         if no path from the input version to the required client version exists
     * @throws Exception                if an error occurred while constructing the packet
     */
    @Nullable PacketWrapper transform(UserConnection connection, C packetType, Consumer<PacketWrapper> packetWriter) throws Exception;

    /**
     * Transforms a packet to the server protocol version the connection is on, or null if cancelled at some point.
     * The target version is given by {@link ProtocolInfo#getServerProtocolVersion()} with the connection as the sender.
     *
     * @param connection   user connection
     * @param packetType   serverbound packet type
     * @param packetWriter consumer filling the packet with data
     * @return created and transformed packet wrapper, or null if cancelled at some point
     * @throws IllegalArgumentException if the packet type is not of the expected serverbound packets class
     * @throws RuntimeException         if no path from the input version to the required server version exists
     * @throws Exception                if an error occurred while constructing the packet
     */
    @Nullable PacketWrapper transform(UserConnection connection, S packetType, Consumer<PacketWrapper> packetWriter) throws Exception;
}
