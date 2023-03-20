/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2023 ViaVersion and contributors
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
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntPredicate;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class AbstractViaConfig extends Config implements ViaVersionConfig {

    private boolean checkForUpdates;
    private boolean preventCollision;
    private boolean useNewEffectIndicator;
    private boolean useNewDeathmessages;
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
    private boolean itemCache;
    private boolean nmsPlayerTicking;
    private boolean replacePistons;
    private int pistonReplacementId;
    private boolean chunkBorderFix;
    private boolean autoTeam;
    private boolean forceJsonTransform;
    private boolean nbtArrayFix;
    private BlockedProtocolVersions blockedProtocolVersions;
    private String blockedDisconnectMessage;
    private String reloadDisconnectMessage;
    private boolean suppressConversionWarnings;
    private boolean disable1_13TabComplete;
    private boolean minimizeCooldown;
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
    private Map<String, String> chatTypeFormats;

    protected AbstractViaConfig(File configFile) {
        super(configFile);
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        loadFields();
    }

    protected void loadFields() {
        checkForUpdates = getBoolean("checkforupdates", true);
        preventCollision = getBoolean("prevent-collision", true);
        useNewEffectIndicator = getBoolean("use-new-effect-indicator", true);
        useNewDeathmessages = getBoolean("use-new-deathmessages", true);
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
        itemCache = getBoolean("item-cache", true);
        nmsPlayerTicking = getBoolean("nms-player-ticking", true);
        replacePistons = getBoolean("replace-pistons", false);
        pistonReplacementId = getInt("replacement-piston-id", 0);
        chunkBorderFix = getBoolean("chunk-border-fix", false);
        autoTeam = getBoolean("auto-team", true);
        forceJsonTransform = getBoolean("force-json-transform", false);
        nbtArrayFix = getBoolean("chat-nbt-fix", true);
        blockedProtocolVersions = loadBlockedProtocolVersions();
        blockedDisconnectMessage = getString("block-disconnect-msg", "You are using an unsupported Minecraft version!");
        reloadDisconnectMessage = getString("reload-disconnect-msg", "Server reload, please rejoin!");
        minimizeCooldown = getBoolean("minimize-cooldown", true);
        teamColourFix = getBoolean("team-colour-fix", true);
        suppressConversionWarnings = getBoolean("suppress-conversion-warnings", false);
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
        Map<String, String> worlds = get("map-1_16-world-names", Map.class, new HashMap<String, String>());
        map1_16WorldNames = new WorldIdentifiers(worlds.getOrDefault("overworld", WorldIdentifiers.OVERWORLD_DEFAULT),
                worlds.getOrDefault("nether", WorldIdentifiers.NETHER_DEFAULT),
                worlds.getOrDefault("end", WorldIdentifiers.END_DEFAULT));
        cache1_17Light = getBoolean("cache-1_17-light", true);
        chatTypeFormats = get("chat-types-1_19", Map.class, new HashMap<String, String>());
    }

    private BlockedProtocolVersions loadBlockedProtocolVersions() {
        List<Integer> blockProtocols = getListSafe("block-protocols", Integer.class, "Invalid blocked version protocol found in config: '%s'");
        List<String> blockVersions = getListSafe("block-versions", String.class, "Invalid blocked version found in config: '%s'");
        IntSet blockedProtocols = new IntOpenHashSet(blockProtocols);
        int lowerBound = -1;
        int upperBound = -1;
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
                    if (lowerBound != -1) {
                        Via.getPlatform().getLogger().warning("Already set lower bound " + lowerBound + " overridden by " + protocolVersion.getName());
                    }
                    lowerBound = protocolVersion.getVersion();
                } else {
                    if (upperBound != -1) {
                        Via.getPlatform().getLogger().warning("Already set upper bound " + upperBound + " overridden by " + protocolVersion.getName());
                    }
                    upperBound = protocolVersion.getVersion();
                }
                continue;
            }

            ProtocolVersion protocolVersion = protocolVersion(s);
            if (protocolVersion == null) {
                continue;
            }

            // Add single protocol version and check for duplication
            if (!blockedProtocols.add(protocolVersion.getVersion())) {
                Via.getPlatform().getLogger().warning("Duplicated blocked protocol version " + protocolVersion.getName() + "/" + protocolVersion.getVersion());
            }
        }

        // Check for duplicated entries
        if (lowerBound != -1 || upperBound != -1) {
            final int finalLowerBound = lowerBound;
            final int finalUpperBound = upperBound;
            blockedProtocols.removeIf((IntPredicate) version -> {
                if (finalLowerBound != -1 && version < finalLowerBound || finalUpperBound != -1 && version > finalUpperBound) {
                    ProtocolVersion protocolVersion = ProtocolVersion.getProtocol(version);
                    Via.getPlatform().getLogger().warning("Blocked protocol version "
                            + protocolVersion.getName() + "/" + protocolVersion.getVersion() + " already covered by upper or lower bound");
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
            Via.getPlatform().getLogger().warning("Unknown protocol version in block-versions: " + s);
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
        return useNewDeathmessages;
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
        return itemCache;
    }

    @Override
    public boolean isNMSPlayerTicking() {
        return nmsPlayerTicking;
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
    public boolean isForceJsonTransform() {
        return forceJsonTransform;
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
    public boolean isMinimizeCooldown() {
        return minimizeCooldown;
    }

    @Override
    public boolean is1_13TeamColourFix() {
        return teamColourFix;
    }

    @Override
    public boolean isSuppressConversionWarnings() {
        return suppressConversionWarnings;
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
    public @Nullable String chatTypeFormat(final String translationKey) {
        return chatTypeFormats.get(translationKey);
    }

    @Override
    public boolean isArmorToggleFix() {
        return false;
    }
}
