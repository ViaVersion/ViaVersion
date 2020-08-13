package us.myles.ViaVersion.bungee.providers;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.MainHandProvider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/*
    This solves the wrong mainhand issue when you join with BungeeCord on a 1.8 server, and switch to a 1.9 or higher.
 */
public class BungeeMainHandProvider extends MainHandProvider {
    private static Method getSettings = null;
    private static Method setMainHand = null;

    static {
        try {
            getSettings = Class.forName("net.md_5.bungee.UserConnection").getDeclaredMethod("getSettings");
            setMainHand = Class.forName("net.md_5.bungee.protocol.packet.ClientSettings").getDeclaredMethod("setMainHand", int.class);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void setMainHand(UserConnection user, int hand) {
        ProtocolInfo info = user.getProtocolInfo();
        if (info == null || info.getUuid() == null) return;
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(info.getUuid());
        if (player == null) return;
        try {
            Object settings = getSettings.invoke(player);
            if (settings != null) {
                setMainHand.invoke(settings, hand);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
