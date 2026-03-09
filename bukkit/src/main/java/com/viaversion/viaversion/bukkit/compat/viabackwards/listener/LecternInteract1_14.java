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
package com.viaversion.viaversion.bukkit.compat.viabackwards.listener;

import com.viaversion.viabackwards.protocol.v1_14to1_13_2.Protocol1_14To1_13_2;
import com.viaversion.viaversion.bukkit.listeners.ViaBukkitListener;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Lectern;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.Plugin;

public final class LecternInteract1_14 extends ViaBukkitListener {

    public LecternInteract1_14(final Plugin plugin) {
        super(plugin, Protocol1_14To1_13_2.class);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLecternInteract(final PlayerInteractEvent event) {
        final Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.LECTERN) {
            return;
        }

        final Player player = event.getPlayer();
        if (!isOnPipe(player)) {
            return;
        }

        final Lectern lectern = (Lectern) block.getState();
        final ItemStack book = lectern.getInventory().getItem(0);
        if (book == null) {
            return;
        }

        final BookMeta meta = (BookMeta) book.getItemMeta();
        final ItemStack newBook = new ItemStack(Material.WRITTEN_BOOK);
        final BookMeta newBookMeta = (BookMeta) newBook.getItemMeta();
        //noinspection deprecation
        newBookMeta.setPages(meta.getPages());
        newBookMeta.setAuthor("an upsidedown person");
        newBookMeta.setTitle("buk");
        newBook.setItemMeta(newBookMeta);
        player.openBook(newBook);

        event.setCancelled(true);
    }
}
