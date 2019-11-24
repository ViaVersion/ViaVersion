package us.myles.ViaVersion.bukkit.listeners.protocol1_9to1_8;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.bukkit.listeners.ViaBukkitListener;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.EntityTracker1_9;

public class BlockListener extends ViaBukkitListener {

    public BlockListener(ViaVersionPlugin plugin) {
        super(plugin, Protocol1_9To1_8.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void placeBlock(BlockPlaceEvent e) {
        if (isOnPipe(e.getPlayer())) {
            Block b = e.getBlockPlaced();
            getUserConnection(e.getPlayer())
                    .get(EntityTracker1_9.class)
                    .addBlockInteraction(new Position(b.getX(), (short) b.getY(), b.getZ()));
        }
    }
}
