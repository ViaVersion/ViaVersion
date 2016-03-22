package us.myles.ViaVersion.update;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import us.myles.ViaVersion.ViaVersionPlugin;

@RequiredArgsConstructor
public class UpdateListener implements Listener {

    private final ViaVersionPlugin plugin;

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (e.getPlayer().hasPermission("viaversion.update")
                && plugin.isCheckForUpdates()) {
            UpdateUtil.sendUpdateMessage(e.getPlayer().getUniqueId(), plugin);
        }
    }

}
