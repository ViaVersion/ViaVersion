package us.myles.ViaVersion.bungee.providers;

import com.google.common.collect.Lists;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.base.VersionProvider;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.util.List;

public class BungeeVersionProvider extends VersionProvider {
    private static Class<?> ref;

    public BungeeVersionProvider() {
        try {
            ref = Class.forName("net.md_5.bungee.protocol.ProtocolConstants");
        } catch (Exception e) {
            System.out.println("Could not detect the ProtocolConstants class");
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
            return list.get(0);
        }

        // Loop through all protocols to get the closest protocol id that bungee supports
        for (Integer protocol : Lists.reverse(list)) {
            if (info.getProtocolVersion() > protocol)
                return protocol;
        }

        System.out.println("Panic, no protocol id found for " + info.getProtocolVersion());
        return info.getProtocolVersion();
    }
}
