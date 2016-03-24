package us.myles.ViaVersion.api.data;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.socket.SocketChannel;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserConnection {
    @Getter
    private final SocketChannel channel;
    Map<Class, StoredObject> storedObjects = new ConcurrentHashMap<>();
    @Getter
    @Setter
    private boolean active = true;
    @Getter
    @Setter
    private Object lastPacket;
    @Getter
    private long sentPackets = 0L;
    @Getter
    private long receivedPackets = 0L;


    public UserConnection(SocketChannel socketChannel) {
        this.channel = socketChannel;
    }

    /**
     * Get an object from the storage
     *
     * @param objectClass The class of the object to get
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
     */
    public void incrementReceived() {
        this.receivedPackets++;
    }
}
