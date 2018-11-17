package us.myles.ViaVersion;

import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import net.kyori.text.serializer.ComponentSerializers;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.slf4j.Logger;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.command.ViaCommandSender;
import us.myles.ViaVersion.api.configuration.ConfigurationProvider;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.platform.TaskId;
import us.myles.ViaVersion.api.platform.ViaPlatform;
import us.myles.ViaVersion.dump.PluginInfo;
import us.myles.ViaVersion.util.GsonUtil;
import us.myles.ViaVersion.velocity.VersionInfo;
import us.myles.ViaVersion.velocity.command.VelocityCommandHandler;
import us.myles.ViaVersion.velocity.command.VelocityCommandSender;
import us.myles.ViaVersion.velocity.platform.*;
import us.myles.ViaVersion.velocity.service.ProtocolDetectorService;
import us.myles.ViaVersion.velocity.util.LoggerWrapper;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "viaversion",
        name = "ViaVersion",
        version = VersionInfo.VERSION,
        authors = {"_MylesC", "Matsv"},
        description = "Allow newer Minecraft versions to connect to an older server version.",
        url = "https://viaversion.com"
)
@Getter
public class VelocityPlugin implements ViaPlatform<Player> {
    @Inject
    private ProxyServer proxy;
    @Inject
    public static ProxyServer PROXY;
    @Inject
    private Logger loggerslf4j;
    private java.util.logging.Logger logger;
    @Inject
    @DataDirectory
    private Path configDir;
    private VelocityViaAPI api;
    private VelocityViaConfig conf;

    @Subscribe
    public void onProxyInit(ProxyInitializeEvent e) {
        PROXY = proxy;
        VelocityCommandHandler commandHandler = new VelocityCommandHandler();
        PROXY.getCommandManager().register(commandHandler, "viaver", "vvvelocity", "viaversion");
        api = new VelocityViaAPI();
        conf = new VelocityViaConfig(configDir.toFile());
        logger = new LoggerWrapper(loggerslf4j);
        Via.init(ViaManager.builder()
                .platform(this)
                .commandHandler(commandHandler)
                .loader(new VelocityViaLoader())
                .injector(new VelocityViaInjector()).build());
        Via.getManager().init();
    }

    @Subscribe
    public void onQuit(DisconnectEvent e) {
        UserConnection userConnection = Via.getManager().getPortedPlayers().get(e.getPlayer().getUniqueId());
        if (userConnection != null) {
            // Only remove if the connection is disconnected (eg. relogin)
            if (userConnection.getChannel() == null || !userConnection.getChannel().isOpen()) {
                Via.getManager().removePortedClient(e.getPlayer().getUniqueId());
            }
        }
    }

    @Override
    public String getPlatformName() {
        String proxyImpl = ProxyServer.class.getPackage().getImplementationTitle();
        return (proxyImpl != null) ? proxyImpl : "Velocity";
    }

    @Override
    public String getPlatformVersion() {
        String version = ProxyServer.class.getPackage().getImplementationVersion();
        return (version != null) ? version : "Unknown";
    }

    @Override
    public String getPluginVersion() {
        return VersionInfo.VERSION;
    }

    @Override
    public TaskId runAsync(Runnable runnable) {
        return runSync(runnable);
    }

    @Override
    public TaskId runSync(Runnable runnable) {
        return runSync(runnable, 0L);
    }

    @Override
    public TaskId runSync(Runnable runnable, Long ticks) {
        return new VelocityTaskId(
                PROXY.getScheduler()
                        .buildTask(this, runnable)
                        .delay(ticks * 50, TimeUnit.MILLISECONDS).schedule()
        );
    }

    @Override
    public TaskId runRepeatingSync(Runnable runnable, Long ticks) {
        return new VelocityTaskId(
                PROXY.getScheduler()
                        .buildTask(this, runnable)
                        .repeat(ticks * 50, TimeUnit.MILLISECONDS).schedule()
        );
    }

    @Override
    public void cancelTask(TaskId taskId) {
        if (taskId instanceof VelocityTaskId) {
            ((VelocityTaskId) taskId).getObject().cancel();
        }
    }

    @Override
    public ViaCommandSender[] getOnlinePlayers() {
        return PROXY.getAllPlayers().stream()
                .map(VelocityCommandSender::new)
                .toArray(ViaCommandSender[]::new);
    }

    @Override
    public void sendMessage(UUID uuid, String message) {
        PROXY.getPlayer(uuid).ifPresent(it -> it.sendMessage(
                ComponentSerializers.JSON.deserialize(
                        ComponentSerializer.toString(TextComponent.fromLegacyText(message)) // Fixes links
                )
        ));
    }

    @Override
    public boolean kickPlayer(UUID uuid, String message) {
        return PROXY.getPlayer(uuid).map(it -> {
            it.disconnect(
                    ComponentSerializers.JSON.deserialize(
                            ComponentSerializer.toString(TextComponent.fromLegacyText(message)) // ComponentSerializers.LEGACY is deprecated
                    )
            );
            return true;
        }).orElse(false);
    }

    @Override
    public boolean isPluginEnabled() {
        return true;
    }

    @Override
    public ConfigurationProvider getConfigurationProvider() {
        return conf;
    }

    @Override
    public void onReload() {

    }

    @Override
    public JsonObject getDump() {
        JsonObject extra = new JsonObject();
        List<PluginInfo> plugins = new ArrayList<>();
        for (PluginContainer p : PROXY.getPluginManager().getPlugins()) {
            plugins.add(new PluginInfo(
                    true,
                    p.getDescription().getName().orElse(p.getDescription().getId()),
                    p.getDescription().getVersion().orElse("Unknown Version"),
                    p.getInstance().isPresent() ? p.getInstance().get().getClass().getCanonicalName() : "Unknown",
                    p.getDescription().getAuthors()
            ));
        }
        extra.add("plugins", GsonUtil.getGson().toJsonTree(plugins));
        extra.add("servers", GsonUtil.getGson().toJsonTree(ProtocolDetectorService.getDetectedIds()));
        return extra;
    }

    @Override
    public boolean isOldClientsAllowed() {
        return true;
    }
}
