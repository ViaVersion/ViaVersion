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
package com.viaversion.viaversion.bukkit.platform;

import com.viaversion.viaversion.ViaVersionPlugin;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.platform.ViaPlatformLoader;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.bukkit.listeners.UpdateListener;
import com.viaversion.viaversion.bukkit.listeners.multiversion.PlayerSneakListener;
import com.viaversion.viaversion.bukkit.listeners.v1_14_4to1_15.EntityToggleGlideListener;
import com.viaversion.viaversion.bukkit.listeners.v1_18_2to1_19.BlockBreakListener;
import com.viaversion.viaversion.bukkit.listeners.v1_19_3to1_19_4.ArmorToggleListener;
import com.viaversion.viaversion.bukkit.listeners.v1_20_5to1_21.LegacyChangeItemListener;
import com.viaversion.viaversion.bukkit.listeners.v1_20_5to1_21.PaperPlayerChangeItemListener;
import com.viaversion.viaversion.bukkit.listeners.v1_21_4to1_21_5.ContainerClickDesyncCheck;
import com.viaversion.viaversion.bukkit.listeners.v1_8to1_9.ArmorListener;
import com.viaversion.viaversion.bukkit.listeners.v1_8to1_9.BlockListener;
import com.viaversion.viaversion.bukkit.listeners.v1_8to1_9.DeathListener;
import com.viaversion.viaversion.bukkit.listeners.v1_8to1_9.HandItemCache;
import com.viaversion.viaversion.bukkit.listeners.v1_8to1_9.PaperPatch;
import com.viaversion.viaversion.bukkit.providers.BukkitAckSequenceProvider;
import com.viaversion.viaversion.bukkit.providers.BukkitBlockConnectionProvider;
import com.viaversion.viaversion.bukkit.providers.BukkitInventoryQuickMoveProvider;
import com.viaversion.viaversion.bukkit.providers.BukkitPickItemProvider;
import com.viaversion.viaversion.bukkit.providers.BukkitViaMovementTransmitter;
import com.viaversion.viaversion.bukkit.util.NMSUtil;
import com.viaversion.viaversion.protocols.v1_11_1to1_12.provider.InventoryQuickMoveProvider;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.blockconnections.ConnectionData;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.blockconnections.providers.BlockConnectionProvider;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.provider.AckSequenceProvider;
import com.viaversion.viaversion.protocols.v1_21_2to1_21_4.provider.PickItemProvider;
import com.viaversion.viaversion.protocols.v1_8to1_9.provider.HandItemProvider;
import com.viaversion.viaversion.protocols.v1_8to1_9.provider.MovementTransmitterProvider;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;

public class BukkitViaLoader implements ViaPlatformLoader {

    private final Set<BukkitTask> tasks = new HashSet<>();
    private final ViaVersionPlugin plugin;
    private HandItemCache handItemCache;

