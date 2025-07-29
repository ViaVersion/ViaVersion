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
package com.viaversion.viaversion.api.connection;

import com.viaversion.viaversion.api.configuration.ViaVersionConfig;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.data.item.ItemHasher;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketTracker;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.exception.InformativeException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.CodecException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface UserConnection {

    /**
     * Get an object from the storage.
     *
     * @param objectClass The class of the object to get
     * @param <T>         The type of the class you want to get.
     * @return The requested object
     */
    @Nullable <T extends StorableObject> T get(Class<T> objectClass);

    /**
     * Check if the storage has an object.
     *
     * @param objectClass The object class to check
     * @return True if the object is in the storage
     */
    boolean has(Class<? extends StorableObject> objectClass);

    /**
     * Removes and returns an object from the storage.
     *
     * @param objectClass class of the object to get
     * @param <T>         type of the class you want to get
     * @return removed storable object if present
     */
    @Nullable <T extends StorableObject> T remove(Class<T> objectClass);

    /**
     * Put an object into the stored objects based on class.
     *
     * @param object The object to store.
     */
    void put(StorableObject object);

    /**
     * Returns a collection of entity trackers currently registered.
     *
     * @return collection of entity trackers currently registered
     */
    Collection<EntityTracker> getEntityTrackers();

    /**
     * Returns the entity tracker by the given protocol class if present.
     *
     * @param protocolClass protocol class
     * @param <T>           entity tracker type
     * @return entity tracker if present
     */
    @Nullable <T extends EntityTracker> T getEntityTracker(Class<? extends Protocol> protocolClass);

    /**
     * Adds an entity tracker to the user connection.
     * Does not override existing entity trackers.
     *
     * @param protocolClass protocol class
     * @param tracker       entity tracker
     */
    void addEntityTracker(Class<? extends Protocol> protocolClass, EntityTracker tracker);

    /**
     * Adds an item hasher to the user connection.
     *
     * @param protocolClass protocol class
     * @param itemHasher   item hasher
     */
    void addItemHasher(Class<? extends Protocol> protocolClass, ItemHasher itemHasher);

    /**
     * Returns the item hasher by the given protocol class if present.
     *
     * @param protocolClass protocol class
     * @param <T>           item hasher type
     * @return item hasher if present
     */
    @Nullable <T extends ItemHasher> T getItemHasher(Class<? extends Protocol> protocolClass);

    /**
     * Returns the client world by the given protocol class if present.
     *
     * @param protocolClass protocol class
     * @param <T>           client world type
     * @return client world if present
     */
    @Nullable
    <T extends ClientWorld> T getClientWorld(Class<? extends Protocol> protocolClass);

    /**
     * Adds a client world to the user connection.
     * Does not override existing client worlds.
     *
     * @param protocolClass protocol class
     * @param clientWorld   client world
     */
    void addClientWorld(Class<? extends Protocol> protocolClass, ClientWorld clientWorld);

    /**
     * Clear stored objects, entity trackers and client worlds.
     */
    void clearStoredObjects();

    /**
     * Sends a raw packet to the connection on the current thread.
     *
     * @param packet raw packet to send
     */
    void sendRawPacket(ByteBuf packet);

    /**
     * Send a raw packet to the player, submitted to the netty event loop.
     *
     * @param packet raw packet to send
     */
    void scheduleSendRawPacket(ByteBuf packet);

    /**
     * Send a raw packet to the player with returning the future.
     *
     * @param packet The raw packet to send
     * @return ChannelFuture of the packet being sent
     */
    ChannelFuture sendRawPacketFuture(ByteBuf packet);

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
     * Sends a raw packet to the server on the current thread.
     *
     * @param packet raw packet to be sent
     */
    void sendRawPacketToServer(ByteBuf packet);

    /**
     * Sends a raw packet to the server, submitted to the netty event loop.
     *
     * @param packet raw packet to be sent
     */
    void scheduleSendRawPacketToServer(ByteBuf packet);

    /**
     * Monitors serverbound packets and returns whether a packet can/should be processed.
     *
     * @param bytes the number of bytes in the packet
     * @return false if this packet should be cancelled
     */
    boolean checkServerboundPacket(int bytes);

    @Deprecated(forRemoval = true)
    default boolean checkServerboundPacket() {
        return checkServerboundPacket(0);
    }

    /**
     * Monitors clientbound packets and returns whether a packet can/should be processed.
     *
     * @return false if this packet should be cancelled
     */
    boolean checkClientboundPacket();

    /**
     * @see #checkClientboundPacket()
     * @see #checkServerboundPacket()
     */
    default boolean checkIncomingPacket(final int bytes) {
        return isClientSide() ? checkClientboundPacket() : checkServerboundPacket(bytes);
    }

    @Deprecated(forRemoval = true)
    default boolean checkIncomingPacket() {
        return isClientSide() ? checkClientboundPacket() : checkServerboundPacket();
    }

    /**
     * @see #checkClientboundPacket()
     * @see #checkServerboundPacket()
     */
    default boolean checkOutgoingPacket() {
        return isClientSide() ? checkServerboundPacket() : checkClientboundPacket();
    }

    /**
     * Checks if packets needs transforming.
     *
     * @return whether packets should be passed through
     */
    boolean shouldTransformPacket();

    /**
     * Transforms the clientbound packet contained in ByteBuf.
     *
     * @param buf            ByteBuf with packet id and packet contents
     * @param cancelSupplier function called with original CancelException for generating the Exception when the packet is cancelled
     * @throws CodecException       if the packet should be cancelled (by netty)
     * @throws InformativeException if packet transforming failed
     */
    void transformClientbound(ByteBuf buf, Function<Throwable, CodecException> cancelSupplier) throws InformativeException;

    /**
     * Transforms the serverbound packet contained in ByteBuf.
     *
     * @param buf            ByteBuf with packet id and packet contents
     * @param cancelSupplier Function called with original CancelException for generating the Exception used when
     *                       packet is cancelled
     * @throws CodecException       if the packet should be cancelled (by netty)
     * @throws InformativeException if packet transforming failed
     */
    void transformServerbound(ByteBuf buf, Function<Throwable, CodecException> cancelSupplier) throws InformativeException;

    /**
     * Transforms the packet depending on whether the connection is clientside or not.
     *
     * @see #transformClientbound(ByteBuf, Function)
     * @see #transformServerbound(ByteBuf, Function)
     */
    default void transformOutgoing(ByteBuf buf, Function<Throwable, CodecException> cancelSupplier) throws InformativeException {
        if (isClientSide()) {
            transformServerbound(buf, cancelSupplier);
        } else {
            transformClientbound(buf, cancelSupplier);
        }
    }

    /**
     * Transforms the packet depending on whether the connection is clientside or not.
     *
     * @see #transformClientbound(ByteBuf, Function)
     * @see #transformServerbound(ByteBuf, Function)
     */
    default void transformIncoming(ByteBuf buf, Function<Throwable, CodecException> cancelSupplier) throws InformativeException {
        if (isClientSide()) {
            transformClientbound(buf, cancelSupplier);
        } else {
            transformServerbound(buf, cancelSupplier);
        }
    }

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
    ProtocolInfo getProtocolInfo();

    /**
     * Returns a map of stored objects.
     *
     * @return map of stored objects
     * @see #has(Class)
     * @see #get(Class)
     * @see #put(StorableObject)
     */
    Map<Class<?>, StorableObject> getStoredObjects();

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
     * Returns whether this is a backend connection.
     * This is a mod integrated into the client, or for example a backend Velocity connection.
     *
     * @return whether this is a backend connection
     */
    boolean isClientSide();

    /**
     * Returns whether this is a frontend connection.
     * This is a plugin integrated into the server, or for example a frontend Velocity connection.
     *
     * @return whether this is a frontend connection
     */
    default boolean isServerSide() {
        return !isClientSide();
    }

    /**
     * Returns whether {@link ViaVersionConfig#blockedProtocolVersions()} should be checked for this connection.
     *
     * @return whether blocked protocols should be applied
     */
    default boolean shouldApplyBlockProtocol() {
        return isServerSide();
    }

    /**
     * Returns a newly generated uuid that will let a packet be passed through without
     * transforming its contents if used together with {@link PacketWrapper#PASSTHROUGH_ID}.
     *
     * @return generated passthrough token
     */
    UUID generatePassthroughToken();
}
