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
package com.viaversion.viaversion.connection;

import com.google.common.cache.CacheBuilder;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketTracker;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.exception.CancelException;
import com.viaversion.viaversion.protocol.packet.PacketWrapperImpl;
import com.viaversion.viaversion.util.ChatColorUtil;
import com.viaversion.viaversion.util.PipelineUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.Nullable;

public class UserConnectionImpl implements UserConnection {
    private static final AtomicLong IDS = new AtomicLong();
    private final long id = IDS.incrementAndGet();
    private final Map<Class<?>, StorableObject> storedObjects = new ConcurrentHashMap<>();
    private final Map<Class<? extends Protocol>, EntityTracker> entityTrackers = new HashMap<>();
    private final PacketTracker packetTracker = new PacketTracker(this);
    private final Set<UUID> passthroughTokens = Collections.newSetFromMap(CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .<UUID, Boolean>build().asMap());
    private final ProtocolInfo protocolInfo = new ProtocolInfoImpl(this);
    private final Channel channel;
    private final boolean clientSide;
    private boolean active = true;
    private boolean pendingDisconnect;
    private boolean packetLimiterEnabled = true;

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
    public @Nullable <T extends StorableObject> T get(Class<T> objectClass) {
        return (T) storedObjects.get(objectClass);
    }

    @Override
    public boolean has(Class<? extends StorableObject> objectClass) {
        return storedObjects.containsKey(objectClass);
    }

    @Override
    public <T extends StorableObject> @Nullable T remove(Class<T> objectClass) {
        final StorableObject object = storedObjects.remove(objectClass);
        if (object != null) {
            object.onRemove();
        }
        return (T) object;
    }

    @Override
    public void put(StorableObject object) {
        final StorableObject previousObject = storedObjects.put(object.getClass(), object);
        if (previousObject != null) {
            previousObject.onRemove();
        }
    }

    @Override
    public Collection<EntityTracker> getEntityTrackers() {
        return entityTrackers.values();
    }

    @Override
    public @Nullable <T extends EntityTracker> T getEntityTracker(Class<? extends Protocol> protocolClass) {
        return (T) entityTrackers.get(protocolClass);
    }

    @Override
    public void addEntityTracker(Class<? extends Protocol> protocolClass, EntityTracker tracker) {
        if (!entityTrackers.containsKey(protocolClass)) {
            entityTrackers.put(protocolClass, tracker);
        }
    }

    @Override
    public void clearStoredObjects(boolean isServerSwitch) {
        if (isServerSwitch) {
            storedObjects.values().removeIf(storableObject -> {
                if (storableObject.clearOnServerSwitch()) {
                    storableObject.onRemove();
                    return true;
                }
                return false;
            });
            for (EntityTracker tracker : entityTrackers.values()) {
                tracker.clearEntities();
                tracker.trackClientEntity();
            }
        } else {
            for (StorableObject object : storedObjects.values()) {
                object.onRemove();
            }
            storedObjects.clear();
            entityTrackers.clear();
        }
    }

    @Override
    public void sendRawPacket(ByteBuf packet) {
        sendRawPacket(packet, true);
    }

    @Override
    public void scheduleSendRawPacket(ByteBuf packet) {
        sendRawPacket(packet, false);
    }

    private void sendRawPacket(final ByteBuf packet, boolean currentThread) {
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
            // Assume that decoder isn't wrapping
            getChannel().pipeline().context(Via.getManager().getInjector().getDecoderName()).fireChannelRead(packet);
            return getChannel().newSucceededFuture();
        } else {
            return channel.pipeline().context(Via.getManager().getInjector().getEncoderName()).writeAndFlush(packet);
        }
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
    public void sendRawPacketToServer(ByteBuf packet) {
        if (clientSide) {
            sendRawPacketToServerClientSide(packet, true);
        } else {
            sendRawPacketToServerServerSide(packet, true);
        }
    }

    @Override
    public void scheduleSendRawPacketToServer(ByteBuf packet) {
        if (clientSide) {
            sendRawPacketToServerClientSide(packet, false);
        } else {
            sendRawPacketToServerServerSide(packet, false);
        }
    }

    private void sendRawPacketToServerServerSide(final ByteBuf packet, boolean currentThread) {
        final ByteBuf buf = packet.alloc().buffer();
        try {
            // We'll use passing through because there are some encoder wrappers
            ChannelHandlerContext context = PipelineUtil
                    .getPreviousContext(Via.getManager().getInjector().getDecoderName(), channel.pipeline());

            if (shouldTransformPacket()) {
                // Bypass serverbound packet decoder transforming
                try {
                    Type.VAR_INT.writePrimitive(buf, PacketWrapper.PASSTHROUGH_ID);
                    Type.UUID.write(buf, generatePassthroughToken());
                } catch (Exception shouldNotHappen) {
                    throw new RuntimeException(shouldNotHappen);
                }
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
        if (pendingDisconnect) {
            return false;
        }
        // Increment received + Check PPS
        return !packetLimiterEnabled || !packetTracker.incrementReceived() || !packetTracker.exceedsMaxPPS();
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

        PacketWrapper wrapper = new PacketWrapperImpl(id, buf, this);
        State state = protocolInfo.getState(direction);
        try {
            protocolInfo.getPipeline().transform(direction, state, wrapper);
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
    public Map<Class<?>, StorableObject> getStoredObjects() {
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
    public boolean isPacketLimiterEnabled() {
        return packetLimiterEnabled;
    }

    @Override
    public void setPacketLimiterEnabled(boolean packetLimiterEnabled) {
        this.packetLimiterEnabled = packetLimiterEnabled;
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
