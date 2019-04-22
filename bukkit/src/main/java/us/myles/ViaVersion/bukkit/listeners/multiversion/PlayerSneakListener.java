package us.myles.ViaVersion.bukkit.listeners.multiversion;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;
import us.myles.ViaVersion.bukkit.listeners.ViaBukkitListener;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PlayerSneakListener extends ViaBukkitListener {
    private Method getHandle;
    private Method setSize;
    private boolean is1_9Fix;
    private boolean is1_14Fix;

    public PlayerSneakListener(ViaVersionPlugin plugin, boolean is1_9Fix, boolean is1_14Fix) {
        super(plugin, null);
        this.is1_9Fix = is1_9Fix;
        this.is1_14Fix = is1_14Fix;
        try {
            getHandle = Class.forName(plugin.getServer().getClass().getPackage().getName() + ".entity.CraftPlayer").getMethod("getHandle");
            setSize = Class.forName(plugin.getServer().getClass().getPackage().getName()
                    .replace("org.bukkit.craftbukkit", "net.minecraft.server") + ".EntityPlayer").getMethod("setSize", Float.TYPE, Float.TYPE);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        getPlugin().getServer().getScheduler().runTaskTimer(getPlugin(), new Runnable() {
            @Override
            public void run() {
                for (Player onlinePlayer : getPlugin().getServer().getOnlinePlayers()) {
                    try {
                        final Object handle = getHandle.invoke(onlinePlayer);
                        final Class<?> aClass = Class.forName(getPlugin().getServer().getClass().getPackage().getName()
                                .replace("org.bukkit.craftbukkit", "net.minecraft.server") + ".EntityPlayer");
                        System.out.println(aClass.getField("length").get(handle));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 1, 1);
    }

    @EventHandler(ignoreCancelled = true)
    public void playerToggleSneak(final PlayerToggleSneakEvent event) {
        final Player player = event.getPlayer();
        final int protocolVersion = getUserConnection(player).get(ProtocolInfo.class).getProtocolVersion();
        if (is1_14Fix && protocolVersion >= ProtocolVersion.v1_14.getId()) {
            setHight(player, event.isSneaking() ? 1.5F : 1.8F);
        } else if (is1_9Fix && protocolVersion >= ProtocolVersion.v1_9.getId()) {
            setHight(player, event.isSneaking() ? 1.6F : 1.8F);
        }
    }

    private void setHight(Player player, float hight) {
        try {
            setSize.invoke(getHandle.invoke(player), 0.6F, hight);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
