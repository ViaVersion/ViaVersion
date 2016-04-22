package us.myles.ViaVersion.protocols.protocol1_9to1_8;

import io.netty.channel.ChannelHandlerContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.MovementTracker;
import us.myles.ViaVersion.util.PipelineUtil;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

public class ViaIdleThread extends BukkitRunnable {
    private static boolean USE_NMS = true;
    private final Map<UUID, UserConnection> portedPlayers;
    // Used for packet mode
    private Object idlePacket;
    private Object idlePacket2;
    // Use for nms
    private Method getHandle;
    private Field connection;
    private Method handleFlying;

    public ViaIdleThread(Map<UUID, UserConnection> portedPlayers) {
        USE_NMS = ((ViaVersionPlugin) ViaVersion.getInstance()).getConfig().getBoolean("nms-player-ticking", true);

        this.portedPlayers = portedPlayers;
        Class<?> idlePacketClass;
        try {
            idlePacketClass = ReflectionUtil.nms("PacketPlayInFlying");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Couldn't find idle packet, help!", e);
        }
        try {
            idlePacket = idlePacketClass.newInstance();
            idlePacket2 = idlePacketClass.newInstance();

            Field flying = idlePacketClass.getDeclaredField("f");
            flying.setAccessible(true);

            flying.set(idlePacket2, true);
        } catch (NoSuchFieldException | InstantiationException | IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException("Couldn't make player idle packet, help!", e);
        }
        if (USE_NMS) {
            try {
                getHandle = ReflectionUtil.obc("entity.CraftPlayer").getDeclaredMethod("getHandle");
            } catch (NoSuchMethodException | ClassNotFoundException e) {
                throw new RuntimeException("Couldn't find CraftPlayer", e);
            }

            try {
                connection = ReflectionUtil.nms("EntityPlayer").getDeclaredField("playerConnection");
            } catch (NoSuchFieldException | ClassNotFoundException e) {
                throw new RuntimeException("Couldn't find Player Connection", e);
            }

            try {
                handleFlying = ReflectionUtil.nms("PlayerConnection").getDeclaredMethod("a", idlePacketClass);
            } catch (NoSuchMethodException | ClassNotFoundException e) {
                throw new RuntimeException("Couldn't find CraftPlayer", e);
            }
        }
    }

    @Override
    public void run() {
        for (UserConnection info : portedPlayers.values()) {
            if (info.get(ProtocolInfo.class).getPipeline().contains(Protocol1_9TO1_8.class)) {
                long nextIdleUpdate = info.get(MovementTracker.class).getNextIdlePacket();
                if (nextIdleUpdate <= System.currentTimeMillis()) {
                    if (info.getChannel().isOpen()) {
                        if (USE_NMS) {
                            Player player = Bukkit.getPlayer(info.get(ProtocolInfo.class).getUuid());
                            if (player != null) {
                                try {
                                    // Tick player
                                    Object entityPlayer = getHandle.invoke(player);
                                    Object pc = connection.get(entityPlayer);
                                    if (pc != null) {
                                        handleFlying.invoke(pc, (info.get(MovementTracker.class).isGround() ? idlePacket2 : idlePacket));
                                        // Tick world
                                        info.get(MovementTracker.class).incrementIdlePacket();
                                    }
                                } catch (IllegalAccessException | InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            // Old method using packets.
                            ChannelHandlerContext context = PipelineUtil.getContextBefore("decoder", info.getChannel().pipeline());
                            if (context != null) {
                                if (info.get(MovementTracker.class).isGround()) {
                                    context.fireChannelRead(idlePacket2);
                                } else {
                                    context.fireChannelRead(idlePacket);
                                }
                                info.get(MovementTracker.class).incrementIdlePacket();
                            }
                        }
                    }
                }
            }
        }
    }
}
