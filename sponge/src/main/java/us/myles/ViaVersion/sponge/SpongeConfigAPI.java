package us.myles.ViaVersion.sponge;

import org.yaml.snakeyaml.Yaml;
import us.myles.ViaVersion.api.ViaVersionConfig;
import us.myles.ViaVersion.api.configuration.ConfigurationProvider;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpongeConfigAPI implements ViaVersionConfig, ConfigurationProvider {
    private final File defaultConfig;
    private Map<Object, Object> config;
    private ThreadLocal<Yaml> yaml = new ThreadLocal<Yaml>() {
        @Override
        protected Yaml initialValue() {
            return new Yaml();
        }
    };

    public SpongeConfigAPI(File defaultConfig) {
        this.defaultConfig = defaultConfig;
        reloadConfig();
    }

    public boolean isCheckForUpdates() {
        return getBoolean("checkforupdates", true);
    }

    @Override
    public boolean isPreventCollision() {
        return getBoolean("prevent-collision", true);
    }

    @Override
    public boolean isNewEffectIndicator() {
        return getBoolean("use-new-effect-indicator", true);
    }

    @Override
    public boolean isShowNewDeathMessages() {
        return getBoolean("use-new-deathmessages", false);
    }

    @Override
    public boolean isSuppressMetadataErrors() {
        return getBoolean("suppress-metadata-errors", false);
    }

    @Override
    public boolean isShieldBlocking() {
        return getBoolean("shield-blocking", true);
    }

    @Override
    public boolean isHologramPatch() {
        return getBoolean("hologram-patch", false);
    }

    @Override
    public boolean isBossbarPatch() {
        return getBoolean("bossbar-patch", true);
    }

    @Override
    public boolean isBossbarAntiflicker() {
        return getBoolean("bossbar-anti-flicker", false);
    }

    @Override
    public boolean isUnknownEntitiesSuppressed() {
        return false;
    }

    @Override
    public double getHologramYOffset() {
        return getDouble("hologram-y", -0.96D);
    }

    @Override
    public boolean isBlockBreakPatch() {
        return false;
    }

    @Override
    public int getMaxPPS() {
        return getInt("max-pps", 140);
    }

    @Override
    public String getMaxPPSKickMessage() {
        return getString("max-pps-kick-msg", "Sending packets too fast? lag?");
    }

    @Override
    public int getTrackingPeriod() {
        return getInt("tracking-period", 6);
    }

    @Override
    public int getWarningPPS() {
        return getInt("tracking-warning-pps", 120);
    }

    @Override
    public int getMaxWarnings() {
        return getInt("tracking-max-warnings", 3);
    }

    @Override
    public String getMaxWarningsKickMessage() {
        return getString("tracking-max-kick-msg", "You are sending too many packets, :(");
    }

    @Override
    public boolean isAntiXRay() {
        return getBoolean("anti-xray-patch", true);
    }

    @Override
    public boolean isSendSupportedVersions() {
        return getBoolean("send-supported-versions", false);
    }

    @Override
    public boolean isStimulatePlayerTick() {
        return getBoolean("simulate-pt", true);
    }

    @Override
    public boolean isItemCache() {
        return getBoolean("item-cache", true);
    }

    @Override
    public boolean isNMSPlayerTicking() {
        return getBoolean("nms-player-ticking", true);
    }

    @Override
    public boolean isReplacePistons() {
        return getBoolean("replace-pistons", false);
    }

    @Override
    public int getPistonReplacementId() {
        return getInt("replacement-piston-id", 0);
    }

    public boolean isAutoTeam() {
        // Collision has to be enabled first
        return isPreventCollision() && getBoolean("auto-team", true);
    }

    @Override
    public boolean isForceJsonTransform() {
        return getBoolean("force-json-transform", false);
    }

    @Override
    public List<Integer> getBlockedProtocols() {
        return getIntegerList("block-protocols");
    }

    @Override
    public String getBlockedDisconnectMsg() {
        return getString("block-disconnect-msg", "You are using an unsupported Minecraft version!");
    }

    @Override
    public String getReloadDisconnectMsg() {
        return getString("reload-disconnect-msg", "Server reload, please rejoin!");
    }

    @Override
    public void set(String path, Object value) {
        config.put(path, value);
    }

    @Override
    public void saveConfig() {
        if (!defaultConfig.isDirectory()) {
            defaultConfig.mkdir();
        }
        File config = new File(defaultConfig, "config.yml");
        try (FileWriter fw = new FileWriter(config)) {
            yaml.get().dump(this.config, fw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reloadConfig() {
        if (!defaultConfig.isDirectory()) {
            defaultConfig.mkdir();
        }
        File config = new File(defaultConfig, "config.yml");
        URL jarConfigFile = this.getClass().getClassLoader().getResource("config.yml");
        this.config = null;
        if (config.exists()) {
            try (FileInputStream input = new FileInputStream(config)) {
                this.config = (Map<Object, Object>) yaml.get().load(input);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (this.config == null) {
            this.config = new HashMap<>();
        }
        Map<Object, Object> defaults;
        try (InputStream stream = jarConfigFile.openStream()) {
            defaults = (Map<Object, Object>) yaml.get().load(stream);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        // Merge with defaultLoader
        for (Object key : this.config.keySet()) {
            // Set option in new conf if exists
            if (defaults.containsKey(key)) {
                defaults.put(key, this.config.get(key));
            }
        }
        this.config = defaults;
        // Save
        saveConfig();

    }

    public boolean getBoolean(String key, boolean def) {
        if (this.config.containsKey(key)) {
            return (boolean) this.config.get(key);
        } else {
            return def;
        }
    }

    public String getString(String key, String def) {
        if (this.config.containsKey(key)) {
            return (String) this.config.get(key);
        } else {
            return def;
        }
    }

    public int getInt(String key, int def) {
        if (this.config.containsKey(key)) {
            return (int) this.config.get(key);
        } else {
            return def;
        }
    }

    public double getDouble(String key, double def) {
        if (this.config.containsKey(key)) {
            return (double) this.config.get(key);
        } else {
            return def;
        }
    }

    public List<Integer> getIntegerList(String key) {
        if (this.config.containsKey(key)) {
            return (List<Integer>) this.config.get(key);
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> getValues() {
        return (Map<String, Object>) ((Map) this.config);
    }
}
