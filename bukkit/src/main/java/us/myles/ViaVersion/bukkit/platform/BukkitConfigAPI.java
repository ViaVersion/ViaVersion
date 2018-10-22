package us.myles.ViaVersion.bukkit.platform;

import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.ViaVersionConfig;
import us.myles.ViaVersion.util.Config;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BukkitConfigAPI extends Config implements ViaVersionConfig {
    private static List<String> UNSUPPORTED = Arrays.asList("bungee-ping-interval", "bungee-ping-save", "bungee-servers");

    public BukkitConfigAPI() {
        super(new File(((ViaVersionPlugin) Via.getPlatform()).getDataFolder(), "config.yml"));
        // Load config
        reloadConfig();
    }

    @Override
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
        return getBoolean("use-new-deathmessages", true);
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
    public boolean isPistonAnimationPatch() {
        return getBoolean("piston-animation-patch", false);
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
        return getInt("max-pps", 800);
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
    public boolean is1_12NBTArrayFix() {
        return getBoolean("chat-nbt-fix", true);
    }
    
    @Override
    public boolean is1_12QuickMoveActionFix() {
        return getBoolean("quick-move-action-fix", false);
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
    public boolean isMinimizeCooldown() {
        return getBoolean("minimize-cooldown", true);
    }

    @Override
    public URL getDefaultConfigURL() {
        return BukkitConfigAPI.class.getClassLoader().getResource("assets/viaversion/config.yml");
    }

    @Override
    protected void handleConfig(Map<String, Object> config) {
        // Nothing currently
    }

    @Override
    public List<String> getUnsupportedOptions() {
        return UNSUPPORTED;
    }

    @Override
    public boolean is1_13TeamColourFix() {
        return getBoolean("team-colour-fix", true);
    }

    @Override
    public boolean isSuppress1_13ConversionErrors() {
        return getBoolean("suppress-1_13-conversion-errors", false);
    }

    @Override
    public boolean isDisable1_13AutoComplete() {
        return getBoolean("disable-1_13-auto-complete", false);
    }
}
