/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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

import com.viaversion.viaversion.api.configuration.ViaVersionConfig;
import com.viaversion.viaversion.util.Config;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.io.File;

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
    private boolean autoTeam;
    private boolean forceJsonTransform;
    private boolean nbtArrayFix;
    private IntSet blockedProtocols;
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
        autoTeam = getBoolean("auto-team", true);
        forceJsonTransform = getBoolean("force-json-transform", false);
        nbtArrayFix = getBoolean("chat-nbt-fix", true);
        blockedProtocols = new IntOpenHashSet(getIntegerList("block-protocols"));
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
    public boolean isAntiXRay() {
        return false;
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
    public boolean is1_12QuickMoveActionFix() {
        return false;
    }

    @Override
    public IntSet getBlockedProtocols() {
        return blockedProtocols;
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
}
