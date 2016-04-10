package us.myles.ViaVersion.protocols.protocol1_9to1_8.listeners;

import lombok.RequiredArgsConstructor;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9TO1_8;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.EntityTracker;

@RequiredArgsConstructor
public class BlockListener implements Listener {

    private final ViaVersionPlugin plugin;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void placeBlock(BlockPlaceEvent e) {
        if (plugin.isPorted(e.getPlayer())) {
            UserConnection c = plugin.getConnection(e.getPlayer());
            if (!c.get(ProtocolInfo.class).getPipeline().contains(Protocol1_9TO1_8.class)) return;
            Block b = e.getBlockPlaced();
            plugin.getConnection(e.getPlayer()).get(EntityTracker.class).addBlockInteraction(new Position((long) b.getX(), (long) b.getY(), (long) b.getZ()));
        }
    }
}
