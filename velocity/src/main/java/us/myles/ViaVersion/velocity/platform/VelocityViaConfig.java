package us.myles.ViaVersion.velocity.platform;

import us.myles.ViaVersion.AbstractViaConfig;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;

import java.io.File;
import java.net.URL;
import java.util.*;

public class VelocityViaConfig extends AbstractViaConfig {
    private static final List<String> UNSUPPORTED = Arrays.asList("nms-player-ticking", "item-cache", "anti-xray-patch", "quick-move-action-fix", "bungee-ping-interval", "bungee-ping-save", "bungee-servers", "blockconnection-method", "change-1_9-hitbox", "change-1_14-hitbox");

    public VelocityViaConfig(File configFile) {
        super(new File(configFile, "config.yml"));
        // Load config
        reloadConfig();
    }

    @Override
    public URL getDefaultConfigURL() {
        return getClass().getClassLoader().getResource("assets/viaversion/config.yml");
    }

    @Override
    protected void handleConfig(Map<String, Object> config) {
        // Parse servers
        Map<String, Object> servers;
        if (!(config.get("velocity-servers") instanceof Map)) {
            servers = new HashMap<>();
        } else {
            servers = (Map) config.get("velocity-servers");
        }
        // Convert any bad Protocol Ids
        for (Map.Entry<String, Object> entry : new HashSet<>(servers.entrySet())) {
            if (!(entry.getValue() instanceof Integer)) {
                if (entry.getValue() instanceof String) {
                    ProtocolVersion found = ProtocolVersion.getClosest((String) entry.getValue());
                    if (found != null) {
                        servers.put(entry.getKey(), found.getId());
                    } else {
                        servers.remove(entry.getKey()); // Remove!
                    }
                } else {
                    servers.remove(entry.getKey()); // Remove!
                }
            }
        }
        // Ensure default exists
        if (!servers.containsKey("default")) {
            // Side note: This doesn't use ProtocolRegistry as it doesn't know the protocol version at boot.
            try {
                servers.put("default", VelocityViaInjector.getLowestSupportedProtocolVersion());
            } catch (Exception e) {
                // Something went very wrong
                e.printStackTrace();
            }
        }
        // Put back
        config.put("velocity-servers", servers);
    }

    @Override
    public List<String> getUnsupportedOptions() {
        return UNSUPPORTED;
    }

    @Override
    public boolean isAntiXRay() {
        return false;
    }

    @Override
    public boolean isItemCache() {
        return false;
    }

    @Override
    public boolean isNMSPlayerTicking() {
        return false;
    }

    @Override
    public boolean is1_12QuickMoveActionFix() {
        return false;
    }

    @Override
    public String getBlockConnectionMethod() {
        return "packet";
    }

    @Override
    public boolean is1_9HitboxFix() {
        return false;
    }

    @Override
    public boolean is1_14HitboxFix() {
        return false;
    }

    /**
     * What is the interval for checking servers via ping
     * -1 for disabled
     *
     * @return Ping interval in seconds
     */
    public int getVelocityPingInterval() {
        return getInt("velocity-ping-interval", 60);
    }

    /**
     * Should the velocity ping be saved to the config on change.
     *
     * @return True if it should save
     */
    public boolean isVelocityPingSave() {
        return getBoolean("velocity-ping-save", true);
    }

    /**
     * Get the listed server protocols in the config.
     * default will be listed as default.
     *
     * @return Map of String, Integer
     */
    public Map<String, Integer> getVelocityServerProtocols() {
        return get("velocity-servers", Map.class, new HashMap<>());
    }
}
