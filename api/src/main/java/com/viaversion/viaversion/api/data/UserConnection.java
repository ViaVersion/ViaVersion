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
package us.myles.ViaVersion.api.data;

import com.google.common.cache.CacheBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import org.checkerframework.checker.nullness.qual.Nullable;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.ViaVersionConfig;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.exception.CancelException;
import us.myles.ViaVersion.exception.InformativeException;
import us.myles.ViaVersion.packets.Direction;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.util.ChatColorUtil;
import us.myles.ViaVersion.util.PipelineUtil;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class UserConnection {
    private static final AtomicLong IDS = new AtomicLong();
    private final long id = IDS.incrementAndGet();
    private final Map<Class<?>, StoredObject> storedObjects = new ConcurrentHashMap<>();
    private final PacketTracker packetTracker = new PacketTracker(this);
    private final Set<UUID> passthroughTokens = Collections.newSetFromMap(CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .<UUID, Boolean>build().asMap());
    private final Channel channel;
    private final boolean clientSide;
    private ProtocolInfo protocolInfo;
    private boolean active = true;
    private boolean pendingDisconnect;

    /**
     * Creates an UserConnection. When it's a client-side connection, some method behaviors are modified.
     *
     * @param channel    netty channel.
     * @param clientSide true if it's a client-side connection
     */
    public UserConnection(@Nullable Channel channel, boolean clientSide) {
        this.channel = channel;
        this.clientSide = clientSide;
    }

    /**
     * @see #UserConnection(Channel, boolean)
     */
    public UserConnection(@Nullable Channel channel) {
        this(channel, false);
    }

    /**
     * Get an object from the storage.
     *
     * @param objectClass The class of the object to get
     * @param <T>         The type of the class you want to get.
     * @return The requested object
     */
    public @Nullable <T extends StoredObject> T get(Class<T> objectClass) {
        return (T) storedObjects.get(objectClass);
    }

    /**
     * Check if the storage has an object.
     *
     * @param objectClass The object class to check
     * @return True if the object is in the storage
     */
    public boolean has(Class<? extends StoredObject> objectClass) {
        return storedObjects.containsKey(objectClass);
    }

    /**
     * Put an object into the stored objects based on class.
     *
     * @param object The object to store.
     */
    public void put(StoredObject object) {
        storedObjects.put(object.getClass(), object);
    }

    /**
     * Clear all the stored objects.
     * Used for bungee when switching servers.
     */
    public void clearStoredObjects() {
        storedObjects.clear();
    }

    /**
     * Send a raw packet to the player.
     *
     * @param packet        The raw packet to send
     * @param currentThread Should it run in the same thread
     */
    public void sendRawPacket(final ByteBuf packet, boolean currentThread) {
        Runnable act;
        if (clientSide) {
            // We'll just assume that Via decoder isn't wrapping the original decoder
            act = () -> getChannel().pipeline()
                    .context(Via.getManager().getInjector().getDecoderName()).fireChannelRead(packet);
        } else {
            act = () -> channel.pipeline().context(Via.getManager().getInjector().getEncoderName()).writeAndFlush(packet);
        }
        if (currentThread) {
            act.run();
        } else {
            try {
                channel.eventLoop().submit(act);
            } catch (Throwable e) {
                packet.release(); // Couldn't schedule
                e.printStackTrace();
            }
        }
    }

    /**
     * Send a raw packet to the player with returning the future.
     *
     * @param packet The raw packet to send
     * @return ChannelFuture of the packet being sent
     */
    public ChannelFuture sendRawPacketFuture(final ByteBuf packet) {
        if (clientSide) {
            return sendRawPacketFutureClientSide(packet);
        } else {
            return sendRawPacketFutureServerSide(packet);
        }
    }

    private ChannelFuture sendRawPacketFutureServerSide(final ByteBuf packet) {
        return channel.pipeline().context(Via.getManager().getInjector().getEncoderName()).writeAndFlush(packet);
    }

    private ChannelFuture sendRawPacketFutureClientSide(final ByteBuf packet) {
        // Assume that decoder isn't wrapping
        getChannel().pipeline().context(Via.getManager().getInjector().getDecoderName()).fireChannelRead(packet);
        return getChannel().newSucceededFuture();
    }

    /**
     * Send a raw packet to the player (netty thread).
     *
     * @param packet The packet to send
     */
    public void sendRawPacket(ByteBuf packet) {
        sendRawPacket(packet, false);
    }

    /**
     * Returns the user's packet tracker used for the inbuilt packet-limiter.
     *
     * @return packet tracker
     */
    public PacketTracker getPacketTracker() {
        return packetTracker;
    }

    /**
     * Disconnect a connection.
     *
     * @param reason The reason to use, not used if player is not active.
     */
    public void disconnect(String reason) {
        if (!channel.isOpen() || pendingDisconnect) return;

        pendingDisconnect = true;
        Via.getPlatform().runSync(() -> {
            if (!Via.getPlatform().disconnect(this, ChatColorUtil.translateAlternateColorCodes(reason))) {
                channel.close(); // =)
            }
        });
    }

    /**
     * Sends a raw packet to the server.
     *
     * @param packet        Raw packet to be sent
     * @param currentThread If {@code true} executes immediately, {@code false} submits a task to EventLoop
     */
    public void sendRawPacketToServer(final ByteBuf packet, boolean currentThread) {
        if (clientSide) {
            sendRawPacketToServerClientSide(packet, currentThread);
        } else {
            sendRawPacketToServerServerSide(packet, currentThread);
        }
    }

    private void sendRawPacketToServerServerSide(final ByteBuf packet, boolean currentThread) {
        final ByteBuf buf = packet.alloc().buffer();
        try {
            // We'll use passing through because there are some encoder wrappers
            ChannelHandlerContext context = PipelineUtil
                    .getPreviousContext(Via.getManager().getInjector().getDecoderName(), channel.pipeline());
            try {
                Type.VAR_INT.writePrimitive(buf, PacketWrapper.PASSTHROUGH_ID);
                Type.UUID.write(buf, generatePassthroughToken());
            } catch (Exception shouldNotHappen) {
                throw new RuntimeException(shouldNotHappen);
            }
            buf.writeBytes(packet);
            Runnable act = () -> {
                if (context != null) {
                    context.fireChannelRead(buf);
                } else {
                    channel.pipeline().fireChannelRead(buf);
                }
            };
            if (currentThread) {
                act.run();
            } else {
                try {
                    channel.eventLoop().submit(act);
                } catch (Throwable t) {
                    // Couldn't schedule
                    buf.release();
                    throw t;
                }
            }
        } finally {
            packet.release();
        }
    }

    private void sendRawPacketToServerClientSide(final ByteBuf packet, boolean currentThread) {
        Runnable act = () -> getChannel().pipeline()
                .context(Via.getManager().getInjector().getEncoderName()).writeAndFlush(packet);
        if (currentThread) {
            act.run();
        } else {
            try {
                getChannel().eventLoop().submit(act);
            } catch (Throwable e) {
                e.printStackTrace();
                packet.release(); // Couldn't schedule
            }
        }
    }

    /**
     * Sends a raw packet to the server. It will submit a task to EventLoop.
     *
     * @param packet Raw packet to be sent
     */
    public void sendRawPacketToServer(ByteBuf packet) {
        sendRawPacketToServer(packet, false);
    }

    /**
     * Monitors incoming packets
     *
     * @return false if this packet should be cancelled
     */
    public boolean checkIncomingPacket() {
        if (clientSide) {
            return checkClientbound();
        } else {
            return checkServerbound();
        }
    }

    private boolean checkClientbound() {
        packetTracker.incrementSent();
        return true;
    }

    private boolean checkServerbound() {
        // Ignore if pending disconnect
        if (pendingDisconnect) return false;
        // Increment received + Check PPS
        return !packetTracker.incrementReceived() || !packetTracker.exceedsMaxPPS();
    }

    /**
     * Monitors outgoing packets
     *
     * @return false if this packet should be cancelled
     */
    public boolean checkOutgoingPacket() {
        if (clientSide) {
            return checkServerbound();
        } else {
            return checkClientbound();
        }
    }

    /**
     * Checks if packets needs transforming.
     *
     * @return if packets should be passed through
     */
    public boolean shouldTransformPacket() {
        return active;
    }

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
    public void transformOutgoing(ByteBuf buf, Function<Throwable, Exception> cancelSupplier) throws Exception {
        if (!buf.isReadable()) return;
        transform(buf, clientSide ? Direction.INCOMING : Direction.OUTGOING, cancelSupplier);
    }

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
    public void transformIncoming(ByteBuf buf, Function<Throwable, Exception> cancelSupplier) throws Exception {
        if (!buf.isReadable()) return;
        transform(buf, clientSide ? Direction.OUTGOING : Direction.INCOMING, cancelSupplier);
    }

    private void transform(ByteBuf buf, Direction direction, Function<Throwable, Exception> cancelSupplier) throws Exception {
        int id = Type.VAR_INT.readPrimitive(buf);
        if (id == PacketWrapper.PASSTHROUGH_ID) {
            if (!passthroughTokens.remove(Type.UUID.read(buf))) {
                throw new IllegalArgumentException("Invalid token");
            }
            return;
        }

        PacketWrapper wrapper = new PacketWrapper(id, buf, this);
        try {
            protocolInfo.getPipeline().transform(direction, protocolInfo.getState(), wrapper);
        } catch (CancelException ex) {
            throw cancelSupplier.apply(ex);
        }

        ByteBuf transformed = buf.alloc().buffer();
        try {
            wrapper.writeToBuffer(transformed);
            buf.clear().writeBytes(transformed);
        } finally {
            transformed.release();
        }
    }

    /**
     * Returns the internal id incremented for each new connection.
     *
     * @return internal id
     */
    public long getId() {
        return id;
    }

    /**
     * Returns the netty channel if present.
     *
     * @return netty channel if present
     */
    public @Nullable Channel getChannel() {
        return channel;
    }

    /**
     * Returns info containing the current protocol state and userdata.
     *
     * @return info containing the current protocol state and userdata
     */
    public @Nullable ProtocolInfo getProtocolInfo() {
        return protocolInfo;
    }

    public void setProtocolInfo(@Nullable ProtocolInfo protocolInfo) {
        this.protocolInfo = protocolInfo;
        if (protocolInfo != null) {
            storedObjects.put(ProtocolInfo.class, protocolInfo);
        } else {
            storedObjects.remove(ProtocolInfo.class);
        }
    }

    /**
     * Returns a map of stored objects.
     *
     * @return map of stored objects
     * @see #has(Class)
     * @see #get(Class)
     * @see #put(StoredObject)
     */
    public Map<Class<?>, StoredObject> getStoredObjects() {
        return storedObjects;
    }

    /**
     * Returns whether the connection has protocols other than the base protocol applied.
     *
     * @return whether the connection is active
     */
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Returns whether the connection is pending a disconnect, initiated through {@link #disconnect(String)}.
     *
     * @return whether the connection is pending a disconnect
     */
    public boolean isPendingDisconnect() {
        return pendingDisconnect;
    }

    public void setPendingDisconnect(boolean pendingDisconnect) {
        this.pendingDisconnect = pendingDisconnect;
    }

    /**
     * Returns whether this is a client-side connection.
     * This is a mod integrated into the client itself, or for example a backend Velocity connection.
     *
     * @return whether this is a client-side connection
     */
    public boolean isClientSide() {
        return clientSide;
    }

    /**
     * Returns whether {@link ViaVersionConfig#getBlockedProtocols()} should be checked for this connection.
     *
     * @return whether blocked protocols should be applied
     */
    public boolean shouldApplyBlockProtocol() {
        return !clientSide; // Don't apply protocol blocking on client-side
    }

    /**
     * Returns a newly generated uuid that will let a packet be passed through without
     * transformig its contents if used together with {@link PacketWrapper#PASSTHROUGH_ID}.
     *
     * @return generated passthrough token
     */
    public UUID generatePassthroughToken() {
        UUID token = UUID.randomUUID();
        passthroughTokens.add(token);
        return token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserConnection that = (UserConnection) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
