package us.myles.ViaVersion.bukkit.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import us.myles.ViaVersion.api.ViaListener;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;

public class ViaBukkitListener extends ViaListener implements Listener {
    private final Plugin plugin;

    public ViaBukkitListener(Plugin plugin, Class<? extends Protocol> requiredPipeline) {
        super(requiredPipeline);
        this.plugin = plugin;
    }

    /**
     * Get the UserConnection from a player
     *
     * @param player Player object
     * @return The UserConnection
     */
    protected UserConnection getUserConnection(Player player) {
        return getUserConnection(player.getUniqueId());
    }

    /**
     * Checks if the player is on the selected pipe
     *
     * @param player Player Object
     * @return True if on pipe
     */
    protected boolean isOnPipe(Player player) {
        return isOnPipe(player.getUniqueId());
    }

    /**
     * Register as Bukkit event
     */
    @Override
    public void register() {
        if (isRegistered()) return;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setRegistered(true);
    }

    public Plugin getPlugin() {
        return plugin;
    }
}
