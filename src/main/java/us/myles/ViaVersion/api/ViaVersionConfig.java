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

    boolean isPreventCollision();

    boolean isNewEffectIndicator();

    boolean isSuppressMetadataErrors();

    boolean isShieldBlocking();

    boolean isHologramPatch();

    boolean isBossbarPatch();

    boolean isBossbarAntiflicker();

    boolean isUnkownEntitiesSuppressed();

    double getHologramYOffset();

    boolean isAutoTeam();
}
