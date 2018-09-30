package us.myles.ViaVersion.bukkit.listeners.protocol1_9to1_8;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import us.myles.ViaVersion.api.minecraft.item.Item;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HandItemCache extends BukkitRunnable {
    public static boolean CACHE = false;
    private static Map<UUID, Item> handCache = new ConcurrentHashMap<>();

    public static Item getHandItem(UUID player) {
        return handCache.get(player);
    }

    @Override
    public void run() {
        List<UUID> players = new ArrayList<>(handCache.keySet());

        for (Player p : Bukkit.getOnlinePlayers()) {
            handCache.put(p.getUniqueId(), convert(p.getItemInHand()));
            players.remove(p.getUniqueId());
        }
        // Remove offline players
        for (UUID uuid : players) {
            handCache.remove(uuid);
        }
    }

    public static Item convert(ItemStack itemInHand) {
        if (itemInHand == null) return new Item((short) 0, (byte) 0, (short) 0, null);
        return new Item((short) itemInHand.getTypeId(), (byte) itemInHand.getAmount(), itemInHand.getDurability(), null);
    }
}
