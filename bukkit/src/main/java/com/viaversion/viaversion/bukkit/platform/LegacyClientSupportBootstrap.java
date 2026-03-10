/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.bukkit.platform;

import com.viaversion.viabackwards.protocol.v1_20_2to1_20.provider.AdvancementCriteriaProvider;
import com.viaversion.viarewind.protocol.v1_9to1_8.provider.InventoryProvider;
import com.viaversion.viaversion.ViaVersionPlugin;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.bukkit.compat.viabackwards.listener.DurabilitySync1_11;
import com.viaversion.viaversion.bukkit.compat.viabackwards.listener.FireExtinguish1_16;
import com.viaversion.viaversion.bukkit.compat.viabackwards.listener.ItemDropSync1_17;
import com.viaversion.viaversion.bukkit.compat.viabackwards.listener.LecternInteract1_14;
import com.viaversion.viaversion.bukkit.compat.viabackwards.listener.PlayerHurtSound1_12;
import com.viaversion.viaversion.bukkit.compat.viabackwards.listener.SpearAttack1_21_11;
import com.viaversion.viaversion.bukkit.compat.viabackwards.provider.BukkitAdvancementCriteriaProvider;
import com.viaversion.viaversion.bukkit.compat.viarewind.provider.BukkitInventoryProvider;

public final class LegacyClientSupportBootstrap {
    private final ViaVersionPlugin plugin;
    private final IntegratedViaBackwardsPlatform viaBackwardsPlatform;
    private final IntegratedViaRewindPlatform viaRewindPlatform;

    private LegacyClientSupportBootstrap(final ViaVersionPlugin plugin) {
        this.plugin = plugin;
        this.viaBackwardsPlatform = new IntegratedViaBackwardsPlatform(plugin);
        this.viaRewindPlatform = new IntegratedViaRewindPlatform(plugin);
    }

    public static void install(final ViaVersionPlugin plugin) {
        final LegacyClientSupportBootstrap bootstrap = new LegacyClientSupportBootstrap(plugin);
        Via.getManager().addEnableListener(bootstrap::registerProtocols);
        Via.getManager().addPostEnableListener(bootstrap::enableBukkitSupport);
    }

    private void registerProtocols() {
        if (plugin.getConf().isAutoEnableViaBackwards()) {
            viaBackwardsPlatform.initSupport();
        }

        if (plugin.getConf().isAutoEnableViaRewind()) {
            viaRewindPlatform.initSupport();
        }
    }

    private void enableBukkitSupport() {
        if (plugin.getConf().isAutoEnableViaBackwards()) {
            viaBackwardsPlatform.enableSupport();
        }

        final ViaProviders providers = Via.getManager().getProviders();
        final ProtocolVersion protocolVersion = Via.getAPI().getServerVersion().highestSupportedProtocolVersion();

        if (plugin.getConf().isAutoEnableViaRewind() && protocolVersion.newerThanOrEqualTo(ProtocolVersion.v1_9)) {
            providers.use(InventoryProvider.class, new BukkitInventoryProvider());
        }

        if (plugin.getConf().isAutoEnableViaBackwards() && protocolVersion.newerThanOrEqualTo(ProtocolVersion.v1_21_11)) {
            new SpearAttack1_21_11(plugin).register();
        }
        if (plugin.getConf().isAutoEnableViaBackwards() && protocolVersion.newerThanOrEqualTo(ProtocolVersion.v1_17)) {
            new ItemDropSync1_17(plugin).register();
        }
        if (plugin.getConf().isAutoEnableViaBackwards() && protocolVersion.newerThanOrEqualTo(ProtocolVersion.v1_16)) {
            new FireExtinguish1_16(plugin).register();
        }
        if (plugin.getConf().isAutoEnableViaBackwards() && protocolVersion.newerThanOrEqualTo(ProtocolVersion.v1_14)) {
            new LecternInteract1_14(plugin).register();
        }
        if (plugin.getConf().isAutoEnableViaBackwards() && protocolVersion.newerThanOrEqualTo(ProtocolVersion.v1_12)) {
            new PlayerHurtSound1_12(plugin).register();
        }
        if (plugin.getConf().isAutoEnableViaBackwards() && protocolVersion.newerThanOrEqualTo(ProtocolVersion.v1_11)) {
            new DurabilitySync1_11(plugin).register();
        }
        if (plugin.getConf().isAutoEnableViaBackwards() && protocolVersion.newerThanOrEqualTo(ProtocolVersion.v1_20_2)) {
            providers.use(AdvancementCriteriaProvider.class, new BukkitAdvancementCriteriaProvider());
        }
    }
}

