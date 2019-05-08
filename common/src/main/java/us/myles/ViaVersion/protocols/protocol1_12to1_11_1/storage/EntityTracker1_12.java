package us.myles.ViaVersion.protocols.protocol1_12to1_11_1.storage;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.storage.EntityTracker;

import static us.myles.ViaVersion.api.entities.Entity1_12Types.EntityType;

public class EntityTracker1_12 extends EntityTracker<EntityType> {

    public EntityTracker1_12(UserConnection user) {
        super(user, EntityType.PLAYER);
    }
}
