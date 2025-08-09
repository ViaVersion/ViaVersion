/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.viaversion.viaversion.api.configuration;

import com.google.gson.JsonElement;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.WorldIdentifiers;
import com.viaversion.viaversion.api.protocol.version.BlockedProtocolVersions;

public interface ViaVersionConfig extends Config {

    /**
     * Get if the plugin should check for updates
     *
     * @return true if update checking is enabled
     */
    boolean isCheckForUpdates();

    /**
     * Sets whether to check for updates. This updates the internally cached value
     * as well as the config, but does not save the config to disk.
     *
     * @param checkForUpdates true to check for updates on server start and joins
     */
    void setCheckForUpdates(boolean checkForUpdates);

    /**
     * Get if collision preventing for players is enabled
     *
     * @return true if collision preventing is enabled
     */
    boolean isPreventCollision();

    /**
     * Get if 1.9 &amp; 1.10 clients are shown the new effect indicator in the top-right corner
     *
     * @return true if the using of the new effect indicator is enabled
     */
    boolean isNewEffectIndicator();

    /**
     * Get if 1.9 &amp; 1.10 clients are shown the new death message on the death screen
     *
     * @return true if enabled
     */
    boolean isShowNewDeathMessages();

    /**
     * Get if entity data errors will be suppressed
     *
     * @return true if entity data errors suppression is enabled
     */
    boolean isSuppressMetadataErrors();

    /**
     * Get if blocking in 1.9 &amp; 1.10 appears as a player holding a shield
     *
     * @return true if shield blocking is enabled
     */
    boolean isShieldBlocking();

    /**
     * Whether the player can block with the shield without a delay.
     * <p>
     * This option requires {@link #isShowShieldWhenSwordInHand()} to be disabled
     *
     * @return {@code true} if non-delayed shield blocking is enabled.
     */
    boolean isNoDelayShieldBlocking();

    /**
     * Puts the shield into the second hand when holding a sword.
     * The shield will disappear when switching to another item.
     * <p>
     * This option requires {@link #isShieldBlocking()} to be enabled
     *
     * @return {@code true} if the shield should appear when holding a sword
     */
    boolean isShowShieldWhenSwordInHand();

    /**
     * Get if armor stand positions are fixed so holograms show up at the correct height in 1.9 &amp; 1.10
     *
     * @return true if hologram patching is enabled
     */
    boolean isHologramPatch();

    /**
     * Get if the 1.11 piston animation patch is enabled
     *
     * @return true if the piston patch is enabled.
     */
    boolean isPistonAnimationPatch();

    /**
     * Get if boss bars are fixed for 1.9 &amp; 1.10 clients
     *
     * @return true if boss bar patching is enabled
     */
    boolean isBossbarPatch();

    /**
     * Get if the boss bars for 1.9 &amp; 1.10 clients are being stopped from flickering
     * This will keep all boss bars on 100% (not recommended)
     *
     * @return true if boss bar anti-flickering is enabled
     */
    boolean isBossbarAntiflicker();

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

    /**
     * Get the maximum number of packets a client can send per second.
     *
     * @return The number of packets a client can send per second.
     */
    @Deprecated(forRemoval = true)
    int getMaxPPS();

    /**
     * Get the kick message sent if the user hits the max packets per second.
     *
     * @return Kick message, with colour codes using '&amp;amp;'
     */
    @Deprecated(forRemoval = true)
    String getMaxPPSKickMessage();

    /**
     * The time in seconds that should be tracked for warnings
     *
     * @return Time in seconds that should be tracked for warnings
     */
    @Deprecated(forRemoval = true)
    int getTrackingPeriod();

    /**
     * The number of packets per second to count as a warning
     *
     * @return The number of packets per second to count as a warning.
     */
    @Deprecated(forRemoval = true)
    int getWarningPPS();

    /**
     * Get the maximum number of warnings the client can have in the interval
     *
     * @return The number of packets a client can send per second.
     */
    @Deprecated(forRemoval = true)
    int getMaxWarnings();

