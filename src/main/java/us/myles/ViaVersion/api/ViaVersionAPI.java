package us.myles.ViaVersion.api;

import org.bukkit.entity.Player;

public interface ViaVersionAPI {
    /**
     * Is player using 1.9?
     * @param player
     * @return
     */
    boolean isPorted(Player player);
    String getVersion();
}
