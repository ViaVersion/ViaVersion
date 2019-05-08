package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.storage.EntityTracker;

import static us.myles.ViaVersion.api.entities.Entity1_13Types.EntityType;

public class EntityTracker1_13 extends EntityTracker<EntityType> {

    public EntityTracker1_13(UserConnection user) {
        super(user, EntityType.PLAYER);
    }
}
