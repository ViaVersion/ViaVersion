package us.myles.ViaVersion.api.data;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.socket.SocketChannel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
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


    public UserConnection(SocketChannel socketChannel) {
        this.channel = socketChannel;
    }

    public <T extends StoredObject> T get(Class<T> objectClass) {
        return (T) storedObjects.get(objectClass);
    }

    public <T extends StoredObject> boolean has(Class<T> objectClass) {
        return storedObjects.containsKey(objectClass);
    }

    public void put(StoredObject object) {
        storedObjects.put(object.getClass(), object);
    }

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

    public void sendRawPacket(final ByteBuf packet) {
        sendRawPacket(packet, false);
    }
}
