package us.myles.ViaVersion.bukkit.platform;

import us.myles.ViaVersion.AbstractViaConfig;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.Via;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BukkitViaConfig extends AbstractViaConfig {
    private static final List<String> UNSUPPORTED = Arrays.asList("bungee-ping-interval", "bungee-ping-save", "bungee-servers", "velocity-ping-interval", "velocity-ping-save", "velocity-servers");

    public BukkitViaConfig() {
        super(new File(((ViaVersionPlugin) Via.getPlatform()).getDataFolder(), "config.yml"));
        // Load config
        reloadConfig();
    }

    @Override
    public URL getDefaultConfigURL() {
        return BukkitViaConfig.class.getClassLoader().getResource("assets/viaversion/config.yml");
    }

    @Override
    protected void handleConfig(Map<String, Object> config) {
    }

    @Override
    public List<String> getUnsupportedOptions() {
        return UNSUPPORTED;
    }
}
