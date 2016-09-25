package us.myles.ViaVersion.api.platform;

import com.google.gson.JsonObject;
import us.myles.ViaVersion.api.ViaAPI;
import us.myles.ViaVersion.api.ViaVersionConfig;
import us.myles.ViaVersion.api.command.ViaCommandSender;
import us.myles.ViaVersion.api.configuration.ConfigurationProvider;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * ViaPlatform represents a platform ViaVersion runs on
 *
 * @param <T> - The player type for the platform, used for API related methods
 */
public interface ViaPlatform<T> {
    Logger getLogger();

    String getPlatformName();

    String getPluginVersion();

    int runAsync(Runnable runnable);

    int runSync(Runnable runnable);

    int runRepeatingSync(Runnable runnable, Long ticks);

    void cancelTask(int taskId);

    ViaCommandSender[] getOnlinePlayers();

    void sendMessage(UUID uuid, String message);

    boolean kickPlayer(UUID uuid, String message);

    boolean isPluginEnabled();

    ViaAPI<T> getApi();

    ViaVersionConfig getConf();

    ConfigurationProvider getConfigurationProvider();

    void onReload();

    JsonObject getDump();
}
