package us.myles.ViaVersion.bukkit.providers;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.protocols.protocol1_12to1_11_1.providers.InvContainerItemProvider;

public class BukkitInvContainerItemProvider extends InvContainerItemProvider {

    @Override
    public boolean registerInvClickPacket(int windowId, short slotId, byte button, short amumber, int mode, UserConnection uconnection) {
        // TODO: lets add some stuff here :)
        return true;
    }
}