package us.myles.ViaVersion2.api.protocol1_9to1_8.storage;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.EntityType;
import us.myles.ViaVersion.api.boss.BossBar;
import us.myles.ViaVersion2.api.data.StoredObject;

import java.util.*;

@Getter
public class EntityTracker extends StoredObject{
    private final Map<Integer, UUID> uuidMap = new HashMap<>();
    private final Map<Integer, EntityType> clientEntityTypes = new HashMap<>();
    private final Map<Integer, Integer> vehicleMap = new HashMap<>();
    private final Map<Integer, BossBar> bossBarMap = new HashMap<>();
    private final Set<Integer> validBlocking = new HashSet<>();
    private final Set<Integer> knownHolograms = new HashSet<>();
    @Setter
    private int entityID;

    public UUID getEntityUUID(int id) {
        if (uuidMap.containsKey(id)) {
            return uuidMap.get(id);
        } else {
            UUID uuid = UUID.randomUUID();
            uuidMap.put(id, uuid);
            return uuid;
        }
    }

    public void removeEntity(Integer entityID) {
        clientEntityTypes.remove(entityID);
        vehicleMap.remove(entityID);
        uuidMap.remove(entityID);
        validBlocking.remove(entityID);
        knownHolograms.remove(entityID);

        BossBar bar = bossBarMap.remove(entityID);
        if (bar != null) {
            bar.hide();
        }
    }
}
