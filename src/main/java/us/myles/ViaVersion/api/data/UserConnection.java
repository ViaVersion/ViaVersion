package us.myles.ViaVersion.api.data;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.socket.SocketChannel;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class UserConnection {
    private final SocketChannel channel;
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


    public UserConnection(SocketChannel socketChannel) {
        this.channel = socketChannel;
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
     * Send a raw packet to the player
     *
     * @param packet        The raw packet to send
     * @param currentThread Should it run in the same thread
     */
    public void sendRawPacket(final ByteBuf packet, boolean currentThread) {
        final ChannelHandler handler = channel.pipeline().get("encoder");
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
        final ChannelHandler handler = channel.pipeline().get("encoder");
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
        Long diff = System.currentTimeMillis() - startTime;
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
            if (Bukkit.getPlayer(uuid) != null) {
                Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("ViaVersion"), new Runnable() {
                    @Override
                    public void run() {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null)
                            player.kickPlayer(ChatColor.translateAlternateColorCodes('&', reason));
                    }
                });
                return;
            }
        }
        getChannel().close(); // =)
    }
}
