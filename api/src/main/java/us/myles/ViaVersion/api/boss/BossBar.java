/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package us.myles.ViaVersion.api.boss;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;

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
     * Show the bossbar to a player (uuid). This only works for frontend connections. Use #addConnection(UserConnection) for other types.
     *
     * @param player uuid of the player
     * @return The BossBar object
     */
    public abstract BossBar addPlayer(UUID player);

    /**
     * Show the bossbar to a player connection.
     *
     * @param conn UserConnection of the connection
     * @return The BossBar object
     */
    public abstract BossBar addConnection(UserConnection conn);

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
     * Removes the bossbar from a player. This only works for frontend connections. For others types, use #removeConnection(UserConnection)
     *
     * @param uuid The players UUID
     * @return The BossBar object
     */
    public abstract BossBar removePlayer(UUID uuid);

    /**
     * Removes the bossbar from a player connection.
     *
     * @param conn The UserConnection
     * @return The BossBar object
     */
    public abstract BossBar removeConnection(UserConnection conn);

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
     * Get players. Only returns UUIDs which are front-end. For all connections, use #getConnections()
     *
     * @return UUIDS from players (sorry I lied)
     */
    public abstract Set<UUID> getPlayers();

    /**
     * Get UserConnections.
     *
     * @return UserConnection from players
     */
    public abstract Set<UserConnection> getConnections();

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
