package us.myles.ViaVersion.protocols.protocol1_12to1_11_1.storage;

public class ItemTransaction {
    private final short windowId;
    private final short slotId;
    private final short actionId;

    public ItemTransaction(short windowId, short slotId, short actionId) {
        this.windowId = windowId;
        this.slotId = slotId;
        this.actionId = actionId;
    }

    public short getWindowId() {
        return windowId;
    }

    public short getSlotId() {
        return slotId;
    }

    public short getActionId() {
        return actionId;
    }

    @Override
    public String toString() {
        return "ItemTransaction{" +
                "windowId=" + windowId +
                ", slotId=" + slotId +
                ", actionId=" + actionId +
                '}';
    }
}