    /**
     * Get the kick message sent if the user goes over the warnings in the interval
     *
     * @return Kick message, with colour codes using '&amp;amp;'
     */
    @Deprecated(forRemoval = true)
    String getMaxWarningsKickMessage();

    RateLimitConfig getPacketTrackerConfig();

    RateLimitConfig getPacketSizeTrackerConfig();

    /**
     * Send supported versions in the status response packet
     *
     * @return If true, enabled
     */
    boolean isSendSupportedVersions();

    /**
     * Stimulate the player tick
     *
     * @return if true, enabled
     */
    boolean isSimulatePlayerTick();

    /**
     * Use the item cache to prevent high resource usage
     *
     * @return if true, enabled
     */
    boolean isItemCache();

    /**
     * Use the NMS player ticking
     *
     * @return if true, enabled
     */
    boolean isNMSPlayerTicking();

    /**
     * Replace extended pistons on 1.10 chunk loading.
     *
     * @return true if to replace them
     */
    boolean isReplacePistons();

    /**
     * Get the id for replacing extended pistons.
     *
     * @return The integer id
     */
    int getPistonReplacementId();

    /**
     * Fix 1.9+ clients not rendering the far away chunks
     *
     * @return true to fix chunk borders
     */
    boolean isChunkBorderFix();

    /**
     * Should we make team colours based on the last colour in team prefix
     *
     * @return true if enabled
     */
    boolean is1_13TeamColourFix();

    boolean shouldRegisterUserConnectionOnJoin();

    /**
     * Should we fix shift quick move action for 1.12 clients
     *
     * @return true if enabled
     */
    boolean is1_12QuickMoveActionFix();

    /**
     * API to check for blocked protocol versions.
     *
     * @return blocked protocol versions
     */
    BlockedProtocolVersions blockedProtocolVersions();

    /**
     * Get the custom disconnect message
     *
     * @return Disconnect message
     */
    String getBlockedDisconnectMsg();

    boolean logBlockedJoins();

    /**
     * Get the message sent to players being kicked on reload.
     * Players are kicked to stop the server crashing
     *
     * @return Disconnect message
     */
    String getReloadDisconnectMsg();

    /**
     * Should we hide errors that occur when trying to convert block and item data over versions?
     *
     * @return true if enabled
     */
    boolean isSuppressConversionWarnings();

    /**
     * Should we hide errors that occur when trying to convert text components?
     *
     * @return true if enabled
     */
    boolean isSuppressTextComponentConversionWarnings();

    /**
     * Should we disable the 1.13 auto-complete feature to stop spam kicks? (for any server lower than 1.13)
     *
     * @return true if enabled
     */
    boolean isDisable1_13AutoComplete();

    /**
     * Enable the serverside blockconnections for 1.13+ clients
     *
     * @return true if enabled
     */
    boolean isServersideBlockConnections();

    /**
     * Get the type of block-connection provider which should be used
     *
     * @return String world for world-level or packet for packet-level
     */
    String getBlockConnectionMethod();

    /**
     * When activated, only the most important blocks are saved in the BlockStorage.
     *
     * @return true if enabled
     */
    boolean isReduceBlockStorageMemory();

    /**
     * When activated with serverside-blockconnections, flower parts with blocks above will be sent as stems.
     * Useful for lobbyservers where users can't build and those stems are used decoratively.
     *
     * @return true if enabled
     */
    boolean isStemWhenBlockAbove();

    /**
     * Vines not connected to any blocks will be mapped to air for 1.13+ clients to prevent them from climbing up.
     *
     * @return true if enabled
     */
    boolean isVineClimbFix();

    /**
     * When activated, the 1-layer snow will be sent as 2-layer snow to 1.13+ clients to have collision.
     *
     * @return true if enabled
     */
    boolean isSnowCollisionFix();

    /**
     * When activated, infested blocks will be mapped to their normal stone variants for 1.13+ clients.
     *
     * @return true if enabled
     */
    boolean isInfestedBlocksFix();

