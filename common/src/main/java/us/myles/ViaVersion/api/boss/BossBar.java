package us.myles.ViaVersion.api.boss;

import us.myles.ViaVersion.api.Via;

import java.util.Set;
import java.util.UUID;

public abstract class BossBar<T> {

    /**
     * Get the current title
     *
     * @return the title
     */
    public abstract String getTitle();

    /**
     * Change the title
     *
     * @param title Title can be in either JSON or just text
     * @return The BossBar object
     */
    public abstract BossBar setTitle(String title);

    /**
     * Get the health
     *
     * @return float between 0F - 1F
     */
    public abstract float getHealth();

    /**
     * Change the health
     *
     * @param health this float has to be between 0F - 1F
     * @return The BossBar object
     */
    public abstract BossBar setHealth(float health);

    /**
     * Get the bossbar color
     *
     * @return The colour
     */
    public abstract BossColor getColor();

    /**
     * Yay colors!
     *
     * @param color Whatever color you want!
     * @return The BossBar object
     */
    public abstract BossBar setColor(BossColor color);

    /**
     * Get the bosbar style
     *
     * @return BossStyle
     */
    public abstract BossStyle getStyle();

    /**
     * Change the bosbar style
     *
     * @param style BossStyle
     * @return The BossBar object
     */
    public abstract BossBar setStyle(BossStyle style);

    /**
     * Show the bossbar to a player.
     *
     * @param player The player
     * @return The BossBar object
     * @deprecated Deprecated use UUID's instead of Player objects {@link #addPlayer(UUID)}
     */
    @Deprecated
    public BossBar addPlayer(T player) {
        throw new UnsupportedOperationException("This method is not implemented for the platform " + Via.getPlatform().getPlatformName());
    }

    /**
     * Show the bossbar to a player (uuid)
     *
     * @param player uuid of the player
     * @return The BossBar object
     */
    public abstract BossBar addPlayer(UUID player);

    /**
     * add multiple players
     *
     * @param players list of players
     * @return The BossBar object
     * @deprecated Deprecated use UUID's instead of Player objects {@link #addPlayer(UUID)}
     */
    @Deprecated
    public BossBar addPlayers(T... players) {
        throw new UnsupportedOperationException("This method is not implemented for the platform " + Via.getPlatform().getPlatformName());
    }

    /**
     * Remove the bossbar from a player
     *
     * @param player The player
     * @return The BossBar object
     * @deprecated Deprecated use UUID's instead of Player objects {@link #removePlayer(UUID)}
     */
    @Deprecated
    public BossBar removePlayer(T player) {
        throw new UnsupportedOperationException("This method is not implemented for the platform " + Via.getPlatform().getPlatformName());
    }

    /**
     * Removes the bossbar from a player
     *
     * @param uuid The platers YYUD
     * @return The BossBar object
     */
    public abstract BossBar removePlayer(UUID uuid);

    /**
     * Add flags
     *
     * @param flag The flag to add
     * @return The BossBar object
     */
    public abstract BossBar addFlag(BossFlag flag);

    /**
     * Remove flags.
     *
     * @param flag The flag to remove
     * @return The BossBar object
     */
    public abstract BossBar removeFlag(BossFlag flag);

    /**
     * @param flag The flag to check against
     * @return True if it has the flag
     */
    public abstract boolean hasFlag(BossFlag flag);

    /**
     * Get players
     *
     * @return UUIDS from players (sorry I lied)
     */
    public abstract Set<UUID> getPlayers();

    /**
     * Show the bossbar to everyone (In the getPlayer set)
     *
     * @return The BossBar object
     */
    public abstract BossBar show();

    /**
     * Hide the bossbar from everyone (In the getPlayer set)
     *
     * @return The BossBar object
     */
    public abstract BossBar hide();

    /**
     * Is it visible?
     *
     * @return visibility changable with show() and hide()
     */
    public abstract boolean isVisible();

    /**
     * Get the UUID of this bossbar
     *
     * @return Unique Id for this bossbar
     */
    public abstract UUID getId();
}
