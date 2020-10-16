package us.myles.ViaVersion.velocity.platform;

import com.google.gson.JsonObject;
import io.netty.channel.ChannelInitializer;
import us.myles.ViaVersion.VelocityPlugin;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.platform.ViaInjector;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;
import us.myles.ViaVersion.util.ReflectionUtil;
import us.myles.ViaVersion.velocity.handlers.VelocityChannelInitializer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class VelocityViaInjector implements ViaInjector {
    public static Method getPlayerInfoForwardingMode;

    static {
        try {
            getPlayerInfoForwardingMode = Class.forName("com.velocitypowered.proxy.config.VelocityConfiguration").getMethod("getPlayerInfoForwardingMode");
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private ChannelInitializer getInitializer() throws Exception {
        Object connectionManager = ReflectionUtil.get(VelocityPlugin.PROXY, "cm", Object.class);
        Object channelInitializerHolder = ReflectionUtil.invoke(connectionManager, "getServerChannelInitializer");
       return (ChannelInitializer) ReflectionUtil.invoke(channelInitializerHolder, "get");
    }

    @Override
    public void inject() throws Exception {
        Object connectionManager = ReflectionUtil.get(VelocityPlugin.PROXY, "cm", Object.class);
        Object channelInitializerHolder = ReflectionUtil.invoke(connectionManager, "getServerChannelInitializer");
        ChannelInitializer originalInitializer = getInitializer();
        channelInitializerHolder.getClass().getMethod("set", ChannelInitializer.class)
                .invoke(channelInitializerHolder, new VelocityChannelInitializer(originalInitializer));
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
        try {
            if (getPlayerInfoForwardingMode != null
                    && ((Enum<?>) getPlayerInfoForwardingMode.invoke(VelocityPlugin.PROXY.getConfiguration()))
                    .name().equals("MODERN")) return ProtocolVersion.v1_13.getVersion();
        } catch (IllegalAccessException | InvocationTargetException ignored) {
        }
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

    @Override
    public JsonObject getDump() {
        JsonObject data = new JsonObject();
        try {
            data.addProperty("currentInitializer", getInitializer().getClass().getName());
        } catch (Exception e) {
            // Ignored
        }
        return data;
    }
}
