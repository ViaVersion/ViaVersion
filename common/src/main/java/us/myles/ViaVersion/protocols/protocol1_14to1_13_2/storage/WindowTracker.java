package us.myles.ViaVersion.protocols.protocol1_14to1_13_2.storage;

import lombok.Data;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;

@Data
public class WindowTracker extends StoredObject {
    // Used only when chest size is oversized or 0
    private int currentChestSize = -1;
    private int chestId = -1;
    private int currentPage = 0;

    public WindowTracker(UserConnection user) {
        super(user);
    }

    public void reset() {
        setCurrentChestSize(-1);
        setChestId(-1);
        setCurrentPage(0);
    }
}
