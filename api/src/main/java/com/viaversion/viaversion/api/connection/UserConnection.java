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
package com.viaversion.viaversion.api.connection;

import com.viaversion.viaversion.api.configuration.ViaVersionConfig;
import com.viaversion.viaversion.api.protocol.packet.PacketTracker;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.exception.CancelException;
import com.viaversion.viaversion.exception.InformativeException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public interface UserConnection {

    /**
     * Get an object from the storage.
     *
     * @param objectClass The class of the object to get
     * @param <T>         The type of the class you want to get.
     * @return The requested object
     */
    @Nullable <T extends StoredObject> T get(Class<T> objectClass);

    /**
     * Check if the storage has an object.
     *
     * @param objectClass The object class to check
     * @return True if the object is in the storage
     */
    boolean has(Class<? extends StoredObject> objectClass);

    /**
     * Put an object into the stored objects based on class.
     *
     * @param object The object to store.
     */
    void put(StoredObject object);

    /**
     * Clear all the stored objects.
     * Used for bungee when switching servers.
     */
    void clearStoredObjects();

    /**
     * Send a raw packet to the player.
     *
     * @param packet        The raw packet to send
     * @param currentThread Should it run in the same thread
     */
    void sendRawPacket(ByteBuf packet, boolean currentThread);

    /**
     * Send a raw packet to the player with returning the future.
     *
     * @param packet The raw packet to send
     * @return ChannelFuture of the packet being sent
     */
    ChannelFuture sendRawPacketFuture(ByteBuf packet);

    /**
     * Send a raw packet to the player (netty thread).
     *
     * @param packet The packet to send
     */
    void sendRawPacket(ByteBuf packet);

    /**
     * Returns the user's packet tracker used for the inbuilt packet-limiter.
     *
     * @return packet tracker
     */
    PacketTracker getPacketTracker();

    /**
     * Disconnect a connection.
     *
     * @param reason The reason to use, not used if player is not active.
     */
    void disconnect(String reason);

    /**
     * Sends a raw packet to the server.
     *
     * @param packet        Raw packet to be sent
     * @param currentThread If {@code true} executes immediately, {@code false} submits a task to EventLoop
     */
    void sendRawPacketToServer(ByteBuf packet, boolean currentThread);

    /**
     * Sends a raw packet to the server. It will submit a task to EventLoop.
     *
     * @param packet Raw packet to be sent
     */
    void sendRawPacketToServer(ByteBuf packet);

    /**
     * Monitors incoming packets
     *
     * @return false if this packet should be cancelled
     */
    boolean checkIncomingPacket();

    /**
     * Monitors outgoing packets
     *
     * @return false if this packet should be cancelled
     */
    boolean checkOutgoingPacket();

    /**
     * Checks if packets needs transforming.
     *
     * @return if packets should be passed through
     */
    boolean shouldTransformPacket();

    /**
     * Transforms the outgoing packet contained in ByteBuf. When clientSide is true, this packet is considered
     * serverbound.
     *
     * @param buf            ByteBuf with packet id and packet contents
     * @param cancelSupplier Function called with original CancelException for generating the Exception used when
     *                       packet is cancelled
     * @throws CancelException      if the packet should be cancelled
     * @throws InformativeException if packet transforming failed
     * @throws Exception            if any other processing outside of transforming fails
     */
    void transformOutgoing(ByteBuf buf, Function<Throwable, Exception> cancelSupplier) throws Exception;

    /**
     * Transforms the incoming packet contained in ByteBuf. When clientSide is true, this packet is considered
     * clientbound
     *
     * @param buf            ByteBuf with packet id and packet contents
     * @param cancelSupplier Function called with original CancelException for generating the Exception used when
     *                       packet is cancelled
     * @throws CancelException      if the packet should be cancelled
     * @throws InformativeException if packet transforming failed
     * @throws Exception            if any other processing outside of transforming fails
     */
    void transformIncoming(ByteBuf buf, Function<Throwable, Exception> cancelSupplier) throws Exception;

    /**
     * Returns the internal id incremented for each new connection.
     *
     * @return internal id
     */
    long getId();

    /**
     * Returns the netty channel if present.
     *
     * @return netty channel if present
     */
    @Nullable Channel getChannel();

    /**
     * Returns info containing the current protocol state and userdata.
     *
     * @return info containing the current protocol state and userdata
     */
    @Nullable ProtocolInfo getProtocolInfo();

    void setProtocolInfo(@Nullable ProtocolInfo protocolInfo);

    /**
     * Returns a map of stored objects.
     *
     * @return map of stored objects
     * @see #has(Class)
     * @see #get(Class)
     * @see #put(StoredObject)
     */
    Map<Class<?>, StoredObject> getStoredObjects();

    /**
     * Returns whether the connection has protocols other than the base protocol applied.
     *
     * @return whether the connection is active
     */
    boolean isActive();

    void setActive(boolean active);

    /**
     * Returns whether the connection is pending a disconnect, initiated through {@link #disconnect(String)}.
     *
     * @return whether the connection is pending a disconnect
     */
    boolean isPendingDisconnect();

    void setPendingDisconnect(boolean pendingDisconnect);

    /**
     * Returns whether this is a client-side connection.
     * This is a mod integrated into the client itself, or for example a backend Velocity connection.
     *
     * @return whether this is a client-side connection
     */
    boolean isClientSide();

    /**
     * Returns whether {@link ViaVersionConfig#getBlockedProtocols()} should be checked for this connection.
     *
     * @return whether blocked protocols should be applied
     */
    boolean shouldApplyBlockProtocol();

    /**
     * Returns a newly generated uuid that will let a packet be passed through without
     * transformig its contents if used together with {@link PacketWrapper#PASSTHROUGH_ID}.
     *
     * @return generated passthrough token
     */
    UUID generatePassthroughToken();
}
