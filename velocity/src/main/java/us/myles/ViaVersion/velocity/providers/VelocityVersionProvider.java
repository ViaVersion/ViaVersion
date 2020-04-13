package us.myles.ViaVersion.velocity.providers;

import com.velocitypowered.api.proxy.ServerConnection;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ServerChannel;
import us.myles.ViaVersion.VelocityPlugin;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;
import us.myles.ViaVersion.protocols.base.VersionProvider;
import us.myles.ViaVersion.velocity.platform.VelocityViaInjector;
import us.myles.ViaVersion.velocity.service.ProtocolDetectorService;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.IntStream;

public class VelocityVersionProvider extends VersionProvider {
    private static Method getAssociation;

    static {
        try {
            getAssociation = Class.forName("com.velocitypowered.proxy.connection.MinecraftConnection").getMethod("getAssociation");
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getServerProtocol(UserConnection user) throws Exception {
        return user.isClientSide() ? getBackProtocol(user) : getFrontProtocol(user);
    }

    private int getBackProtocol(UserConnection user) throws Exception {
        System.out.println("backend protocol!");
        ChannelHandler mcHandler = user.getChannel().pipeline().get("handler");
        return ProtocolDetectorService.getProtocolId(
                ((ServerConnection) getAssociation.invoke(mcHandler)).getServerInfo().getName());
    }

    private int getFrontProtocol(UserConnection user) throws Exception {
        System.out.println("frontend protocol!");
        int playerVersion = user.get(ProtocolInfo.class).getProtocolVersion();

        IntStream versions = com.velocitypowered.api.network.ProtocolVersion.SUPPORTED_VERSIONS.stream()
                .mapToInt(com.velocitypowered.api.network.ProtocolVersion::getProtocol);

        // Modern forwarding mode needs 1.13 Login plugin message
        if (VelocityViaInjector.getPlayerInfoForwardingMode != null
                && ((Enum<?>) VelocityViaInjector.getPlayerInfoForwardingMode.invoke(VelocityPlugin.PROXY.getConfiguration()))
                .name().equals("MODERN")) {
            versions = versions.filter(ver -> ver >= ProtocolVersion.v1_13.getVersion());
        }
        int[] compatibleProtocols = versions.toArray();

        // Bungee supports it
        if (Arrays.binarySearch(compatibleProtocols, playerVersion) >= 0)
            return playerVersion;

        // Older than bungee supports, get the lowest version
        if (playerVersion < compatibleProtocols[0]) {
            return compatibleProtocols[0];
        }

        // Loop through all protocols to get the closest protocol id that bungee supports (and that viaversion does too)

        // TODO: This needs a better fix, i.e checking ProtocolRegistry to see if it would work.
        // This is more of a workaround for snapshot support by bungee.
        for (int i = compatibleProtocols.length - 1; i >= 0; i--) {
            int protocol = compatibleProtocols[i];
            if (playerVersion > protocol && ProtocolVersion.isRegistered(protocol))
                return protocol;
        }

        Via.getPlatform().getLogger().severe("Panic, no protocol id found for " + playerVersion);
        return playerVersion;
    }
}
