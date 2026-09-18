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
package com.viaversion.viaversion.bukkit.listeners.v1_8to1_9;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.bukkit.listeners.ViaBukkitListener;
import com.viaversion.viaversion.protocols.v1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.data.ArmorTypes1_8;
import com.viaversion.viaversion.protocols.v1_8to1_9.storage.ArmorTracker;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class ArmorListener extends ViaBukkitListener {

    public ArmorListener(Plugin plugin) {
        super(plugin, Protocol1_8To1_9.class);
    }

    public void sendArmorUpdate(Player player) {
        // Ensure that the player is on our pipe
        if (!player.isOnline() || !isOnPipe(player)) return;

        UserConnection connection = getUserConnection(player);
        ItemStack[] contents = player.getInventory().getArmorContents();
        int[] armor = new int[4];
        for (int slot = 0; slot < armor.length; slot++) {
            ItemStack stack = contents[3 - slot];
            armor[slot] = stack == null ? 0 : stack.getTypeId();
        }

        // Bukkit reads stay on the server thread; packet tracking stays on the connection's event loop.
        connection.getChannel().eventLoop().execute(() -> {
            ArmorTracker tracker = connection.get(ArmorTracker.class);
            if (!connection.getChannel().isActive() || tracker == null) return;
            for (int slot = 0; slot < armor.length; slot++) {
                tracker.setArmor(slot, armor[slot]);
            }
            tracker.sendArmorUpdate(connection);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        HumanEntity human = e.getWhoClicked();
        if (human instanceof final Player player && e.getInventory() instanceof CraftingInventory) {
            if ((e.getCurrentItem() != null && ArmorTypes1_8.isArmor(e.getCurrentItem().getTypeId()))
                || (e.getRawSlot() >= 5 && e.getRawSlot() <= 8)) {
                sendDelayedArmorUpdate(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getItem() == null) {
            return;
        }
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            final Player player = e.getPlayer();
            // Due to odd bugs it's 3 ticks later
            Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> sendArmorUpdate(player), 3L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemBreak(PlayerItemBreakEvent e) {
        sendDelayedArmorUpdate(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        sendDelayedArmorUpdate(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRespawn(PlayerRespawnEvent e) {
        sendDelayedArmorUpdate(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        sendArmorUpdate(e.getPlayer());
    }

    public void sendDelayedArmorUpdate(final Player player) {
        if (!isOnPipe(player)) return; // Don't start a task if the player is not on the pipe
        Via.getPlatform().runSync(() -> sendArmorUpdate(player));
    }
}
