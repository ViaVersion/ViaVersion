package us.myles.ViaVersion.bukkit.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import us.myles.ViaVersion.bukkit.platform.BukkitViaInjector;

public class ProtocolLibEnableListener implements Listener {
    private final BukkitViaInjector injector;

    public ProtocolLibEnableListener(BukkitViaInjector injector) {
        this.injector = injector;
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent e) {
        if (e.getPlugin().getName().equals("ProtocolLib")) {
            injector.setProtocolLib(true);
        }
    }

    @EventHandler
	public void onPluginDisable(PluginDisableEvent e) {
	    if (e.getPlugin().getName().equals("ProtocolLib")) {
		    injector.setProtocolLib(false);
	    }
    }
}
