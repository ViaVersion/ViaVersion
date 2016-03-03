package us.myles.ViaVersion;

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
    private int protocol = 0;
    private int compression = 0;
    private boolean active = true;

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

    public void setCompression(int compression) {
        this.compression = compression;
    }

    public int getCompression() {
        return compression;
    }

    public void setLastPacket(Object lastPacket) {
        this.lastPacket = lastPacket;
    }

    public Object getLastPacket() {
        return lastPacket;
    }

    public void setUUID(UUID UUID) {
        this.UUID = UUID;
    }

    public java.util.UUID getUUID() {
        return UUID;
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
}
