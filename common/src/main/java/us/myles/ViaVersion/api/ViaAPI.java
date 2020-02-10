package us.myles.ViaVersion.api;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.boss.BossBar;
import us.myles.ViaVersion.api.boss.BossColor;
import us.myles.ViaVersion.api.boss.BossStyle;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;

import java.util.SortedSet;
import java.util.UUID;

/**
 * Represents the ViaAPI
 *
 * @param <T> The player type for the specific platform, for bukkit it's {@code ViaAPI<Player>}
 */
public interface ViaAPI<T> {

    /**
     * Get protocol number from a player
     * Will also retrieve version from ProtocolSupport if it's being used.
     *
     * @param player Platform player object, eg. Bukkit this is Player
     * @return Protocol ID, For example (47=1.8-1.8.8, 107=1.9, 108=1.9.1)
     */
    int getPlayerVersion(T player);

    /**
     * Get protocol number from a player
     *
     * @param uuid UUID of a player
     * @return Protocol ID, For example (47=1.8-1.8.8, 107=1.9, 108=1.9.1)
     */
    int getPlayerVersion(UUID uuid);

    /**
     * Returns if the player is ported by Via.
     *
     * @param playerUUID UUID of a player
     * @return true if Via has a cached userconnection for this player
     * @deprecated as of 0.9.9, because all players are ported use {@link #getPlayerVersion(UUID)}
     */
    @Deprecated
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
     * @param player Platform player object, eg. Bukkit this is Player
     * @param packet The packet, you need a VarInt ID then the packet contents.
     * @throws IllegalArgumentException If not on 1.9 throws IllegalArg
     */
    void sendRawPacket(T player, ByteBuf packet) throws IllegalArgumentException;

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
     * @return BossBar instance
     */
    BossBar createBossBar(String title, BossColor color, BossStyle style);

    /**
     * Create a new bossbar instance
     *
     * @param title  The title
     * @param health Number between 0 and 1
     * @param color  The color
     * @param style  The style
     * @return BossBar instance
     */
    BossBar createBossBar(String title, float health, BossColor color, BossStyle style);

    /**
     * Get the supported protocol versions
     * This method removes any blocked protocol versions.
     *
     * @return a list of protocol versions
     * @see ProtocolRegistry#getSupportedVersions() for full list.
     */
    SortedSet<Integer> getSupportedVersions();
}
