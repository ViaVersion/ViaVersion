package us.myles.ViaVersion;

import org.jetbrains.annotations.Nullable;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.platform.TaskId;
import us.myles.ViaVersion.api.platform.ViaConnectionManager;
import us.myles.ViaVersion.api.platform.ViaInjector;
import us.myles.ViaVersion.api.platform.ViaPlatform;
import us.myles.ViaVersion.api.platform.ViaPlatformLoader;
import us.myles.ViaVersion.api.platform.providers.ViaProviders;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;
import us.myles.ViaVersion.commands.ViaCommandHandler;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.TabCompleteThread;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.ViaIdleThread;
import us.myles.ViaVersion.update.UpdateUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ViaManager {
    private final ViaPlatform<?> platform;
    private final ViaProviders providers = new ViaProviders();
    // Internals
    private final ViaInjector injector;
    private final ViaCommandHandler commandHandler;
    private final ViaPlatformLoader loader;
    private final Set<String> subPlatforms = new HashSet<>();
    private List<Runnable> enableListeners = new ArrayList<>();
    private TaskId mappingLoadingTask;
    private boolean debug;

    public ViaManager(ViaPlatform<?> platform, ViaInjector injector, ViaCommandHandler commandHandler, ViaPlatformLoader loader) {
        this.platform = platform;
        this.injector = injector;
        this.commandHandler = commandHandler;
        this.loader = loader;
    }

    public static ViaManagerBuilder builder() {
        return new ViaManagerBuilder();
    }

    public void init() {
        if (System.getProperty("ViaVersion") != null) {
            // Reload?
            platform.onReload();
        }
        // Check for updates
        if (platform.getConf().isCheckForUpdates()) {
            UpdateUtil.sendUpdateMessage();
        }

        // Force class load
        ProtocolRegistry.init();

        // Inject
        try {
            injector.inject();
        } catch (Exception e) {
            platform.getLogger().severe("ViaVersion failed to inject:");
            e.printStackTrace();
            return;
        }

        // Mark as injected
        System.setProperty("ViaVersion", platform.getPluginVersion());

        for (Runnable listener : enableListeners) {
            listener.run();
        }
        enableListeners = null;

        // If successful
        platform.runSync(this::onServerLoaded);
    }

    public void onServerLoaded() {
        // Load Server Protocol
        try {
            ProtocolRegistry.SERVER_PROTOCOL = injector.getServerProtocolVersion();
        } catch (Exception e) {
            platform.getLogger().severe("ViaVersion failed to get the server protocol!");
            e.printStackTrace();
        }
        // Check if there are any pipes to this version
        if (ProtocolRegistry.SERVER_PROTOCOL != -1) {
            platform.getLogger().info("ViaVersion detected server version: " + ProtocolVersion.getProtocol(ProtocolRegistry.SERVER_PROTOCOL));
            if (!ProtocolRegistry.isWorkingPipe()) {
                platform.getLogger().warning("ViaVersion does not have any compatible versions for this server version!");
                platform.getLogger().warning("Please remember that ViaVersion only adds support for versions newer than the server version.");
                platform.getLogger().warning("If you need support for older versions you may need to use one or more ViaVersion addons too.");
                platform.getLogger().warning("In that case please read the ViaVersion resource page carefully, and if you're");
                platform.getLogger().warning("still unsure, feel free to join our Discord-Server for further assistance.");
            }
        }
        // Load Listeners / Tasks
        ProtocolRegistry.onServerLoaded();

        // Load Platform
        loader.load();
        // Common tasks
        mappingLoadingTask = Via.getPlatform().runRepeatingSync(() -> {
            if (ProtocolRegistry.checkForMappingCompletion()) {
                platform.cancelTask(mappingLoadingTask);
                mappingLoadingTask = null;
            }
        }, 10L);
        if (ProtocolRegistry.SERVER_PROTOCOL < ProtocolVersion.v1_9.getId()) {
            if (Via.getConfig().isSimulatePlayerTick()) {
                Via.getPlatform().runRepeatingSync(new ViaIdleThread(), 1L);
            }
        }
        if (ProtocolRegistry.SERVER_PROTOCOL < ProtocolVersion.v1_13.getId()) {
            if (Via.getConfig().get1_13TabCompleteDelay() > 0) {
                Via.getPlatform().runRepeatingSync(new TabCompleteThread(), 1L);
            }
        }

        // Refresh Versions
        ProtocolRegistry.refreshVersions();
    }

    public void destroy() {
        // Uninject
        platform.getLogger().info("ViaVersion is disabling, if this is a reload and you experience issues consider rebooting.");
        try {
            injector.uninject();
        } catch (Exception e) {
            platform.getLogger().severe("ViaVersion failed to uninject:");
            e.printStackTrace();
        }

        // Unload
        loader.unload();
    }

    public Set<UserConnection> getConnections() {
        return platform.getConnectionManager().getConnections();
    }

    /**
     * @deprecated use getConnectedClients()
     */
    @Deprecated
    public Map<UUID, UserConnection> getPortedPlayers() {
        return getConnectedClients();
    }

    public Map<UUID, UserConnection> getConnectedClients() {
        return platform.getConnectionManager().getConnectedClients();
    }

    /**
     * @see ViaConnectionManager#isClientConnected(UUID)
     */
    public boolean isClientConnected(UUID player) {
        return platform.getConnectionManager().isClientConnected(player);
    }

    public void handleLoginSuccess(UserConnection info) {
        platform.getConnectionManager().onLoginSuccess(info);
    }

    public ViaPlatform<?> getPlatform() {
        return platform;
    }

    public ViaProviders getProviders() {
        return providers;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public ViaInjector getInjector() {
        return injector;
    }

    public ViaCommandHandler getCommandHandler() {
        return commandHandler;
    }

    public ViaPlatformLoader getLoader() {
        return loader;
    }

    /**
     * Returns a mutable set of self-added subplatform version strings.
     * This set is expanded by the subplatform itself (e.g. ViaBackwards), and may not contain all running ones.
     *
     * @return mutable set of subplatform versions
     */
    public Set<String> getSubPlatforms() {
        return subPlatforms;
    }

    /**
     * @see ViaConnectionManager#getConnectedClient(UUID)
     */
    @Nullable
    public UserConnection getConnection(UUID playerUUID) {
        return platform.getConnectionManager().getConnectedClient(playerUUID);
    }

    /**
     * Adds a runnable to be executed when ViaVersion has finished its init before the full server load.
     *
     * @param runnable runnable to be executed
     */
    public void addEnableListener(Runnable runnable) {
        enableListeners.add(runnable);
    }

    public static final class ViaManagerBuilder {
        private ViaPlatform<?> platform;
        private ViaInjector injector;
        private ViaCommandHandler commandHandler;
        private ViaPlatformLoader loader;

        public ViaManagerBuilder platform(ViaPlatform<?> platform) {
            this.platform = platform;
            return this;
        }

        public ViaManagerBuilder injector(ViaInjector injector) {
            this.injector = injector;
            return this;
        }

        public ViaManagerBuilder loader(ViaPlatformLoader loader) {
            this.loader = loader;
            return this;
        }

        public ViaManagerBuilder commandHandler(ViaCommandHandler commandHandler) {
            this.commandHandler = commandHandler;
            return this;
        }

        public ViaManager build() {
            return new ViaManager(platform, injector, commandHandler, loader);
        }
    }
}
