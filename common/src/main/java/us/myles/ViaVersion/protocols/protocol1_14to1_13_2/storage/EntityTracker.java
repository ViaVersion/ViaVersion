package us.myles.ViaVersion.protocols.protocol1_14to1_13_2.storage;

import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_14Types;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EntityTracker extends StoredObject {
    private final Map<Integer, Entity1_14Types.EntityType> clientEntityTypes = new ConcurrentHashMap<>();
    private final BiMap<UUID, Integer> uuidToId = Maps.synchronizedBiMap(HashBiMap.<UUID, Integer>create());

    public EntityTracker(UserConnection user) {
        super(user);
    }

    public void removeEntity(int entityId) {
        uuidToId.inverse().remove(entityId);
        clientEntityTypes.remove(entityId);
    }

    public void addEntity(int entityId, UUID uuid, Entity1_14Types.EntityType type) {
        uuidToId.forcePut(uuid, entityId);
        clientEntityTypes.put(entityId, type);
    }

    public boolean has(int entityId) {
        return clientEntityTypes.containsKey(entityId);
    }

    public Optional<Entity1_14Types.EntityType> get(int id) {
        return Optional.fromNullable(clientEntityTypes.get(id));
    }

    public Optional<UUID> getUUID(int id) {
        return Optional.fromNullable(uuidToId.inverse().get(id));
    }
}
