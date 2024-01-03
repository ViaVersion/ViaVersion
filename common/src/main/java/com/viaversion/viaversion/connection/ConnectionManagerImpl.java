/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.connection;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ConnectionManager;
import com.viaversion.viaversion.api.connection.UserConnection;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ConnectionManagerImpl implements ConnectionManager {
    protected final Map<UUID, UserConnection> clients = new ConcurrentHashMap<>();
    protected final Set<UserConnection> connections = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public void onLoginSuccess(UserConnection connection) {
        Objects.requireNonNull(connection, "connection is null!");
        Channel channel = connection.getChannel();

        // This user has already disconnected...
        if (channel != null && !channel.isOpen()) return;

        boolean newlyAdded = connections.add(connection);

        if (isFrontEnd(connection)) {
            UUID id = connection.getProtocolInfo().getUuid();
            UserConnection previous = clients.put(id, connection);
            if (previous != null && previous != connection) {
                Via.getPlatform().getLogger().warning("Duplicate UUID on frontend connection! (" + id + ")");
            }
        }

        if (channel != null) {
            // We managed to add a user that had already disconnected!
            // Let's clean up the mess here and now
            if (!channel.isOpen()) {
                onDisconnect(connection);
            } else if (newlyAdded) { // Setup to clean-up on disconnect
                channel.closeFuture().addListener((ChannelFutureListener) future -> onDisconnect(connection));
            }
        }
    }

    @Override
    public void onDisconnect(UserConnection connection) {
        Objects.requireNonNull(connection, "connection is null!");
        connections.remove(connection);

        if (isFrontEnd(connection)) {
            UUID id = connection.getProtocolInfo().getUuid();
            clients.remove(id);
        }

        connection.clearStoredObjects();
    }

    @Override
    public Map<UUID, UserConnection> getConnectedClients() {
        return Collections.unmodifiableMap(clients);
    }

    @Override
    public @Nullable UserConnection getConnectedClient(UUID clientIdentifier) {
        return clients.get(clientIdentifier);
    }

    @Override
    public @Nullable UUID getConnectedClientId(UserConnection connection) {
        if (connection.getProtocolInfo() == null) return null;
        UUID uuid = connection.getProtocolInfo().getUuid();
        UserConnection client = clients.get(uuid);
        if (connection.equals(client)) {
            // This is frontend
            return uuid;
        }
        return null;
    }

    @Override
    public Set<UserConnection> getConnections() {
        return Collections.unmodifiableSet(connections);
    }

    @Override
    public boolean isClientConnected(UUID playerId) {
        return clients.containsKey(playerId);
    }
}
