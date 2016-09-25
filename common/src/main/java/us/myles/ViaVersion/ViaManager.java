package us.myles.ViaVersion;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.platform.ViaInjector;
import us.myles.ViaVersion.api.platform.ViaPlatform;
import us.myles.ViaVersion.api.platform.ViaPlatformLoader;
import us.myles.ViaVersion.api.platform.providers.ViaProviders;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;
import us.myles.ViaVersion.commands.ViaCommandHandler;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.update.UpdateUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Builder
public class ViaManager {
    private final Map<UUID, UserConnection> portedPlayers = new ConcurrentHashMap<>();
    private ViaPlatform platform;
    private ViaProviders providers = new ViaProviders();
    @Setter
    private boolean debug = false;
    // Internals
    private ViaInjector injector;
    private ViaCommandHandler commandHandler;
    private ViaPlatformLoader loader;

    public void init() {
        if (System.getProperty("ViaVersion") != null) {
            // Reload?
            platform.onReload();
        }
        // Check for updates
        if (platform.getConf().isCheckForUpdates())
            UpdateUtil.sendUpdateMessage();
        // Inject
        try {
            injector.inject();
        } catch (Exception e) {
            getPlatform().getLogger().severe("ViaVersion failed to inject:");
            e.printStackTrace();
            return;
        }
        // Mark as injected
        System.setProperty("ViaVersion", getPlatform().getPluginVersion());
        // If successful
        // TODO: This method might run in onLoad, ensure sync tasks can still run if plugin not enabled.
        platform.runSync(new Runnable() {
            @Override
            public void run() {
                onServerLoaded();
            }
        });

    }

    public void onServerLoaded() {
        // Load Server Protocol
        try {
            ProtocolRegistry.SERVER_PROTOCOL = injector.getServerProtocolVersion();
        } catch (Exception e) {
            getPlatform().getLogger().severe("ViaVersion failed to get the server protocol!");
            e.printStackTrace();
        }
        // Check if there are any pipes to this version
        if (ProtocolRegistry.SERVER_PROTOCOL != -1) {
            getPlatform().getLogger().info("ViaVersion detected server version: " + ProtocolVersion.getProtocol(ProtocolRegistry.SERVER_PROTOCOL));
            if (!ProtocolRegistry.isWorkingPipe()) {
                getPlatform().getLogger().warning("ViaVersion does not have any compatible versions for this server version, please read our resource page carefully.");
            }
        }
        // Load Listeners / Tasks
        ProtocolRegistry.onServerLoaded();

        // Load Platform
        loader.load();

        // Refresh Versions
        ProtocolRegistry.refreshVersions();
    }

    public void destroy() {
        // Uninject
        getPlatform().getLogger().info("ViaVersion is disabling, if this is a reload and you experience issues consider rebooting.");
        try {
            injector.uninject();
        } catch (Exception e) {
            getPlatform().getLogger().severe("ViaVersion failed to uninject:");
            e.printStackTrace();
        }
    }

    public void addPortedClient(UserConnection info) {
        portedPlayers.put(info.get(ProtocolInfo.class).getUuid(), info);
    }

    public void removePortedClient(UUID clientID) {
        portedPlayers.remove(clientID);
    }

    public UserConnection getConnection(UUID playerUUID) {
        return portedPlayers.get(playerUUID);
    }

}
