package us.myles.ViaVersion.sponge.listeners;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import us.myles.ViaVersion.api.Via;

public class ClientLeaveListener {
    @Listener
    public void onDisconnect(ClientConnectionEvent.Disconnect disconnect) {
        Via.getManager().removePortedClient(disconnect.getTargetEntity().getUniqueId());
    }
}
