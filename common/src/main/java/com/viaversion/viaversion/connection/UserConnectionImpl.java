/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.ProtocolStorables;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.data.item.ItemHasher;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.platform.ViaInjector;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketTracker;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.VarIntType;
import com.viaversion.viaversion.platform.ViaPassthroughHandler;
import com.viaversion.viaversion.exception.CancelException;
import com.viaversion.viaversion.exception.InformativeException;
import com.viaversion.viaversion.protocol.packet.PacketWrapperImpl;
import com.viaversion.viaversion.util.ChatColorUtil;
import com.viaversion.viaversion.util.PipelineUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.util.AttributeKey;
import io.netty.handler.codec.CodecException;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.Nullable;

public class UserConnectionImpl implements UserConnection {
    public static final AttributeKey<UserConnection> CHANNEL_ATTRIBUTE = AttributeKey.valueOf("viaversion-user-connection");
    private static final int PASSTHROUGH_DATA_BYTES = Long.BYTES + 2;
    private static final AtomicLong IDS = new AtomicLong();
    private final long id = IDS.incrementAndGet();
    private final SplittableRandom random = new SplittableRandom();
    private final Map<Class<?>, StorableObject> storedObjects = new ConcurrentHashMap<>(8);
    private final ProtocolStorables[] protocolStorables = new ProtocolStorables[Via.getManager().getProtocolManager().registeredProtocolCount()];
    private final PacketTracker packetTracker = new PacketTracker(this);
    private final LongSet passthroughTokens = new LongOpenHashSet(4);
    private final ProtocolInfo protocolInfo = new ProtocolInfoImpl();
    private final Channel channel;
    private final boolean clientSide;
    private boolean active = true;
    private boolean pendingDisconnect;

