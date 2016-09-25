package us.myles.ViaVersion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import us.myles.ViaVersion.api.command.ViaVersionCommand;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.platform.ViaInjector;
import us.myles.ViaVersion.api.platform.ViaPlatform;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.update.UpdateUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class ViaManager {
    private ViaPlatform platform;
    private final Map<UUID, UserConnection> portedPlayers = new ConcurrentHashMap<>();
    @Setter
    private boolean debug = false;
    // Internals
    private ViaInjector injector;
    private ViaVersionCommand commandHandler;

    public ViaManager(ViaPlatform platform) {
        this.platform = platform;
    }

    public void init() {
        if (System.getProperty("ViaVersion") != null) {
            // Reload?
            platform.onReload();
        }
        // Check for updates
        if (platform.getConf().isCheckForUpdates())
            UpdateUtil.sendUpdateMessage();
        // Inject
        // TODO: Get errors
        injector.inject();
        // Mark as injected
        System.setProperty("ViaVersion", getPlatform().getPluginVersion());
        // If successful
        // TODO: This method might run in onLoad, ensure sync tasks can still run if plugin not enabled.
        platform.runSync(new Runnable() {
            @Override
            public void run() {
                ProtocolRegistry.SERVER_PROTOCOL = injector.getServerProtocolVersion();

                // Check if there are any pipes to this version
                if (ProtocolRegistry.SERVER_PROTOCOL != -1) {
                    getPlatform().getLogger().info("ViaVersion detected server version: " + ProtocolVersion.getProtocol(ProtocolRegistry.SERVER_PROTOCOL));
                    if (!ProtocolRegistry.isWorkingPipe()) {
                        getPlatform().getLogger().warning("ViaVersion does not have any compatible versions for this server version, please read our resource page carefully.");
                    }
                }
                ProtocolRegistry.refreshVersions();
            }
        });

    }

    public void destroy() {
        // Uninject
        getPlatform().getLogger().info("ViaVersion is disabling, if this is a reload and you experience issues consider rebooting.");
        injector.uninject();
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
