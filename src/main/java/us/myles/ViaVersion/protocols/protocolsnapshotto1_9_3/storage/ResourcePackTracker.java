package us.myles.ViaVersion.protocols.protocolsnapshotto1_9_3.storage;

import lombok.*;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;

@Getter
@Setter
@ToString
public class ResourcePackTracker extends StoredObject {
    private String lastHash = "";

    public ResourcePackTracker(UserConnection user) {
        super(user);
    }
}
