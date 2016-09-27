package us.myles.ViaVersion.bungee.platform;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import net.md_5.bungee.netty.PipelineUtils;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.platform.ViaInjector;
import us.myles.ViaVersion.bungee.handlers.ViaVersionInitializer;
import us.myles.ViaVersion.util.ReflectionUtil;

public class BungeeViaInjector implements ViaInjector {
    @Override
    public void inject() throws Exception {
        try {
            try {
                ChannelInitializer<Channel> oldInit = PipelineUtils.SERVER_CHILD;
                ChannelInitializer newInit = new ViaVersionInitializer(oldInit);

                ReflectionUtil.setStatic(PipelineUtils.class, "SERVER_CHILD", newInit);
            } catch (NoSuchFieldException e) {
                throw new Exception("Unable to find core component 'childHandler', please check your plugins. issue: ");

            }
        } catch (Exception e) {
            Via.getPlatform().getLogger().severe("Unable to inject ViaVersion, please post these details on our GitHub and ensure you're using a compatible server version.");
            throw e;
        }
    }

    @Override
    public void uninject() {
        // TODO: Uninject from players currently online
        Via.getPlatform().getLogger().severe("ViaVersion cannot remove itself from Bungee without a reboot!");
    }


    @Override
    public int getServerProtocolVersion() throws Exception {
        return 47;
    }

    @Override
    public String getEncoderName() {
        return "packet-encoder";
    }
}
