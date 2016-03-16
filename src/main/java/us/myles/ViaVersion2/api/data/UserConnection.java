package us.myles.ViaVersion2.api.data;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.socket.SocketChannel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class UserConnection {
    List<StoredObject> storedObjects = new ArrayList<>();
    @Getter
    @Setter
    private boolean active = true;
    @Getter
    private final SocketChannel channel;
    @Getter
    @Setter
    private Object lastPacket;


    public UserConnection(SocketChannel socketChannel) {
        this.channel = socketChannel;
    }

    public <T extends StoredObject> T get(Class<T> objectClass) {
        for (StoredObject o : storedObjects) {
            if (o.getClass().equals(objectClass))
                return (T) o;
        }
        return null;
    }

    public <T extends StoredObject> boolean has(Class<T> objectClass) {
        for (StoredObject o : storedObjects) {
            if (o.getClass().equals(objectClass))
                return true;
        }
        return false;
    }

    public void put(StoredObject object) {
        storedObjects.add(object);
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
