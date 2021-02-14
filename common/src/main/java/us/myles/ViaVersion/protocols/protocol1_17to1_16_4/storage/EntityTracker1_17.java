package us.myles.ViaVersion.protocols.protocol1_17to1_16_4.storage;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_17Types;
import us.myles.ViaVersion.api.storage.EntityTracker;

public class EntityTracker1_17 extends EntityTracker {

    public EntityTracker1_17(UserConnection user) {
        super(user, Entity1_17Types.PLAYER);
    }
}
