package UpdateCheck;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class updateJoinEvent implements Listener {

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		if (e.getPlayer().hasPermission("ViaVersion.newupdate")) {
			CheckUtil.update(e.getPlayer());
		}
	}
}
