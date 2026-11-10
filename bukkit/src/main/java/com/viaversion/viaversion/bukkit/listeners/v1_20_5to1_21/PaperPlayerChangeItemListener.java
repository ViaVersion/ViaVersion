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
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public final class PaperPlayerChangeItemListener extends PlayerChangeItemListener {

    public PaperPlayerChangeItemListener(final ViaVersionPlugin plugin) {
        super(plugin);
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
        } else if (slot == 39) {
            sendAttributeUpdate(player, item, Slot.HELMET);
        }
    }

}
