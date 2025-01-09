/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
package com.viaversion.viaversion.bukkit.providers;

import com.viaversion.viaversion.ViaVersionPlugin;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.bukkit.platform.PaperViaInjector;
import com.viaversion.viaversion.protocols.v1_21_2to1_21_4.provider.PickItemProvider;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class BukkitPickItemProvider extends PickItemProvider {
    private static final boolean HAS_PLACEMENT_MATERIAL_METHOD = PaperViaInjector.hasMethod("org.bukkit.block.data.BlockData", "getPlacementMaterial");
    private static final boolean HAS_SPAWN_EGG_METHOD = PaperViaInjector.hasMethod(ItemFactory.class, "getSpawnEgg", EntityType.class);
    private static final double BLOCK_RANGE = 4.5 + 1;
    private static final double BLOCK_RANGE_SQUARED = BLOCK_RANGE * BLOCK_RANGE;
    private static final double ENTITY_RANGE = 3 + 3;
    private final ViaVersionPlugin plugin;

    public BukkitPickItemProvider(final ViaVersionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void pickItemFromBlock(final UserConnection connection, final BlockPosition blockPosition, final boolean includeData) {
        final UUID uuid = connection.getProtocolInfo().getUuid();
        final Player player = plugin.getServer().getPlayer(uuid);
        if (player == null) {
            return;
        }

        plugin.runSyncFor(() -> {
            final Location playerLocation = player.getLocation();
            if (blockPosition.distanceFromCenterSquared(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ()) > BLOCK_RANGE_SQUARED) {
                return;
            }

            final Block block = player.getWorld().getBlockAt(blockPosition.x(), blockPosition.y(), blockPosition.z());
            if (block.getType() == Material.AIR) {
                return;
            }

            final ItemStack item = blockToItem(block, includeData && player.getGameMode() == GameMode.CREATIVE);
            if (item != null) {
                pickItem(player, item);
            }
        }, player);
    }

    private @Nullable ItemStack blockToItem(final Block block, final boolean includeData) {
        if (HAS_PLACEMENT_MATERIAL_METHOD) {
            final ItemStack item = new ItemStack(block.getBlockData().getPlacementMaterial(), 1);
            if (includeData && item.getItemMeta() instanceof final BlockStateMeta blockStateMeta) {
                blockStateMeta.setBlockState(block.getState());
                item.setItemMeta(blockStateMeta);
            }
            return item;
        } else if (block.getType().isItem()) {
            return new ItemStack(block.getType(), 1);
        }
        return null;
    }

    @Override
    public void pickItemFromEntity(final UserConnection connection, final int entityId, final boolean includeData) {
        if (!HAS_SPAWN_EGG_METHOD) {
            return;
        }

        final UUID uuid = connection.getProtocolInfo().getUuid();
        final Player player = plugin.getServer().getPlayer(uuid);
        if (player == null) {
            return;
        }

        plugin.runSyncFor(() -> {
            final Entity entity = player.getWorld().getNearbyEntities(player.getLocation(), ENTITY_RANGE, ENTITY_RANGE, ENTITY_RANGE).stream()
                .filter(e -> e.getEntityId() == entityId)
                .findAny()
                .orElse(null);
            if (entity == null) {
                return;
            }

            final Material spawnEggType = Bukkit.getItemFactory().getSpawnEgg(entity.getType());
            if (spawnEggType != null) {
                pickItem(player, new ItemStack(spawnEggType, 1));
            }
        }, player);
    }

    private void pickItem(final Player player, final ItemStack item) {
        // Find matching item
        final PlayerInventory inventory = player.getInventory();
        final ItemStack[] contents = inventory.getStorageContents();
        int sourceSlot = -1;
        for (int i = 0; i < contents.length; i++) {
            final ItemStack content = contents[i];
            if (content == null || !content.isSimilar(item)) {
                continue;
            }

            sourceSlot = i;
            break;
        }

        if (sourceSlot != -1) {
            moveToHotbar(inventory, sourceSlot, contents);
        } else if (player.getGameMode() == GameMode.CREATIVE) {
            spawnItem(item, inventory, contents);
        }
    }

    private void spawnItem(final ItemStack item, final PlayerInventory inventory, final ItemStack[] contents) {
        final int targetSlot = findEmptyHotbarSlot(inventory, inventory.getHeldItemSlot());
        inventory.setHeldItemSlot(targetSlot);
        final ItemStack heldItem = inventory.getItem(targetSlot);
        int emptySlot = targetSlot;
        if (heldItem != null && heldItem.getType() != Material.AIR) {
            // Swap to the first free slot in the inventory, else add it to the current hotbar slot
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] == null || contents[i].getType() == Material.AIR) {
                    emptySlot = i;
                    break;
                }
            }
        }

        inventory.setItem(emptySlot, heldItem);
        inventory.setItemInMainHand(item);
    }

    private void moveToHotbar(final PlayerInventory inventory, final int sourceSlot, final ItemStack[] contents) {
        if (sourceSlot < 9) {
            inventory.setHeldItemSlot(sourceSlot);
            return;
        }

        final int heldSlot = inventory.getHeldItemSlot();
        final int targetSlot = findEmptyHotbarSlot(inventory, heldSlot);
        inventory.setHeldItemSlot(targetSlot);
        final ItemStack heldItem = inventory.getItem(targetSlot);
        inventory.setItemInMainHand(contents[sourceSlot]);
        inventory.setItem(sourceSlot, heldItem);
    }

    private int findEmptyHotbarSlot(final PlayerInventory inventory, final int heldSlot) {
        for (int i = 0; i < 9; i++) {
            final ItemStack item = inventory.getItem(i);
            if (item == null || item.getType() == Material.AIR) {
                return i;
            }
        }
        return heldSlot;
    }
}
