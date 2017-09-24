package us.myles.ViaVersion.protocols.protocol1_12to1_11_1.providers;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.platform.providers.Provider;

public class InvContainerItemProvider implements Provider {

    public boolean registerInvClickPacket(int windowId, short slotId, byte button, short amumber, int mode, UserConnection uconnection) {
        return false; // not supported :/ plays very sad violin
    }
}