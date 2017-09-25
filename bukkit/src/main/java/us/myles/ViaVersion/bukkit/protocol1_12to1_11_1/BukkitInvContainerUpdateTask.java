package us.myles.ViaVersion.bukkit.protocol1_12to1_11_1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import us.myles.ViaVersion.bukkit.providers.BukkitInvContainerItemProvider;
import us.myles.ViaVersion.protocols.protocol1_12to1_11_1.storage.InvItemStorage;

public class BukkitInvContainerUpdateTask implements Runnable {

    private BukkitInvContainerItemProvider provider;
    private final UUID uuid;
    private final List<InvItemStorage> items;

    public BukkitInvContainerUpdateTask(BukkitInvContainerItemProvider provider, UUID uuid) {
        this.provider = provider;
        this.uuid = uuid;
        this.items = Collections.synchronizedList(new ArrayList<>());
    }

    public void addItem(short windowId, short slotId, short anumber) {
        InvItemStorage storage = new InvItemStorage(windowId, slotId, anumber);
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
                for (InvItemStorage storage : items) {
                    Object packet = provider.buildWindowClickPacket(p, storage);
                    boolean result = provider.sendPlayer(p, packet);
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