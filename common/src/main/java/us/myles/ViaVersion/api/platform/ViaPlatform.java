package us.myles.ViaVersion.api.platform;

import java.util.UUID;
import java.util.logging.Logger;

public interface ViaPlatform {
    public Logger getLogger();

    public String getPlatformName();

    public String getPluginVersion();

    public void runAsync(Runnable runnable);

    public void runSync(Runnable runnable);

    public void sendMessage(UUID uuid, String message);

    public boolean kickPlayer(UUID uuid, String message);

    public boolean isPluginEnabled();
}
