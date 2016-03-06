package us.myles.ViaVersion.api;

import io.netty.buffer.ByteBuf;
import org.bukkit.entity.Player;

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
     * Obtains if syncing chunks is on
     * @return true if it is
     */
    boolean isSyncedChunks();
}
