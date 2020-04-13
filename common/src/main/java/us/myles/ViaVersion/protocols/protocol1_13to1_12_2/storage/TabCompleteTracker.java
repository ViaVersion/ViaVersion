package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;

public class TabCompleteTracker extends StoredObject {
    private int transactionId;
    private String input;
    private String lastTabComplete;
    private long timeToSend;

    public TabCompleteTracker(UserConnection user) {
        super(user);
    }

    public void sendPacketToServer() {
        if (lastTabComplete == null || timeToSend > System.currentTimeMillis()) return;
        PacketWrapper wrapper = new PacketWrapper(0x01, null, getUser());
        wrapper.write(Type.STRING, lastTabComplete);
        wrapper.write(Type.BOOLEAN, false);
        wrapper.write(Type.OPTIONAL_POSITION, null);
        try {
            wrapper.sendToServer(Protocol1_13To1_12_2.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        lastTabComplete = null;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public String getInput() {
        return input;
    }

    public void setInput(final String input) {
        this.input = input;
    }

    public String getLastTabComplete() {
        return lastTabComplete;
    }

    public void setLastTabComplete(String lastTabComplete) {
        this.lastTabComplete = lastTabComplete;
    }

    public long getTimeToSend() {
        return timeToSend;
    }

    public void setTimeToSend(long timeToSend) {
        this.timeToSend = timeToSend;
    }
}