    /**
     * When greater than 0, enables tab complete request delaying by x ticks
     *
     * @return the delay in ticks
     */
    int get1_13TabCompleteDelay();

    /**
     * When activated, edited books with more than 50 pages will be shortened to 50.
     *
     * @return true if enabled
     */
    boolean isTruncate1_14Books();

    /**
     * Handles left-handed info by using unused bit 7 on Client Settings packet
     *
     * @return true if enabled
     */
    boolean isLeftHandedHandling();

    /**
     * Fixes velocity bugs due to different hitbox for 1.9-1.13 clients on 1.8 servers.
     *
     * @return true if enabled
     */
    boolean is1_9HitboxFix();

    /**
     * Fixes velocity bugs due to different hitbox for 1.14+ clients on sub 1.14 servers.
     *
     * @return true if enabled
     */
    boolean is1_14HitboxFix();

    /**
     * Fixes non full blocks having 0 light for 1.14+ clients on sub 1.14 servers.
     *
     * @return true if enabled
     */
    boolean isNonFullBlockLightFix();

    boolean is1_14HealthNaNFix();

    /**
     * Should 1.15 clients respawn instantly / without showing the death screen.
     *
     * @return true if enabled
     */
    boolean is1_15InstantRespawn();

    /**
     * Ignores incoming plugin channel messages of 1.16+ clients with channel names longer than 32 charatcers.
     *
     * @return true if enabled
     */
    boolean isIgnoreLong1_16ChannelNames();

    /**
     * Force 1.17+ client to accept the server resource pack.
     *
     * @return true if enabled
     */
    boolean isForcedUse1_17ResourcePack();

    /**
     * Get the message that is sent when a user displays a resource pack prompt.
     *
     * @return cached serialized component
     */
    JsonElement get1_17ResourcePackPrompt();

    /***
     * Get the world names that should be returned for each Vanilla dimension.
     * Note that this can be overridden per-user by using {@link UserConnection#put(StorableObject)} with
     * a custom instance of {@link WorldIdentifiers} for the user's {@link UserConnection}.
     *
     * @return the global map from vanilla dimensions to world name
     */
    WorldIdentifiers get1_16WorldNamesMap();

    /**
     * Caches light until chunks are unloaded to allow subsequent chunk update packets as opposed to instantly uncaching when the first chunk data is sent.
     *
     * @return true if enabled
     */
    boolean cache1_17Light();

    /**
     * Force-update 1.19.4+ player's inventory when they try to swap armor in a pre-occupied slot.
     *
     * @return true if enabled
     */
    boolean isArmorToggleFix();

    /**
     * If disabled, tamed cats will be displayed as ocelots to 1.14+ clients on 1.13 servers. Otherwise, ocelots (tamed and untamed) will be displayed as cats.
     *
     * @return true if enabled
     */
    boolean translateOcelotToCat();

    /**
     * Returns the value of the "enforce secure chat" setting sent to 1.19+ clients on join.
     *
     * @return the value sent to 1.19+ clients on join
     */
    boolean enforceSecureChat();

    /**
     * Handles items with invalid count values (higher than max stack size) on 1.20.3 servers.
     *
     * @return true if enabled
     */
    boolean handleInvalidItemCount();

    /**
     * Tries to cancel block break/place sounds sent by 1.8 servers to 1.9+ clients to prevent them from playing twice
     *
     * @return true if enabled
     */
    boolean cancelBlockSounds();

    /**
     * Hides scoreboard numbers for 1.20.3+ clients on older server versions.
     *
     * @return true if enabled
     */
    boolean hideScoreboardNumbers();

    /**
     * Fixes 1.21+ clients on 1.20.5 servers placing water/lava buckets at the wrong location when moving fast.
     *
     * @return true if enabled
     */
    boolean fix1_21PlacementRotation();

    /**
     * If enabled, cancel swing packets sent while having an inventory opened on 1.15.2 and below servers.
     * This can cause false positives with anti-cheat plugins.
     *
     * @return true if enabled
     */
    boolean cancelSwingInInventory();
}
