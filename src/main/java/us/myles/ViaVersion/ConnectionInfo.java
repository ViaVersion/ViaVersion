package us.myles.ViaVersion;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.socket.SocketChannel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.myles.ViaVersion.packets.State;

@Getter
@Setter
public class ConnectionInfo {
    private final SocketChannel channel;
    private Object lastPacket;
    private java.util.UUID UUID;
    private State state = State.HANDSHAKE;
    private String openWindow;
    private int protocol = 0;
    private int compression = 0;
    private int entityID;
    private boolean active = true;
    private String username;

    public ConnectionInfo(SocketChannel socketChannel) {
        this.channel = socketChannel;
    }

    public Player getPlayer() {
        return UUID == null ? null : Bukkit.getPlayer(UUID);
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public void closeWindow() {
        this.openWindow = null;
    }
}
