package us.myles.ViaVersion;

import com.google.gson.JsonObject;
import com.google.inject.Inject;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
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
import us.myles.ViaVersion.sponge.*;

import java.util.Arrays;
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

    private SpongeExecutorService asyncExecutor;
    private SpongeExecutorService syncExecutor;
    private SpongeConfigAPI conf = new SpongeConfigAPI(this);
    private SpongeViaAPI api = new SpongeViaAPI();
    private Logger logger;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        // Setup Logger
        logger = new LoggerWrapper(container.getLogger());
        // Setup Plugin
        syncExecutor = game.getScheduler().createSyncExecutor(this);
        asyncExecutor = game.getScheduler().createAsyncExecutor(this);
        SpongeCommandHandler commandHandler = new SpongeCommandHandler();
        game.getCommandManager().register(this, commandHandler, Arrays.asList("viaversion", "viaver"));
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
        // TODO: Warning?
    }

    @Override
    public JsonObject getDump() {
        return new JsonObject();
    }
}
