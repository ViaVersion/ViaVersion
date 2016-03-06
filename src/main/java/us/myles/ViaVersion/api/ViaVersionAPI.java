package us.myles.ViaVersion.api;

import io.netty.buffer.ByteBuf;
import org.bukkit.entity.Player;
import us.myles.ViaVersion.api.boss.BossBar;
import us.myles.ViaVersion.api.boss.BossColor;
import us.myles.ViaVersion.api.boss.BossStyle;

import java.util.UUID;

public interface ViaVersionAPI {
    /**
     * Is player using 1.9?
     *
     * @param player
     * @return True if the client is on 1.9
     */
    boolean isPorted(Player player);

    /**
     * Is player using 1.9?
     *
     * @param playerUUID
     * @return True if the client is on 1.9
     */
    boolean isPorted(UUID playerUUID);

    /**
     * Get the version of the plugin
     *
     * @return Plugin version
     */
    String getVersion();

    /**
     * Send a raw packet to the player (Use new IDs)
     *
     * @param player The player to send packet
     * @param packet The packet, you need a VarInt ID then the packet contents.
     * @throws IllegalArgumentException If not on 1.9 throws IllegalArg
     */
    void sendRawPacket(Player player, ByteBuf packet) throws IllegalArgumentException;

    /**
     * Send a raw packet to the player (Use new IDs)
     *
     * @param uuid   The uuid from the player to send packet
     * @param packet The packet, you need a VarInt ID then the packet contents.
     * @throws IllegalArgumentException If not on 1.9 throws IllegalArg
     */
    void sendRawPacket(UUID uuid, ByteBuf packet) throws IllegalArgumentException;

    /**
     * Create a new bossbar instance
     *
     * @param title The title
     * @param color The color
     * @param style The style
     * @return Bossbar instance
     */
    BossBar createBossBar(String title, BossColor color, BossStyle style);

    /**
     * Create a new bossbar instance
     *
     * @param title  The title
     * @param health Number between 0 and 1
     * @param color  The color
     * @param style  The style
     * @return Bossbar instance
     */
    BossBar createBossBar(String title, float health, BossColor color, BossStyle style);

    /**
     * Obtain if global debug is enabled
     *
     * @return true if debug is enabled
     */
    boolean isDebug();

    /**
     * Obtains if syncing chunks is on
     *
     * @return true if it is
     */
    boolean isSyncedChunks();
}
