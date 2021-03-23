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
package us.myles.ViaVersion.api.platform;

import io.netty.channel.ChannelFutureListener;
import org.jetbrains.annotations.Nullable;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles injected UserConnections
 */
public class ViaConnectionManager {
    protected final Map<UUID, UserConnection> clients = new ConcurrentHashMap<>();
    protected final Set<UserConnection> connections = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public void onLoginSuccess(UserConnection connection) {
        Objects.requireNonNull(connection, "connection is null!");
        connections.add(connection);

        if (isFrontEnd(connection)) {
            UUID id = connection.getProtocolInfo().getUuid();
            if (clients.put(id, connection) != null) {
                Via.getPlatform().getLogger().warning("Duplicate UUID on frontend connection! ("+id+")");
            }
        }

        if (connection.getChannel() != null) {
            connection.getChannel().closeFuture().addListener((ChannelFutureListener) future -> onDisconnect(connection));
        }
    }

    public void onDisconnect(UserConnection connection) {
        Objects.requireNonNull(connection, "connection is null!");
        connections.remove(connection);

        if (isFrontEnd(connection)) {
            UUID id = connection.getProtocolInfo().getUuid();
            clients.remove(id);
        }
    }

    /**
     * Frontend connections will have the UUID stored. Override this if your platform isn't always frontend.
     * UUIDs can't be duplicate between frontend connections.
     */
    public boolean isFrontEnd(UserConnection conn) {
        return !conn.isClientSide();
    }

    /**
     * Returns a map containing the UUIDs and frontend UserConnections from players connected to this proxy server
     * Returns empty list when there isn't a server
     * When ViaVersion is reloaded, this method may not return some players.
     * May not contain ProtocolSupport players.
     */
    public Map<UUID, UserConnection> getConnectedClients() {
        return Collections.unmodifiableMap(clients);
    }

    /**
     * Returns the frontend UserConnection from the player connected to this proxy server
     * Returns null when there isn't a server or connection was not found
     * When ViaVersion is reloaded, this method may not return some players.
     * May not return ProtocolSupport players.
     * <p>
     * Note that connections are removed as soon as their channel is closed,
     * so avoid using this method during player quits for example.
     */
    @Nullable
    public UserConnection getConnectedClient(UUID clientIdentifier) {
        return clients.get(clientIdentifier);
    }

    /**
     * Returns the UUID from the frontend connection to this proxy server
     * Returns null when there isn't a server or this connection isn't frontend or it doesn't have an id
     * When ViaVersion is reloaded, this method may not return some players.
     * May not return ProtocolSupport players.
     * <p>
     * Note that connections are removed as soon as their channel is closed,
     * so avoid using this method during player quits for example.
     */
    @Nullable
    public UUID getConnectedClientId(UserConnection conn) {
        if (conn.getProtocolInfo() == null) return null;
        UUID uuid = conn.getProtocolInfo().getUuid();
        UserConnection client = clients.get(uuid);
        if (conn.equals(client)) {
            // This is frontend
            return uuid;
        }
        return null;
    }

    /**
     * Returns all UserConnections which are registered
     * May contain duplicated UUIDs on multiple ProtocolInfo.
     * May contain frontend, backend and/or client-sided connections.
     * When ViaVersion is reloaded, this method may not return some players.
     * May not contain ProtocolSupport players.
     */
    public Set<UserConnection> getConnections() {
        return Collections.unmodifiableSet(connections);
    }

    /**
     * Returns if Via injected into this player connection.
     *
     * @param playerId player uuid
     * @return true if the player is handled by Via
     */
    public boolean isClientConnected(UUID playerId) {
        return clients.containsKey(playerId);
    }
}
