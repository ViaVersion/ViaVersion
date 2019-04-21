package us.myles.ViaVersion.protocols.protocol1_14to1_13_2.storage;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.Setter;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_14Types;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EntityTracker extends StoredObject {
    private final Map<Integer, Entity1_14Types.EntityType> clientEntityTypes = new ConcurrentHashMap<>();
    private final Map<Integer, Byte> insentientData = new ConcurrentHashMap<>();
    @Getter
    @Setter
    private int latestTradeWindowId;

    public EntityTracker(UserConnection user) {
        super(user);
    }

    public void removeEntity(int entityId) {
        clientEntityTypes.remove(entityId);
        insentientData.remove(entityId);
    }

    public void addEntity(int entityId, UUID uuid, Entity1_14Types.EntityType type) {
        clientEntityTypes.put(entityId, type);
    }

    public byte getInsentientData(int entity) {
        Byte val = insentientData.get(entity);
        return val == null ? 0 : val;
    }

    public void setInsentientData(int entity, byte value) {
        insentientData.put(entity, value);
    }

    public boolean has(int entityId) {
        return clientEntityTypes.containsKey(entityId);
    }

    public Optional<Entity1_14Types.EntityType> get(int id) {
        return Optional.fromNullable(clientEntityTypes.get(id));
    }
}
