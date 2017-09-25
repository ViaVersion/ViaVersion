package us.myles.ViaVersion.bukkit.providers;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.bukkit.protocol1_12to1_11_1.BukkitInvContainerUpdateTask;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.protocol1_12to1_11_1.providers.InvContainerItemProvider;

public class BukkitInvContainerItemProvider extends InvContainerItemProvider {

    private static Map<UUID, BukkitInvContainerUpdateTask> updateTasks = new ConcurrentHashMap<>();

    @Override
    public boolean registerInvClickPacket(short windowId, short slotId, short anumber, UserConnection uconnection) {
        ProtocolInfo info = uconnection.get(ProtocolInfo.class);
        // TODO: lets add some stuff here :)
        // http://wiki.vg/index.php?title=Protocol&oldid=13223#Click_Window
        System.out.println("info: " + info + " QUICK ACTION windowId: " + windowId + " slotId: " + slotId + " button: " + 0 + " anumber: " + anumber + " mode: " + 1);
        return false; // change to true once supported
    }

    public void onTaskExecuted(UUID uuid) {
        updateTasks.remove(uuid);
    }
}