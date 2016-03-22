package us.myles.ViaVersion.api;

public interface ViaVersionConfig {

    /**
     * Get if global debug is enabled
     *
     * @return true if debug is enabled
     */
    boolean isDebug();

    /**
     * Get if the plugin should check for updates
     *
     * @return true if update checking is enabled
     */
    boolean isCheckForUpdates();

    /**
     * Get if collision preventing for players is enabled
     *
     * @return true if collision preventing is enabled
     */
    boolean isPreventCollision();

    /**
     * Get if 1.9 clients are shown the new effect indicator in the top-right corner
     *
     * @return true if the using of the new effect indicator is enabled
     */
    boolean isNewEffectIndicator();

    /**
     * Get if metadata errors will be suppressed
     *
     * @return true if metadata errors suppression is enabled
     */
    boolean isSuppressMetadataErrors();

    /**
     * Get if blocking in 1.9 appears as a player holding a shield
     *
     * @return true if shield blocking is enabled
     */
    boolean isShieldBlocking();

    /**
     * Get if armor stand positions are fixed so holograms show up at the correct height in 1.9
     *
     * @return true if hologram patching is enabled
     */
    boolean isHologramPatch();

    /**
     * Get if boss bars are fixed for 1.9 clients
     *
     * @return true if boss bar patching is enabled
     */
    boolean isBossbarPatch();

    /**
     * Get if the boss bars for 1.9 clients are being stopped from flickering
     * This will keep all boss bars on 100% (not recommended)
     *
     * @return true if boss bar anti flickering is enabled
     */
    boolean isBossbarAntiflicker();

    /**
     * Get if unknown entity errors will be suppressed
     *
     * @return true if boss bar patching is enabled
     */
    boolean isUnknownEntitiesSuppressed();

    /**
     * Get the vertical offset armor stands are being moved with when the hologram patch is enabled
     *
     * @return the vertical offset holograms will be moved with
     */
    double getHologramYOffset();

    /**
     * Get if players will be automatically put in the same team when collision preventing is enabled
     *
     * @return true if automatic teaming is enabled
     */
    boolean isAutoTeam();
}
