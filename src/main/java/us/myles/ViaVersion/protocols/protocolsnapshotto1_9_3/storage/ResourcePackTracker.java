package us.myles.ViaVersion.protocols.protocolsnapshotto1_9_3.storage;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
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
