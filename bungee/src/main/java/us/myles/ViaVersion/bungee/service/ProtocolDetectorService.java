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
        Integer protocol = servers.get(serverName);
        if (protocol != null) {
            return protocol;
        }
        // Step 2. Check Detected
        Integer detectedProtocol = detectedProtocolIds.get(serverName);
        if (detectedProtocol != null) {
            return detectedProtocol;
        }
        // Step 3. Use Default
        Integer defaultProtocol = servers.get("default");
        if (defaultProtocol != null) {
            return defaultProtocol;
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
                if (throwable == null && serverPing != null && serverPing.getVersion() != null) {
                    detectedProtocolIds.put(key, serverPing.getVersion().getProtocol());
                    if (((BungeeConfigAPI) Via.getConfig()).isBungeePingSave()) {
                        Map<String, Integer> servers = ((BungeeConfigAPI) Via.getConfig()).getBungeeServerProtocols();
                        Integer protocol = servers.get(key);
                        if (protocol != null && protocol == serverPing.getVersion().getProtocol()) {
                            return;
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
