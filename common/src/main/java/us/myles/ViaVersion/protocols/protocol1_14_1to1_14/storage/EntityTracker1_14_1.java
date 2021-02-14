package us.myles.ViaVersion.protocols.protocol1_14_1to1_14.storage;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_14Types;
import us.myles.ViaVersion.api.storage.EntityTracker;

public class EntityTracker1_14_1 extends EntityTracker {

    public EntityTracker1_14_1(UserConnection user) {
        super(user, Entity1_14Types.PLAYER);
    }
}
