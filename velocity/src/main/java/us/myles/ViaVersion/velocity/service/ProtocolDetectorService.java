package us.myles.ViaVersion.velocity.service;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.Getter;
import us.myles.ViaVersion.VelocityPlugin;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;
import us.myles.ViaVersion.velocity.platform.VelocityViaConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProtocolDetectorService implements Runnable {
    private static final Map<String, Integer> detectedProtocolIds = new ConcurrentHashMap<>();
    @Getter
    private static ProtocolDetectorService instance;

    public ProtocolDetectorService() {
        instance = this;
    }

    public static Integer getProtocolId(String serverName) {
        // Step 1. Check Config
        Map<String, Integer> servers = ((VelocityViaConfig) Via.getConfig()).getVelocityServerProtocols();
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
        try {
            return Via.getManager().getInjector().getServerProtocolVersion();
        } catch (Exception e) {
            e.printStackTrace();
            return ProtocolVersion.v1_8.getId();
        }
    }

    @Override
    public void run() {
        for (final RegisteredServer serv : VelocityPlugin.PROXY.getAllServers()) {
            probeServer(serv);
        }
    }

    public static void probeServer(final RegisteredServer serverInfo) {
        final String key = serverInfo.getServerInfo().getName();
        serverInfo.ping().thenAccept((serverPing) -> {
            if (serverPing != null && serverPing.getVersion() != null) {
                detectedProtocolIds.put(key, serverPing.getVersion().getProtocol());
                if (((VelocityViaConfig) Via.getConfig()).isVelocityPingSave()) {
                    Map<String, Integer> servers = ((VelocityViaConfig) Via.getConfig()).getVelocityServerProtocols();
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
        });
    }

    public static Map<String, Integer> getDetectedIds() {
        return new HashMap<>(detectedProtocolIds);
    }

}
