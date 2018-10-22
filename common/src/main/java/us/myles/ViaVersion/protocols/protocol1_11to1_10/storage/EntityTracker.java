package us.myles.ViaVersion.protocols.protocol1_11to1_10.storage;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_11Types;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EntityTracker extends StoredObject {
    private final Map<Integer, Entity1_11Types.EntityType> clientEntityTypes = new ConcurrentHashMap<>();
    private final Set<Integer> holograms = Sets.newConcurrentHashSet();

    public EntityTracker(UserConnection user) {
        super(user);
    }

    public void removeEntity(int entityId) {
        clientEntityTypes.remove(entityId);
        if (isHologram(entityId))
            removeHologram(entityId);
    }

    public void addEntity(int entityId, Entity1_11Types.EntityType type) {
        clientEntityTypes.put(entityId, type);
    }

    public boolean has(int entityId) {
        return clientEntityTypes.containsKey(entityId);
    }

    public Optional<Entity1_11Types.EntityType> get(int id) {
        return Optional.fromNullable(clientEntityTypes.get(id));
    }

    public void addHologram(int entId) {
        holograms.add(entId);
    }

    public boolean isHologram(int entId) {
        return holograms.contains(entId);
    }

    public void removeHologram(int entId) {
        holograms.remove(entId);
    }
}
