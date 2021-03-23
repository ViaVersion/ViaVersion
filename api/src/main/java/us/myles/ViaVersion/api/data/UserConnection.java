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

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import org.jetbrains.annotations.Nullable;
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class UserConnection {
    private static final AtomicLong IDS = new AtomicLong();
    private final long id = IDS.incrementAndGet();
    private final Channel channel;
    private final boolean clientSide;
    Map<Class, StoredObject> storedObjects = new ConcurrentHashMap<>();
    private ProtocolInfo protocolInfo;
    private boolean active = true;
    private boolean pendingDisconnect;
    private Object lastPacket;
    private long sentPackets;
    private long receivedPackets;
    // Used for tracking pps
    private long startTime;
    private long intervalPackets;
    private long packetsPerSecond = -1L;
    // Used for handling warnings (over time)
    private int secondsObserved;
    private int warnings;

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
    @Nullable
    public <T extends StoredObject> T get(Class<T> objectClass) {
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
     * Used for incrementing the number of packets sent to the client.
     */
    public void incrementSent() {
        this.sentPackets++;
    }

    /**
     * Used for incrementing the number of packets received from the client.
     *
     * @return true if the interval has reset and can now be checked for the packets sent
     */
    public boolean incrementReceived() {
        // handle stats
        long diff = System.currentTimeMillis() - startTime;
        if (diff >= 1000) {
            packetsPerSecond = intervalPackets;
            startTime = System.currentTimeMillis();
            intervalPackets = 1;
            return true;
        } else {
            intervalPackets++;
        }
        // increase total
        this.receivedPackets++;
        return false;
    }

    /**
     * Checks for packet flood with the packets sent in the last second.
     * ALWAYS check for {@link #incrementReceived()} before using this method.
     *
     * @return true if the packet should be cancelled
     * @see #incrementReceived()
     */
    public boolean exceedsMaxPPS() {
        if (clientSide) return false; // Don't apply PPS limiting for client-side
        ViaVersionConfig conf = Via.getConfig();
        // Max PPS Checker
        if (conf.getMaxPPS() > 0) {
            if (packetsPerSecond >= conf.getMaxPPS()) {
                disconnect(conf.getMaxPPSKickMessage().replace("%pps", Long.toString(packetsPerSecond)));
                return true; // don't send current packet
            }
        }

        // Tracking PPS Checker
        if (conf.getMaxWarnings() > 0 && conf.getTrackingPeriod() > 0) {
            if (secondsObserved > conf.getTrackingPeriod()) {
                // Reset
                warnings = 0;
                secondsObserved = 1;
            } else {
                secondsObserved++;
                if (packetsPerSecond >= conf.getWarningPPS()) {
                    warnings++;
                }

                if (warnings >= conf.getMaxWarnings()) {
                    disconnect(conf.getMaxWarningsKickMessage().replace("%pps", Long.toString(packetsPerSecond)));
                    return true; // don't send current packet
                }
            }
        }
        return false;
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
            } catch (Exception e) {
                // Should not happen
                Via.getPlatform().getLogger().warning("Type.VAR_INT.write thrown an exception: " + e);
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
            return checkClientBound();
        } else {
            return checkServerBound();
        }
    }

    private boolean checkClientBound() {
        incrementSent();
        return true;
    }

    private boolean checkServerBound() {
        // Ignore if pending disconnect
        if (pendingDisconnect) return false;
        // Increment received + Check PPS
        return !incrementReceived() || !exceedsMaxPPS();
    }

    /**
     * Monitors outgoing packets
     *
     * @return false if this packet should be cancelled
     */
    public boolean checkOutgoingPacket() {
        if (clientSide) {
            return checkServerBound();
        } else {
            return checkClientBound();
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
        if (id == PacketWrapper.PASSTHROUGH_ID) return;

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

    public long getId() {
        return id;
    }

    @Nullable
    public Channel getChannel() {
        return channel;
    }

    @Nullable
    public ProtocolInfo getProtocolInfo() {
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

    public Map<Class, StoredObject> getStoredObjects() {
        return storedObjects;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isPendingDisconnect() {
        return pendingDisconnect;
    }

    public void setPendingDisconnect(boolean pendingDisconnect) {
        this.pendingDisconnect = pendingDisconnect;
    }

    @Nullable
    public Object getLastPacket() {
        return lastPacket;
    }

    public void setLastPacket(@Nullable Object lastPacket) {
        this.lastPacket = lastPacket;
    }

    public long getSentPackets() {
        return sentPackets;
    }

    public void setSentPackets(long sentPackets) {
        this.sentPackets = sentPackets;
    }

    public long getReceivedPackets() {
        return receivedPackets;
    }

    public void setReceivedPackets(long receivedPackets) {
        this.receivedPackets = receivedPackets;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getIntervalPackets() {
        return intervalPackets;
    }

    public void setIntervalPackets(long intervalPackets) {
        this.intervalPackets = intervalPackets;
    }

    public long getPacketsPerSecond() {
        return packetsPerSecond;
    }

    public void setPacketsPerSecond(long packetsPerSecond) {
        this.packetsPerSecond = packetsPerSecond;
    }

    public int getSecondsObserved() {
        return secondsObserved;
    }

    public void setSecondsObserved(int secondsObserved) {
        this.secondsObserved = secondsObserved;
    }

    public int getWarnings() {
        return warnings;
    }

    public void setWarnings(int warnings) {
        this.warnings = warnings;
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

    public boolean isClientSide() {
        return clientSide;
    }

    public boolean shouldApplyBlockProtocol() {
        return !clientSide; // Don't apply protocol blocking on client-side
    }
}
