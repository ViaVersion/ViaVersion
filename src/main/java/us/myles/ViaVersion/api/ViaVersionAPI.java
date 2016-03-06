package us.myles.ViaVersion.api;

import io.netty.buffer.ByteBuf;
import org.bukkit.entity.Player;
import us.myles.ViaVersion.ConnectionInfo;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ViaVersionAPI {
    /**
     * Is player using 1.9?
     * @param player
     * @return True if the client is on 1.9
     */
    boolean isPorted(Player player);

    /**
     * Get the version of the plugin
     * @return Plugin version
     */
    String getVersion();

    /**
     * Send a raw packet to the player (Use new IDs)
     * @param player The player to send packet
     * @param packet The packet, you need a VarInt ID then the packet contents.
     * @throws IllegalArgumentException If not on 1.9 throws IllegalArg
     */
    void sendRawPacket(Player player, ByteBuf packet) throws IllegalArgumentException;

    /**
     * Obtain if global debug is enabled
     * @return true if debug is enabled
     */
    boolean isDebug();

    /**
     * Returns all ported players. (1.9 players)
     * Warning: This returns a unmodifiable map.
     * @return Map<UUID, ConnectionInfo> of all ported players.
     */
    Map<UUID, ConnectionInfo> getPortedPlayers();

    /**
     * Returns all non ported players. (1.8 players)
     * Warning: This returns a unmodifiable list.
     * @return List<UUID> of all non ported players.
     */
    List<UUID> getNonPortedPlayers();

    /**
     * Returns all ported players. (1.9 players)
     * Warning: This returns a unmodifiable list.
     * @return List<UUID> of all ported players.
     */
    List<UUID> getPortedPlayersList();
}
