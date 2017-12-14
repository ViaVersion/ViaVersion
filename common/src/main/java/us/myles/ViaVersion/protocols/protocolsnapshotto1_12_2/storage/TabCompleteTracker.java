package us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.storage;

import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;

public class TabCompleteTracker extends StoredObject {
    private int transactionId;

    public TabCompleteTracker(UserConnection user) {
        super(user);
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }
}
