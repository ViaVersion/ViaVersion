package us.myles.ViaVersion.protocols.protocol1_9to1_8.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class PaperPatch implements Listener {

    /*
    This patch is applied when Paper is detected.
    I'm unsure of what causes this but essentially,
    placing blocks where your standing works?
    If there is a better fix then we'll replace this.
     */

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        if (e.getPlayer().getLocation().getBlock().equals(e.getBlock())) {
            e.setCancelled(true);
        }
    }
}
