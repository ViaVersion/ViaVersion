/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Utility to send packets from a given base version to or from any client version supported by Via.
 *
 * @param <C> clientbound packet type
 * @param <S> serverbound packet type
 */
public interface VersionedPacketTransformer<C extends ClientboundPacketType, S extends ServerboundPacketType> {

    /**
     * Sends a packet to the user or server, depending on the packet type given by {@link PacketWrapper#getPacketType()}.
     * Returns false if the packet has been cancelled at some point, but does not indicate whether a replacement has been constructed.
     *
     * @return whether this packet specifically has been sent, false if cancelled
     * @throws IllegalArgumentException if the packet type is not of the expected clientbound or serverbound packets class
     * @throws IllegalArgumentException if {@link PacketWrapper#user()} returns null
     * @throws RuntimeException         if no path from the input version to the required client version exists
     * @throws Exception                if an error occurred while transforming or sending the packet
     */
    boolean send(PacketWrapper packet) throws Exception;

    /**
     * @see #send(PacketWrapper)
     */
    boolean send(UserConnection connection, C packetType, Consumer<PacketWrapper> packetWriter) throws Exception;

    /**
     * @see #send(PacketWrapper)
     */
    boolean send(UserConnection connection, S packetType, Consumer<PacketWrapper> packetWriter) throws Exception;

    /**
     * Sends a packet to the user or server, depending on the packet type given by {@link PacketWrapper#getPacketType()}, submitted to the netty event loop.
     * Returns false if the packet has been cancelled at some point, but does not indicate whether a replacement has been constructed.
     *
     * @param packet packet wrapper
     * @return whether this packet specifically has been sent, false if cancelled
     * @throws IllegalArgumentException if the packet type is not of the expected clientbound or serverbound packets class
     * @throws IllegalArgumentException if {@link PacketWrapper#user()} returns null
     * @throws RuntimeException         if no path from the input version to the required client version exists
     * @throws Exception                if an error occurred while transforming or sending the packet
     */
    boolean scheduleSend(PacketWrapper packet) throws Exception;

    /**
     * @see #scheduleSend(PacketWrapper)
     */
    boolean scheduleSend(UserConnection connection, C packetType, Consumer<PacketWrapper> packetWriter) throws Exception;

    /**
     * @see #scheduleSend(PacketWrapper)
     */
    boolean scheduleSend(UserConnection connection, S packetType, Consumer<PacketWrapper> packetWriter) throws Exception;

    /**
     * Transforms a packet to the protocol version of the given connection or server, or null if cancelled at some point.
     * The target version is given by {@link ProtocolInfo#getProtocolVersion()} or {@link ProtocolInfo#getServerProtocolVersion()}.
     *
     * @param packet packet wrapper
     * @return created and transformed packet wrapper, or null if cancelled at some point
     * @throws IllegalArgumentException if the packet type is not of the expected clientbound or serverbound packets class
     * @throws IllegalArgumentException if {@link PacketWrapper#user()} returns null
     * @throws RuntimeException         if no path from the input version to the required client version exists
     * @throws Exception                if an error occurred while transforming the packet
     */
    @Nullable PacketWrapper transform(PacketWrapper packet) throws Exception;

    /**
     * @see #transform(PacketWrapper)
     */
    @Nullable PacketWrapper transform(UserConnection connection, C packetType, Consumer<PacketWrapper> packetWriter) throws Exception;

    /**
     * @see #transform(PacketWrapper)
     */
    @Nullable PacketWrapper transform(UserConnection connection, S packetType, Consumer<PacketWrapper> packetWriter) throws Exception;
}
