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

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.exception.InformativeException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface PacketWrapper {

    int PASSTHROUGH_ID = 1000;

    /**
     * Creates a new packet wrapper instance.
     *
     * @param packetType packet type, or null if none should be written to the buffer (raw id = -1)
     * @param connection user connection
     * @return new packet wrapper
     */
    static PacketWrapper create(@Nullable PacketType packetType, UserConnection connection) {
        return create(packetType, null, connection);
    }

    /**
     * Creates a new packet wrapper instance.
     *
     * @param packetType  packet type, or null if none should be written to the buffer (raw id = -1)
     * @param inputBuffer input buffer
     * @param connection  user connection
     * @return new packet wrapper
     */
    static PacketWrapper create(@Nullable PacketType packetType, @Nullable ByteBuf inputBuffer, UserConnection connection) {
        return Via.getManager().getProtocolManager().createPacketWrapper(packetType, inputBuffer, connection);
    }

    /**
     * Creates a new packet wrapper instance.
     *
     * @param packetId    packet id, or -1 if none should be written to the buffer
     * @param inputBuffer input buffer
     * @param connection  user connection
     * @return new packet wrapper
     * @deprecated magic id; prefer using {@link #create(PacketType, ByteBuf, UserConnection)}
     */
    @Deprecated
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
    @Deprecated
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
     * @param protocolClass the protocol class to start from in the pipeline
     * @return new ChannelFuture for the write operation
     * @throws Exception if it fails to write
     */
    ChannelFuture sendFuture(Class<? extends Protocol> protocolClass) throws Exception;

    /**
     * @deprecated misleading; use {@link #sendRaw()}. This method will be removed in 5.0.0
     */
    @Deprecated/*(forRemoval = true)*/
    default void send() throws Exception {
        sendRaw();
    }

    /**
     * Sends this packet to the connection.
     * <b>Unlike {@link #send(Class)}, this method does not handle the pipeline with packet id and data changes.</b>
     *
     * @throws Exception if it fails to write
     */
    void sendRaw() throws Exception;

    /**
     * Sends this packet to the associated user, submitted to netty's event loop.
     * <b>Unlike {@link #send(Class)}, this method does not handle the pipeline with packet id and data changes.</b>
     *
     * @throws Exception if it fails to write
     */
    void scheduleSendRaw() throws Exception;

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
     * @param pipeline  protocol pipeline
     * @throws Exception If it fails to transform a packet, exception will be thrown
     */
    void apply(Direction direction, State state, List<Protocol> pipeline) throws Exception;

    /**
     * @deprecated use {@link #apply(Direction, State, List)}
     */
    @Deprecated
    PacketWrapper apply(Direction direction, State state, int index, List<Protocol> pipeline, boolean reverse) throws Exception;

    /**
     * @deprecated use {@link #apply(Direction, State, List)}
     */
    @Deprecated
    default PacketWrapper apply(Direction direction, State state, int index, List<Protocol> pipeline) throws Exception {
        return apply(direction, state, index, pipeline, false);
    }

    /**
     * Check if this packet is cancelled.
     *
     * @return True if the packet won't be sent.
     */
    boolean isCancelled();

    /**
     * Cancel this packet from sending.
     */
    default void cancel() {
        setCancelled(true);
    }

    /**
     * Sets the cancellation state of the packet.
     *
     * @param cancel whether the packet should be cancelled
     */
    void setCancelled(boolean cancel);

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
     * @deprecated misleading; use {@link #sendToServerRaw()}. This method will be removed in 5.0.0
     */
    @Deprecated/*(forRemoval = true)*/
    default void sendToServer() throws Exception {
        sendToServerRaw();
    }

    /**
     * Sends this packet to the server.
     * <b>Unlike {@link #sendToServer(Class)}, this method does not handle the pipeline with packet id and data changes.</b>
     *
     * @throws Exception if it fails to write
     */
    void sendToServerRaw() throws Exception;

    /**
     * Sends this packet to the server, submitted to netty's event loop.
     * <b>Unlike {@link #sendToServer(Class)}, this method does not handle the pipeline with packet id and data changes.</b>
     *
     * @throws Exception if it fails to write
     */
    void scheduleSendToServerRaw() throws Exception;

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
     * Returns the packet type.
     * Currently only non-null for manually constructed packets before transformation.
     *
     * @return packet type if set
     */
    @Nullable PacketType getPacketType();

    /**
     * Sets the packet type. If set to null, it will not be written to the buffer with {@link #writeToBuffer(ByteBuf)}.
     * Setting the type to null also sets the raw packet id to -1.
     *
     * @param packetType packet type
     */
    void setPacketType(@Nullable PacketType packetType);

    /**
     * Returns the raw packet id.
     *
     * @return raw packet id
     */
    int getId();

    /**
     * Sets the packet type.
     *
     * @param packetType packet type
     * @deprecated use {@link #setPacketType(PacketType)}. This method will be removed in 5.0.0
     */
    @Deprecated/*(forRemoval = true)*/
    default void setId(PacketType packetType) {
        setPacketType(packetType);
    }

    /**
     * Sets the packet id. If set to -1, it will not be written to the buffer with {@link #writeToBuffer(ByteBuf)}.
     *
     * @param id packet id
     * @deprecated magic id, loses packet type info; use {@link #setPacketType(PacketType)}
     */
    @Deprecated
    void setId(int id);
}
