package us.myles.ViaVersion.sponge.listeners.protocol1_9to1_8.sponge4;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import us.myles.ViaVersion.sponge.listeners.protocol1_9to1_8.ItemGrabber;

public class Sponge4ItemGrabber implements ItemGrabber {
    @Override
    public ItemStack getItem(Player player) {
        return player.getItemInHand().orElse(null);
    }
}
