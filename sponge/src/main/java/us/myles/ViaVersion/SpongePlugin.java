package us.myles.ViaVersion;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import org.spongepowered.api.Game;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.text.serializer.TextSerializers;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.ViaAPI;
import us.myles.ViaVersion.api.ViaVersionConfig;
import us.myles.ViaVersion.api.command.ViaCommandSender;
import us.myles.ViaVersion.api.configuration.ConfigurationProvider;
import us.myles.ViaVersion.api.platform.ViaPlatform;
import us.myles.ViaVersion.dump.PluginInfo;
import us.myles.ViaVersion.sponge.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
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
    private SpongeExecutorService asyncExecutor;
    private SpongeExecutorService syncExecutor;
    private SpongeConfigAPI conf;
    private Logger logger;

    @Listener
    public void onServerStart(GameAboutToStartServerEvent event) {
        // Setup Logger
        logger = new LoggerWrapper(container.getLogger());
        // Setup Plugin
        conf = new SpongeConfigAPI(defaultConfig.getParentFile());
        syncExecutor = game.getScheduler().createSyncExecutor(this);
        asyncExecutor = game.getScheduler().createAsyncExecutor(this);
        SpongeCommandHandler commandHandler = new SpongeCommandHandler();
        game.getCommandManager().register(this, commandHandler, Arrays.asList("viaversion", "viaver"));
        getLogger().info("ViaVersion " + getPluginVersion() + " is now loaded, injecting!");
        // Init platform
        Via.init(ViaManager.builder()
                .platform(this)
                .commandHandler(commandHandler)
                .injector(new SpongeViaInjector())
                .loader(new SpongeViaLoader(this))
                .build());

        // Inject!
        Via.getManager().init();
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public String getPlatformName() {
        return "Sponge";
    }

    @Override
    public String getPluginVersion() {
        return container.getVersion().orElse("Unknown Version");
    }

    @Override
    public int runAsync(Runnable runnable) {
        asyncExecutor.execute(runnable);
        return -1;
    }

    @Override
    public int runSync(Runnable runnable) {
        syncExecutor.execute(runnable);
        return -1;
    }

    @Override
    public int runRepeatingSync(Runnable runnable, Long ticks) {
        Long time = ticks * 50L;
        syncExecutor.scheduleAtFixedRate(runnable, time, time, TimeUnit.MILLISECONDS);
        // use id?
        return -1;
    }

    @Override
    public void cancelTask(int taskId) {
        // oh.
    }

    @Override
    public ViaCommandSender[] getOnlinePlayers() {
        ViaCommandSender[] array = new ViaCommandSender[game.getServer().getOnlinePlayers().size()];
        int i = 0;
        for (Player player : game.getServer().getOnlinePlayers()) {
            array[i++] = new SpongeCommandSender(player);
        }
        return array;
    }

    @Override
    public void sendMessage(UUID uuid, String message) {
        for (Player player : game.getServer().getOnlinePlayers()) {
            if (player.getUniqueId().equals(uuid))
                player.sendMessage(TextSerializers.LEGACY_FORMATTING_CODE.deserialize(message));
        }
    }

    @Override
    public boolean kickPlayer(UUID uuid, String message) {
        for (Player player : game.getServer().getOnlinePlayers()) {
            if (player.getUniqueId().equals(uuid)) {
                player.kick(TextSerializers.LEGACY_FORMATTING_CODE.deserialize(message));
                return true;
            }
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
        platformSpecific.add("plugins", new Gson().toJsonTree(plugins));

        return platformSpecific;
    }
}
