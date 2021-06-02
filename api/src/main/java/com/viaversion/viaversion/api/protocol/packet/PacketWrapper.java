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

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.exception.InformativeException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

public interface PacketWrapper {

    int PASSTHROUGH_ID = 1000;

    /**
     * Creates a new packet wrapper instance.
     *
     * @param packetType packet
     * @param connection user connection
     * @return new packet wrapper
     */
    static PacketWrapper create(PacketType packetType, UserConnection connection) {
        return create(packetType.getId(), null, connection);
    }

    /**
     * Creates a new packet wrapper instance.
     *
     * @param packetType  packet type
     * @param inputBuffer input buffer
     * @param connection  user connection
     * @return new packet wrapper
     */
    static PacketWrapper create(PacketType packetType, @Nullable ByteBuf inputBuffer, UserConnection connection) {
        return create(packetType.getId(), inputBuffer, connection);
    }

    /**
     * Creates a new packet wrapper instance.
     *
     * @param packetId    packet id
     * @param inputBuffer input buffer
     * @param connection  user connection
     * @return new packet wrapper
     */
    static PacketWrapper create(int packetId, @Nullable ByteBuf inputBuffer, UserConnection connection) {
        return Via.getManager().getProtocolManager().createPacketWrapper(packetId, inputBuffer, connection);
    }

    /**
     * Get a part from the output
     *
     * @param type  The type of the part you wish to get.
     * @param <T>   The return type of the type you wish to get.
     * @param index The index of the part (relative to the type)
     * @return The requested type or throws ArrayIndexOutOfBounds
     * @throws InformativeException If it fails to find it, an exception will be thrown.
     */
    <T> T get(Type<T> type, int index) throws Exception;

    /**
     * Check if a type is at an index
     *
     * @param type  The type of the part you wish to get.
     * @param index The index of the part (relative to the type)
     * @return True if the type is at the index
     */
    boolean is(Type type, int index);

    /**
     * Check if a type is at an index
     *
     * @param type  The type of the part you wish to get.
     * @param index The index of the part (relative to the type)
     * @return True if the type is at the index
     */
    boolean isReadable(Type type, int index);

    /**
     * Set a currently existing part in the output
     *
     * @param type  The type of the part you wish to set.
     * @param <T>   The return type of the type you wish to set.
     * @param index The index of the part (relative to the type)
     * @param value The value of the part you wish to set it to.
     * @throws InformativeException If it fails to set it, an exception will be thrown.
     */
    <T> void set(Type<T> type, int index, T value) throws Exception;

    /**
     * Read a type from the input.
     *
     * @param type The type you wish to read
     * @param <T>  The return type of the type you wish to read.
     * @return The requested type
     * @throws InformativeException If it fails to read
     */
    <T> T read(Type<T> type) throws Exception;

    /**
     * Write a type to the output.
     *
     * @param type  The type to write.
     * @param <T>   The return type of the type you wish to write.
     * @param value The value of the type to write.
     */
    <T> void write(Type<T> type, T value);

    /**
     * Take a value from the input and write to the output.
     *
     * @param type The type to read and write.
     * @param <T>  The return type of the type you wish to pass through.
     * @return The type which was read/written.
     * @throws Exception If it failed to read or write
     */
    <T> T passthrough(Type<T> type) throws Exception;

    /**
     * Take all the inputs and write them to the output.
     *
     * @throws Exception If it failed to read or write
     */
    void passthroughAll() throws Exception;

    /**
     * Write the current output to a buffer.
     *
     * @param buffer The buffer to write to.
     * @throws InformativeException Throws an exception if it fails to write a value.
     */
    void writeToBuffer(ByteBuf buffer) throws Exception;

    /**
     * Clear the input buffer / readable objects
     */
    void clearInputBuffer();

    /**
     * Clear the packet, used if you have to change the packet completely
     */
    void clearPacket();

    /**
     * Send this packet to the connection on the current thread, skipping the current protocol.
     *
     * @param protocol protocol to be sent through
     * @throws Exception if it fails to write
     */
    default void send(Class<? extends Protocol> protocol) throws Exception {
        send(protocol, true);
    }

    /**
     * Send this packet to the connection on the current thread.
     *
     * @param protocol            protocol to be sent through
     * @param skipCurrentPipeline whether transformation of the current protocol should be skipped
     * @throws Exception if it fails to write
     */
    void send(Class<? extends Protocol> protocol, boolean skipCurrentPipeline) throws Exception;

    /**
     * Send this packet to the connection, submitted to netty's event loop and skipping the current protocol.
     *
     * @param protocol protocol to be sent through
     * @throws Exception if it fails to write
     */
    default void scheduleSend(Class<? extends Protocol> protocol) throws Exception {
        scheduleSend(protocol, true);
    }

    /**
     * Send this packet to the connection, submitted to netty's event loop.
     *
     * @param protocol            protocol to be sent through
     * @param skipCurrentPipeline whether transformation of the current protocol should be skipped
     * @throws Exception if it fails to write
     */
    void scheduleSend(Class<? extends Protocol> protocol, boolean skipCurrentPipeline) throws Exception;

