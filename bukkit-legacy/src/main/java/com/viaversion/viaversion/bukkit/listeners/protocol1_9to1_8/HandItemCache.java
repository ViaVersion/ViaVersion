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

import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class HandItemCache extends BukkitRunnable {

    private final Map<UUID, Item> handCache = new ConcurrentHashMap<>();

    @Override
    public void run() {
        List<UUID> players = new ArrayList<>(handCache.keySet());

        for (Player p : Bukkit.getOnlinePlayers()) {
            handCache.put(p.getUniqueId(), convert(p.getItemInHand()));
            players.remove(p.getUniqueId());
        }
        // Remove offline players
        for (UUID uuid : players) {
            handCache.remove(uuid);
        }
    }

    public Item getHandItem(UUID player) {
        return handCache.get(player);
    }

    public static Item convert(ItemStack itemInHand) {
        if (itemInHand == null) return new DataItem(0, (byte) 0, (short) 0, null);
        return new DataItem(itemInHand.getTypeId(), (byte) itemInHand.getAmount(), itemInHand.getDurability(), null);
    }
}
