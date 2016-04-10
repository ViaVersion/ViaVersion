package us.myles.ViaVersion;

import io.netty.channel.ChannelHandlerContext;
import org.bukkit.scheduler.BukkitRunnable;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9TO1_8;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.MovementTracker;
import us.myles.ViaVersion.util.PipelineUtil;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.util.Map;
import java.util.UUID;

public class ViaIdleThread extends BukkitRunnable {
    private final Map<UUID, UserConnection> portedPlayers;
    private final Object idlePacket;

    public ViaIdleThread(Map<UUID, UserConnection> portedPlayers) {
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
        for (UserConnection info : portedPlayers.values()) {
            if (info.get(ProtocolInfo.class).getPipeline().contains(Protocol1_9TO1_8.class)) {
                long nextIdleUpdate = info.get(MovementTracker.class).getNextIdlePacket();
                if (nextIdleUpdate <= System.currentTimeMillis()) {
                    ChannelHandlerContext context = PipelineUtil.getContextBefore("decoder", info.getChannel().pipeline());
                    if (info.getChannel().isOpen()) {
                        if (context != null) {
                            context.fireChannelRead(idlePacket);

                            info.get(MovementTracker.class).incrementIdlePacket();
                        }
                    }
                }
            }
        }
    }
}
