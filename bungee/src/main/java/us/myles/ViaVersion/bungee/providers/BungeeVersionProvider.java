package us.myles.ViaVersion.bungee.providers;

import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.base.VersionProvider;
import us.myles.ViaVersion.util.InvokeUtil;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BungeeVersionProvider extends VersionProvider {
    private static final MethodHandle MH_VERSIONS;

    static {
        try {
            MH_VERSIONS = InvokeUtil.lookup().findStaticGetter(Class.forName("net.md_5.bungee.protocol.ProtocolConstants"), "SUPPORTED_VERSION_IDS", List.class);
        } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Override
    @SneakyThrows
    public int getServerProtocol(UserConnection user) {
        List<Integer> list = (List<Integer>) MH_VERSIONS.invokeExact();
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
        for (int protocol : Lists.reverse(sorted)) {
            if (info.getProtocolVersion() > protocol && ProtocolVersion.isRegistered(protocol))
                return protocol;
        }

        Via.getPlatform().getLogger().severe("Panic, no protocol id found for " + info.getProtocolVersion());
        return info.getProtocolVersion();
    }

    @SneakyThrows
    public static int getLowestSupportedVersion() {
        return ((List<Integer>)MH_VERSIONS.invokeExact()).get(0);
    }
}
