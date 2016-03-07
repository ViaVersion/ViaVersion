package us.myles.ViaVersion.api.boss;

import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public interface BossBar {
    /**
     * Change the title
     *
     * @param title Title can be in either JSON or just text
     */
    void setTitle(String title);

    /**
     * Change the health
     *
     * @param health this float has to be between 0F - 1F
     */
    void setHealth(float health);

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
    void setColor(BossColor color);

    /**
     * Change the bosbar style
     *
     * @param style BossStyle
     */
    void setStyle(BossStyle style);

    /**
     * Show the bossbar to a player.
     *
     * @param player
     */
    void addPlayer(Player player);

    /**
     * Remove the bossbar from a player
     *
     * @param player
     */
    void removePlayer(Player player);

    /**
     * Add flags
     *
     * @param flag
     */
    void addFlag(BossFlag flag);

    /**
     * Remove flags.
     *
     * @param flag
     */
    void removeFlag(BossFlag flag);

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
    void show();

    /**
     * Hide the bossbar from everyone (In the getPlayer set)
     */
    void hide();

    /**
     * Is it visible?
     *
     * @return visibility changable with show() and hide()
     */
    boolean isVisible();
}
