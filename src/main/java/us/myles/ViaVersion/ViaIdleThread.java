package us.myles.ViaVersion;

import org.bukkit.scheduler.BukkitRunnable;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.util.Map;
import java.util.UUID;

public class ViaIdleThread extends BukkitRunnable {
    private final Map<UUID, ConnectionInfo> portedPlayers;
    private final Object idlePacket;

    public ViaIdleThread(Map<UUID, ConnectionInfo> portedPlayers) {
        this.portedPlayers = portedPlayers;
        try {
            Class<?> idlePacketClass = ReflectionUtil.nms("PacketPlayInFlying");
            idlePacket = idlePacketClass.newInstance();
        } catch (InstantiationException | IllegalArgumentException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException("Couldn't find/make player idle packet, help!", e);
        }
    }

    @Override
    public void run() {
        for (ConnectionInfo info : portedPlayers.values()) {
            long nextIdleUpdate = info.getNextIdlePacket();
            if (nextIdleUpdate <= System.currentTimeMillis()) {
                info.getChannel().pipeline().fireChannelRead(idlePacket);
                info.incrementIdlePacket();
            }
        }
    }
}
