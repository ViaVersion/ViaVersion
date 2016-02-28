package us.myles.ViaVersion;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.ServerConnection;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import us.myles.ViaVersion.handlers.ViaVersionInitializer;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class Core extends JavaPlugin {
    @Override
    public void onEnable() {
        System.out.println("ViaVersion enabled, injecting. (Allows 1.8 to be accessed via 1.9)");
        /* Obvious message here:
            If loading this plugin nobody will be on 1.9 cause only 1.8 so we're fine, as for reloading ugh.
            Clients might crash cause of it being a bum maybe? :P
         */
        try {
            injectPacketHandler();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unable to inject handlers, this version only supports 1.8.");
        }
    }

    public void injectPacketHandler() throws NoSuchFieldException, IllegalAccessException {
        MinecraftServer server = MinecraftServer.getServer();
        ServerConnection connection = server.getServerConnection();

        List<ChannelFuture> futures = getPrivateField(connection, "g", List.class);

        for (ChannelFuture future : futures) {
            ChannelPipeline pipeline = future.channel().pipeline();
            ChannelHandler bootstrapAcceptor = pipeline.first();
            ChannelInitializer<SocketChannel> oldInit = getPrivateField(bootstrapAcceptor, "childHandler", ChannelInitializer.class);
            ChannelInitializer newInit = new ViaVersionInitializer(oldInit);
            setPrivateField(bootstrapAcceptor, "childHandler", newInit);
        }
    }


    public static Entity getEntity(final UUID player, final int id) {

        try {
            return Bukkit.getScheduler().callSyncMethod(getPlugin(Core.class), new Callable<Entity>() {
                @Override
                public Entity call() throws Exception {
                    Player p = Bukkit.getPlayer(player);
                    if (p == null) return null;
                    WorldServer ws = ((CraftWorld) p.getWorld()).getHandle();
                    for (Entity e : ws.entityList) {
                        if (e.getId() == id) {
                            return e;
                        }
                    }
                    System.out.println("Couldn't find in the world!!");
                    return null;
                }
            }).get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.out.println("Error fetching entity ");
            e.printStackTrace();
            return null;
        }

    }

    public static <T> T getPrivateField(Object o, String f, Class<T> t) throws NoSuchFieldException, IllegalAccessException {
        Field field = o.getClass().getDeclaredField(f);
        field.setAccessible(true);
        return (T) field.get(o);
    }

    public static void setPrivateField(Object o, String f, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = o.getClass().getDeclaredField(f);
        field.setAccessible(true);
        field.set(o, value);
    }

    @Override
    public void onDisable() {

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
