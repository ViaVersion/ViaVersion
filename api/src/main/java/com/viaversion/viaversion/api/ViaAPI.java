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
package com.viaversion.viaversion.api;

import com.viaversion.viaversion.api.connection.ConnectionManager;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.legacy.LegacyViaAPI;
import com.viaversion.viaversion.api.platform.ViaPlatform;
import com.viaversion.viaversion.api.protocol.ProtocolManager;
import com.viaversion.viaversion.api.protocol.version.ServerProtocolVersion;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.SortedSet;
import java.util.UUID;

/**
 * General api point. For more specialized api methods, see {@link Via#getManager()}.
 *
 * @param <T> player type for the specific platform
 * @see ViaManager
 * @see ProtocolManager
 * @see ConnectionManager
 * @see ViaPlatform
 */
public interface ViaAPI<T> {

    /**
     * Returns the API version incremented with meaningful API changes.
     * This includes breaking changes to existing API and larger additions.
     *
     * @return API version incremented with meaningful API changes
     */
    default int apiVersion() {
        return 3;
    }

    /**
     * Returns the server's protocol version info.
     *
     * @return the server's protocol version info
     */
    ServerProtocolVersion getServerVersion();

    /**
     * Returns the protocol version of a player.
     * This will also retrieve the version from ProtocolSupport if it's being used.
     *
     * @param player the platform's player object, e.g. Bukkit this is Player
     * @return protocol version, for example (47=1.8-1.8.8, 107=1.9, 108=1.9.1), or -1 if no longer connected
     */
    int getPlayerVersion(T player);

    /**
     * Returns the protocol version of a player.
     *
     * @param uuid UUID of a player
     * @return protocol version, for example (47=1.8-1.8.8, 107=1.9, 108=1.9.1), or -1 if not connected
     */
    int getPlayerVersion(UUID uuid);

    /**
     * Returns whether Via injected into this player connection.
     *
     * @param uuid uuid of the player
     * @return whether Via has a cached a UserConnection for this player
     */
    boolean isInjected(UUID uuid);

    /**
     * Returns the Via injected UserConnection if present.
     *
     * @param uuid uuid of the player
     * @return user connection if present
     */
    @Nullable UserConnection getConnection(UUID uuid);

    /**
     * Returns the version of the plugin.
     *
     * @return plugin version
     */
    String getVersion();

    /**
     * Sends a raw packet to the player.
     *
     * @param player the platform's player object, e.g. for Bukkit this is Player
     * @param packet the packet; you need a VarInt Id, then the packet contents
     * @throws IllegalArgumentException if the player is not injected by Via
     */
    void sendRawPacket(T player, ByteBuf packet);

    /**
     * Sends a raw packet to the player.
     *
     * @param uuid   the uuid from the player to send packet
     * @param packet the packet; you need a VarInt Id, then the packet contents
     * @throws IllegalArgumentException if the player is not injected by Via
     */
    void sendRawPacket(UUID uuid, ByteBuf packet);

    /**
     * Returns the supported protocol versions.
     * This method removes any blocked protocol versions.
     *
     * @return a list of protocol versions
     * @see #getFullSupportedVersions() for a full list
     */
    SortedSet<Integer> getSupportedVersions();

    /**
     * Returns the supported protocol versions, including blocked protocols.
     *
     * @return a list of protocol versions
     */
    SortedSet<Integer> getFullSupportedVersions();

    /**
     * Returns legacy api only applicable on/to legacy versions.
     * <p>
     * These methods are safe to use and are unlikely to be removed,
     * but it is important to be aware of their limited use.
     *
     * @return legacy api only applicable on/to legacy versions
     */
    LegacyViaAPI<T> legacyAPI();
}
