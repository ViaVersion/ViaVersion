package us.myles.ViaVersion.api;

import io.netty.buffer.ByteBuf;
import org.bukkit.entity.Player;
import us.myles.ViaVersion.api.boss.BossBar;
import us.myles.ViaVersion.api.boss.BossColor;
import us.myles.ViaVersion.api.boss.BossStyle;
import us.myles.ViaVersion.api.command.ViaVersionCommand;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;

import java.util.SortedSet;
import java.util.UUID;

public interface ViaVersionAPI {
    /**
     * Is the player connection modified by ViaVersion?
     *
     * @param player Bukkit player object
     * @return True if the client is modified (At the moment it also means version 1.9 and higher)
     */
    boolean isPorted(Player player);

    /**
     * Get protocol number from a player
     * Will also retrieve version from ProtocolSupport if it's being used.
     *
     * @param player Bukkit player object
     * @return Protocol ID, For example (47=1.8-1.8.8, 107=1.9, 108=1.9.1)
     */
    int getPlayerVersion(Player player);

    /**
     * Get protocol number from a player
     *
     * @param uuid UUID of a player
     * @return Protocol ID, For example (47=1.8-1.8.8, 107=1.9, 108=1.9.1)
     */
    int getPlayerVersion(UUID uuid);

    /**
     * Is player using 1.9?
     *
     * @param playerUUID UUID of a player
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
     * Get if global debug is enabled
     *
     * @return true if debug is enabled
     */
    boolean isDebug();

    /**
     * Get ViaVersions command handler
     *
     * @return command handler
     */
    ViaVersionCommand getCommandHandler();

    /**
     * Get if this version is a compatibility build for spigot.
     * Eg. 1.9.1 / 1.9.2 allow certain versions to connect
     *
     * @return True if it is
     */
    boolean isCompatSpigotBuild();

    /**
     * Get the supported protocol versions
     * This method removes any blocked protocol versions.
     * @see ProtocolRegistry#getSupportedVersions() for full list.
     *
     * @return a list of protocol versions
     */
    SortedSet<Integer> getSupportedVersions();

    /**
     * Gets if the server uses spigot
     * <p>
     * Note: Will only work after ViaVersion load
     *
     * @return True if spigot
     */
    boolean isSpigot();

    /**
     * Gets if protocol support is also being used.
     *
     * @return True if it is being used.
     */
    boolean isProtocolSupport();
}
