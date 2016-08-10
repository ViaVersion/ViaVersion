package us.myles.ViaVersion.protocols.protocolsnapshotto1_10.storage;

import lombok.Getter;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class EntityTracker extends StoredObject{
    private final Map<Integer, Integer> clientEntityTypes = new ConcurrentHashMap<>();

    public EntityTracker(UserConnection user) {
        super(user);
    }

    public void removeEntity(Integer entityID) {
        clientEntityTypes.remove(entityID);
    }
}
