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
package com.viaversion.viaversion.bukkit.listeners.protocol1_9to1_8;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.bukkit.listeners.ViaBukkitListener;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ArmorType;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import java.util.UUID;
import java.util.logging.Level;
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

    private static final UUID ARMOR_ATTRIBUTE = UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150");

    public ArmorListener(Plugin plugin) {
        super(plugin, Protocol1_9To1_8.class);
    }

    public void sendArmorUpdate(Player player) {
        // Ensure that the player is on our pipe
        if (!isOnPipe(player)) return;

        int armor = 0;
        for (ItemStack stack : player.getInventory().getArmorContents()) {
            armor += ArmorType.findById(stack.getTypeId()).getArmorPoints();
        }

        PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_9.ENTITY_PROPERTIES, null, getUserConnection(player));
        try {
            wrapper.write(Type.VAR_INT, player.getEntityId()); // Player ID
            wrapper.write(Type.INT, 1); // only 1 property
            wrapper.write(Type.STRING, "generic.armor");
            wrapper.write(Type.DOUBLE, 0D); //default 0 armor
            wrapper.write(Type.VAR_INT, 1); // 1 modifier
            wrapper.write(Type.UUID, ARMOR_ATTRIBUTE); // armor modifier uuid
            wrapper.write(Type.DOUBLE, (double) armor); // the modifier value
            wrapper.write(Type.BYTE, (byte) 0);// the modifier operation, 0 is add number

            wrapper.scheduleSend(Protocol1_9To1_8.class);
        } catch (Exception e) {
            Via.getPlatform().getLogger().log(Level.WARNING, "Failed to send armor update", e);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        HumanEntity human = e.getWhoClicked();
        if (human instanceof Player && e.getInventory() instanceof CraftingInventory) {
            final Player player = (Player) human;
            if (e.getCurrentItem() != null) {
                if (ArmorType.isArmor(e.getCurrentItem().getTypeId())) {
                    sendDelayedArmorUpdate(player);
                    return;
                }
            }
            if (e.getRawSlot() >= 5 && e.getRawSlot() <= 8) {
                sendDelayedArmorUpdate(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getItem() != null) {
            if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                final Player player = e.getPlayer();
                // Due to odd bugs it's 3 ticks later
                Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> sendArmorUpdate(player), 3L);
            }
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