package us.myles.ViaVersion.api.storage;

import org.jetbrains.annotations.Nullable;
import us.myles.ViaVersion.api.data.ExternalJoinGameListener;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.EntityType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class EntityTracker extends StoredObject implements ExternalJoinGameListener {
    private final Map<Integer, EntityType> clientEntityTypes = new ConcurrentHashMap<>();
    private final EntityType playerType;
    private int clientEntityId;

    protected EntityTracker(UserConnection user, EntityType playerType) {
        super(user);
        this.playerType = playerType;
    }

    public void removeEntity(int entityId) {
        clientEntityTypes.remove(entityId);
    }

    public void addEntity(int entityId, EntityType type) {
        clientEntityTypes.put(entityId, type);
    }

    public boolean hasEntity(int entityId) {
        return clientEntityTypes.containsKey(entityId);
    }

    @Nullable
    public EntityType getEntity(int entityId) {
        return clientEntityTypes.get(entityId);
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
