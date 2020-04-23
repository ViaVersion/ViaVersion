package us.myles.ViaVersion.api.platform;

import io.netty.channel.ChannelFutureListener;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;

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
        UUID id = connection.get(ProtocolInfo.class).getUuid();
        connections.add(connection);
        clients.put(id, connection);

        connection.getChannel().closeFuture().addListener((ChannelFutureListener) future -> onDisconnect(connection));
    }

    public void onDisconnect(UserConnection connection) {
        Objects.requireNonNull(connection, "connection is null!");
        UUID id = connection.get(ProtocolInfo.class).getUuid();
        connections.remove(connection);
        clients.remove(id);
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
     */
    public UserConnection getConnectedClient(UUID clientIdentifier) {
        return clients.get(clientIdentifier);
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

    public boolean isClientConnected(UUID playerId) {
        return clients.containsKey(playerId);
    }
}
