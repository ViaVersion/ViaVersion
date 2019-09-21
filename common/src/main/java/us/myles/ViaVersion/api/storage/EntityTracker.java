package us.myles.ViaVersion.api.storage;

import us.myles.ViaVersion.api.data.ExternalJoinGameListener;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.EntityType;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public abstract class EntityTracker<T extends EntityType> extends StoredObject implements ExternalJoinGameListener {
    private final Map<Integer, T> clientEntityTypes = new ConcurrentHashMap<>();
    private int clientEntityId;
    private final T playerType;

    protected EntityTracker(UserConnection user, T playerType) {
        super(user);
        this.playerType = playerType;
    }

    public void removeEntity(int entityId) {
        clientEntityTypes.remove(entityId);
    }

    public void addEntity(int entityId, T type) {
        clientEntityTypes.put(entityId, type);
    }

    public boolean hasEntity(int entityId) {
        return clientEntityTypes.containsKey(entityId);
    }

    public Optional<T> getEntity(int entityId) {
        return Optional.ofNullable(clientEntityTypes.get(entityId));
    }

    @Override
    public void onExternalJoinGame(int playerEntityId) {
        clientEntityId = playerEntityId;
        clientEntityTypes.put(playerEntityId, playerType);
    }

    public int getClientEntityId() {
        return clientEntityId;
    }

    public void setClientEntityId(int clientEntityId) {
        this.clientEntityId = clientEntityId;
    }
}
