/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
import com.viaversion.viaversion.exception.CancelException;
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
    <T> T get(Type<T> type, int index) throws InformativeException;

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
    <T> void set(Type<T> type, int index, @Nullable T value) throws InformativeException;

    /**
     * Read a type from the input.
     *
     * @param type The type you wish to read
     * @param <T>  The return type of the type you wish to read.
     * @return The requested type
     * @throws InformativeException If it fails to read
     */
    <T> T read(Type<T> type) throws InformativeException;

    /**
     * Write a type to the output.
     *
     * @param type  The type to write.
     * @param <T>   The return type of the type you wish to write.
     * @param value The value of the type to write.
     */
    <T> void write(Type<T> type, @Nullable T value);

    /**
     * Take a value from the input and write to the output.
     *
     * @param type The type to read and write.
     * @param <T>  The return type of the type you wish to pass through.
     * @return The type which was read/written.
     * @throws InformativeException If it failed to read or write
     */
    <T> T passthrough(Type<T> type) throws InformativeException;

    /**
     * Take a value from the input and write to the output, mapping the output type.
     * This only works for types implementing {@link com.viaversion.viaversion.api.type.TypeConverter}, which is generally only the primitive wrapper types.
     *
     * @param type       The type to read.
     * @param mappedType The type to write.
     * @param <T>        The return type of the type you wish to pass through.
     * @return The type which was read/written.
     * @throws InformativeException If it failed to read or write
     */
    <T> T passthroughAndMap(Type<?> type, Type<T> mappedType) throws InformativeException;

    /**
     * Take all the inputs and write them to the output.
     *
     * @throws InformativeException If it failed to read or write
     */
    void passthroughAll() throws InformativeException;

    /**
     * Write the current output to a buffer.
     *
     * @param buffer The buffer to write to.
     * @throws InformativeException Throws an exception if it fails to write a value.
     */
    void writeToBuffer(ByteBuf buffer) throws InformativeException;

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
     * @throws InformativeException if it fails to write
     */
    default void send(Class<? extends Protocol> protocol) throws InformativeException {
        send(protocol, true);
    }

    /**
     * Send this packet to the connection on the current thread.
     *
     * @param protocol            protocol to be sent through
     * @param skipCurrentPipeline whether transformation of the current protocol should be skipped
     * @throws InformativeException if it fails to write
     */
    void send(Class<? extends Protocol> protocol, boolean skipCurrentPipeline) throws InformativeException;

    /**
     * Send this packet to the connection, submitted to netty's event loop and skipping the current protocol.
     *
     * @param protocol protocol to be sent through
     * @throws InformativeException if it fails to write
     */
    default void scheduleSend(Class<? extends Protocol> protocol) throws InformativeException {
        scheduleSend(protocol, true);
    }

    /**
     * Send this packet to the connection, submitted to netty's event loop.
     *
     * @param protocol            protocol to be sent through
     * @param skipCurrentPipeline whether transformation of the current protocol should be skipped
     */
    void scheduleSend(Class<? extends Protocol> protocol, boolean skipCurrentPipeline) throws InformativeException;

    /**
     * Sends this packet to the associated user.
     * The ChannelFuture fails exceptionally if the packet is cancelled during construction.
     *
     * @param protocolClass the protocol class to start from in the pipeline
     * @return new ChannelFuture for the write operation
     */
    ChannelFuture sendFuture(Class<? extends Protocol> protocolClass) throws InformativeException;

    /**
     * Sends this packet to the connection.
     * <b>Unlike {@link #send(Class)}, this method does not handle the pipeline with packet id and data changes.</b>
     *
     * @throws InformativeException if it fails to write
     */
    void sendRaw() throws InformativeException;

    /**
     * Sends this packet to the associated user, submitted to netty's event loop.
     * <b>Unlike {@link #sendFuture(Class)}, this method does not handle the pipeline with packet id and data changes.</b>
     *
     * @throws InformativeException if it fails to write
     */
    ChannelFuture sendFutureRaw() throws InformativeException;

    /**
     * Sends this packet to the associated user, submitted to netty's event loop.
     * <b>Unlike {@link #send(Class)}, this method does not handle the pipeline with packet id and data changes.</b>
     *
     * @throws InformativeException if it fails to write
     */
    void scheduleSendRaw() throws InformativeException;

    /**
     * Creates a new packet for the target of this packet.
     *
     * @param packetType packet type of the new packet
     * @return The newly created packet wrapper
     */
    PacketWrapper create(PacketType packetType);

    /**
     * Creates a new packet with values.
     *
     * @param packetType packet type of the new packet
     * @param handler    handler to write to the packet
     * @return newly created packet wrapper
     */
    PacketWrapper create(PacketType packetType, PacketHandler handler) throws InformativeException;

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
     */
    PacketWrapper create(int packetId, PacketHandler handler) throws InformativeException;

    /**
     * Applies a pipeline from an index to the wrapper.
     *
     * @param direction protocol direction
     * @param state     protocol state
     * @param pipeline  protocol pipeline
     */
    void apply(Direction direction, State state, List<Protocol> pipeline) throws InformativeException, CancelException;

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
     * Sends this packet to the server.
     * <b>Unlike {@link #sendToServer(Class)}, this method does not handle the pipeline with packet id and data changes.</b>
     *
     * @throws InformativeException if it fails to write
     */
    void sendToServerRaw() throws InformativeException;

    /**
     * Sends this packet to the server, submitted to netty's event loop.
     * <b>Unlike {@link #sendToServer(Class)}, this method does not handle the pipeline with packet id and data changes.</b>
     *
     * @throws InformativeException if it fails to write
     */
    void scheduleSendToServerRaw() throws InformativeException;

    /**
     * Send this packet to the server on the current thread, skipping the current protocol.
     *
     * @param protocol protocol to be sent through
     * @throws InformativeException if it fails to write
     */
    default void sendToServer(Class<? extends Protocol> protocol) throws InformativeException {
        sendToServer(protocol, true);
    }

    /**
     * Send this packet to the server on the current thread.
     *
     * @param protocol            protocol to be sent through
     * @param skipCurrentPipeline whether transformation of the current protocol should be skipped
     * @throws InformativeException if it fails to write
     */
    void sendToServer(Class<? extends Protocol> protocol, boolean skipCurrentPipeline) throws InformativeException;

    /**
     * Send this packet to the server, submitted to netty's event loop and skipping the current protocol.
     *
     * @param protocol protocol to be sent through
     * @throws InformativeException if it fails to write
     */
    default void scheduleSendToServer(Class<? extends Protocol> protocol) throws InformativeException {
        scheduleSendToServer(protocol, true);
    }

    /**
     * Send this packet to the server, submitted to netty's event loop.
     *
     * @param protocol            protocol to be sent through
     * @param skipCurrentPipeline whether transformation of the current protocol should be skipped
     * @throws InformativeException if it fails to write
     */
    void scheduleSendToServer(Class<? extends Protocol> protocol, boolean skipCurrentPipeline) throws InformativeException;

    /**
     * Returns the packet type, or null if not transformed or manually unset.
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
     * Sets the packet id. If set to -1, it will not be written to the buffer with {@link #writeToBuffer(ByteBuf)}.
     *
     * @param id packet id
     * @deprecated magic id, loses packet type info; use {@link #setPacketType(PacketType)}
     */
    @Deprecated
    void setId(int id);
}
