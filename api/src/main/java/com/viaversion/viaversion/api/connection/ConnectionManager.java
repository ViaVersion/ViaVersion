/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
package com.viaversion.viaversion.api.connection;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Handles injected UserConnections.
 * Check {@link UserConnection#isServer()} and {@link UserConnection#isClient()} to determine the connection type.
 */
public interface ConnectionManager {

    /**
     * Returns if Via has injected. See above for the connection types.
     *
     * @param uuid player uuid
     * @return true if the player is handled by Via
     */
    boolean hasServerConnection(UUID uuid);

    /**
     * Returns if Via has injected. See above for the connection types.
     *
     * @param uuid player uuid
     * @return true if the player is handled by Via
     */
    boolean hasClientConnection(UUID uuid);

    /**
     * Returns the server UserConnection. See above for the connection types.
     * When ViaVersion is reloaded, this method may not return some players.
     * <p>
     * Note that connections are removed as soon as their channel is closed,
     * so avoid using this method during player quits for example.
     *
     * @return server UserConnection of the player or null
     */
    @Nullable
    UserConnection getServerConnection(UUID uuid);

    /**
     * Returns the client UserConnection. See above for the connection types.
     * When ViaVersion is reloaded, this method may not return some players.
     * <p>
     * Note that connections are removed as soon as their channel is closed,
     * so avoid using this method during player quits for example.
     *
     * @return client UserConnection of the player or null
     */
    @Nullable
    UserConnection getClientConnection(UUID uuid);

    /**
     * Returns the UUID from the server connection. See above for the connection types.
     * Returns null when there isn't a server or this connection isn't a server connection, or it doesn't have an id
     * When ViaVersion is reloaded, this method may not return some players.
     * <p>
     * Note that connections are removed as soon as their channel is closed,
     * so avoid using this method during player quits for example.
     *
     * @return UUID of the server connection to this proxy server
     */
    @Nullable
    UUID getServerConnectionId(UserConnection connection);

    /**
     * Returns the UUID from the client connection. See above for the connection types.
     * Returns null when there isn't a client or this connection isn't a client connection, or it doesn't have an id
     * When ViaVersion is reloaded, this method may not return some players.
     * <p>
     * Note that connections are removed as soon as their channel is closed,
     * so avoid using this method during player quits for example.
     *
     * @return UUID of the client connection to this proxy server
     */
    @Nullable
    UUID getClientConnectionId(UserConnection connection);

    /**
     * Returns a map containing the UUIDs and server UserConnections
     * When ViaVersion is reloaded, this method may not return some players.
     *
     * @return map containing the UUIDs and frontend UserConnections
     */
    Map<UUID, UserConnection> getServerConnections();

    /**
     * Returns a map containing the UUIDs and client UserConnections
     * When ViaVersion is reloaded, this method may not return some players.
     *
     * @return map containing the UUIDs and client UserConnections
     */
    Map<UUID, UserConnection> getClientConnections();

    /**
     * Returns all UserConnections which are registered
     * May contain duplicated UUIDs on multiple ProtocolInfo.
     * May contain frontend, backend and/or client-sided connections.
     * When ViaVersion is reloaded, this method may not return some players.
     *
     * @return connected UserConnections
     */
    Set<UserConnection> getConnections();

    void onLoginSuccess(UserConnection connection);

    void onDisconnect(UserConnection connection);
}
