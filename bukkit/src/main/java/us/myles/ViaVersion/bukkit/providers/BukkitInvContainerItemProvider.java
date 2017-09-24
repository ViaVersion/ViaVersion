package us.myles.ViaVersion.bukkit.providers;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.protocols.protocol1_12to1_11_1.providers.InvContainerItemProvider;

public class BukkitInvContainerItemProvider extends InvContainerItemProvider {

    @Override
    public boolean registerInvClickPacket(short windowId, short slotId, short anumber, UserConnection uconnection) {
        // TODO: lets add some stuff here :)
        //http://wiki.vg/index.php?title=Protocol&oldid=13223#Click_Window
        System.out.println("QUICK ACTION windowId: " + windowId + " slotId: " + slotId + " button: " + 0 + " anumber: " + anumber + " mode: " + 1);
        return false; // change to true once supported
    }
}