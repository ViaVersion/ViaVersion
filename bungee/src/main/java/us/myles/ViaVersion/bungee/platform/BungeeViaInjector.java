package us.myles.ViaVersion.bungee.platform;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.platform.ViaInjector;
import us.myles.ViaVersion.bungee.handlers.BungeeChannelInitializer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class BungeeViaInjector implements ViaInjector {
    @Override
    public void inject() throws Exception {
        try {
            try {

                Class<?> pipelineUtils = Class.forName("net.md_5.bungee.netty.PipelineUtils");
                Field field = pipelineUtils.getDeclaredField("SERVER_CHILD");
                field.setAccessible(true);
                // Remove any final stuff
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

                BungeeChannelInitializer newInit = new BungeeChannelInitializer((ChannelInitializer<Channel>) field.get(null));
                field.set(null, newInit);
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
        return "via-encoder";
    }
}
