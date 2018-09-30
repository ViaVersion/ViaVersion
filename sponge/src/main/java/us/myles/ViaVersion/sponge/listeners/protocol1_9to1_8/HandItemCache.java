package us.myles.ViaVersion.sponge.listeners.protocol1_9to1_8;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.sponge.listeners.protocol1_9to1_8.sponge4.Sponge4ItemGrabber;
import us.myles.ViaVersion.sponge.listeners.protocol1_9to1_8.sponge5.Sponge5ItemGrabber;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HandItemCache implements Runnable {
    public static boolean CACHE = false;
    private static Map<UUID, Item> handCache = new ConcurrentHashMap<>();
    private static Field GET_DAMAGE;
    private static Method GET_ID;
    private static ItemGrabber grabber;

    static {
        try {
            Class.forName("org.spongepowered.api.event.entity.DisplaceEntityEvent");
            grabber = new Sponge4ItemGrabber();
        } catch (ClassNotFoundException e) {
            grabber = new Sponge5ItemGrabber();
        }
    }

    public static Item getHandItem(UUID player) {
        return handCache.get(player);
    }

    @Override
    public void run() {
        List<UUID> players = new ArrayList<>(handCache.keySet());

        for (Player p : Sponge.getServer().getOnlinePlayers()) {
            handCache.put(p.getUniqueId(), convert(grabber.getItem(p)));
            players.remove(p.getUniqueId());
        }
        // Remove offline players
        for (UUID uuid : players) {
            handCache.remove(uuid);
        }
    }

    public static Item convert(ItemStack itemInHand) {
        if (itemInHand == null) return new Item((short) 0, (byte) 0, (short) 0, null);
        if (GET_DAMAGE == null) {
            try {
                GET_DAMAGE = itemInHand.getClass().getDeclaredField("field_77991_e");
                GET_DAMAGE.setAccessible(true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        if (GET_ID == null) {
            try {
                GET_ID = Class.forName("net.minecraft.item.Item").getDeclaredMethod("func_150891_b", Class.forName("net.minecraft.item.Item"));
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        int id = 0;
        try {
            id = (int) GET_ID.invoke(null, itemInHand.getItem());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        int damage = 0;
        try {
            damage = (int) GET_DAMAGE.get(itemInHand);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return new Item((short) id, (byte) itemInHand.getQuantity(), (short) damage, null);
    }
}

