/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
package com.viaversion.viaversion.bukkit.listeners.v1_20_5to1_21;

import com.viaversion.viaversion.ViaVersionPlugin;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.bukkit.listeners.ViaBukkitListener;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.Protocol1_20_5To1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.storage.EfficiencyAttributeStorage;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * For some reason, mining efficiency is not calculated by the client anymore, but by the server,
 * then sending the current value to the client every time the item changes. This roughly emulates that behavior.
 */
public final class PlayerChangeItemListener extends ViaBukkitListener {

    private final Enchantment efficiency = Enchantment.getByKey(NamespacedKey.minecraft("efficiency"));
    private final Enchantment depthStrider = Enchantment.getByKey(NamespacedKey.minecraft("depth_strider"));
    private final Enchantment soulSpeed = Enchantment.getByKey(NamespacedKey.minecraft("soul_speed"));
    private final Enchantment swiftSneak = Enchantment.getByKey(NamespacedKey.minecraft("swift_sneak"));

    public PlayerChangeItemListener(final ViaVersionPlugin plugin) {
        super(plugin, Protocol1_20_5To1_21.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInventorySlotChangedEvent(final PlayerInventorySlotChangeEvent event) {
        final Player player = event.getPlayer();
        final ItemStack item = event.getNewItemStack();
        final PlayerInventory inventory = player.getInventory();
        final int slot = event.getSlot();
        if (slot == inventory.getHeldItemSlot()) {
            sendAttributeUpdate(player, item, Slot.HAND);
        } else if (slot == 36) {
            sendAttributeUpdate(player, item, Slot.BOOTS);
        } else if (slot == 37) {
            sendAttributeUpdate(player, item, Slot.LEGGINGS);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerItemHeld(final PlayerItemHeldEvent event) {
        final Player player = event.getPlayer();
        final ItemStack item = player.getInventory().getItem(event.getNewSlot());
        sendAttributeUpdate(player, item, Slot.HAND);
    }

    private void sendAttributeUpdate(final Player player, @Nullable final ItemStack item, final Slot slot) {
        final UserConnection connection = Via.getAPI().getConnection(player.getUniqueId());
        if (connection == null || !isOnPipe(player)) {
            return;
        }

        final EfficiencyAttributeStorage storage = connection.get(EfficiencyAttributeStorage.class);
        if (storage == null) {
            return;
        }

        final EfficiencyAttributeStorage.ActiveEnchants activeEnchants = storage.activeEnchants();
        int efficiencyLevel = activeEnchants.efficiency().level();
        int soulSpeedLevel = activeEnchants.soulSpeed().level();
        int swiftSneakLevel = activeEnchants.swiftSneak().level();
        int depthStriderLevel = activeEnchants.depthStrider().level();
        switch (slot) {
            case HAND -> efficiencyLevel = item != null ? item.getEnchantmentLevel(efficiency) : 0;
            case LEGGINGS -> swiftSneakLevel = item != null && swiftSneak != null ? item.getEnchantmentLevel(swiftSneak) : 0;
            case BOOTS -> {
                depthStriderLevel = item != null && depthStrider != null ? item.getEnchantmentLevel(depthStrider) : 0;
                soulSpeedLevel = item != null && soulSpeed != null ? item.getEnchantmentLevel(soulSpeed) : 0;
            }
        }
        storage.setEnchants(player.getEntityId(), connection, efficiencyLevel, soulSpeedLevel, swiftSneakLevel, depthStriderLevel);
    }

    private enum Slot {
        HAND, BOOTS, LEGGINGS
    }
}