    /**
     * Creates a UserConnection. When it's a client-side connection, some method behaviors are modified.
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
        final List<EntityTracker> trackers = new ArrayList<>();
        for (final ProtocolStorables storable : protocolStorables) {
            if (storable != null && storable.entityTracker() != null) {
                trackers.add(storable.entityTracker());
            }
        }
        return trackers;
    }

    @Override
    public @Nullable <T extends EntityTracker> T getEntityTracker(Class<? extends Protocol> protocolClass) {
        final ProtocolStorables storables = storablesByClassIfPresent(protocolClass);
        return storables != null ? (T) storables.entityTracker() : null;
    }

    @Override
    public void addEntityTracker(Class<? extends Protocol> protocolClass, EntityTracker tracker) {
        storables(protocolClass).setEntityTracker(tracker);
    }

    @Override
    public void addItemHasher(final Class<? extends Protocol> protocolClass, final ItemHasher itemHasher) {
        storables(protocolClass).setItemHasher(itemHasher);
    }

    @Override
    public @Nullable <T extends ItemHasher> T getItemHasher(Class<? extends Protocol> protocolClass) {
        final ProtocolStorables storables = storablesByClassIfPresent(protocolClass);
        return storables != null ? (T) storables.itemHasher() : null;
    }

    @Override
    public @Nullable <T extends ClientWorld> T getClientWorld(final Class<? extends Protocol> protocolClass) {
        final ProtocolStorables storables = storablesByClassIfPresent(protocolClass);
        return storables != null ? (T) storables.clientWorld() : null;
    }

    @Override
    public void addClientWorld(final Class<? extends Protocol> protocolClass, final ClientWorld clientWorld) {
        storables(protocolClass).setClientWorld(clientWorld);
    }

    private @Nullable ProtocolStorables storablesByClassIfPresent(final Class<? extends Protocol> protocolClass) {
        final int index = protocolIndex(protocolClass);
        return index >= 0 && index < protocolStorables.length ? protocolStorables[index] : null;
    }

    private static int protocolIndex(final Class<? extends Protocol> protocolClass) {
        final Protocol<?, ?, ?, ?> protocol = Via.getManager().getProtocolManager().getProtocol(protocolClass);
        return protocol != null ? protocol.index() : -1;
    }

    @Override
    public @Nullable <T extends EntityTracker> T getEntityTracker(final Protocol<?, ?, ?, ?> protocol) {
        final ProtocolStorables storables = storablesIfPresent(protocol);
        //noinspection unchecked
        return storables != null ? (T) storables.entityTracker() : null;
    }

    @Override
    public @Nullable <T extends ItemHasher> T getItemHasher(final Protocol<?, ?, ?, ?> protocol) {
        final ProtocolStorables storables = storablesIfPresent(protocol);
        //noinspection unchecked
        return storables != null ? (T) storables.itemHasher() : null;
    }

    @Override
    public <T extends ProtocolStorables> T storables(final Protocol<?, ?, ?, ?> protocol) {
        T storables = storablesIfPresent(protocol);
        if (storables == null) {
            //noinspection unchecked
            storables = (T) protocol.createStorables();
            protocolStorables[protocol.index()] = storables;
        }
        return storables;
    }

    public <T extends ProtocolStorables> T storablesIfPresent(final Protocol<?, ?, ?, ?> protocol) {
        final int index = protocol.index();
        if (index == -1) {
            throw new IllegalArgumentException("Provided protocol does not have a valid index: " + index);
        }
        //noinspection unchecked
        return (T) protocolStorables[index];
    }

    @Override
    public void clearStoredObjects() {
        for (StorableObject object : storedObjects.values()) {
            object.onRemove();
        }
        storedObjects.clear();
        Arrays.fill(protocolStorables, null);
    }

    @Override
    public void sendRawPacket(ByteBuf packet) {
        sendRawPacket(packet, true);
    }

    @Override
    public void scheduleSendRawPacket(ByteBuf packet) {
        sendRawPacket(packet, false);
    }

    private void sendRawPacket(final ByteBuf packet, final boolean currentThread) {
        if (currentThread) {
            sendRawPacketNow(packet);
        } else {
            try {
                channel.eventLoop().execute(() -> sendRawPacketNow(packet));
            } catch (Throwable e) {
                packet.release(); // Couldn't schedule
                e.printStackTrace();
            }
        }
    }

    private void sendRawPacketNow(final ByteBuf buf) {
        final ChannelPipeline pipeline = getChannel().pipeline();
        final ViaInjector injector = Via.getManager().getInjector();
        if (clientSide) {
            // We'll just assume that Via decoder isn't wrapping the original decoder
            pipeline.context(injector.getDecoderName()).fireChannelRead(buf);
        } else {
            pipeline.context(injector.getEncoderName()).writeAndFlush(buf);
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
        if (!channel.isOpen() || pendingDisconnect) {
            return;
        }

        pendingDisconnect = true;
        if (isServerSide()) {
            Via.getPlatform().runSync(() -> {
                if (!Via.getPlatform().kickPlayer(this, ChatColorUtil.translateAlternateColorCodes(reason))) {
                    channel.close();
                }
            });
        } else {
            channel.close();
        }
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

    private void sendRawPacketToServerServerSide(final ByteBuf packet, final boolean currentThread) {
        final int initialCapacity = active ? packet.readableBytes() + PASSTHROUGH_DATA_BYTES : packet.readableBytes();
        final ByteBuf buf = packet.alloc().buffer(initialCapacity);
        try {
            // We'll use passing through because there are some encoder wrappers
            ChannelHandlerContext context = PipelineUtil
                .getPreviousContext(Via.getManager().getInjector().getDecoderName(), channel.pipeline());

            if (shouldTransformPacket()) {
                // Bypass serverbound packet decoder transforming
                Types.VAR_INT.writePrimitive(buf, PacketWrapper.PASSTHROUGH_ID);
                Types.LONG.writePrimitive(buf, generatePassthroughToken());
            }

            buf.writeBytes(packet);
            if (currentThread) {
                fireChannelRead(context, buf);
            } else {
                try {
                    channel.eventLoop().execute(() -> fireChannelRead(context, buf));
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

    private void fireChannelRead(@Nullable final ChannelHandlerContext context, final ByteBuf buf) {
        if (context != null) {
            context.fireChannelRead(buf);
        } else {
            channel.pipeline().fireChannelRead(buf);
        }
    }

    private void sendRawPacketToServerClientSide(final ByteBuf packet, final boolean currentThread) {
        if (currentThread) {
            writeAndFlush(packet);
        } else {
            try {
                getChannel().eventLoop().execute(() -> writeAndFlush(packet));
            } catch (Throwable e) {
                e.printStackTrace();
                packet.release(); // Couldn't schedule
            }
        }
    }

    private void writeAndFlush(final ByteBuf buf) {
        getChannel().pipeline().context(Via.getManager().getInjector().getEncoderName()).writeAndFlush(buf);
    }

    @Override
    public boolean checkServerboundPacket(final int bytes) {
        if (pendingDisconnect) {
            return false; // Cancel everything while disconnecting
        }
        if (!packetTracker.isPacketLimiterEnabled()) {
            return true;
        }

        packetTracker.incrementReceived(bytes);
        return !packetTracker.exceedsLimits();
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
    public boolean shouldTransformPacket(final ByteBuf buf, final Direction direction) {
        if (!active) {
            return false;
        }
        if (!buf.isReadable()) {
            return false;
        }

        if (protocolInfo.getPipeline() == null) {
            return true;
        }

        final State state = protocolInfo.getState(direction);
        // InitialBaseProtocol.disable-on-bad-handshake lives in transform(); never skip that state.
        if (state == State.HANDSHAKE) {
            return true;
        }

        final int readerIndex = buf.readerIndex();
        final int packetId;
        try {
            packetId = Types.VAR_INT.readPrimitive(buf);
        } catch (final Exception e) {
            return true;
        } finally {
            buf.readerIndex(readerIndex);
        }
        return !protocolInfo.getPipeline().canPassthroughPacket(direction, state, packetId);
    }

    /**
     * Replaces encode/decode codecs with a named passthrough after native-protocol login.
     * Must not run inside the current encode; it is queued on the event loop.
     */
    public void scheduleIdentityPassthrough() {
        if (channel == null) {
            return;
        }
        channel.eventLoop().execute(this::installIdentityPassthrough);
    }

