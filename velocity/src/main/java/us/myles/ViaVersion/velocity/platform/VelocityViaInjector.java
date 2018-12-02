package us.myles.ViaVersion.velocity.platform;

import io.netty.channel.ChannelInitializer;
import us.myles.ViaVersion.VelocityPlugin;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.platform.ViaInjector;
import us.myles.ViaVersion.util.ReflectionUtil;
import us.myles.ViaVersion.velocity.handlers.VelocityChannelInitializer;


public class VelocityViaInjector implements ViaInjector {
    @Override
    public void inject() throws Exception {
        Object connectionManager = ReflectionUtil.get(VelocityPlugin.PROXY, "cm", Object.class);
        Object channelInitializerHolder = ReflectionUtil.invoke(connectionManager, "getServerChannelInitializer");
        ChannelInitializer originalIntializer = (ChannelInitializer) ReflectionUtil.invoke(channelInitializerHolder, "get");
        channelInitializerHolder.getClass().getMethod("set", ChannelInitializer.class)
                .invoke(channelInitializerHolder, new VelocityChannelInitializer(originalIntializer));
    }

    @Override
    public void uninject() {
        Via.getPlatform().getLogger().severe("ViaVersion cannot remove itself from Velocity without a reboot!");
    }


    @Override
    public int getServerProtocolVersion() throws Exception {
        return getLowestSupportedProtocolVersion();
    }

    public static int getLowestSupportedProtocolVersion() {
        return com.velocitypowered.api.network.ProtocolVersion.MINIMUM_VERSION.getProtocol();
    }

    @Override
    public String getEncoderName() {
        return "via-encoder";
    }

    @Override
    public String getDecoderName() {
        return "via-decoder";
    }
}
