package us.myles.ViaVersion2.api.protocol1_9to1_8.storage;

import lombok.Getter;
import org.bukkit.entity.EntityType;
import us.myles.ViaVersion2.api.data.StoredObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class EntityTracker extends StoredObject{
    private final Map<Integer, UUID> uuidMap = new HashMap<>();
    private final Map<Integer, EntityType> clientEntityTypes = new HashMap<>();
    private final Map<Integer, Integer> vehicleMap = new HashMap<>();

    public UUID getEntityUUID(int id) {
        if (uuidMap.containsKey(id)) {
            return uuidMap.get(id);
        } else {
            UUID uuid = UUID.randomUUID();
            uuidMap.put(id, uuid);
            return uuid;
        }
    }
}
