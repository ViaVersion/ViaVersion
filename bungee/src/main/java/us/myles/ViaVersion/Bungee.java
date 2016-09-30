package us.myles.ViaVersion;

import com.google.gson.JsonObject;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.ViaAPI;
import us.myles.ViaVersion.api.ViaVersionConfig;
import us.myles.ViaVersion.api.command.ViaCommandSender;
import us.myles.ViaVersion.api.configuration.ConfigurationProvider;
import us.myles.ViaVersion.api.platform.TaskId;
import us.myles.ViaVersion.api.platform.ViaPlatform;
import us.myles.ViaVersion.bungee.commands.BungeeCommand;
import us.myles.ViaVersion.bungee.commands.BungeeCommandHandler;
import us.myles.ViaVersion.bungee.commands.BungeeCommandSender;
import us.myles.ViaVersion.bungee.platform.*;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Bungee extends Plugin implements ViaPlatform, Listener {

    private BungeeViaAPI api;
    private BungeeConfigAPI config;
    private BungeeCommandHandler commandHandler;

    @Override
    public void onLoad() {
        api = new BungeeViaAPI();
        config = new BungeeConfigAPI(getDataFolder());
        commandHandler = new BungeeCommandHandler();
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new BungeeCommand(commandHandler));
        // Init platform
        Via.init(ViaManager.builder()
                .platform(this)
                .injector(new BungeeViaInjector())
                .loader(new BungeeViaLoader())
                .commandHandler(commandHandler)
                .build());

        getProxy().getPluginManager().registerListener(this, this);
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
    public TaskId runAsync(Runnable runnable) {
        return new BungeeTaskId(getProxy().getScheduler().runAsync(this, runnable).getId());
    }

    @Override
    public TaskId runSync(Runnable runnable) {
        return new BungeeTaskId(getProxy().getScheduler().runAsync(this, runnable).getId());
    }

    @Override
    public TaskId runRepeatingSync(Runnable runnable, Long ticks) {
        return new BungeeTaskId(getProxy().getScheduler().schedule(this, runnable, 0, ticks * 50, TimeUnit.MILLISECONDS).getId());
    }

    @Override
    public void cancelTask(TaskId taskId) {
        if (taskId == null) return;
        if (taskId.getObject() == null) return;
        if (taskId instanceof BungeeTaskId) {
            getProxy().getScheduler().cancel((Integer) taskId.getObject());
        }
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
        // Injector prints a message <3
    }

    @Override
    public JsonObject getDump() {
        return new JsonObject();
    }

    @EventHandler
    public void onQuit(PlayerDisconnectEvent e) {
        Via.getManager().removePortedClient(e.getPlayer().getUniqueId());
    }

}
