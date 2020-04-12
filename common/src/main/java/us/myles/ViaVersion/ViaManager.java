package us.myles.ViaVersion;

import lombok.Builder;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
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

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ViaManager {
    private final ViaPlatform<?> platform;
    private final ViaProviders providers = new ViaProviders();
    private boolean debug;
    // Internals
    private final ViaInjector injector;
    private final ViaCommandHandler commandHandler;
    private final ViaPlatformLoader loader;

    @Builder
    public ViaManager(ViaPlatform<?> platform, ViaInjector injector, ViaCommandHandler commandHandler, ViaPlatformLoader loader) {
        this.platform = platform;
        this.injector = injector;
        this.commandHandler = commandHandler;
        this.loader = loader;
    }

    public void init() {
        if (System.getProperty("ViaVersion") != null) {
            // Reload?
            platform.onReload();
        }
        // Check for updates
        if (platform.getConf().isCheckForUpdates())
            UpdateUtil.sendUpdateMessage();
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
                platform.getLogger().warning("ViaVersion does not have any compatible versions for this server version, please read our resource page carefully.");
            }
        }
        // Load Listeners / Tasks
        ProtocolRegistry.onServerLoaded();

        // Load Platform
        loader.load();
        // Common tasks
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
    public Map<UUID, UserConnection> getPortedClients() {
        return getConnectedClients();
    }

    public Map<UUID, UserConnection> getConnectedClients() {
        return platform.getConnectionManager().getConnectedClients();
    }

    public void handleLoginSuccess(UserConnection info) {
        platform.getConnectionManager().onLoginSuccess(info);
    }

    public void handleDisconnect(UUID id) {
        handleDisconnect(getConnection(id));
    }

    public void handleDisconnect(UserConnection info) {
        platform.getConnectionManager().onDisconnect(info);
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

    public UserConnection getConnection(UUID playerUUID) {
        return platform.getConnectionManager().getConnectedClient(playerUUID);
    }
}
