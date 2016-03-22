package us.myles.ViaVersion.api;

/**
 * Created by Hugo on 22/03/2016.
 */
public interface ViaVersionConfig {

    /**
     * Obtain if global debug is enabled
     *
     * @return true if debug is enabled
     */
    boolean isDebug();

    public boolean isPreventCollision();

    public boolean isNewEffectIndicator();

    public boolean isSuppressMetadataErrors();

    public boolean isShieldBlocking();

    public boolean isHologramPatch();

    public boolean isBossbarPatch();

    public boolean isBossbarAntiflicker();

    public boolean isUnkownEntitiesSuppressed();

    public double getHologramYOffset();

    public boolean isAutoTeam();
}
