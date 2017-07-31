package us.myles.ViaVersion.bungee.service;

import lombok.Getter;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import us.myles.ViaVersion.BungeePlugin;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.bungee.platform.BungeeConfigAPI;
import us.myles.ViaVersion.bungee.providers.BungeeVersionProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProtocolDetectorService implements Runnable {
    private static final Map<String, Integer> detectedProtocolIds = new ConcurrentHashMap<>();
    private BungeePlugin plugin;
    @Getter
    private static ProtocolDetectorService instance;

    public ProtocolDetectorService(BungeePlugin plugin) {
        this.plugin = plugin;
        instance = this;
    }

    public static Integer getProtocolId(String serverName) {
        // Step 1. Check Config
        Map<String, Integer> servers = ((BungeeConfigAPI) Via.getConfig()).getBungeeServerProtocols();
        if (servers.containsKey(serverName)) {
            return servers.get(serverName);
        }
        // Step 2. Check Detected
        if (detectedProtocolIds.containsKey(serverName)) {
            return detectedProtocolIds.get(serverName);
        }
        // Step 3. Use Default
        if (servers.containsKey("default")) {
            return servers.get("default");
        }
        // Step 4: Use bungee lowest supported... *cries*
        return BungeeVersionProvider.getLowestSupportedVersion();
    }

    @Override
    public void run() {
        for (final Map.Entry<String, ServerInfo> lists : plugin.getProxy().getServers().entrySet()) {
            probeServer(lists.getValue());
        }
    }

    public static void probeServer(final ServerInfo serverInfo) {
        final String key = serverInfo.getName();
        serverInfo.ping(new Callback<ServerPing>() {
            @Override
            public void done(ServerPing serverPing, Throwable throwable) {
                if (throwable == null) {
                    detectedProtocolIds.put(key, serverPing.getVersion().getProtocol());
                    if (((BungeeConfigAPI) Via.getConfig()).isBungeePingSave()) {
                        Map<String, Integer> servers = ((BungeeConfigAPI) Via.getConfig()).getBungeeServerProtocols();
                        if (servers.containsKey(key)) {
                            if (servers.get(key) == serverPing.getVersion().getProtocol()) {
                                return;
                            }
                        }
                        // Ensure we're the only ones writing to the config
                        synchronized (Via.getPlatform().getConfigurationProvider()) {
                            servers.put(key, serverPing.getVersion().getProtocol());
                        }
                        // Save
                        Via.getPlatform().getConfigurationProvider().saveConfig();

                    }
                }
            }
        });
    }

    public static Map<String, Integer> getDetectedIds() {
        return new HashMap<>(detectedProtocolIds);
    }

}
