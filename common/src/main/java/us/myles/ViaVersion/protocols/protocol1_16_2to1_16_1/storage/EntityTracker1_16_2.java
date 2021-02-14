package us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.storage;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_16_2Types;
import us.myles.ViaVersion.api.storage.EntityTracker;

public class EntityTracker1_16_2 extends EntityTracker {

    public EntityTracker1_16_2(UserConnection user) {
        super(user, Entity1_16_2Types.PLAYER);
    }
}
