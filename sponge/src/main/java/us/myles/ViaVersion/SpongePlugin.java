package us.myles.ViaVersion;

import com.google.gson.JsonObject;
import com.google.inject.Inject;
import org.spongepowered.api.Game;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.serializer.TextSerializers;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.ViaAPI;
import us.myles.ViaVersion.api.command.ViaCommandSender;
import us.myles.ViaVersion.api.configuration.ConfigurationProvider;
import us.myles.ViaVersion.api.platform.TaskId;
import us.myles.ViaVersion.api.platform.ViaPlatform;
import us.myles.ViaVersion.api.platform.ViaPlatformLoader;
import us.myles.ViaVersion.dump.PluginInfo;
import us.myles.ViaVersion.sponge.VersionInfo;
import us.myles.ViaVersion.sponge.commands.SpongeCommandHandler;
import us.myles.ViaVersion.sponge.commands.SpongeCommandSender;
import us.myles.ViaVersion.sponge.platform.*;
import us.myles.ViaVersion.sponge.util.LoggerWrapper;
import us.myles.ViaVersion.util.GsonUtil;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

@Plugin(id = "viaversion",
        name = "ViaVersion",
        version = VersionInfo.VERSION,
        authors = {"_MylesC", "Matsv"},
        description = "Allow newer Minecraft versions to connect to an older server version."
)
public class SpongePlugin implements ViaPlatform {
    @Inject
    private Game game;

    @Inject
    private PluginContainer container;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private File defaultConfig;

    private SpongeViaAPI api = new SpongeViaAPI();
    private SpongeConfigAPI conf;
    private Logger logger;

    @Listener
    public void onGameInitialization(GameInitializationEvent event) {
        // Setup Logger
        logger = new LoggerWrapper(container.getLogger());
        // Setup Plugin
        conf = new SpongeConfigAPI(container, defaultConfig.getParentFile());
        SpongeCommandHandler commandHandler = new SpongeCommandHandler();
        game.getCommandManager().register(
                this,
                commandHandler,
                Arrays.asList("viaversion", "viaver", "vvsponge")
        );
        getLogger().info("ViaVersion " + getPluginVersion() + " is now loaded, initializing platform");
        // Init platform
        Via.init(ViaManager.builder()
                .platform(this)
                .commandHandler(commandHandler)
                .injector(new SpongeViaInjector())
                .loader(new SpongeViaLoader(this))
                .build());
    }

    @Listener
    public void onServerStart(GameAboutToStartServerEvent event) {
        // Inject!
        getLogger().info("ViaVersion is injecting");
        Via.getManager().init();
    }

    @Listener
    public void onServerStop(GameStoppingServerEvent event) {
        Via.getManager().destroy();
        ViaPlatformLoader loader = Via.getManager().getLoader();
        if (loader instanceof SpongeViaLoader) {
            ((SpongeViaLoader) loader).unload();
        }
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public String getPlatformName() {
        return game.getPlatform().getImplementation().getName();
    }

    @Override
    public String getPlatformVersion() {
        return game.getPlatform().getImplementation().getVersion().orElse("Unknown Version");
    }

    @Override
    public String getPluginVersion() {
        return container.getVersion().orElse("Unknown Version");
    }

    @Override
    public TaskId runAsync(Runnable runnable) {
        return new SpongeTaskId(Task.builder()
                .async()
                .execute(runnable)
                .submit(this));
    }

    @Override
    public TaskId runSync(Runnable runnable) {
        return new SpongeTaskId(Task.builder()
                .execute(runnable)
                .submit(this));
    }

    @Override
    public TaskId runSync(Runnable runnable, Long ticks) {
        return new SpongeTaskId(Task.builder()
                .execute(runnable)
                .delayTicks(ticks)
                .submit(this));
    }

    @Override
    public TaskId runRepeatingSync(Runnable runnable, Long ticks) {
        return new SpongeTaskId(Task.builder()
                .execute(runnable)
                .delayTicks(ticks)
                .intervalTicks(ticks)
                .submit(this));
    }

    @Override
    public void cancelTask(TaskId taskId) {
        if (taskId == null) return;
        if (taskId.getObject() == null) return;
        if (taskId instanceof SpongeTaskId) {
            ((SpongeTaskId) taskId).getObject().cancel();
        }
    }

    @Override
    public ViaCommandSender[] getOnlinePlayers() {
        if (!game.isServerAvailable()) return new ViaCommandSender[0];
        Collection<Player> players = game.getServer().getOnlinePlayers();
        ViaCommandSender[] array = new ViaCommandSender[players.size()];
        int i = 0;
        for (Player player : players) {
            array[i++] = new SpongeCommandSender(player);
        }
        return array;
    }

    @Override
    public void sendMessage(UUID uuid, String message) {
        if (!game.isServerAvailable()) return;
        Optional<Player> player = game.getServer().getPlayer(uuid);
        if (player.isPresent())
                player.get().sendMessage(TextSerializers.LEGACY_FORMATTING_CODE.deserialize(message));
    }

    @Override
    public boolean kickPlayer(UUID uuid, String message) {
        if (!game.isServerAvailable()) return false;
        Optional<Player> player = game.getServer().getPlayer(uuid);
        if (player.isPresent()) {
            player.get().kick(TextSerializers.LEGACY_FORMATTING_CODE.deserialize(message));
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
    public SpongeConfigAPI getConf() {
        return conf;
    }

    @Override
    public ConfigurationProvider getConfigurationProvider() {
        return conf;
    }

    @Override
    public void onReload() {
        getLogger().severe("ViaVersion is already loaded, this should work fine. If you get any console errors, try rebooting.");
    }

    @Override
    public JsonObject getDump() {
        JsonObject platformSpecific = new JsonObject();

        List<PluginInfo> plugins = new ArrayList<>();
        for (PluginContainer p : game.getPluginManager().getPlugins()) {
            plugins.add(new PluginInfo(
                    true,
                    p.getName(),
                    p.getVersion().orElse("Unknown Version"),
                    p.getInstance().isPresent() ? p.getInstance().get().getClass().getCanonicalName() : "Unknown",
                    p.getAuthors()
            ));
        }
        platformSpecific.add("plugins", GsonUtil.getGson().toJsonTree(plugins));

        return platformSpecific;
    }

    @Override
    public boolean isOldClientsAllowed() {
        return true;
    }
}
