package us.myles.ViaVersion.velocity.providers;

import com.google.common.collect.Lists;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.base.VersionProvider;
import us.myles.ViaVersion.velocity.platform.VelocityViaInjector;

import java.util.*;
import java.util.stream.Collectors;

public class VelocityVersionProvider extends VersionProvider {
    private static final List<Integer> VELOCITY_PROTOCOLS = com.velocitypowered.api.network.ProtocolVersion.SUPPORTED_VERSIONS.stream()
            .map(com.velocitypowered.api.network.ProtocolVersion::getProtocol)
            .collect(Collectors.toList());

    @Override
    public int getServerProtocol(UserConnection user) throws Exception {
        int playerVersion = user.get(ProtocolInfo.class).getProtocolVersion();

        // Bungee supports it
        if (Collections.binarySearch(VELOCITY_PROTOCOLS, playerVersion) >= 0)
            return playerVersion;

        // Older than bungee supports, get the lowest version
        if (playerVersion < VELOCITY_PROTOCOLS.get(0)) {
            return VelocityViaInjector.getLowestSupportedProtocolVersion();
        }

        // Loop through all protocols to get the closest protocol id that bungee supports (and that viaversion does too)

        // TODO: This needs a better fix, i.e checking ProtocolRegistry to see if it would work.
        // This is more of a workaround for snapshot support by bungee.
        for (Integer protocol : Lists.reverse(VELOCITY_PROTOCOLS)) {
            if (playerVersion > protocol && ProtocolVersion.isRegistered(protocol))
                return protocol;
        }

        Via.getPlatform().getLogger().severe("Panic, no protocol id found for " + playerVersion);
        return playerVersion;
    }
}
