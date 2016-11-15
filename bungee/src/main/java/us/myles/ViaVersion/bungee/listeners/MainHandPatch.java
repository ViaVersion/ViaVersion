package us.myles.ViaVersion.bungee.listeners;

import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9TO1_8;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.EntityTracker;

import java.lang.reflect.Method;

/*
    This solves the wrong mainhand issue when you join with BungeeCord on a 1.8 server, and switch to a 1.9 or higher.
 */
public class MainHandPatch implements Listener {
    private static Method getSettings = null;
    private static Method setMainHand = null;

    static {
        try {
            getSettings = Class.forName("net.md_5.bungee.UserConnection").getDeclaredMethod("getSettings");
            setMainHand = Class.forName("net.md_5.bungee.protocol.packet.ClientSettings").getDeclaredMethod("setMainHand", int.class);
        } catch (Exception ignored) {
        }
    }

    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        // Ignore if it doesn't exist (Like BungeeCord 1.8)
        if (setMainHand == null)
            return;

        UserConnection user = Via.getManager().getConnection(event.getPlayer().getUniqueId());
        if(user == null) return;
        
        try {
            if (user.get(ProtocolInfo.class).getPipeline().contains(Protocol1_9TO1_8.class)) {
                Object settings = getSettings.invoke(event.getPlayer());
                setMainHand.invoke(settings, user.get(EntityTracker.class).getMainHand());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
