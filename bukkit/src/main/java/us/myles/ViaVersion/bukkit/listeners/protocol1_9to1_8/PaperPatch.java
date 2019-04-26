package us.myles.ViaVersion.bukkit.listeners.protocol1_9to1_8;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.bukkit.listeners.ViaBukkitListener;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9To1_8;

public class PaperPatch extends ViaBukkitListener {

    public PaperPatch(ViaVersionPlugin plugin) {
        super(plugin, Protocol1_9To1_8.class);
    }

    /*
    This patch is applied when Paper is detected.
    I'm unsure of what causes this but essentially,
    placing blocks where you're standing works?
    If there is a better fix then we'll replace this.
     */

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        if (isOnPipe(e.getPlayer())) {
            Location location = e.getPlayer().getLocation();
            Location diff = location.subtract(e.getBlock().getLocation().add(0.5D, 0, 0.5D));
            Material block = e.getBlockPlaced().getType();
            if (isPlacable(block)) {
                return;
            }
            if (location.getBlock().equals(e.getBlock())) {
                e.setCancelled(true);
            } else {
                if (location.getBlock().getRelative(BlockFace.UP).equals(e.getBlock())) {
                    e.setCancelled(true);
                } else {
                    // Within radius of block
                    if (Math.abs(diff.getX()) <= 0.8 && Math.abs(diff.getZ()) <= 0.8D) {
                        // Are they on the edge / shifting ish
                        if (diff.getY() <= 0.1D && diff.getY() >= -0.1D) {
                            e.setCancelled(true);
                            return;
                        }
                        BlockFace relative = e.getBlockAgainst().getFace(e.getBlock());
                        // Are they towering up, (handles some latency)
                        if (relative == BlockFace.UP) {
                            if (diff.getY() < 1D && diff.getY() >= 0D) {
                                e.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isPlacable(Material material) {
        if (!material.isSolid()) return true;
        // signs and banners
        switch (material.getId()) {
            case 63:
            case 68:
            case 176:
            case 177:
                return true;
            default:
                return false;
        }
    }
}
