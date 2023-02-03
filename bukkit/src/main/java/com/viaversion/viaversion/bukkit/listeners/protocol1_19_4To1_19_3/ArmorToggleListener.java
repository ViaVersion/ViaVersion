/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2023 ViaVersion and contributors
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
package com.viaversion.viaversion.bukkit.listeners.protocol1_19_4To1_19_3;

import com.viaversion.viaversion.ViaVersionPlugin;
import com.viaversion.viaversion.bukkit.listeners.ViaBukkitListener;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.Protocol1_19_4To1_19_3;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class ArmorToggleListener extends ViaBukkitListener {

    public ArmorToggleListener(ViaVersionPlugin plugin) {
        super(plugin, Protocol1_19_4To1_19_3.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void itemUse(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        if (!isOnPipe(player)) return;

        final ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        final EquipmentSlot equipmentSlot = item.getType().getEquipmentSlot();
        // Name comparison for OFF_HAND so 1.8 doesn't complain
        if (equipmentSlot != EquipmentSlot.HAND && !equipmentSlot.name().equals("OFF_HAND")) {
            final ItemStack armor = player.getInventory().getItem(equipmentSlot);
            // If two pieces of armor are equal, the client will do nothing.
            if (armor != null && armor.getType() != Material.AIR && !armor.equals(item)) {
                player.updateInventory();
            }
        }
    }
}
