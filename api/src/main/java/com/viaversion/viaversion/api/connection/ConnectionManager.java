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
package com.viaversion.viaversion.api.connection;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Handles injected UserConnections
 */
public interface ConnectionManager {

    /**
     * Returns if Via injected into this player connection.
     *
     * @param playerId player uuid
     * @return true if the player is handled by Via
     */
    boolean isClientConnected(UUID playerId);

    /**
     * Frontend connections will have the UUID stored. Override this if your platform isn't always frontend.
     * UUIDs can't be duplicate between frontend connections.
     *
     * @return true if the user is a frontend connection
     */
    default boolean isFrontEnd(UserConnection connection) {
        return !connection.isClientSide();
    }

    /**
     * Returns the frontend UserConnection from the player connected to this proxy server
     * Returns null when there isn't a server or connection was not found
     * When ViaVersion is reloaded, this method may not return some players.
     * May not return ProtocolSupport players.
     * <p>
     * Note that connections are removed as soon as their channel is closed,
     * so avoid using this method during player quits for example.
     *
     * @return frontend UserConnection of the player connected to this proxy server
     */
    @Nullable UserConnection getConnectedClient(UUID clientIdentifier);

    /**
     * Returns the UUID from the frontend connection to this proxy server
     * Returns null when there isn't a server or this connection isn't frontend, or it doesn't have an id
     * When ViaVersion is reloaded, this method may not return some players.
     * May not return ProtocolSupport players.
     * <p>
     * Note that connections are removed as soon as their channel is closed,
     * so avoid using this method during player quits for example.
     *
     * @return UUID of the frontend connection to this proxy server
     */
    @Nullable UUID getConnectedClientId(UserConnection connection);

    /**
     * Returns all UserConnections which are registered
     * May contain duplicated UUIDs on multiple ProtocolInfo.
     * May contain frontend, backend and/or client-sided connections.
     * When ViaVersion is reloaded, this method may not return some players.
     * May not contain ProtocolSupport players.
     *
     * @return connected UserConnections
     */
    Set<UserConnection> getConnections();

    /**
     * Returns a map containing the UUIDs and frontend UserConnections from players connected to this proxy server
     * Returns empty list when there isn't a server
     * When ViaVersion is reloaded, this method may not return some players.
     * May not contain ProtocolSupport players.
     *
     * @return map containing the UUIDs and frontend UserConnections from players connected to this proxy server
     */
    Map<UUID, UserConnection> getConnectedClients();

    void onLoginSuccess(UserConnection connection);

    void onDisconnect(UserConnection connection);
}
