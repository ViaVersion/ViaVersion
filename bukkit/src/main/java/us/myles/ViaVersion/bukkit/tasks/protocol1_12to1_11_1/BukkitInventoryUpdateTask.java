package us.myles.ViaVersion.bukkit.tasks.protocol1_12to1_11_1;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.myles.ViaVersion.bukkit.providers.BukkitInventoryQuickMoveProvider;
import us.myles.ViaVersion.protocols.protocol1_12to1_11_1.storage.ItemTransaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class BukkitInventoryUpdateTask implements Runnable {

    private BukkitInventoryQuickMoveProvider provider;
    private final UUID uuid;
    private final List<ItemTransaction> items;

    public BukkitInventoryUpdateTask(BukkitInventoryQuickMoveProvider provider, UUID uuid) {
        this.provider = provider;
        this.uuid = uuid;
        this.items = Collections.synchronizedList(new ArrayList<ItemTransaction>());
    }

    public void addItem(short windowId, short slotId, short actionId) {
        ItemTransaction storage = new ItemTransaction(windowId, slotId, actionId);
        items.add(storage);
    }

    @Override
    public void run() {
        Player p = Bukkit.getServer().getPlayer(uuid);
        if (p == null) {
            provider.onTaskExecuted(uuid);
            return;
        }
        try {
            synchronized (items) {
                for (ItemTransaction storage : items) {
                    Object packet = provider.buildWindowClickPacket(p, storage);
                    boolean result = provider.sendPacketToServer(p, packet);
                    if (!result) {
                        break;
                    }
                }
                items.clear();
            }
        } finally {
            provider.onTaskExecuted(uuid);
        }
    }
}