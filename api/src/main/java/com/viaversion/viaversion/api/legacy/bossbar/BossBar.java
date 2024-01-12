/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
package com.viaversion.viaversion.api.legacy.bossbar;

import com.viaversion.viaversion.api.connection.UserConnection;
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
     * Yay, colors!
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
     * Show the bossbar to a player (uuid). This only works for frontend connections. Use #addConnection(UserConnection) for other types.
     *
     * @param player uuid of the player
     * @return The BossBar object
     */
    BossBar addPlayer(UUID player);

    /**
     * Show the bossbar to a player connection.
     *
     * @param conn UserConnection of the connection
     * @return The BossBar object
     */
    BossBar addConnection(UserConnection conn);

    /**
     * Removes the bossbar from a player. This only works for frontend connections. For others types, use #removeConnection(UserConnection)
     *
     * @param uuid The players UUID
     * @return The BossBar object
     */
    BossBar removePlayer(UUID uuid);

    /**
     * Removes the bossbar from a player connection.
     *
     * @param conn The UserConnection
     * @return The BossBar object
     */
    BossBar removeConnection(UserConnection conn);

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
     * Get players. Only returns UUIDs which are front-end. For all connections, use #getConnections()
     *
     * @return UUIDS from players (sorry I lied)
     */
    Set<UUID> getPlayers();

    /**
     * Get UserConnections.
     *
     * @return UserConnection from players
     */
    Set<UserConnection> getConnections();

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

    /**
     * Get the UUID of this bossbar
     *
     * @return Unique Id for this bossbar
     */
    UUID getId();
}
