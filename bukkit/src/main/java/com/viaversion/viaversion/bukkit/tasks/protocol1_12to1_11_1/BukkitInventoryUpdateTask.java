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
package com.viaversion.viaversion.bukkit.tasks.protocol1_12to1_11_1;

import com.viaversion.viaversion.bukkit.providers.BukkitInventoryQuickMoveProvider;
import com.viaversion.viaversion.protocols.protocol1_12to1_11_1.storage.ItemTransaction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BukkitInventoryUpdateTask implements Runnable {

    private final BukkitInventoryQuickMoveProvider provider;
    private final UUID uuid;
    private final List<ItemTransaction> items;

    public BukkitInventoryUpdateTask(BukkitInventoryQuickMoveProvider provider, UUID uuid) {
        this.provider = provider;
        this.uuid = uuid;
        this.items = Collections.synchronizedList(new ArrayList<>());
    }

    public void addItem(short windowId, short slotId, short actionId) {
        ItemTransaction storage = new ItemTransaction(windowId, slotId, actionId);
        items.add(storage);
    }

    @Override
    public void run() {
        Player p = Bukkit.getServer().getPlayer(uuid);
        if (p == null) {
            provider.onTaskExecuted(uuid);
            return;
        }
        try {
            synchronized (items) {
                for (ItemTransaction storage : items) {
                    Object packet = provider.buildWindowClickPacket(p, storage);
                    boolean result = provider.sendPacketToServer(p, packet);
                    if (!result) {
                        break;
                    }
                }
                items.clear();
            }
        } finally {
            provider.onTaskExecuted(uuid);
        }
    }
}