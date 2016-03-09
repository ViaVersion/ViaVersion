package us.myles.ViaVersion;

import org.bukkit.scheduler.BukkitRunnable;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.util.Map;
import java.util.UUID;

public class ViaIdleThread extends BukkitRunnable {
    private final Map<UUID, ConnectionInfo> portedPlayers;
    private final Class<?> idlePacketClass;

    public ViaIdleThread(Map<UUID, ConnectionInfo> portedPlayers) {
        this.portedPlayers = portedPlayers;
        try {
            this.idlePacketClass = ReflectionUtil.nms("PacketPlayInFlying");
        } catch(ClassNotFoundException e) {
            throw new RuntimeException("Couldn't find player idle packet, help!", e);
        }
    }

    @Override
    public void run() {
        for(ConnectionInfo info : portedPlayers.values()) {
            long nextIdleUpdate = info.getNextIdlePacket();
            if(nextIdleUpdate <= System.currentTimeMillis()) {
                try {
                    Object packet = idlePacketClass.newInstance();
                    info.getChannel().pipeline().fireChannelRead(packet);
                } catch(InstantiationException | IllegalAccessException e) {
                    System.out.println("Failed to create idle packet.");
                    if(ViaVersion.getInstance().isDebug()) {
                        e.printStackTrace();
                    }
                } finally {
                    info.incrementIdlePacket();
                }
            }
        }
    }
}
