package us.myles.ViaVersion.sponge.listeners.protocol1_9to1_8;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.world.Location;
import us.myles.ViaVersion.SpongePlugin;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9TO1_8;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.EntityTracker;
import us.myles.ViaVersion.sponge.listeners.ViaSpongeListener;

public class BlockListener extends ViaSpongeListener {

    public BlockListener(SpongePlugin plugin) {
        super(plugin, Protocol1_9TO1_8.class);
    }

    @Listener
    public void placeBlock(ChangeBlockEvent.Place e, @Root Player player) {
        if (isOnPipe(player.getUniqueId())) {
            Location loc = e.getTransactions().get(0).getFinal().getLocation().get();
            getUserConnection(player.getUniqueId())
                    .get(EntityTracker.class)
                    .addBlockInteraction(new Position((long) loc.getX(), (long) loc.getY(), (long) loc.getZ()));
        }
    }
}
