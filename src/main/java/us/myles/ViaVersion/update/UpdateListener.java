package us.myles.ViaVersion.update;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class UpdateListener implements Listener {
	
	private Plugin plugin;
	
	public UpdateListener(Plugin plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		if(e.getPlayer().hasPermission("viaversion.update")
				&& plugin.getConfig().getBoolean("checkforupdates", true)) {
			UpdateUtil.sendUpdateMessage(e.getPlayer().getUniqueId(), plugin);
		}
	}

}
