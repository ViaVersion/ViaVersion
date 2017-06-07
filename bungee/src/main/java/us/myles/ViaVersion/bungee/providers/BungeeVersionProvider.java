package us.myles.ViaVersion.bungee.providers;

import com.google.common.collect.Lists;
import net.md_5.bungee.api.ProxyServer;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.base.VersionProvider;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.util.ArrayList;
import java.util.Collections;
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
        List<Integer> sorted = new ArrayList<>(list);
        Collections.sort(sorted);

        ProtocolInfo info = user.get(ProtocolInfo.class);

        // Bungee supports it
        if (sorted.contains(info.getProtocolVersion()))
            return info.getProtocolVersion();

        // Older than bungee supports, get the lowest version
        if (info.getProtocolVersion() < sorted.get(0)) {
            return getLowestSupportedVersion();
        }

        // Loop through all protocols to get the closest protocol id that bungee supports (and that viaversion does too)

        // TODO: This needs a better fix, i.e checking ProtocolRegistry to see if it would work.
        // This is more of a workaround for snapshot support by bungee.
        for (Integer protocol : Lists.reverse(sorted)) {
            if (info.getProtocolVersion() > protocol && ProtocolVersion.isRegistered(protocol))
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
