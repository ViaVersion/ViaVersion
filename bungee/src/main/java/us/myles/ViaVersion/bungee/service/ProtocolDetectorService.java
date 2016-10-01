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
        for (final Map.Entry<String, ServerInfo> lists : plugin.getProxy().getServers().entrySet()) {
            updateProtocolInfo(lists.getKey(), lists.getValue());
        }
    }

    private void updateProtocolInfo(final String key, ServerInfo value) {
        value.ping(new Callback<ServerPing>() {
            @Override
            public void done(ServerPing serverPing, Throwable throwable) {
                if (throwable == null)
                    protocolIds.put(key, serverPing.getVersion().getProtocol());
                else
                    throwable.printStackTrace();
            }
        });
    }

}