    private void installIdentityPassthrough() {
        if (channel == null || !channel.isOpen() || active) {
            return;
        }
        if (protocolInfo.getPipeline().hasNonBaseProtocols()) {
            return;
        }

        final ViaInjector injector = Via.getManager().getInjector();
        final ChannelPipeline pipeline = channel.pipeline();
        replaceWithPassthrough(pipeline, injector.getEncoderName());
        replaceWithPassthrough(pipeline, injector.getDecoderName());
    }

    private void replaceWithPassthrough(final ChannelPipeline pipeline, final String name) {
        if (pipeline.get(name) == null || pipeline.get(name) instanceof ViaPassthroughHandler) {
            return;
        }
        pipeline.replace(name, name, new ViaPassthroughHandler(this));
    }

    @Override
    public void transformClientbound(ByteBuf buf, Function<Throwable, CodecException> cancelSupplier) throws InformativeException, CodecException {
        transform(buf, Direction.CLIENTBOUND, cancelSupplier);
    }

    @Override
    public void transformServerbound(ByteBuf buf, Function<Throwable, CodecException> cancelSupplier) throws InformativeException, CodecException {
        transform(buf, Direction.SERVERBOUND, cancelSupplier);
    }

    private void transform(ByteBuf buf, Direction direction, Function<Throwable, CodecException> cancelSupplier) throws InformativeException, CodecException {
        if (!buf.isReadable()) {
            return;
        }

        final int id = Types.VAR_INT.readPrimitive(buf);
        if (id == PacketWrapper.PASSTHROUGH_ID && buf.readableBytes() >= Long.BYTES) {
            if (removePassthroughToken(buf.readLong())) {
                return;
            }

            // Move reader back and treat it like a regular packet
            buf.readerIndex(buf.readerIndex() - Long.BYTES);
        }

        final int valuesReaderIndex = buf.readerIndex();
        final PacketWrapperImpl wrapper = new PacketWrapperImpl(id, buf, this);
        try {
            protocolInfo.getPipeline().transform(direction, protocolInfo.getState(direction), wrapper);
        } catch (final CancelException ex) {
            throw cancelSupplier.apply(ex);
        }

        writeToBuffer(wrapper, buf, id, valuesReaderIndex);
    }

    private void writeToBuffer(final PacketWrapperImpl wrapper, final ByteBuf buf, final int originalId, final int originalReaderIndex) {
        final int remainingBytes = buf.readableBytes();
        if (buf.readerIndex() == originalReaderIndex && wrapper.areStoredPacketValuesEmpty()) {
            if (wrapper.getId() == originalId) {
                // No changes needed; just set the reader and writer indexes and we're done
                buf.setIndex(0, originalReaderIndex + remainingBytes);
                return;
            }
            if (VarIntType.varIntLength(wrapper.getId()) == VarIntType.varIntLength(originalId)) {
                // If the var int encoded length is the same, simply replace the id at the head
                buf.setIndex(0, 0);
                Types.VAR_INT.writePrimitive(buf, wrapper.getId());
                buf.writerIndex(originalReaderIndex + remainingBytes);
                return;
            }
        }

        // Instead of allocating a possible unnecessarily large buffer to write the wrapper contents to,
        // only allocate the remaining bytes and write the rest to the original buf's head directly.
        final ByteBuf remainingBuf = buf.alloc().buffer(remainingBytes, remainingBytes);
        try {
            // Copy before modifying the buffer
            remainingBuf.writeBytes(buf, remainingBytes);

            // Reset indexes, write wrapper contents, then the unread bytes
            buf.setIndex(0, 0);
            wrapper.writeProcessedValues(buf);
            buf.writeBytes(remainingBuf);
        } finally {
            remainingBuf.release();
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
    public long generatePassthroughToken() {
        // Don't use TLR so it's not entirely predictable; use SplittableRandom over Random since it actually has a 64 bit seed
        while (true) {
            synchronized (passthroughTokens) {
                final long token = random.nextLong();
                if (passthroughTokens.add(token)) {
                    return token;
                }
            }
        }
    }

    private boolean removePassthroughToken(final long token) {
        synchronized (passthroughTokens) {
            return passthroughTokens.remove(token);
        }
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
