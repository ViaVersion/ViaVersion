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
package com.viaversion.viaversion.bukkit.listeners.v26_2to26_3;

import com.viaversion.viaversion.ViaVersionPlugin;
import com.viaversion.viaversion.bukkit.listeners.ViaBukkitListener;
import com.viaversion.viaversion.protocols.v26_2to26_3.Protocol26_2To26_3;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerSwingAnimationListener extends ViaBukkitListener {

    public PlayerSwingAnimationListener(final ViaVersionPlugin plugin) {
        super(plugin, Protocol26_2To26_3.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDropItem(final PlayerDropItemEvent event) {
        final Player player = event.getPlayer();
        if (!isOnPipe(player)) return;

        player.swingMainHand();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(final BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        if (!isOnPipe(player)) return;

        if (event.getHand() == EquipmentSlot.HAND) {
            player.swingMainHand();
        } else if (event.getHand() == EquipmentSlot.OFF_HAND) {
            player.swingOffHand();
        }
    }
}
