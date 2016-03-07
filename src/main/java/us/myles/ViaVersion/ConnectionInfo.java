package us.myles.ViaVersion;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.myles.ViaVersion.packets.State;

import java.util.UUID;

public class ConnectionInfo {
    private final SocketChannel channel;
    private Object lastPacket;
    private java.util.UUID UUID;
    private State state = State.HANDSHAKE;
    private String openWindow;
    private int protocol = 0;
    private int compression = 0;
    private boolean active = true;
    private String username;

    public ConnectionInfo(SocketChannel socketChannel) {
        this.channel = socketChannel;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public int getCompression() {
        return compression;
    }

    public void setCompression(int compression) {
        this.compression = compression;
    }

    public Object getLastPacket() {
        return lastPacket;
    }

    public void setLastPacket(Object lastPacket) {
        this.lastPacket = lastPacket;
    }

    public java.util.UUID getUUID() {
        return UUID;
    }

    public void setUUID(UUID UUID) {
        this.UUID = UUID;
    }

    public Player getPlayer() {
        return UUID == null ? null : Bukkit.getPlayer(UUID);
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void sendRawPacket(final ByteBuf packet) {
        final ChannelHandler handler = channel.pipeline().get("encoder");
        channel.eventLoop().submit(new Runnable() {
            @Override
            public void run() {
                channel.pipeline().context(handler).writeAndFlush(packet);
            }
        });
    }

    public String getOpenWindow() {
        return openWindow;
    }

    public void setOpenWindow(String openWindow) {
        this.openWindow = openWindow;
    }

    public void closeWindow() {
        this.openWindow = null;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
