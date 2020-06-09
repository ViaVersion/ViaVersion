package us.myles.ViaVersion.api.data;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.Nullable;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.ViaVersionConfig;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.exception.CancelException;
import us.myles.ViaVersion.packets.Direction;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.util.PipelineUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class UserConnection {
    private static final AtomicLong IDS = new AtomicLong();
    private final long id = IDS.incrementAndGet();
    private final Channel channel;
    private ProtocolInfo protocolInfo;
    Map<Class, StoredObject> storedObjects = new ConcurrentHashMap<>();
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

    public UserConnection(@Nullable Channel channel) {
        this.channel = channel;
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
    public void sendRawPacket(ByteBuf packet, boolean currentThread) {
        ChannelHandler handler = channel.pipeline().get(Via.getManager().getInjector().getEncoderName());
        if (currentThread) {
            channel.pipeline().context(handler).writeAndFlush(packet);
        } else {
            channel.eventLoop().submit(() -> channel.pipeline().context(handler).writeAndFlush(packet));
        }
    }

    /**
     * Send a raw packet to the player with returning the future.
     *
     * @param packet The raw packet to send
     * @return ChannelFuture of the packet being sent
     */
    public ChannelFuture sendRawPacketFuture(ByteBuf packet) {
        ChannelHandler handler = channel.pipeline().get(Via.getManager().getInjector().getEncoderName());
        return channel.pipeline().context(handler).writeAndFlush(packet);
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
        UUID uuid = protocolInfo.getUuid();
        if (uuid == null) {
            channel.close(); // Just disconnect, we don't know what the connection is
            return;
        }

        Via.getPlatform().runSync(() -> {
            if (!Via.getPlatform().kickPlayer(uuid, ChatColor.translateAlternateColorCodes('&', reason))) {
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
    public void sendRawPacketToServer(ByteBuf packet, boolean currentThread) {
        ByteBuf buf = packet.alloc().buffer();
        try {
            try {
                Type.VAR_INT.write(buf, PacketWrapper.PASSTHROUGH_ID);
            } catch (Exception e) {
                // Should not happen
                Via.getPlatform().getLogger().warning("Type.VAR_INT.write thrown an exception: " + e);
            }
            buf.writeBytes(packet);
            ChannelHandlerContext context = PipelineUtil
                    .getPreviousContext(Via.getManager().getInjector().getDecoderName(), channel.pipeline());
            if (currentThread) {
                if (context != null) {
                    context.fireChannelRead(buf);
                } else {
                    channel.pipeline().fireChannelRead(buf);
                }
            } else {
                try {
                    channel.eventLoop().submit(() -> {
                        if (context != null) {
                            context.fireChannelRead(buf);
                        } else {
                            channel.pipeline().fireChannelRead(buf);
                        }
                    });
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

    /**
     * Sends a raw packet to the server. It will submit a task to EventLoop.
     *
     * @param packet Raw packet to be sent
     */
    public void sendRawPacketToServer(ByteBuf packet) {
        sendRawPacketToServer(packet, false);
    }

    /**
     * Monitors serverbound packets.
     *
     * @return false if this packet should be cancelled
     */
    public boolean checkIncomingPacket() {
        // Ignore if pending disconnect
        if (pendingDisconnect) return false;
        // Increment received + Check PPS
        return !incrementReceived() || !exceedsMaxPPS();
    }

    /**
     * Monitors clientbound packets.
     */
    public void checkOutgoingPacket() {
        incrementSent();
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
     * Transforms the clientbound packet contained in an outgoing ByteBuf.
     *
     * @param buf            ByteBuf with packet id and packet contents
     * @param cancelSupplier Function called with original CancelException for generating the Exception used when
     *                       packet is cancelled
     * @throws Exception when transforming failed or this packet is cancelled
     */
    public void transformOutgoing(ByteBuf buf, Function<Throwable, Exception> cancelSupplier) throws Exception {
        if (!buf.isReadable()) return;
        transform(buf, Direction.OUTGOING, cancelSupplier);
    }

    /**
     * Transforms the serverbound packet contained in an incoming ByteBuf.
     *
     * @param buf            ByteBuf with packet id and packet contents
     * @param cancelSupplier Function called with original CancelException for generating the Exception used when
     *                       packet is cancelled
     * @throws Exception when transforming failed or this packet is cancelled
     */
    public void transformIncoming(ByteBuf buf, Function<Throwable, Exception> cancelSupplier) throws Exception {
        if (!buf.isReadable()) return;
        transform(buf, Direction.INCOMING, cancelSupplier);
    }

    private void transform(ByteBuf buf, Direction direction, Function<Throwable, Exception> cancelSupplier) throws Exception {
        int id = Type.VAR_INT.read(buf);
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
}
