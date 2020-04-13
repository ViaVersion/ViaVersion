package us.myles.ViaVersion;

import com.google.gson.JsonObject;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.ProtocolConstants;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.ViaAPI;
import us.myles.ViaVersion.api.command.ViaCommandSender;
import us.myles.ViaVersion.api.configuration.ConfigurationProvider;
import us.myles.ViaVersion.api.data.MappingDataLoader;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.platform.TaskId;
import us.myles.ViaVersion.api.platform.ViaConnectionManager;
import us.myles.ViaVersion.api.platform.ViaPlatform;
import us.myles.ViaVersion.bungee.commands.BungeeCommand;
import us.myles.ViaVersion.bungee.commands.BungeeCommandHandler;
import us.myles.ViaVersion.bungee.commands.BungeeCommandSender;
import us.myles.ViaVersion.bungee.platform.*;
import us.myles.ViaVersion.bungee.service.ProtocolDetectorService;
import us.myles.ViaVersion.dump.PluginInfo;
import us.myles.ViaVersion.util.GsonUtil;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class BungeePlugin extends Plugin implements ViaPlatform<ProxiedPlayer>, Listener {
    private final ViaConnectionManager connectionManager = new ViaConnectionManager();
    private BungeeViaAPI api;
    private BungeeViaConfig config;

    @Override
    public void onLoad() {
        try {
            ProtocolConstants.class.getField("MINECRAFT_1_15_2");
        } catch (NoSuchFieldException e) {
            getLogger().warning("      / \\");
            getLogger().warning("     /   \\");
            getLogger().warning("    /  |  \\");
            getLogger().warning("   /   |   \\         BUNGEECORD IS OUTDATED");
            getLogger().warning("  /         \\   VIAVERSION MAY NOT WORK AS INTENDED");
            getLogger().warning(" /     o     \\");
            getLogger().warning("/_____________\\");
        }

        api = new BungeeViaAPI();
        config = new BungeeViaConfig(getDataFolder());
        BungeeCommandHandler commandHandler = new BungeeCommandHandler();
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new BungeeCommand(commandHandler));

        // Init platform
        Via.init(ViaManager.builder()
                .platform(this)
                .injector(new BungeeViaInjector())
                .loader(new BungeeViaLoader(this))
                .commandHandler(commandHandler)
                .build());
    }

    @Override
    public void onEnable() {
        if (ProxyServer.getInstance().getPluginManager().getPlugin("ViaBackwards") != null) {
            MappingDataLoader.enableMappingsCache();
        }

        // Inject
        Via.getManager().init();
    }

    @Override
    public String getPlatformName() {
        return getProxy().getName();
    }

    @Override
    public String getPlatformVersion() {
        return getProxy().getVersion();
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
        return runAsync(runnable);
    }

    @Override
    public TaskId runSync(Runnable runnable, Long ticks) {
        return new BungeeTaskId(getProxy().getScheduler().schedule(this, runnable, ticks * 50, TimeUnit.MILLISECONDS).getId());
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
        getProxy().getPlayer(uuid).sendMessage(TextComponent.fromLegacyText(message));
    }

    @Override
    public boolean kickPlayer(UUID uuid, String message) {
        if (getProxy().getPlayer(uuid) != null) {
            getProxy().getPlayer(uuid).disconnect(TextComponent.fromLegacyText(message));
            return true;
        }
        return false;
    }

    @Override
    public boolean isPluginEnabled() {
        return true;
    }

    @Override
    public ViaAPI<ProxiedPlayer> getApi() {
        return api;
    }

    @Override
    public BungeeViaConfig getConf() {
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
        JsonObject platformSpecific = new JsonObject();

        List<PluginInfo> plugins = new ArrayList<>();
        for (Plugin p : ProxyServer.getInstance().getPluginManager().getPlugins())
            plugins.add(new PluginInfo(
                    true,
                    p.getDescription().getName(),
                    p.getDescription().getVersion(),
                    p.getDescription().getMain(),
                    Collections.singletonList(p.getDescription().getAuthor())
            ));

        platformSpecific.add("plugins", GsonUtil.getGson().toJsonTree(plugins));
        platformSpecific.add("servers", GsonUtil.getGson().toJsonTree(ProtocolDetectorService.getDetectedIds()));
        return platformSpecific;
    }

    @Override
    public boolean isOldClientsAllowed() {
        return true;
    }

    @Override
    public ViaConnectionManager getConnectionManager() {
        return connectionManager;
    }

    @EventHandler
    public void onQuit(PlayerDisconnectEvent e) {
        UserConnection userConnection = getConnectionManager().getConnectedClient(e.getPlayer().getUniqueId());
        if (userConnection != null) {
            // Only remove if the connection is disconnected (eg. relogin)
            if (userConnection.getChannel() == null || !userConnection.getChannel().isOpen()) {
                Via.getManager().handleDisconnect(e.getPlayer().getUniqueId());
            }
        }

    }
}
