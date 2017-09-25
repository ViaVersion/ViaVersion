package us.myles.ViaVersion.bukkit.protocol1_12to1_11_1;

import java.util.UUID;

import us.myles.ViaVersion.bukkit.providers.BukkitInvContainerItemProvider;

public class BukkitInvContainerUpdateTask implements Runnable {

    private BukkitInvContainerItemProvider provider;
    private UUID uuid;

    public BukkitInvContainerUpdateTask(BukkitInvContainerItemProvider provider, UUID uuid) {
        this.provider = provider;
        this.uuid = uuid;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        provider.onTaskExecuted(uuid);
    }
}