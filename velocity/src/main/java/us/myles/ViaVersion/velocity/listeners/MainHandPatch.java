package us.myles.ViaVersion.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.player.PlayerSettings;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9TO1_8;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.EntityTracker;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.lang.reflect.Method;

/*
    This solves the wrong mainhand issue when you join with BungeeCord on a 1.8 server, and switch to a 1.9 or higher.
 */
public class MainHandPatch {
    private static Method setSettings;

    static {
        try {
            Class clientSettings = Class.forName("com.velocitypowered.proxy.protocol.packet.ClientSettings");
            setSettings = Class.forName("com.velocitypowered.proxy.connection.client.ConnectedPlayer").getDeclaredMethod("setPlayerSettings", clientSettings);
            setSettings.setAccessible(true);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onServerConnect(ServerConnectedEvent event) {
        UserConnection user = Via.getManager().getConnection(event.getPlayer().getUniqueId());
        if (user == null || setSettings == null) return;

        try {
            if (user.get(ProtocolInfo.class).getPipeline().contains(Protocol1_9TO1_8.class)) {
                PlayerSettings settings = event.getPlayer().getPlayerSettings();
                if (user.has(EntityTracker.class)) {
                    Object clientSettings = ReflectionUtil.get(settings, "settings", Object.class);
                    ReflectionUtil.set(clientSettings, "mainHand", user.get(EntityTracker.class).getMainHand());
                    setSettings.invoke(event.getPlayer(), clientSettings);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
