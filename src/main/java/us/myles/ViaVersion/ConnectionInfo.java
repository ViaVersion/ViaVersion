package us.myles.ViaVersion;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.socket.SocketChannel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.myles.ViaVersion.chunks.ChunkManager;
import us.myles.ViaVersion.packets.State;

@Getter
@Setter
public class ConnectionInfo {
    private static final long IDLE_PACKET_DELAY = 50L; // Update every 50ms (20tps)
    private static final long IDLE_PACKET_LIMIT = 20; // Max 20 ticks behind

    private final SocketChannel channel;
    private final ChunkManager chunkManager;
    private Object lastPacket;
    private java.util.UUID UUID;
    private State state = State.HANDSHAKE;
    private String openWindow;
    private int protocol = 0;
    private int compression = 0;
    private int entityID;
    private boolean active = true;
    private String username;
    private long nextIdlePacket = 0L;

    public ConnectionInfo(SocketChannel socketChannel) {
        this.channel = socketChannel;
        this.chunkManager = new ChunkManager(this);
    }

    public Player getPlayer() {
        return UUID == null ? null : Bukkit.getPlayer(UUID);
    }

    public void setActive(boolean active) {
        this.active = active;
        this.nextIdlePacket = System.currentTimeMillis() + IDLE_PACKET_DELAY; // Update every 50 ticks
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

    public void incrementIdlePacket() {
        // Notify of next update
        // Allow a maximum lag spike of 1 second (20 ticks/updates)
        this.nextIdlePacket = Math.max(nextIdlePacket + IDLE_PACKET_DELAY, System.currentTimeMillis() - IDLE_PACKET_DELAY * IDLE_PACKET_LIMIT);
    }
}
