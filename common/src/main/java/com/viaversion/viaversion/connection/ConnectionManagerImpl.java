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
package com.viaversion.viaversion.connection;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ConnectionManager;
import com.viaversion.viaversion.api.connection.UserConnection;
import io.netty.channel.ChannelFutureListener;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManagerImpl implements ConnectionManager {
    protected final Map<UUID, UserConnection> clients = new ConcurrentHashMap<>();
    protected final Set<UserConnection> connections = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public void onLoginSuccess(UserConnection connection) {
        Objects.requireNonNull(connection, "connection is null!");
        connections.add(connection);

        if (isFrontEnd(connection)) {
            UUID id = connection.getProtocolInfo().getUuid();
            if (clients.put(id, connection) != null) {
                Via.getPlatform().getLogger().warning("Duplicate UUID on frontend connection! (" + id + ")");
            }
        }

        if (connection.getChannel() != null) {
            connection.getChannel().closeFuture().addListener((ChannelFutureListener) future -> onDisconnect(connection));
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
