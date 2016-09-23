package us.myles.ViaVersion.api.boss;

import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public interface BossBar {
    /**
     * Get the current title
     *
     * @return the title
     */
    String getTitle();

    /**
     * Change the title
     *
     * @param title Title can be in either JSON or just text
     * @return The BossBar object
     */
    BossBar setTitle(String title);

    /**
     * Get the health
     *
     * @return float between 0F - 1F
     */
    float getHealth();

    /**
     * Change the health
     *
     * @param health this float has to be between 0F - 1F
     * @return The BossBar object
     */
    BossBar setHealth(float health);

    /**
     * Get the bossbar color
     *
     * @return The colour
     */
    BossColor getColor();

    /**
     * Yay colors!
     *
     * @param color Whatever color you want!
     * @return The BossBar object
     */
    BossBar setColor(BossColor color);

    /**
     * Get the bosbar style
     *
     * @return BossStyle
     */
    BossStyle getStyle();

    /**
     * Change the bosbar style
     *
     * @param style BossStyle
     * @return The BossBar object
     */
    BossBar setStyle(BossStyle style);

    /**
     * Show the bossbar to a player.
     *
     * @param player The player
     * @return The BossBar object
     */
    BossBar addPlayer(Player player);

    /**
     * Show the bossbar to a player (uuid)
     *
     * @param player uuid of the player
     * @return The BossBar object
     */
    BossBar addPlayer(UUID player);

    /**
     * add multiple players
     *
     * @param players list of players
     * @return The BossBar object
     */
    BossBar addPlayers(Player... players);

    /**
     * Remove the bossbar from a player
     *
     * @param player The player
     * @return The BossBar object
     */
    BossBar removePlayer(Player player);

    /**
     * Add flags
     *
     * @param flag The flag to add
     * @return The BossBar object
     */
    BossBar addFlag(BossFlag flag);

    /**
     * Remove flags.
     *
     * @param flag The flag to remove
     * @return The BossBar object
     */
    BossBar removeFlag(BossFlag flag);

    /**
     * @param flag The flag to check against
     * @return True if it has the flag
     */
    boolean hasFlag(BossFlag flag);

    /**
     * Get players
     *
     * @return UUIDS from players (sorry I lied)
     */
    Set<UUID> getPlayers();

    /**
     * Show the bossbar to everyone (In the getPlayer set)
     *
     * @return The BossBar object
     */
    BossBar show();

    /**
     * Hide the bossbar from everyone (In the getPlayer set)
     *
     * @return The BossBar object
     */
    BossBar hide();

    /**
     * Is it visible?
     *
     * @return visibility changable with show() and hide()
     */
    boolean isVisible();
}
