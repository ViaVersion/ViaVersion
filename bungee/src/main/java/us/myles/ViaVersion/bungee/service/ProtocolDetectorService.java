package us.myles.ViaVersion.bungee.service;

import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import us.myles.ViaVersion.BungeePlugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProtocolDetectorService implements Runnable {
    private static final Map<String, Integer> protocolIds = new ConcurrentHashMap<>();
    private BungeePlugin plugin;

    public ProtocolDetectorService(BungeePlugin plugin) {
        this.plugin = plugin;
    }

    public static Integer getProtocolId(String serverName) {
        if (!hasProtocolId(serverName))
            return -1;
        return protocolIds.get(serverName);
    }

    public static boolean hasProtocolId(String serverName) {
        return protocolIds.containsKey(serverName);
    }

    @Override
    public void run() {
        System.out.println("Checking protocol ids"); // TODO remove message after confirming that it works

        for (final Map.Entry<String, ServerInfo> lists : plugin.getProxy().getServers().entrySet()) {
            lists.getValue().ping(new Callback<ServerPing>() {
                @Override
                public void done(ServerPing serverPing, Throwable throwable) {
                    if (throwable == null)
                        protocolIds.put(lists.getKey(), serverPing.getVersion().getProtocol());
                }
            });
        }
    }
}
