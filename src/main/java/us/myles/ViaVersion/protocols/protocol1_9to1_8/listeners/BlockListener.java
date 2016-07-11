package us.myles.ViaVersion.protocols.protocol1_9to1_8.listeners;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.ViaListener;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9TO1_8;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.EntityTracker;

public class BlockListener extends ViaListener {

    public BlockListener(ViaVersionPlugin plugin) {
        super(plugin, Protocol1_9TO1_8.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void placeBlock(BlockPlaceEvent e) {
        if (isOnPipe(e.getPlayer())) {
            Block b = e.getBlockPlaced();
            getUserConnection(e.getPlayer())
                    .get(EntityTracker.class)
                    .addBlockInteraction(new Position((long) b.getX(), (long) b.getY(), (long) b.getZ()));
        }
    }
}
