package us.myles.ViaVersion.api.platform;

import us.myles.ViaVersion.api.ViaAPI;
import us.myles.ViaVersion.api.ViaVersionConfig;
import us.myles.ViaVersion.api.command.ViaCommandSender;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * ViaPlatform represents a platform ViaVersion runs on
 *
 * @param <T> - The player type for the platform, used for API related methods
 */
public interface ViaPlatform<T> {
    public Logger getLogger();

    public String getPlatformName();

    public String getPluginVersion();

    public void runAsync(Runnable runnable);

    public void runSync(Runnable runnable);

    public ViaCommandSender[] getOnlinePlayers();

    public void sendMessage(UUID uuid, String message);

    public boolean kickPlayer(UUID uuid, String message);

    public boolean isPluginEnabled();

    public ViaAPI<T> getApi();

    public ViaVersionConfig getConf();

    public void onReload();
}
