package us.myles.ViaVersion.update;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

@RequiredArgsConstructor
public class UpdateListener implements Listener {

	private final Plugin plugin;
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		if(e.getPlayer().hasPermission("viaversion.update")
				&& plugin.getConfig().getBoolean("checkforupdates", true)) {
			UpdateUtil.sendUpdateMessage(e.getPlayer().getUniqueId(), plugin);
		}
	}

}
