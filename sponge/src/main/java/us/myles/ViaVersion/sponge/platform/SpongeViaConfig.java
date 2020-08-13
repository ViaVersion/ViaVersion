package us.myles.ViaVersion.sponge.platform;

import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.plugin.PluginContainer;
import us.myles.ViaVersion.AbstractViaConfig;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SpongeViaConfig extends AbstractViaConfig {
    private static final List<String> UNSUPPORTED = Arrays.asList("anti-xray-patch", "bungee-ping-interval",
            "bungee-ping-save", "bungee-servers", "velocity-ping-interval", "velocity-ping-save", "velocity-servers",
            "quick-move-action-fix", "change-1_9-hitbox", "change-1_14-hitbox", "blockconnection-method");
    private final PluginContainer pluginContainer;

    public SpongeViaConfig(PluginContainer pluginContainer, File configFile) {
        super(new File(configFile, "config.yml"));
        this.pluginContainer = pluginContainer;
        reloadConfig();
    }

    @Override
    public URL getDefaultConfigURL() {
        Optional<Asset> config = pluginContainer.getAsset("config.yml");
        if (!config.isPresent()) {
            throw new IllegalArgumentException("Default config is missing from jar");
        }
        return config.get().getUrl();
    }

    @Override
    protected void handleConfig(Map<String, Object> config) {
    }

    @Override
    public List<String> getUnsupportedOptions() {
        return UNSUPPORTED;
    }
}
