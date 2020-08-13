package us.myles.ViaVersion.bungee.platform;

import com.google.gson.JsonObject;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.platform.ViaInjector;
import us.myles.ViaVersion.bungee.handlers.BungeeChannelInitializer;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

public class BungeeViaInjector implements ViaInjector {

    @Override
    public void inject() throws Exception {
        try {
            Class<?> pipelineUtils = Class.forName("net.md_5.bungee.netty.PipelineUtils");
            Field field = pipelineUtils.getDeclaredField("SERVER_CHILD");
            field.setAccessible(true);

            // Remove the final modifier (unless removed by a fork)
            int modifiers = field.getModifiers();
            if (Modifier.isFinal(modifiers)) {
                try {
                    Field modifiersField = Field.class.getDeclaredField("modifiers");
                    modifiersField.setAccessible(true);
                    modifiersField.setInt(field, modifiers & ~Modifier.FINAL);
                } catch (NoSuchFieldException e) {
                    // Java 12 compatibility *this is fine*
                    Method getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
                    getDeclaredFields0.setAccessible(true);
                    Field[] fields = (Field[]) getDeclaredFields0.invoke(Field.class, false);
                    for (Field classField : fields) {
                        if ("modifiers".equals(classField.getName())) {
                            classField.setAccessible(true);
                            classField.set(field, modifiers & ~Modifier.FINAL);
                            break;
                        }
                    }
                }
            }

            BungeeChannelInitializer newInit = new BungeeChannelInitializer((ChannelInitializer<Channel>) field.get(null));
            field.set(null, newInit);
        } catch (Exception e) {
            Via.getPlatform().getLogger().severe("Unable to inject ViaVersion, please post these details on our GitHub and ensure you're using a compatible server version.");
            throw e;
        }
    }

    @Override
    public void uninject() {
        Via.getPlatform().getLogger().severe("ViaVersion cannot remove itself from Bungee without a reboot!");
    }


    @Override
    public int getServerProtocolVersion() throws Exception {
        return (int) ReflectionUtil.getStatic(Class.forName("net.md_5.bungee.protocol.ProtocolConstants"), "SUPPORTED_VERSION_IDS", List.class).get(0);
    }

    @Override
    public String getEncoderName() {
        return "via-encoder";
    }

    @Override
    public String getDecoderName() {
        return "via-decoder";
    }

    private ChannelInitializer<Channel> getChannelInitializer() throws Exception {
        Class<?> pipelineUtils = Class.forName("net.md_5.bungee.netty.PipelineUtils");
        Field field = pipelineUtils.getDeclaredField("SERVER_CHILD");
        field.setAccessible(true);
        return (ChannelInitializer<Channel>) field.get(null);
    }

    @Override
    public JsonObject getDump() {
        JsonObject data = new JsonObject();
        try {
            ChannelInitializer<Channel> initializer = getChannelInitializer();
            data.addProperty("currentInitializer", initializer.getClass().getName());
            if (initializer instanceof BungeeChannelInitializer) {
                data.addProperty("originalInitializer", ((BungeeChannelInitializer) initializer).getOriginal().getClass().getName());
            }
        } catch (Exception e) {
            // Ignored, not printed in the dump
        }
        return data;
    }
}
