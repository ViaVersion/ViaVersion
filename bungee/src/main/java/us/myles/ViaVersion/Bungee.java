package us.myles.ViaVersion;

import com.google.gson.JsonObject;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.ViaAPI;
import us.myles.ViaVersion.api.ViaVersionConfig;
import us.myles.ViaVersion.api.command.ViaCommandSender;
import us.myles.ViaVersion.api.configuration.ConfigurationProvider;
import us.myles.ViaVersion.api.platform.ViaPlatform;
import us.myles.ViaVersion.bungee.BungeeViaAPI;
import us.myles.ViaVersion.bungee.BungeeViaInjector;
import us.myles.ViaVersion.bungee.BungeeViaLoader;
import us.myles.ViaVersion.bungee.command.BungeeCommandSender;
import us.myles.ViaVersion.bungee.config.BungeeConfigProvider;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Bungee extends Plugin implements ViaPlatform {

    private BungeeViaAPI api;
    private BungeeConfigProvider config;

    @Override
    public void onLoad() {
        api = new BungeeViaAPI();
        config = new BungeeConfigProvider();
        // Init platform
        Via.init(ViaManager.builder()
                .platform(this)
                .injector(new BungeeViaInjector())
                .loader(new BungeeViaLoader())
                .build());
    }

    @Override
    public void onEnable() {
        // Inject
        Via.getManager().init();
    }

    @Override
    public String getPlatformName() {
        return "BungeeCord";
    }

    @Override
    public String getPluginVersion() {
        return getDescription().getVersion();
    }

    @Override
    public int runAsync(Runnable runnable) {
        return getProxy().getScheduler().runAsync(this, runnable).getId();
    }

    @Override
    public int runSync(Runnable runnable) {
        return getProxy().getScheduler().runAsync(this, runnable).getId(); // TODO don't run sync @ Bungee?
    }

    @Override
    public int runRepeatingSync(Runnable runnable, Long ticks) {
        return getProxy().getScheduler().schedule(this, runnable, 0, ticks * 50, TimeUnit.MILLISECONDS).getId();
    }

    @Override
    public void cancelTask(int taskId) {
        getProxy().getScheduler().cancel(taskId);
    }

    @Override
    public ViaCommandSender[] getOnlinePlayers() {
        ViaCommandSender[] array = new ViaCommandSender[getProxy().getPlayers().size()];
        int i = 0;
        for (ProxiedPlayer player : getProxy().getPlayers()) {
            array[i++] = new BungeeCommandSender(player);
        }
        return array;
    }

    @Override
    public void sendMessage(UUID uuid, String message) {
        getProxy().getPlayer(uuid).sendMessage(new TextComponent(message));
    }

    @Override
    public boolean kickPlayer(UUID uuid, String message) {
        if (getProxy().getPlayer(uuid) != null) {
            getProxy().getPlayer(uuid).disconnect(new TextComponent(message));
            return true;
        }
        return false;
    }

    @Override
    public boolean isPluginEnabled() {
        return true;
    }

    @Override
    public ViaAPI getApi() {
        return api;
    }

    @Override
    public ViaVersionConfig getConf() {
        return config;
    }

    @Override
    public ConfigurationProvider getConfigurationProvider() {
        return config;
    }

    @Override
    public void onReload() {
        // TODO handle
    }

    @Override
    public JsonObject getDump() {
        return null;
    }

}
