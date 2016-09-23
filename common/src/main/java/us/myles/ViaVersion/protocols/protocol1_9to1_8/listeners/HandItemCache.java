package us.myles.ViaVersion.protocols.protocol1_9to1_8.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import us.myles.ViaVersion.api.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HandItemCache extends BukkitRunnable {
    public static boolean CACHE = false;
    private static ConcurrentHashMap<UUID, Item> handCache = new ConcurrentHashMap<>();

    public static Item getHandItem(UUID player) {
        if (!handCache.containsKey(player))
            return null;
        return handCache.get(player);
    }

    @Override
    public void run() {
        List<UUID> players = new ArrayList<>(handCache.keySet());

        for (Player p : Bukkit.getOnlinePlayers()) {
            handCache.put(p.getUniqueId(), Item.getItem(p.getItemInHand()));
            players.remove(p.getUniqueId());
        }
        // Remove offline players
        for (UUID uuid : players) {
            handCache.remove(uuid);
        }
    }
}
