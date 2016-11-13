package us.myles.ViaVersion.bungee.providers;

import com.google.common.collect.Lists;
import net.md_5.bungee.api.ProxyServer;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.base.VersionProvider;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.util.List;

public class BungeeVersionProvider extends VersionProvider {
    private static Class<?> ref;

    static {
        try {
            ref = Class.forName("net.md_5.bungee.protocol.ProtocolConstants");
        } catch (Exception e) {
            Via.getPlatform().getLogger().severe("Could not detect the ProtocolConstants class");
            e.printStackTrace();
        }
    }

    @Override
    public int getServerProtocol(UserConnection user) throws Exception {
        if (ref == null)
            return super.getServerProtocol(user);
        // TODO Have one constant list forever until restart? (Might limit plugins if they change this)
        List<Integer> list = ReflectionUtil.getStatic(ref, "SUPPORTED_VERSION_IDS", List.class);

        ProtocolInfo info = user.get(ProtocolInfo.class);

        // Bungee supports it
        if (list.contains(info.getProtocolVersion()))
            return info.getProtocolVersion();

        // Older than bungee supports, get the lowest version
        if (info.getProtocolVersion() < list.get(0)) {
            return getLowestSupportedVersion();
        }

        // Loop through all protocols to get the closest protocol id that bungee supports
        for (Integer protocol : Lists.reverse(list)) {
            if (info.getProtocolVersion() > protocol)
                return protocol;
        }

        Via.getPlatform().getLogger().severe("Panic, no protocol id found for " + info.getProtocolVersion());
        return info.getProtocolVersion();
    }

    public static int getLowestSupportedVersion() {
        List<Integer> list;
        try {
            list = ReflectionUtil.getStatic(ref, "SUPPORTED_VERSION_IDS", List.class);
            return list.get(0);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        // Fallback
        return ProxyServer.getInstance().getProtocolVersion();
    }
}
