package us.myles.ViaVersion;

import us.myles.ViaVersion.api.ViaVersionConfig;
import us.myles.ViaVersion.util.Configuration;

import java.io.File;

public class ViaConfig implements ViaVersionConfig {
    private final ViaVersionPlugin plugin;

    public ViaConfig(ViaVersionPlugin plugin) {
        this.plugin = plugin;
        generateConfig();
    }

    public void generateConfig() {
        File file = new File(plugin.getDataFolder(), "config.yml");
        if (file.exists()) {
            // Update conf options
            Configuration oldConfig = new Configuration(file);
            oldConfig.reload(false); // Load current options from conf
            file.delete(); // Delete old conf
            plugin.saveDefaultConfig(); // Generate new conf
            Configuration newConfig = new Configuration(file);
            newConfig.reload(true); // Load default options
            for (String key : oldConfig.getKeys(false)) {
                // Set option in new conf if exists
                if (newConfig.contains(key)) {
                    newConfig.set(key, oldConfig.get(key));
                }
            }
            newConfig.save();
        } else {
            plugin.saveDefaultConfig();
        }
    }

    @Override
    public boolean isCheckForUpdates() {
        return plugin.getConfig().getBoolean("checkforupdates", true);
    }

    @Override
    public boolean isPreventCollision() {
        return plugin.getConfig().getBoolean("prevent-collision", true);
    }

    @Override
    public boolean isNewEffectIndicator() {
        return plugin.getConfig().getBoolean("use-new-effect-indicator", true);
    }

    @Override
    public boolean isShowNewDeathMessages() {
        return plugin.getConfig().getBoolean("use-new-deathmessages", false);
    }

    @Override
    public boolean isSuppressMetadataErrors() {
        return plugin.getConfig().getBoolean("suppress-metadata-errors", false);
    }

    @Override
    public boolean isShieldBlocking() {
        return plugin.getConfig().getBoolean("shield-blocking", true);
    }

    @Override
    public boolean isHologramPatch() {
        return plugin.getConfig().getBoolean("hologram-patch", false);
    }

    @Override
    public boolean isBossbarPatch() {
        return plugin.getConfig().getBoolean("bossbar-patch", true);
    }

    @Override
    public boolean isBossbarAntiflicker() {
        return plugin.getConfig().getBoolean("bossbar-anti-flicker", false);
    }

    @Override
    public boolean isUnknownEntitiesSuppressed() {
        return false;
    }

    @Override
    public double getHologramYOffset() {
        return plugin.getConfig().getDouble("hologram-y", -0.96D);
    }

    @Override
    public boolean isBlockBreakPatch() {
        return false;
    }

    @Override
    public int getMaxPPS() {
        return plugin.getConfig().getInt("max-pps", 140);
    }

    @Override
    public String getMaxPPSKickMessage() {
        return plugin.getConfig().getString("max-pps-kick-msg", "Sending packets too fast? lag?");
    }

    @Override
    public int getTrackingPeriod() {
        return plugin.getConfig().getInt("tracking-period", 6);
    }

    @Override
    public int getWarningPPS() {
        return plugin.getConfig().getInt("tracking-warning-pps", 120);
    }

    @Override
    public int getMaxWarnings() {
        return plugin.getConfig().getInt("tracking-max-warnings", 3);
    }

    @Override
    public String getMaxWarningsKickMessage() {
        return plugin.getConfig().getString("tracking-max-kick-msg", "You are sending too many packets, :(");
    }

    @Override
    public boolean isAntiXRay() {
        return plugin.getConfig().getBoolean("anti-xray-patch", true);
    }

    @Override
    public boolean isSendSupportedVersions() {
        return plugin.getConfig().getBoolean("send-supported-versions", false);
    }

    @Override
    public boolean isStimulatePlayerTick() {
        return plugin.getConfig().getBoolean("simulate-pt", true);
    }

    @Override
    public boolean isItemCache() {
        return plugin.getConfig().getBoolean("item-cache", true);
    }

    @Override
    public boolean isNMSPlayerTicking() {
        return plugin.getConfig().getBoolean("nms-player-ticking", true);
    }

    @Override
    public boolean isReplacePistons() {
        return plugin.getConfig().getBoolean("replace-pistons", false);
    }

    @Override
    public int getPistonReplacementId() {
        return plugin.getConfig().getInt("replacement-piston-id", 0);
    }

    public boolean isAutoTeam() {
        // Collision has to be enabled first
        return isPreventCollision() && plugin.getConfig().getBoolean("auto-team", true);
    }

    @Override
    public boolean isForceJsonTransform() {
        return plugin.getConfig().getBoolean("force-json-transform", false);
    }
}
