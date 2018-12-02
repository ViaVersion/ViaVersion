package us.myles.ViaVersion.velocity.providers;

import com.google.common.collect.Lists;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.base.VersionProvider;
import us.myles.ViaVersion.velocity.platform.VelocityViaInjector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VelocityVersionProvider extends VersionProvider {

    @Override
    public int getServerProtocol(UserConnection user) throws Exception {
        // TODO Have one constant list forever until restart? (Might limit plugins if they change this)
        List<Integer> sorted = new ArrayList<>(com.velocitypowered.api.network.ProtocolVersion.ID_TO_PROTOCOL_CONSTANT.keySet());
        sorted.remove(Integer.valueOf(-1)); // Unknown/legacy
        Collections.sort(sorted);

        int playerVersion = user.get(ProtocolInfo.class).getProtocolVersion();

        // Bungee supports it
        if (sorted.contains(playerVersion))
            return playerVersion;

        // Older than bungee supports, get the lowest version
        if (playerVersion < sorted.get(0)) {
            return VelocityViaInjector.getLowestSupportedProtocolVersion();
        }

        // Loop through all protocols to get the closest protocol id that bungee supports (and that viaversion does too)

        // TODO: This needs a better fix, i.e checking ProtocolRegistry to see if it would work.
        // This is more of a workaround for snapshot support by bungee.
        for (Integer protocol : Lists.reverse(sorted)) {
            if (playerVersion > protocol && ProtocolVersion.isRegistered(protocol))
                return protocol;
        }

        Via.getPlatform().getLogger().severe("Panic, no protocol id found for " + playerVersion);
        return playerVersion;
    }
}
