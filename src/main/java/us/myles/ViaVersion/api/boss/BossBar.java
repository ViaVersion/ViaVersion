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
     */
    BossBar setHealth(float health);

    /**
     * Get the bossbar color
     *
     * @return
     */
    BossColor getColor();

    /**
     * Yay colors!
     *
     * @param color Whatever color you want!
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
     */
    BossBar setStyle(BossStyle style);

    /**
     * Show the bossbar to a player.
     *
     * @param player
     */
    BossBar addPlayer(Player player);

    /**
     * Show the bossbar to a player (uuid)
     *
     * @param player uuid of the player
     * @return the BossBar instance
     */
    BossBar addPlayer(UUID player);

    /**
     * add multiple players
     *
     * @param players list of players
     * @return the bossbar instance
     */
    BossBar addPlayers(Player... players);

    /**
     * Remove the bossbar from a player
     *
     * @param player
     */
    BossBar removePlayer(Player player);

    /**
     * Add flags
     *
     * @param flag
     */
    BossBar addFlag(BossFlag flag);

    /**
     * Remove flags.
     *
     * @param flag
     */
    BossBar removeFlag(BossFlag flag);

    /**
     * @param flag
     * @return
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
     */
    BossBar show();

    /**
     * Hide the bossbar from everyone (In the getPlayer set)
     */
    BossBar hide();

    /**
     * Is it visible?
     *
     * @return visibility changable with show() and hide()
     */
    boolean isVisible();
}
