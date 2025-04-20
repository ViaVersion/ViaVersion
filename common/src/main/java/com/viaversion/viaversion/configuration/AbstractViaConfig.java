/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.configuration;

import com.google.gson.JsonElement;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.configuration.ViaVersionConfig;
import com.viaversion.viaversion.api.minecraft.WorldIdentifiers;
import com.viaversion.viaversion.api.protocol.version.BlockedProtocolVersions;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.protocol.BlockedProtocolVersionsImpl;
import com.viaversion.viaversion.util.Config;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class AbstractViaConfig extends Config implements ViaVersionConfig {
    public static final List<String> BUKKIT_ONLY_OPTIONS = Arrays.asList("register-userconnections-on-join", "quick-move-action-fix",
        "change-1_9-hitbox", "change-1_14-hitbox", "blockconnection-method", "armor-toggle-fix", "use-new-deathmessages",
        "item-cache", "nms-player-ticking");

    public static final List<String> VELOCITY_ONLY_OPTIONS = Arrays.asList("velocity-ping-interval", "velocity-ping-save", "velocity-servers");

    private boolean checkForUpdates;
    private boolean preventCollision;
    private boolean useNewEffectIndicator;
    private boolean suppressMetadataErrors;
    private boolean shieldBlocking;
    private boolean noDelayShieldBlocking;
    private boolean showShieldWhenSwordInHand;
    private boolean hologramPatch;
    private boolean pistonAnimationPatch;
    private boolean bossbarPatch;
    private boolean bossbarAntiFlicker;
    private double hologramOffset;
    private int maxPPS;
    private String maxPPSKickMessage;
    private int trackingPeriod;
    private int warningPPS;
    private int maxPPSWarnings;
    private String maxPPSWarningsKickMessage;
    private boolean sendSupportedVersions;
    private boolean simulatePlayerTick;
    private boolean replacePistons;
    private int pistonReplacementId;
    private boolean chunkBorderFix;
    private boolean autoTeam;
    private boolean nbtArrayFix;
    private BlockedProtocolVersions blockedProtocolVersions;
    private String blockedDisconnectMessage;
    private String reloadDisconnectMessage;
    private boolean suppressConversionWarnings;
    private boolean suppressTextComponentConversionWarnings;
    private boolean disable1_13TabComplete;
    private boolean teamColourFix;
    private boolean serversideBlockConnections;
    private boolean reduceBlockStorageMemory;
    private boolean flowerStemWhenBlockAbove;
    private boolean vineClimbFix;
    private boolean snowCollisionFix;
    private boolean infestedBlocksFix;
    private int tabCompleteDelay;
    private boolean truncate1_14Books;
    private boolean leftHandedHandling;
    private boolean fullBlockLightFix;
    private boolean healthNaNFix;
    private boolean instantRespawn;
    private boolean ignoreLongChannelNames;
    private boolean forcedUse1_17ResourcePack;
    private JsonElement resourcePack1_17PromptMessage;
    private WorldIdentifiers map1_16WorldNames;
    private boolean cache1_17Light;
    private boolean translateOcelotToCat;
    private boolean enforceSecureChat;
    private boolean handleInvalidItemCount;
    private boolean cancelBlockSounds;
    private boolean hideScoreboardNumbers;
    private boolean fix1_21PlacementRotation;

    protected AbstractViaConfig(final File configFile, final Logger logger) {
        super(configFile, logger);
    }

    @Override
    public void reload() {
        super.reload();
        loadFields();
    }

    protected void loadFields() {
        checkForUpdates = getBoolean("check-for-updates", true);
        preventCollision = getBoolean("prevent-collision", true);
        useNewEffectIndicator = getBoolean("use-new-effect-indicator", true);
        suppressMetadataErrors = getBoolean("suppress-metadata-errors", false);
        shieldBlocking = getBoolean("shield-blocking", true);
        noDelayShieldBlocking = getBoolean("no-delay-shield-blocking", false);
        showShieldWhenSwordInHand = getBoolean("show-shield-when-sword-in-hand", false);
        hologramPatch = getBoolean("hologram-patch", false);
        pistonAnimationPatch = getBoolean("piston-animation-patch", false);
        bossbarPatch = getBoolean("bossbar-patch", true);
        bossbarAntiFlicker = getBoolean("bossbar-anti-flicker", false);
        hologramOffset = getDouble("hologram-y", -0.96D);
        maxPPS = getInt("max-pps", 800);
        maxPPSKickMessage = getString("max-pps-kick-msg", "Sending packets too fast? lag?");
        trackingPeriod = getInt("tracking-period", 6);
        warningPPS = getInt("tracking-warning-pps", 120);
        maxPPSWarnings = getInt("tracking-max-warnings", 3);
        maxPPSWarningsKickMessage = getString("tracking-max-kick-msg", "You are sending too many packets, :(");
        sendSupportedVersions = getBoolean("send-supported-versions", false);
        simulatePlayerTick = getBoolean("simulate-pt", true);
        replacePistons = getBoolean("replace-pistons", false);
        pistonReplacementId = getInt("replacement-piston-id", 0);
        chunkBorderFix = getBoolean("chunk-border-fix", false);
        autoTeam = getBoolean("auto-team", true);
        nbtArrayFix = getBoolean("chat-nbt-fix", true);
        blockedProtocolVersions = loadBlockedProtocolVersions();
        blockedDisconnectMessage = getString("block-disconnect-msg", "You are using an unsupported Minecraft version!");
        reloadDisconnectMessage = getString("reload-disconnect-msg", "Server reload, please rejoin!");
        teamColourFix = getBoolean("team-colour-fix", true);
        suppressConversionWarnings = getBoolean("suppress-conversion-warnings", false);
        suppressTextComponentConversionWarnings = getBoolean("suppress-text-component-conversion-warnings", true);
        disable1_13TabComplete = getBoolean("disable-1_13-auto-complete", false);
        serversideBlockConnections = getBoolean("serverside-blockconnections", true);
        reduceBlockStorageMemory = getBoolean("reduce-blockstorage-memory", false);
        flowerStemWhenBlockAbove = getBoolean("flowerstem-when-block-above", false);
        vineClimbFix = getBoolean("vine-climb-fix", false);
        snowCollisionFix = getBoolean("fix-low-snow-collision", false);
        infestedBlocksFix = getBoolean("fix-infested-block-breaking", true);
        tabCompleteDelay = getInt("1_13-tab-complete-delay", 0);
        truncate1_14Books = getBoolean("truncate-1_14-books", false);
        leftHandedHandling = getBoolean("left-handed-handling", true);
        fullBlockLightFix = getBoolean("fix-non-full-blocklight", false);
        healthNaNFix = getBoolean("fix-1_14-health-nan", true);
        instantRespawn = getBoolean("use-1_15-instant-respawn", false);
        ignoreLongChannelNames = getBoolean("ignore-long-1_16-channel-names", true);
        forcedUse1_17ResourcePack = getBoolean("forced-use-1_17-resource-pack", false);
        resourcePack1_17PromptMessage = getSerializedComponent("resource-pack-1_17-prompt");
        Map<String, String> worlds = get("map-1_16-world-names", new HashMap<>());
        map1_16WorldNames = new WorldIdentifiers(worlds.getOrDefault("overworld", WorldIdentifiers.OVERWORLD_DEFAULT),
            worlds.getOrDefault("nether", WorldIdentifiers.NETHER_DEFAULT),
            worlds.getOrDefault("end", WorldIdentifiers.END_DEFAULT));
        cache1_17Light = getBoolean("cache-1_17-light", true);
        translateOcelotToCat = getBoolean("translate-ocelot-to-cat", true);
        enforceSecureChat = getBoolean("enforce-secure-chat", false);
        handleInvalidItemCount = getBoolean("handle-invalid-item-count", false);
        cancelBlockSounds = getBoolean("cancel-block-sounds", true);
        hideScoreboardNumbers = getBoolean("hide-scoreboard-numbers", false);
        fix1_21PlacementRotation = getBoolean("fix-1_21-placement-rotation", true);
    }

    private BlockedProtocolVersions loadBlockedProtocolVersions() {
        List<Integer> blockProtocols = getListSafe("block-protocols", Integer.class, "Invalid blocked version protocol found in config: '%s'");
        List<String> blockVersions = getListSafe("block-versions", String.class, "Invalid blocked version found in config: '%s'");
        ObjectSet<ProtocolVersion> blockedProtocols = blockProtocols.stream().map(ProtocolVersion::getProtocol).collect(ObjectOpenHashSet::of, ObjectSet::add, ObjectSet::addAll);
        ProtocolVersion lowerBound = ProtocolVersion.unknown;
        ProtocolVersion upperBound = ProtocolVersion.unknown;
        for (String s : blockVersions) {
            if (s.isEmpty()) {
                continue;
            }

            char c = s.charAt(0);
            if (c == '<' || c == '>') {
                // Set lower/upper bound
                ProtocolVersion protocolVersion = protocolVersion(s.substring(1));
                if (protocolVersion == null) {
                    continue;
                }

                if (c == '<') {
                    if (lowerBound.isKnown()) {
                        logger.warning("Already set lower bound " + lowerBound + " overridden by " + protocolVersion.getName());
                    }
                    lowerBound = protocolVersion;
                } else {
                    if (upperBound.isKnown()) {
                        logger.warning("Already set upper bound " + upperBound + " overridden by " + protocolVersion.getName());
                    }
                    upperBound = protocolVersion;
                }
                continue;
            }

            ProtocolVersion protocolVersion = protocolVersion(s);
            if (protocolVersion == null) {
                continue;
            }

            // Add single protocol version and check for duplication
            if (!blockedProtocols.add(protocolVersion)) {
                logger.warning("Duplicated blocked protocol version " + protocolVersion);
            }
        }

        // Check for duplicated entries
        if (lowerBound.isKnown() || upperBound.isKnown()) {
            final ProtocolVersion finalLowerBound = lowerBound;
            final ProtocolVersion finalUpperBound = upperBound;
            blockedProtocols.removeIf(version -> {
                if (finalLowerBound.isKnown() && version.olderThan(finalLowerBound) || finalUpperBound.isKnown() && version.newerThan(finalUpperBound)) {
                    logger.warning("Blocked protocol version " + version + " already covered by upper or lower bound");
                    return true;
                }
                return false;
            });
        }
        return new BlockedProtocolVersionsImpl(blockedProtocols, lowerBound, upperBound);
    }

    private @Nullable ProtocolVersion protocolVersion(String s) {
        ProtocolVersion protocolVersion = ProtocolVersion.getClosest(s);
        if (protocolVersion == null) {
            logger.warning("Unknown protocol version in block-versions: " + s);
            return null;
        }
        return protocolVersion;
    }

    @Override
    public boolean isCheckForUpdates() {
        return checkForUpdates;
    }

    @Override
    public void setCheckForUpdates(boolean checkForUpdates) {
        this.checkForUpdates = checkForUpdates;
        set("checkforupdates", checkForUpdates);
    }

    @Override
    public boolean isPreventCollision() {
        return preventCollision;
    }

    @Override
    public boolean isNewEffectIndicator() {
        return useNewEffectIndicator;
    }

    @Override
    public boolean isShowNewDeathMessages() {
        return false;
    }

    @Override
    public boolean isSuppressMetadataErrors() {
        return suppressMetadataErrors;
    }

    @Override
    public boolean isShieldBlocking() {
        return shieldBlocking;
    }

    @Override
    public boolean isNoDelayShieldBlocking() {
        return noDelayShieldBlocking;
    }

    @Override
    public boolean isShowShieldWhenSwordInHand() {
        return showShieldWhenSwordInHand;
    }

    @Override
    public boolean isHologramPatch() {
        return hologramPatch;
    }

    @Override
    public boolean isPistonAnimationPatch() {
        return pistonAnimationPatch;
    }

    @Override
    public boolean isBossbarPatch() {
        return bossbarPatch;
    }

    @Override
    public boolean isBossbarAntiflicker() {
        return bossbarAntiFlicker;
    }

    @Override
    public double getHologramYOffset() {
        return hologramOffset;
    }

    @Override
    public int getMaxPPS() {
        return maxPPS;
    }

    @Override
    public String getMaxPPSKickMessage() {
        return maxPPSKickMessage;
    }

    @Override
    public int getTrackingPeriod() {
        return trackingPeriod;
    }

    @Override
    public int getWarningPPS() {
        return warningPPS;
    }

    @Override
    public int getMaxWarnings() {
        return maxPPSWarnings;
    }

    @Override
    public String getMaxWarningsKickMessage() {
        return maxPPSWarningsKickMessage;
    }

    @Override
    public boolean isSendSupportedVersions() {
        return sendSupportedVersions;
    }

    @Override
    public boolean isSimulatePlayerTick() {
        return simulatePlayerTick;
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
        return replacePistons;
    }

    @Override
    public int getPistonReplacementId() {
        return pistonReplacementId;
    }

    @Override
    public boolean isChunkBorderFix() {
        return chunkBorderFix;
    }

    @Override
    public boolean isAutoTeam() {
        // Collision has to be enabled first
        return preventCollision && autoTeam;
    }

    @Override
    public boolean is1_12NBTArrayFix() {
        return nbtArrayFix;
    }

    @Override
    public boolean shouldRegisterUserConnectionOnJoin() {
        return false;
    }

    @Override
    public boolean is1_12QuickMoveActionFix() {
        return false;
    }

    @Override
    public BlockedProtocolVersions blockedProtocolVersions() {
        return blockedProtocolVersions;
    }

    @Override
    public String getBlockedDisconnectMsg() {
        return blockedDisconnectMessage;
    }

    @Override
    public String getReloadDisconnectMsg() {
        return reloadDisconnectMessage;
    }

    @Override
    public boolean is1_13TeamColourFix() {
        return teamColourFix;
    }

    @Override
    public boolean isSuppressConversionWarnings() {
        return suppressConversionWarnings && !Via.getManager().isDebug(); // Debug mode overrules config
    }

    @Override
    public boolean isSuppressTextComponentConversionWarnings() {
        return suppressTextComponentConversionWarnings && !Via.getManager().isDebug(); // Debug mode overrules config
    }

    @Override
    public boolean isDisable1_13AutoComplete() {
        return disable1_13TabComplete;
    }

    @Override
    public boolean isServersideBlockConnections() {
        return serversideBlockConnections;
    }

    @Override
    public String getBlockConnectionMethod() {
        return "packet";
    }

    @Override
    public boolean isReduceBlockStorageMemory() {
        return reduceBlockStorageMemory;
    }

    @Override
    public boolean isStemWhenBlockAbove() {
        return flowerStemWhenBlockAbove;
    }

    @Override
    public boolean isVineClimbFix() {
        return vineClimbFix;
    }

    @Override
    public boolean isSnowCollisionFix() {
        return snowCollisionFix;
    }

    @Override
    public boolean isInfestedBlocksFix() {
        return infestedBlocksFix;
    }

    @Override
    public int get1_13TabCompleteDelay() {
        return tabCompleteDelay;
    }

    @Override
    public boolean isTruncate1_14Books() {
        return truncate1_14Books;
    }

    @Override
    public boolean isLeftHandedHandling() {
        return leftHandedHandling;
    }

    @Override
    public boolean is1_9HitboxFix() {
        return false;
    }

    @Override
    public boolean is1_14HitboxFix() {
        return false;
    }

    @Override
    public boolean isNonFullBlockLightFix() {
        return fullBlockLightFix;
    }

    @Override
    public boolean is1_14HealthNaNFix() {
        return healthNaNFix;
    }

    @Override
    public boolean is1_15InstantRespawn() {
        return instantRespawn;
    }

    @Override
    public boolean isIgnoreLong1_16ChannelNames() {
        return ignoreLongChannelNames;
    }

    @Override
    public boolean isForcedUse1_17ResourcePack() {
        return forcedUse1_17ResourcePack;
    }

    @Override
    public JsonElement get1_17ResourcePackPrompt() {
        return resourcePack1_17PromptMessage;
    }

    @Override
    public WorldIdentifiers get1_16WorldNamesMap() {
        return map1_16WorldNames;
    }

    @Override
    public boolean cache1_17Light() {
        return cache1_17Light;
    }

    @Override
    public boolean isArmorToggleFix() {
        return false;
    }

    @Override
    public boolean translateOcelotToCat() {
        return translateOcelotToCat;
    }

    @Override
    public boolean enforceSecureChat() {
        return enforceSecureChat;
    }

    @Override
    public boolean handleInvalidItemCount() {
        return handleInvalidItemCount;
    }

    @Override
    public boolean cancelBlockSounds() {
        return cancelBlockSounds;
    }

    @Override
    public boolean hideScoreboardNumbers() {
        return hideScoreboardNumbers;
    }

    @Override
    public boolean fix1_21PlacementRotation() {
        return fix1_21PlacementRotation;
    }
}
