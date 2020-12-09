package us.myles.ViaVersion.bukkit.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.bukkit.platform.BukkitViaInjector;

public class ProtocolLibEnableListener implements Listener {

    @EventHandler
    public void onPluginEnable(PluginEnableEvent e) {
        // Will likely never happen, but try to account for hacky plugin loading systems anyways
        if (e.getPlugin().getName().equals("ProtocolLib")) {
            ((BukkitViaInjector) Via.getManager().getInjector()).setProtocolLib(true);
        }
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent e) {
        if (e.getPlugin().getName().equals("ProtocolLib")) {
            ((BukkitViaInjector) Via.getManager().getInjector()).setProtocolLib(false);
        }
    }
}
