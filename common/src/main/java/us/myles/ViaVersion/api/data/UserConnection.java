package us.myles.ViaVersion.api.data;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import lombok.NonNull;
import net.md_5.bungee.api.ChatColor;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.ViaVersionConfig;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.util.PipelineUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class UserConnection {
    @NonNull
    private final Channel channel;
    Map<Class, StoredObject> storedObjects = new ConcurrentHashMap<>();
    private boolean active = true;
    private boolean pendingDisconnect = false;
    private Object lastPacket;
    private long sentPackets = 0L;
    private long receivedPackets = 0L;
    // Used for tracking pps
    private long startTime = 0L;
    private long intervalPackets = 0L;
    private long packetsPerSecond = -1L;
    // Used for handling warnings (over time)
    private int secondsObserved = 0;
    private int warnings = 0;


    public UserConnection(Channel channel) {
        this.channel = channel;
    }

    /**
     * Get an object from the storage
     *
     * @param objectClass The class of the object to get
     * @param <T>         The type of the class you want to get.
     * @return The requested object
     */
    public <T extends StoredObject> T get(Class<T> objectClass) {
        return (T) storedObjects.get(objectClass);
    }

    /**
     * Check if the storage has an object
     *
     * @param objectClass The object class to check
     * @return True if the object is in the storage
     */
    public boolean has(Class<? extends StoredObject> objectClass) {
        return storedObjects.containsKey(objectClass);
    }

    /**
     * Put an object into the stored objects based on class
     *
     * @param object The object to store.
     */
    public void put(StoredObject object) {
        storedObjects.put(object.getClass(), object);
    }

    /**
     * Clear all the stored objects
     * Used for bungee when switching servers.
     */
    public void clearStoredObjects() {
        storedObjects.clear();
    }

    /**
     * Send a raw packet to the player
     *
     * @param packet        The raw packet to send
     * @param currentThread Should it run in the same thread
     */
    public void sendRawPacket(final ByteBuf packet, boolean currentThread) {
        final ChannelHandler handler = channel.pipeline().get(Via.getManager().getInjector().getEncoderName());
        if (currentThread) {
            channel.pipeline().context(handler).writeAndFlush(packet);
        } else {
            channel.eventLoop().submit(new Runnable() {
                @Override
                public void run() {
                    channel.pipeline().context(handler).writeAndFlush(packet);
                }
            });
        }
    }

    /**
     * Send a raw packet to the player with returning the future
     *
     * @param packet The raw packet to send
     * @return ChannelFuture of the packet being sent
     */
    public ChannelFuture sendRawPacketFuture(final ByteBuf packet) {
        final ChannelHandler handler = channel.pipeline().get(Via.getManager().getInjector().getEncoderName());
        return channel.pipeline().context(handler).writeAndFlush(packet);
    }

    /**
     * Send a raw packet to the player (netty thread)
     *
     * @param packet The packet to send
     */
    public void sendRawPacket(final ByteBuf packet) {
        sendRawPacket(packet, false);
    }

    /**
     * Used for incrementing the number of packets sent to the client
     */
    public void incrementSent() {
        this.sentPackets++;
    }

    /**
     * Used for incrementing the number of packets received from the client
     *
     * @return True if the interval has reset
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

    public boolean handlePPS() {
        ViaVersionConfig conf = Via.getConfig();
        // Max PPS Checker
        if (conf.getMaxPPS() > 0) {
            if (getPacketsPerSecond() >= conf.getMaxPPS()) {
                disconnect(conf.getMaxPPSKickMessage().replace("%pps", Long.toString(getPacketsPerSecond())));
                return true; // don't send current packet
            }
        }

        // Tracking PPS Checker
        if (conf.getMaxWarnings() > 0 && conf.getTrackingPeriod() > 0) {
            if (getSecondsObserved() > conf.getTrackingPeriod()) {
                // Reset
                setWarnings(0);
                setSecondsObserved(1);
            } else {
                setSecondsObserved(getSecondsObserved() + 1);
                if (getPacketsPerSecond() >= conf.getWarningPPS()) {
                    setWarnings(getWarnings() + 1);
                }

                if (getWarnings() >= conf.getMaxWarnings()) {
                    disconnect(conf.getMaxWarningsKickMessage().replace("%pps", Long.toString(getPacketsPerSecond())));
                    return true; // don't send current packet
                }
            }
        }
        return false;
    }

    /**
     * Disconnect a connection
     *
     * @param reason The reason to use, not used if player is not active.
     */
    public void disconnect(final String reason) {
        if (!getChannel().isOpen()) return;
        if (pendingDisconnect) return;
        pendingDisconnect = true;
        if (get(ProtocolInfo.class).getUuid() != null) {
            final UUID uuid = get(ProtocolInfo.class).getUuid();
            Via.getPlatform().runSync(new Runnable() {
                @Override
                public void run() {
                    if (!Via.getPlatform().kickPlayer(uuid, ChatColor.translateAlternateColorCodes('&', reason))) {
                        getChannel().close(); // =)
                    }
                }
            });
        }

    }

    /**
     * Sends a raw packet to the server
     *
     * @param packet Raw packet to be sent
     * @param currentThread If {@code true} executes immediately, {@code false} submits a task to EventLoop
     */
    public void sendRawPacketToServer(final ByteBuf packet, boolean currentThread) {
        final ByteBuf buf = packet.alloc().buffer();
        try {
            Type.VAR_INT.write(buf, PacketWrapper.PASSTHROUGH_ID);
        } catch (Exception e) {
            // Should not happen
            Via.getPlatform().getLogger().warning("Type.VAR_INT.write thrown an exception: " + e);
        }
        buf.writeBytes(packet);
        packet.release();
        final ChannelHandlerContext context = PipelineUtil.getPreviousContext(Via.getManager().getInjector().getDecoderName(), getChannel().pipeline());
        if (currentThread) {
            if (context != null) {
                context.fireChannelRead(buf);
            } else {
                getChannel().pipeline().fireChannelRead(buf);
            }
        } else {
            channel.eventLoop().submit(new Runnable() {
                @Override
                public void run() {
                    if (context != null) {
                        context.fireChannelRead(buf);
                    } else {
                        getChannel().pipeline().fireChannelRead(buf);
                    }
                }
            });
        }
    }

    /**
     * Sends a raw packet to the server. It will submit a task to EventLoop
     *
     * @param packet Raw packet to be sent
     */
    public void sendRawPacketToServer(ByteBuf packet) { sendRawPacketToServer(packet, false); }
}
