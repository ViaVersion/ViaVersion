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
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.bukkit.listeners.ViaBukkitListener;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.Protocol1_20_5To1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.storage.EfficiencyAttributeStorage;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * For some reason, mining efficiency is not calculated by the client anymore, but by the server,
 * then sending the current value to the client every time the item changes. This roughly emulates that behavior.
 */
public class PlayerChangeItemListener extends ViaBukkitListener {

    // Use legacy function and names here to support all versions
    protected final Enchantment efficiency = getByName("efficiency", "DIG_SPEED");
    protected final Enchantment aquaAffinity = getByName("aqua_affinity", "WATER_WORKER");
    protected final Enchantment depthStrider = getByName("depth_strider", "DEPTH_STRIDER");
    protected final Enchantment soulSpeed = getByName("soul_speed", "SOUL_SPEED");
    protected final Enchantment swiftSneak = getByName("swift_sneak", "SWIFT_SNEAK");

    public PlayerChangeItemListener(final ViaVersionPlugin plugin) {
        super(plugin, Protocol1_20_5To1_21.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerItemHeld(final PlayerItemHeldEvent event) {
        final Player player = event.getPlayer();
        final ItemStack item = player.getInventory().getItem(event.getNewSlot());
        sendAttributeUpdate(player, item, Slot.HAND);
    }

    protected EfficiencyAttributeStorage getEfficiencyStorage(final UserConnection connection) {
        return isOnPipe(connection) ? connection.get(EfficiencyAttributeStorage.class) : null;
    }

    void sendAttributeUpdate(final Player player, @Nullable final ItemStack item, final Slot slot) {
        final UserConnection connection = getUserConnection(player);
        final EfficiencyAttributeStorage storage = getEfficiencyStorage(connection);
        if (storage == null) return;

        var enchants = storage.activeEnchants();
        enchants = switch (slot) {
            case HAND -> enchants.efficiency(item != null ? item.getEnchantmentLevel(efficiency) : 0);
            case HELMET -> enchants.aquaAffinity(item != null ? item.getEnchantmentLevel(aquaAffinity) : 0);
            case LEGGINGS -> enchants.swiftSneak(item != null && swiftSneak != null ? item.getEnchantmentLevel(swiftSneak) : 0);
            case BOOTS -> enchants.depthStrider(item != null && depthStrider != null ? item.getEnchantmentLevel(depthStrider) : 0);
            // TODO This needs continuous ticking for the supporting block as a conditional effect
            //  and is even more prone to desync from high ping than the other attributes
            //soulSpeedLevel = item != null && soulSpeed != null ? item.getEnchantmentLevel(soulSpeed) : 0;
        };
        storage.setEnchants(player.getEntityId(), connection, enchants);
    }

    enum Slot {
        HAND, BOOTS, LEGGINGS, HELMET
    }

    private Enchantment getByName(final String newName, final String oldName) {
        final Enchantment enchantment = Enchantment.getByName(newName);
        if (enchantment == null) {
            return Enchantment.getByName(oldName);
        }
        return enchantment;
    }
}
