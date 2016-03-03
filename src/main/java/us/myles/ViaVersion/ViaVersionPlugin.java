package us.myles.ViaVersion;

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
import us.myles.ViaVersion.commands.ViaVersionCommand;
import us.myles.ViaVersion.handlers.ViaVersionInitializer;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ViaVersionPlugin extends JavaPlugin implements ViaVersionAPI {

    private final Set<UUID> portedPlayers = Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());

    @Override
    public void onEnable() {
        ViaVersion.setInstance(this);
        if(System.getProperty("ViaVersion") != null){
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
                setPorted(e.getPlayer().getUniqueId(), false);
            }
        }, this);
        getCommand("viaversion").setExecutor(new ViaVersionCommand());
    }

    public void injectPacketHandler() throws Exception {
        Class<?> serverClazz = ReflectionUtil.nms("MinecraftServer");
        Object server = ReflectionUtil.invokeStatic(serverClazz, "getServer");
        Object connection = serverClazz.getDeclaredMethod("getServerConnection").invoke(server);

        List<ChannelFuture> futures = ReflectionUtil.get(connection, "g", List.class);
        if (futures.size() == 0) {
            throw new Exception("Could not find server to inject (Please ensure late-bind in your spigot.yml is false)");
        }

        for (ChannelFuture future : futures) {
            ChannelPipeline pipeline = future.channel().pipeline();
            ChannelHandler bootstrapAcceptor = pipeline.first();
            ChannelInitializer<SocketChannel> oldInit = ReflectionUtil.get(bootstrapAcceptor, "childHandler", ChannelInitializer.class);
            ChannelInitializer newInit = new ViaVersionInitializer(oldInit);
            ReflectionUtil.set(bootstrapAcceptor, "childHandler", newInit);
        }
    }

    @Override
    public boolean isPorted(Player player) {
        return portedPlayers.contains(player.getUniqueId());
    }

    @Override
    public String getVersion() {
        return getDescription().getVersion();
    }

    public void setPorted(UUID id, boolean value) {
        if (value) {
            portedPlayers.add(id);
        } else {
            portedPlayers.remove(id);
        }
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
