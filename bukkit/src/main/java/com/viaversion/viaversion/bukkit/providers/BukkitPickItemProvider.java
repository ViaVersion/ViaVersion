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
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.bukkit.platform.PaperViaInjector;
import com.viaversion.viaversion.bukkit.util.LegacyBlockToItem;
import com.viaversion.viaversion.protocols.v1_21_2to1_21_4.provider.PickItemProvider;
import java.lang.reflect.Method;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;

public class BukkitPickItemProvider extends PickItemProvider {
    private static final BlockToItem BLOCK_TO_ITEM = BlockToItem.build();
    private static final GetStorageContents GET_STORAGE_CONTENT = GetStorageContents.build();
    private static final SetInHand SET_IN_HAND = SetInHand.build();

    private static final boolean HAS_SPAWN_EGG_METHOD = PaperViaInjector.hasMethod(ItemFactory.class, Material.class, "getSpawnEgg", EntityType.class);
    private static final Method GET_SPAWN_EGG_ITEMSTACK_METHOD;

    private static final double BLOCK_RANGE = 4.5 + 1;
    private static final double BLOCK_RANGE_SQUARED = BLOCK_RANGE * BLOCK_RANGE;
    private static final double ENTITY_RANGE = 3 + 3;
    private final ViaVersionPlugin plugin;

    static {
        if (PaperViaInjector.hasMethod(ItemFactory.class, ItemStack.class, "getSpawnEgg", EntityType.class)) {
            try {
                GET_SPAWN_EGG_ITEMSTACK_METHOD = ItemFactory.class.getDeclaredMethod("getSpawnEgg", EntityType.class);
            } catch (final ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        } else {
            GET_SPAWN_EGG_ITEMSTACK_METHOD = null;
        }
    }

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

            final ItemStack item = BLOCK_TO_ITEM.apply(block, includeData && player.getGameMode() == GameMode.CREATIVE);
            if (item != null && item.getType() != Material.AIR) {
                pickItem(player, item);
            }
        }, player);
    }

    @Override
    public void pickItemFromEntity(final UserConnection connection, final int entityId, final boolean includeData) {
        if (!HAS_SPAWN_EGG_METHOD && GET_SPAWN_EGG_ITEMSTACK_METHOD == null) {
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

            if (GET_SPAWN_EGG_ITEMSTACK_METHOD == null) {
                final Material spawnEggType = Bukkit.getItemFactory().getSpawnEgg(entity.getType());
                if (spawnEggType != null) {
                    pickItem(player, new ItemStack(spawnEggType, 1));
                }
                return;
            }

            try {
                final ItemStack spawnEggItem = (ItemStack) GET_SPAWN_EGG_ITEMSTACK_METHOD.invoke(Bukkit.getItemFactory(), entity.getType());
                if (spawnEggItem != null) {
                    pickItem(player, spawnEggItem);
                }
            } catch (final ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }, player);
    }

    private void pickItem(final Player player, final ItemStack item) {
        // Find matching item
        final PlayerInventory inventory = player.getInventory();
        // Prior to getStorageContents, getContents worked the same (it ignored armor)
        final ItemStack[] contents = GET_STORAGE_CONTENT.apply(inventory);
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
        SET_IN_HAND.apply(inventory, item);
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
        SET_IN_HAND.apply(inventory, contents[sourceSlot]);
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

    @FunctionalInterface
    private interface BlockToItem {
        ItemStack apply(final Block block, final boolean includeData);

        static BlockToItem build() {
            if (PaperViaInjector.hasMethod("org.bukkit.block.data.BlockData", "getPlacementMaterial")) {
                return (block, includeData) -> {
                    final ItemStack item = new ItemStack(block.getBlockData().getPlacementMaterial(), 1);
                    if (includeData && item.getItemMeta() instanceof final BlockStateMeta blockStateMeta) {
                        blockStateMeta.setBlockState(block.getState());
                        item.setItemMeta(blockStateMeta);
                    }
                    return item;
                };
            }

            final ProtocolVersion version = Via.getAPI().getServerVersion().lowestSupportedProtocolVersion();
            if (version.equalTo(ProtocolVersion.v1_8)) {
                final LegacyBlockToItem legacy = LegacyBlockToItem.getInstance();
                return legacy != null ? (block, includeData) -> legacy.blockToItem(block) : (b, i) -> null;
            } else if (PaperViaInjector.hasMethod(Material.class, "isItem")) {
                return (block, includeData) -> {
                    if (!block.getType().isItem()) {
                        return null;
                    }
                    return version.newerThanOrEqualTo(ProtocolVersion.v1_13)
                        ? new ItemStack(block.getType())
                        : new ItemStack(block.getType(), 1, (short) 0, block.getData());
                };
            } else {
                return (block, includeData) -> null;
            }
        }
    }

    @FunctionalInterface
    private interface GetStorageContents {
        ItemStack[] apply(final PlayerInventory inv);

        static GetStorageContents build() {
            if (PaperViaInjector.hasMethod(Inventory.class, "getStorageContents")) {
                return Inventory::getStorageContents;
            } else {
                return Inventory::getContents;
            }
        }

    }

    @FunctionalInterface
    private interface SetInHand {
        void apply(final PlayerInventory inv, final ItemStack stack);

        @SuppressWarnings("deprecation")
        static SetInHand build() {
            if (PaperViaInjector.hasMethod(PlayerInventory.class, "setItemInMainHand")) {
                return PlayerInventory::setItemInMainHand;
            } else {
                return PlayerInventory::setItemInHand;
            }
        }
    }
}
