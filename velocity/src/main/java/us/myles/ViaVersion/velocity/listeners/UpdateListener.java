package us.myles.ViaVersion.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.update.UpdateUtil;

public class UpdateListener {
    @Subscribe
    public void onJoin(PostLoginEvent e) {
        if (e.getPlayer().hasPermission("viaversion.update")
                && Via.getConfig().isCheckForUpdates()) {
            UpdateUtil.sendUpdateMessage(e.getPlayer().getUniqueId());
        }
    }
}
