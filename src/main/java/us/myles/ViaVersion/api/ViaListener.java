package us.myles.ViaVersion.api;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;

import java.util.UUID;

@Getter(AccessLevel.PROTECTED)
@RequiredArgsConstructor
public abstract class ViaListener implements Listener {
    private final ViaVersionPlugin plugin;
    private final Class<? extends Protocol> requiredPipeline;
    private boolean registered = false;

    /**
     * Get the UserConnection from a player
     *
     * @param player Player object
     * @return The UserConnection
     */
    protected UserConnection getUserConnection(@NonNull Player player) {
        return getUserConnection(player.getUniqueId());
    }

    /**
     * Get the UserConnection from an UUID
     *
     * @param uuid UUID object
     * @return The UserConnection
     */
    protected UserConnection getUserConnection(@NonNull UUID uuid) {
        if (!plugin.isPorted(uuid)) return null;
        return plugin.getConnection(uuid);
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
     * Checks if the UUID is on the selected pipe
     *
     * @param uuid UUID Object
     * @return True if on pipe
     */
    protected boolean isOnPipe(UUID uuid) {
        UserConnection userConnection = getUserConnection(uuid);
        return userConnection != null &&
                userConnection.get(ProtocolInfo.class).getPipeline().contains(requiredPipeline);
    }

    /**
     * Register as Bukkit event
     */
    public void register() {
        if (registered) return;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        registered = true;
    }
}
