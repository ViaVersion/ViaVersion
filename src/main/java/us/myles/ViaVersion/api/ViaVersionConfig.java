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

    /**
     * Obtain if collision preventing for players is enabled
     *
     * @return true if collision preventing is enabled
     */
    boolean isPreventCollision();

    boolean isNewEffectIndicator();

    boolean isSuppressMetadataErrors();

    boolean isShieldBlocking();

    boolean isHologramPatch();

    boolean isBossbarPatch();

    boolean isBossbarAntiflicker();

    boolean isUnknownEntitiesSuppressed();

    double getHologramYOffset();

    boolean isAutoTeam();
}
