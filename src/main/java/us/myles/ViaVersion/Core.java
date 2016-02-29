package us.myles.ViaVersion;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import us.myles.ViaVersion.handlers.ViaVersionInitializer;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class Core extends JavaPlugin {
    @Override
    public void onEnable() {
        System.out.println("ViaVersion enabled, injecting. (Allows 1.8 to be accessed via 1.9)");
        try {
            injectPacketHandler();
        } catch (Exception e) {
            if(Bukkit.getPluginManager().getPlugin("ProtocolLib") != null){
                System.out.println("This plugin is not compatible with protocol lib.");
            }
            System.out.println("Unable to inject handlers, are you on 1.8? ");
            e.printStackTrace();
        }
    }

    public void injectPacketHandler() throws Exception {
        Class<?> serverClazz = ReflectionUtil.nms("MinecraftServer");
        Object server = ReflectionUtil.invokeStatic(serverClazz, "getServer");
        Object connection = serverClazz.getDeclaredMethod("getServerConnection").invoke(server);

        List<ChannelFuture> futures = ReflectionUtil.get(connection, "g", List.class);
        if (futures.size() == 0) {
            throw new Exception("Could not find server to inject (late bind?)");
        }

        for (ChannelFuture future : futures) {
            ChannelPipeline pipeline = future.channel().pipeline();
            ChannelHandler bootstrapAcceptor = pipeline.first();
            ChannelInitializer<SocketChannel> oldInit = ReflectionUtil.get(bootstrapAcceptor, "childHandler", ChannelInitializer.class);
            ChannelInitializer newInit = new ViaVersionInitializer(oldInit);
            ReflectionUtil.set(bootstrapAcceptor, "childHandler", newInit);
        }
    }


    public static Entity getEntity(final UUID player, final int id) {
        try {
            return Bukkit.getScheduler().callSyncMethod(getPlugin(Core.class), new Callable<Entity>() {
                @Override
                public Entity call() throws Exception {
                    Player p = Bukkit.getPlayer(player);
                    if (p == null) return null;
                    for (Entity e : p.getWorld().getEntities()) {
                        if (e.getEntityId() == id) {
                            return e;
                        }
                    }
                    return null;
                }
            }).get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.out.println("Error fetching entity ");
            e.printStackTrace();
            return null;
        }
    }

    public static ItemStack getHandItem(final ConnectionInfo info) {
        try {
            return Bukkit.getScheduler().callSyncMethod(getPlugin(Core.class), new Callable<ItemStack>() {
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
