package us.myles.ViaVersion;

import org.bukkit.scheduler.BukkitRunnable;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9TO1_8;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.MovementTracker;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.util.Map;
import java.util.UUID;

public class ViaIdleThread extends BukkitRunnable {
    private final Map<UUID, UserConnection> portedPlayers;
    private final Class<?> idlePacketClass;

    public ViaIdleThread(Map<UUID, UserConnection> portedPlayers) {
        this.portedPlayers = portedPlayers;
        try {
            this.idlePacketClass = ReflectionUtil.nms("PacketPlayInFlying");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Couldn't find player idle packet, help!", e);
        }
    }

    @Override
    public void run() {
        for (UserConnection info : portedPlayers.values()) {
            if (info.get(ProtocolInfo.class).getPipeline().contains(Protocol1_9TO1_8.class)) {
                long nextIdleUpdate = info.get(MovementTracker.class).getNextIdlePacket();
                if (nextIdleUpdate <= System.currentTimeMillis()) {
                    try {
                        Object packet = idlePacketClass.newInstance();
                        info.getChannel().pipeline().fireChannelRead(packet);
                    } catch (InstantiationException | IllegalAccessException e) {
                        System.out.println("Failed to create idle packet.");
                        if (ViaVersion.getInstance().isDebug()) {
                            e.printStackTrace();
                        }
                    } finally {
                        info.get(MovementTracker.class).incrementIdlePacket();
                    }
                }
            }
        }
    }
}
