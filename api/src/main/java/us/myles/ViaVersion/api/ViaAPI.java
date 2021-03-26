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
package us.myles.ViaVersion.api;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.boss.BossBar;
import us.myles.ViaVersion.api.boss.BossColor;
import us.myles.ViaVersion.api.boss.BossStyle;
import us.myles.ViaVersion.api.protocol.ServerProtocolVersion;

import java.util.SortedSet;
import java.util.UUID;

/**
 * Represents the ViaAPI
 *
 * @param <T> The player type for the specific platform, for bukkit it's {@code ViaAPI<Player>}
 */
public interface ViaAPI<T> {

    /**
     * Returns the server's protocol version info.
     *
     * @return the server's protocol version info
     */
    ServerProtocolVersion getServerVersion();

    /**
     * Get protocol version number from a player.
     * Will also retrieve version from ProtocolSupport if it's being used.
     *
     * @param player Platform player object, eg. Bukkit this is Player
     * @return Protocol ID, For example (47=1.8-1.8.8, 107=1.9, 108=1.9.1), or -1 if no longer connected
     */
    int getPlayerVersion(T player);

    /**
     * Get protocol number from a player.
     *
     * @param uuid UUID of a player
     * @return Protocol ID, For example (47=1.8-1.8.8, 107=1.9, 108=1.9.1), or -1 if not connected
     */
    int getPlayerVersion(UUID uuid);

    /**
     * Returns if the player is ported by Via.
     *
     * @param playerUUID UUID of a player
     * @return true if Via has a cached userconnection for this player
     * @see #isInjected(UUID)
     * @deprecated use {@link #isInjected(UUID)}
     */
    @Deprecated
    default boolean isPorted(UUID playerUUID) {
        return isInjected(playerUUID);
    }

    /**
     * Returns if Via injected into this player connection
     *
     * @param playerUUID UUID of a player
     * @return true if Via has a cached UserConnection for this player
     */
    boolean isInjected(UUID playerUUID);

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
     * @throws IllegalArgumentException if the player is not injected by Via
     */
    void sendRawPacket(T player, ByteBuf packet);

    /**
     * Send a raw packet to the player (Use new IDs)
     *
     * @param uuid   The uuid from the player to send packet
     * @param packet The packet, you need a VarInt ID then the packet contents.
     * @throws IllegalArgumentException if the player is not injected by Via
     */
    void sendRawPacket(UUID uuid, ByteBuf packet);

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
     * @see #getFullSupportedVersions() for a full list
     */
    SortedSet<Integer> getSupportedVersions();

    /**
     * Get the supported protocol versions, including blocked protocols.
     *
     * @return a list of protocol versions
     */
    SortedSet<Integer> getFullSupportedVersions();
}
