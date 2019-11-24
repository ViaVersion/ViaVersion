package us.myles.ViaVersion.bukkit.listeners.protocol1_9to1_8;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import us.myles.ViaVersion.api.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HandItemCache extends BukkitRunnable {
    private final Map<UUID, Item> handCache = new ConcurrentHashMap<>();

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

    public Item getHandItem(UUID player) {
        return handCache.get(player);
    }

    public static Item convert(ItemStack itemInHand) {
        if (itemInHand == null) return new Item(0, (byte) 0, (short) 0, null);
        return new Item(itemInHand.getTypeId(), (byte) itemInHand.getAmount(), itemInHand.getDurability(), null);
    }
}
