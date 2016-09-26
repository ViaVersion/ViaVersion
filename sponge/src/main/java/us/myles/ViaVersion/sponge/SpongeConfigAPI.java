package us.myles.ViaVersion.sponge;

import us.myles.ViaVersion.SpongePlugin;
import us.myles.ViaVersion.api.ViaVersionConfig;
import us.myles.ViaVersion.api.configuration.ConfigurationProvider;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpongeConfigAPI implements ViaVersionConfig, ConfigurationProvider{
    private final SpongePlugin spongePlugin;

    public SpongeConfigAPI(SpongePlugin spongePlugin) {
        this.spongePlugin = spongePlugin;
    }

    @Override
    public boolean isCheckForUpdates() {
        return false;
    }

    @Override
    public boolean isPreventCollision() {
        return false;
    }

    @Override
    public boolean isNewEffectIndicator() {
        return false;
    }

    @Override
    public boolean isShowNewDeathMessages() {
        return false;
    }

    @Override
    public boolean isSuppressMetadataErrors() {
        return false;
    }

    @Override
    public boolean isShieldBlocking() {
        return false;
    }

    @Override
    public boolean isHologramPatch() {
        return false;
    }

    @Override
    public boolean isBossbarPatch() {
        return false;
    }

    @Override
    public boolean isBossbarAntiflicker() {
        return false;
    }

    @Override
    public boolean isUnknownEntitiesSuppressed() {
        return false;
    }

    @Override
    public double getHologramYOffset() {
        return 0;
    }

    @Override
    public boolean isAutoTeam() {
        return false;
    }

    @Override
    public boolean isBlockBreakPatch() {
        return false;
    }

    @Override
    public int getMaxPPS() {
        return 0;
    }

    @Override
    public String getMaxPPSKickMessage() {
        return null;
    }

    @Override
    public int getTrackingPeriod() {
        return 0;
    }

    @Override
    public int getWarningPPS() {
        return 0;
    }

    @Override
    public int getMaxWarnings() {
        return 0;
    }

    @Override
    public String getMaxWarningsKickMessage() {
        return null;
    }

    @Override
    public boolean isAntiXRay() {
        return false;
    }

    @Override
    public boolean isSendSupportedVersions() {
        return false;
    }

    @Override
    public boolean isStimulatePlayerTick() {
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
    public boolean isReplacePistons() {
        return false;
    }

    @Override
    public int getPistonReplacementId() {
        return 0;
    }

    @Override
    public boolean isForceJsonTransform() {
        return false;
    }

    @Override
    public List<Integer> getBlockedProtocols() {
        return Arrays.asList(0);
    }

    @Override
    public String getBlockedDisconnectMsg() {
        return "Boop";
    }

    @Override
    public String getReloadDisconnectMsg() {
        return "Beep";
    }

    @Override
    public void set(String path, Object value) {

    }

    @Override
    public void saveConfig() {

    }

    @Override
    public Map<String, Object> getValues() {
        return new HashMap<>();
    }
}
