package us.myles.ViaVersion.bungee.providers;

import lombok.SneakyThrows;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.MainHandProvider;
import us.myles.ViaVersion.util.InvokeUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/*
    This solves the wrong mainhand issue when you join with BungeeCord on a 1.8 server, and switch to a 1.9 or higher.
 */
public class BungeeMainHandProvider extends MainHandProvider {
    private static final MethodHandle getSettings;
    private static MethodHandle setMainHand ;

    static {
        try {
            MethodHandles.Lookup lookup = InvokeUtil.lookup();
            Class<?> settingsClass = Class.forName("net.md_5.bungee.protocol.packet.ClientSettings");
            getSettings = lookup.findVirtual(Class.forName("net.md_5.bungee.UserConnection"), "getSettings", MethodType.methodType(settingsClass));
            setMainHand = lookup.findVirtual(settingsClass, "setMainHand", MethodType.methodType(Void.TYPE, Integer.TYPE));
        } catch (NoSuchMethodException | IllegalAccessException | ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Override
    @SneakyThrows
    public void setMainHand(UserConnection user, int hand) {
        ProtocolInfo info = user.get(ProtocolInfo.class);
        if (info == null || info.getUuid() == null) return;
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(info.getUuid());
        if (player == null) return;
        Object settings = getSettings.invoke(player);
        if (settings != null) {
            setMainHand.invoke(settings, hand);
        }
    }
}
