package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage;

import lombok.Getter;
import lombok.Setter;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.platform.TaskId;

@Getter
@Setter
public class TabCompleteTracker extends StoredObject {
    private int transactionId;
    private String input;
    private TaskId lastTabCompleteTask;

    public TabCompleteTracker(UserConnection user) {
        super(user);
    }
}