    /**
     * Send this packet to the associated user.
     * Be careful not to send packets twice.
     * (Sends it after current)
     * Also returns the packets ChannelFuture
     *
     * @param packetProtocol The protocol version of the packet.
     * @return The packets ChannelFuture
     * @throws Exception if it fails to write
     */
    ChannelFuture sendFuture(Class<? extends Protocol> packetProtocol) throws Exception;

    /**
     * Send this packet to the associated user.
     * Be careful not to send packets twice.
     * (Sends it after current)
     * <b>This method is no longer used, it's favoured to use {@link #send(Class)} as it will handle the pipeline properly.</b>
     *
     * @throws Exception if it fails to write
     */
    @Deprecated
    void send() throws Exception;

    /**
     * Creates a new packet for the target of this packet.
     *
     * @param packetType packet type of the new packet
     * @return The newly created packet wrapper
     */
    default PacketWrapper create(PacketType packetType) {
        return create(packetType.getId());
    }

    /**
     * Creates a new packet with values.
     *
     * @param packetType packet type of the new packet
     * @param handler    handler to write to the packet
     * @return newly created packet wrapper
     * @throws Exception if it failed to write the values from the ValueCreator
     */
    default PacketWrapper create(PacketType packetType, PacketHandler handler) throws Exception {
        return create(packetType.getId(), handler);
    }

    /**
     * Creates a new packet for the target of this packet.
     *
     * @param packetId id of the packet
     * @return newly created packet wrapper
     */
    PacketWrapper create(int packetId);

    /**
     * Creates a new packet with values.
     *
     * @param packetId id of the packet
     * @param handler  handler to write to the packet
     * @return newly created packet wrapper
     * @throws Exception if it failed to write the values from the ValueCreator
     */
    PacketWrapper create(int packetId, PacketHandler handler) throws Exception;

    /**
     * Applies a pipeline from an index to the wrapper.
     *
     * @param direction protocol direction
     * @param state     protocol state
     * @param index     index to start from, will be reversed depending on the reverse parameter
     * @param pipeline  protocol pipeline
     * @param reverse   whether the array should be looped in reverse, will also reverse the given index
     * @return The current packetwrapper
     * @throws Exception If it fails to transform a packet, exception will be thrown
     */
    PacketWrapper apply(Direction direction, State state, int index, List<Protocol> pipeline, boolean reverse) throws Exception;

    /**
     * @see #apply(Direction, State, int, List, boolean)
     */
    PacketWrapper apply(Direction direction, State state, int index, List<Protocol> pipeline) throws Exception;

    /**
     * Cancel this packet from sending
     */
    void cancel();

    /**
     * Check if this packet is cancelled.
     *
     * @return True if the packet won't be sent.
     */
    boolean isCancelled();

    /**
     * Get the user associated with this Packet
     *
     * @return The user
     */
    UserConnection user();

    /**
     * Reset the reader, so that it can be read again.
     */
    void resetReader();

    /**
     * Send the current packet to the server.
     * (Ensure the ID is suitable for viaversion)
     *
     * @throws Exception If it failed to write
     */
    @Deprecated
    void sendToServer() throws Exception;

    /**
     * Send this packet to the server on the current thread, skipping the current protocol.
     *
     * @param protocol protocol to be sent through
     * @throws Exception if it fails to write
     */
    default void sendToServer(Class<? extends Protocol> protocol) throws Exception {
        sendToServer(protocol, true);
    }

    /**
     * Send this packet to the server on the current thread.
     *
     * @param protocol            protocol to be sent through
     * @param skipCurrentPipeline whether transformation of the current protocol should be skipped
     * @throws Exception if it fails to write
     */
    void sendToServer(Class<? extends Protocol> protocol, boolean skipCurrentPipeline) throws Exception;

    /**
     * Send this packet to the server, submitted to netty's event loop and skipping the current protocol.
     *
     * @param protocol protocol to be sent through
     * @throws Exception if it fails to write
     */
    default void scheduleSendToServer(Class<? extends Protocol> protocol) throws Exception {
        scheduleSendToServer(protocol, true);
    }

    /**
     * Send this packet to the server, submitted to netty's event loop.
     *
     * @param protocol            protocol to be sent through
     * @param skipCurrentPipeline whether transformation of the current protocol should be skipped
     * @throws Exception if it fails to write
     */
    void scheduleSendToServer(Class<? extends Protocol> protocol, boolean skipCurrentPipeline) throws Exception;

    /**
     * Returns the packet id.
     *
     * @return packet id
     */
    int getId();

    /**
     * Sets the packet id. If set to -1, it will not be written to the buffer with {@link #writeToBuffer(ByteBuf)}.
     *
     * @param packetType packet type
     */
    default void setId(PacketType packetType) {
        setId(packetType.getId());
    }

    /**
     * Sets the packet id. If set to -1, it will not be written to the buffer with {@link #writeToBuffer(ByteBuf)}.
     *
     * @param id packet id
     */
    void setId(int id);
}
