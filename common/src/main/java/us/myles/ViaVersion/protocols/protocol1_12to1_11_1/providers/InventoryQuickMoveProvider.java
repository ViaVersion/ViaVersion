package us.myles.ViaVersion.protocols.protocol1_12to1_11_1.providers;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.platform.providers.Provider;

public class InventoryQuickMoveProvider implements Provider {

    public boolean registerQuickMove(short windowId, short slotId, short actionId, UserConnection userConnection) {
        return false; // not supported :/ plays very sad violin
    }
}