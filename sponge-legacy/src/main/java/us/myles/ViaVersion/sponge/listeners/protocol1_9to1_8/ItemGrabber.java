package us.myles.ViaVersion.sponge.listeners.protocol1_9to1_8;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

public interface ItemGrabber {
    public ItemStack getItem(Player player);
}
