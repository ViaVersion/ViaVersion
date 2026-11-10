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
package com.viaversion.viaversion.bukkit.listeners.v1_20_5to1_21;

import com.viaversion.viaversion.ViaVersionPlugin;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.storage.EfficiencyAttributeStorage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class LegacyChangeItemListener extends PlayerChangeItemListener {

    public LegacyChangeItemListener(final ViaVersionPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockDamageEvent(final BlockDamageEvent event) {
        final Player player = event.getPlayer();
        final ItemStack item = event.getItemInHand();
        sendAttributeUpdate(player, item, Slot.HAND);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(final InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player &&
            (event.getInventory().getType() == InventoryType.CRAFTING ||
                event.getInventory().getType() == InventoryType.PLAYER)) {
            sendArmorUpdate(player);
        }
    }

    private void sendArmorUpdate(final Player player) {
        final UserConnection connection = getUserConnection(player);
        final EfficiencyAttributeStorage storage = getEfficiencyStorage(connection);
        if (storage == null) {
            return;
        }

        final PlayerInventory inventory = player.getInventory();
        final ItemStack helmet = inventory.getHelmet();
        final ItemStack leggings = swiftSneak != null ? inventory.getLeggings() : null;
        final ItemStack boots = depthStrider != null ? inventory.getBoots() : null;

        storage.setEnchants(player.getEntityId(), connection, storage.activeEnchants()
            .aquaAffinity(helmet != null ? helmet.getEnchantmentLevel(aquaAffinity) : 0)
            .swiftSneak(leggings != null ? leggings.getEnchantmentLevel(swiftSneak) : 0)
            .depthStrider(boots != null ? boots.getEnchantmentLevel(depthStrider) : 0));
    }

}
