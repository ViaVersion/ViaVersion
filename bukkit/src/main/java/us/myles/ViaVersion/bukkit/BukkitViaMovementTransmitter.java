package us.myles.ViaVersion.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.MovementTransmitterProvider;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.MovementTracker;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BukkitViaMovementTransmitter extends MovementTransmitterProvider {
    private static boolean USE_NMS = true;
    // Used for packet mode
    private Object idlePacket;
    private Object idlePacket2;
    // Use for nms
    private Method getHandle;
    private Field connection;
    private Method handleFlying;

    public BukkitViaMovementTransmitter() {
        USE_NMS = Via.getConfig().isNMSPlayerTicking();

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
    public Object getFlyingPacket() {
        return idlePacket2;
    }

    @Override
    public Object getGroundPacket() {
        return idlePacket;
    }

    @Override
    public void sendPlayer(UserConnection info) {
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
            super.sendPlayer(info);
        }
    }
}
