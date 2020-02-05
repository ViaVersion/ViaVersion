package us.myles.ViaVersion.protocols.protocol1_16to1_15_2.storage;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_15Types.EntityType;
import us.myles.ViaVersion.api.storage.EntityTracker;

public class EntityTracker1_16 extends EntityTracker {

    public EntityTracker1_16(UserConnection user) {
        super(user, EntityType.PLAYER);
    }
}
