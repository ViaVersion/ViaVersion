package us.myles.ViaVersion;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.api.ViaVersionAPI;
import us.myles.ViaVersion.armor.ArmorListener;
import us.myles.ViaVersion.commands.ViaVersionCommand;
import us.myles.ViaVersion.handlers.ViaVersionInitializer;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ViaVersionPlugin extends JavaPlugin implements ViaVersionAPI {

    private final Map<UUID, ConnectionInfo> portedPlayers = new ConcurrentHashMap<UUID, ConnectionInfo>();

    @Override
    public void onEnable() {
        ViaVersion.setInstance(this);
        if (System.getProperty("ViaVersion") != null) {
            getLogger().severe("ViaVersion is already loaded, we don't support reloads. Please reboot if you wish to update.");
            return;
        }

        getLogger().info("ViaVersion " + getDescription().getVersion() + " is now enabled, injecting. (Allows 1.8 to be accessed via 1.9)");
        try {
            injectPacketHandler();
            System.setProperty("ViaVersion", getDescription().getVersion());
        } catch (Exception e) {
            getLogger().severe("Unable to inject handlers, are you on 1.8? ");
            e.printStackTrace();
        }

        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPlayerQuit(PlayerQuitEvent e) {
                removePortedClient(e.getPlayer().getUniqueId());
            }
        }, this);

        Bukkit.getPluginManager().registerEvents(new ArmorListener(this), this);

        getCommand("viaversion").setExecutor(new ViaVersionCommand());
    }

    public void injectPacketHandler() throws Exception {
        Class<?> serverClazz = ReflectionUtil.nms("MinecraftServer");
        Object server = ReflectionUtil.invokeStatic(serverClazz, "getServer");
        Object connection = serverClazz.getDeclaredMethod("getServerConnection").invoke(server);
        // loop through all fields checking if list
        boolean injected = false;
        for (Field field : connection.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Object value = field.get(connection);
            if (value instanceof List) {
                for (Object o : (List) value) {
                    if (o instanceof ChannelFuture) {
                        ChannelFuture future = (ChannelFuture) o;
                        ChannelPipeline pipeline = future.channel().pipeline();
                        ChannelHandler bootstrapAcceptor = pipeline.first();
                        ChannelInitializer<SocketChannel> oldInit = ReflectionUtil.get(bootstrapAcceptor, "childHandler", ChannelInitializer.class);
                        ChannelInitializer newInit = new ViaVersionInitializer(oldInit);
                        ReflectionUtil.set(bootstrapAcceptor, "childHandler", newInit);
                        injected = true;
                    } else {
                        break; // not the right list.
                    }
                }
            }
        }
        if (!injected) {
            throw new Exception("Could not find server to inject (Please ensure late-bind in your spigot.yml is false)");
        }
    }

    @Override
    public boolean isPorted(Player player) {
        return portedPlayers.containsKey(player.getUniqueId());
    }

    @Override
    public String getVersion() {
        return getDescription().getVersion();
    }

    public void sendRawPacket(Player player, ByteBuf packet) throws IllegalArgumentException {
        if (!isPorted(player)) throw new IllegalArgumentException("This player is not on 1.9");
        ConnectionInfo ci = portedPlayers.get(player.getUniqueId());
        ci.sendRawPacket(packet);
    }

    public void addPortedClient(ConnectionInfo info) {
        portedPlayers.put(info.getUUID(), info);
    }

    public void removePortedClient(UUID clientID) {
        portedPlayers.remove(clientID);
    }

    public static ItemStack getHandItem(final ConnectionInfo info) {
        try {
            return Bukkit.getScheduler().callSyncMethod(getPlugin(ViaVersionPlugin.class), new Callable<ItemStack>() {
                @Override
                public ItemStack call() throws Exception {
                    if (info.getPlayer() != null) {
                        return info.getPlayer().getItemInHand();
                    }
                    return null;
                }
            }).get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.out.println("Error fetching hand item ");
            e.printStackTrace();
            return null;
        }
    }
}