    public BukkitViaLoader(ViaVersionPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerListener(Listener listener) {
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    @Override
    public void load() {
        registerListener(new UpdateListener());

        /* Base Protocol */
        final ViaVersionPlugin plugin = (ViaVersionPlugin) Bukkit.getPluginManager().getPlugin("ViaVersion");
        if (!Via.getAPI().getServerVersion().isKnown()) {
            Via.getPlatform().getLogger().severe("Server version has not been loaded yet, cannot register additional listeners");
            return;
        }

        ProtocolVersion serverProtocolVersion = Via.getAPI().getServerVersion().lowestSupportedProtocolVersion();

        /* 1.9 client to 1.8 server */
        if (serverProtocolVersion.olderThan(ProtocolVersion.v1_9)) {
            new ArmorListener(plugin).register();
            new DeathListener(plugin).register();
            if (plugin.getConf().cancelBlockSounds()) {
                new BlockListener(plugin).register();
            }

            if (plugin.getConf().isItemCache()) {
                handItemCache = new HandItemCache();
                tasks.add(handItemCache.runTaskTimerAsynchronously(plugin, 1L, 1L)); // Updates player's items :)
            }
        }

        if (serverProtocolVersion.olderThan(ProtocolVersion.v1_14)) {
            boolean use1_9Fix = plugin.getConf().is1_9HitboxFix() && serverProtocolVersion.olderThan(ProtocolVersion.v1_9);
            if (use1_9Fix || plugin.getConf().is1_14HitboxFix()) {
                try {
                    new PlayerSneakListener(plugin, use1_9Fix, plugin.getConf().is1_14HitboxFix()).register();
                } catch (ReflectiveOperationException e) {
                    Via.getPlatform().getLogger().log(Level.WARNING, "Could not load hitbox fix - please report this on our GitHub", e);
                }
            }
        }

        if (serverProtocolVersion.olderThan(ProtocolVersion.v1_15)) {
            try {
                Class.forName("org.bukkit.event.entity.EntityToggleGlideEvent");
                new EntityToggleGlideListener(plugin).register();
            } catch (ClassNotFoundException ignored) {
            }
        }

        if (serverProtocolVersion.olderThan(ProtocolVersion.v1_12) && !Boolean.getBoolean("com.viaversion.ignorePaperBlockPlacePatch")) {
            boolean paper = true;
            try {
                Class.forName("org.github.paperspigot.PaperSpigotConfig"); // Paper 1.8 ?
            } catch (ClassNotFoundException ignored) {
                try {
                    Class.forName("com.destroystokyo.paper.PaperConfig"); // Paper 1.9+ ?
                } catch (ClassNotFoundException alsoIgnored) {
                    paper = false; // Definitely not Paper
                }
            }
            if (paper) {
                new PaperPatch(plugin).register();
            }
        }

        if (serverProtocolVersion.olderThan(ProtocolVersion.v1_19_4) && plugin.getConf().isArmorToggleFix() && hasGetHandMethod()) {
            new ArmorToggleListener(plugin).register();
        }

        /* Providers */
        if (serverProtocolVersion.olderThan(ProtocolVersion.v1_9)) {
            Via.getManager().getProviders().use(MovementTransmitterProvider.class, new BukkitViaMovementTransmitter());

            Via.getManager().getProviders().use(HandItemProvider.class, new HandItemProvider() {
                @Override
                public Item getHandItem(final UserConnection info) {
                    if (handItemCache != null) {
                        return handItemCache.getHandItem(info.getProtocolInfo().getUuid());
                    }
                    try {
                        return Bukkit.getScheduler().callSyncMethod(Bukkit.getPluginManager().getPlugin("ViaVersion"), () -> {
                            UUID playerUUID = info.getProtocolInfo().getUuid();
                            Player player = Bukkit.getPlayer(playerUUID);
                            if (player != null) {
                                return HandItemCache.convert(player.getItemInHand());
                            }
                            return null;
                        }).get(10, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        Via.getPlatform().getLogger().log(Level.SEVERE, "Error fetching hand item", e);
                        return null;
                    }
                }
            });
        }

        if (serverProtocolVersion.olderThan(ProtocolVersion.v1_12)) {
            if (plugin.getConf().is1_12QuickMoveActionFix()) {
                Via.getManager().getProviders().use(InventoryQuickMoveProvider.class, new BukkitInventoryQuickMoveProvider());
            }
        }
        if (serverProtocolVersion.olderThan(ProtocolVersion.v1_13)) {
            if (Via.getConfig().getBlockConnectionMethod().equalsIgnoreCase("world")) {
                BukkitBlockConnectionProvider blockConnectionProvider = new BukkitBlockConnectionProvider();
                Via.getManager().getProviders().use(BlockConnectionProvider.class, blockConnectionProvider);
                ConnectionData.blockConnectionProvider = blockConnectionProvider;
            }
        }
        if (serverProtocolVersion.olderThan(ProtocolVersion.v1_19)) {
            Via.getManager().getProviders().use(AckSequenceProvider.class, new BukkitAckSequenceProvider(plugin));
            new BlockBreakListener(plugin).register();
        }
        if (serverProtocolVersion.olderThan(ProtocolVersion.v1_21)) {
            if (PaperViaInjector.hasClass("io.papermc.paper.event.player.PlayerInventorySlotChangeEvent")) {
                new PaperPlayerChangeItemListener(plugin).register();
            } else {
                new LegacyChangeItemListener(plugin).register();
            }
        }
        if (serverProtocolVersion.olderThan(ProtocolVersion.v1_21_4)) {
            Via.getManager().getProviders().use(PickItemProvider.class, new BukkitPickItemProvider(plugin));
        }

        // Needs to be enabled on any version with the updated item desync check
        if (serverProtocolVersion.newerThanOrEqualTo(ProtocolVersion.v1_17)) {
            try {
                final Class<?> craftPlayerClass = NMSUtil.obc("entity.CraftPlayer");
                if (PaperViaInjector.hasMethod(craftPlayerClass, "simplifyContainerDesyncCheck")) {
                    new ContainerClickDesyncCheck(plugin).register();
                }
            } catch (final ClassNotFoundException e) {
                Via.getPlatform().getLogger().log(Level.WARNING, "Could not find CraftPlayer class", e);
            }
        }
    }

    private boolean hasGetHandMethod() {
        try {
            PlayerInteractEvent.class.getDeclaredMethod("getHand");
            Material.class.getMethod("getEquipmentSlot");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    @Override
    public void unload() {
        for (BukkitTask task : tasks) {
            task.cancel();
        }
        tasks.clear();
    }
}
