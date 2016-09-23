package us.myles.ViaVersion.protocols.protocol1_9to1_8.storage;

import lombok.Getter;
import lombok.Setter;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;

@Getter
@Setter
public class InventoryTracker extends StoredObject {
    private String inventory;

    public InventoryTracker(UserConnection user) {
        super(user);
    }
}
