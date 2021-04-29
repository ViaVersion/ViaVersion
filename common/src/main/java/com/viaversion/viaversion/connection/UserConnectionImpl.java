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
package com.viaversion.viaversion.connection;

import com.google.common.cache.CacheBuilder;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketTracker;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.exception.CancelException;
import com.viaversion.viaversion.util.ChatColorUtil;
import com.viaversion.viaversion.util.PipelineUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class UserConnectionImpl implements UserConnection {
    private static final AtomicLong IDS = new AtomicLong();
    private final long id = IDS.incrementAndGet();
    private final Map<Class<?>, StoredObject> storedObjects = new ConcurrentHashMap<>();
    private final PacketTracker packetTracker = new PacketTracker(this);
    private final Set<UUID> passthroughTokens = Collections.newSetFromMap(CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .<UUID, Boolean>build().asMap());
    private final ProtocolInfo protocolInfo = new ProtocolInfoImpl(this);
    private final Channel channel;
    private final boolean clientSide;
    private boolean active = true;
    private boolean pendingDisconnect;

    /**
     * Creates an UserConnection. When it's a client-side connection, some method behaviors are modified.
     *
     * @param channel    netty channel.
     * @param clientSide true if it's a client-side connection
     */
    public UserConnectionImpl(@Nullable Channel channel, boolean clientSide) {
        this.channel = channel;
        this.clientSide = clientSide;
    }

    /**
     * @see #UserConnectionImpl(Channel, boolean)
     */
    public UserConnectionImpl(@Nullable Channel channel) {
        this(channel, false);
    }

    @Override
    public @Nullable <T extends StoredObject> T get(Class<T> objectClass) {
        return (T) storedObjects.get(objectClass);
    }

    @Override
    public boolean has(Class<? extends StoredObject> objectClass) {
        return storedObjects.containsKey(objectClass);
    }

    @Override
    public void put(StoredObject object) {
        storedObjects.put(object.getClass(), object);
    }

    @Override
    public void clearStoredObjects() {
        storedObjects.clear();
    }

    @Override
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

    @Override
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

    @Override
    public PacketTracker getPacketTracker() {
        return packetTracker;
    }

    @Override
    public void disconnect(String reason) {
        if (!channel.isOpen() || pendingDisconnect) return;

        pendingDisconnect = true;
        Via.getPlatform().runSync(() -> {
            if (!Via.getPlatform().disconnect(this, ChatColorUtil.translateAlternateColorCodes(reason))) {
                channel.close(); // =)
            }
        });
    }

    @Override
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

    @Override
    public boolean checkServerboundPacket() {
        // Ignore if pending disconnect
        if (pendingDisconnect) return false;
        // Increment received + Check PPS
        return !packetTracker.incrementReceived() || !packetTracker.exceedsMaxPPS();
    }

    @Override
    public boolean checkClientboundPacket() {
        packetTracker.incrementSent();
        return true;
    }

    @Override
    public boolean shouldTransformPacket() {
        return active;
    }

    @Override
    public void transformClientbound(ByteBuf buf, Function<Throwable, Exception> cancelSupplier) throws Exception {
        transform(buf, Direction.CLIENTBOUND, cancelSupplier);
    }

    @Override
    public void transformServerbound(ByteBuf buf, Function<Throwable, Exception> cancelSupplier) throws Exception {
        transform(buf, Direction.SERVERBOUND, cancelSupplier);
    }

    private void transform(ByteBuf buf, Direction direction, Function<Throwable, Exception> cancelSupplier) throws Exception {
        if (!buf.isReadable()) return;

        int id = Type.VAR_INT.readPrimitive(buf);
        if (id == PacketWrapper.PASSTHROUGH_ID) {
            if (!passthroughTokens.remove(Type.UUID.read(buf))) {
                throw new IllegalArgumentException("Invalid token");
            }
            return;
        }

        PacketWrapper wrapper = PacketWrapper.create(id, buf, this);
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

    @Override
    public long getId() {
        return id;
    }

    @Override
    public @Nullable Channel getChannel() {
        return channel;
    }

    @Override
    public ProtocolInfo getProtocolInfo() {
        return protocolInfo;
    }

    @Override
    public Map<Class<?>, StoredObject> getStoredObjects() {
        return storedObjects;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean isPendingDisconnect() {
        return pendingDisconnect;
    }

    @Override
    public void setPendingDisconnect(boolean pendingDisconnect) {
        this.pendingDisconnect = pendingDisconnect;
    }

    @Override
    public boolean isClientSide() {
        return clientSide;
    }

    @Override
    public boolean shouldApplyBlockProtocol() {
        return !clientSide; // Don't apply protocol blocking on client-side
    }

    @Override
    public UUID generatePassthroughToken() {
        UUID token = UUID.randomUUID();
        passthroughTokens.add(token);
        return token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserConnectionImpl that = (UserConnectionImpl) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
