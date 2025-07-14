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
package com.viaversion.viaversion.bukkit.util;

import com.viaversion.viaversion.api.Via;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;

public class LegacyBlockToItem {
    private static final LegacyBlockToItem INSTANCE;
    private static final Random RANDOM = new Random();

    static {
        LegacyBlockToItem instance = null;
        try {
            instance = new LegacyBlockToItem();
        } catch (ReflectiveOperationException ex) {
            Via.getPlatform().getLogger().log(
                Level.WARNING,
                "Couldn't find reflection methods/fields for pick-block functionality.\n" +
                    "Pick-block won't work on clients 1.21.4 and newer.", ex);
        }
        INSTANCE = instance;
    }

    private final Method GET_WORLD_HANDLE;
    private final Method GET_BLOCK_TYPE;
    private final Method GET_BLOCK;
    private final Method GET_ITEM_FOR_BLOCK;
    private final Method GET_DROP_TYPE;
    private final Method TO_BUKKIT_ITEM_STACK;
    private final Method NEW_BUKKIT_ITEM_STACK;

    private final Method SET_POSITION;
    private final Object BLOCK_POSITION;

    public LegacyBlockToItem() throws ReflectiveOperationException {
        Class<?> blockPosition = NMSUtil.nms("BlockPosition");
        Class<?> mutableBlockPosition = NMSUtil.nms("BlockPosition$MutableBlockPosition");
        Class<?> world = NMSUtil.nms("World");
        Class<?> block = NMSUtil.nms("Block");
        Class<?> iBlockData = NMSUtil.nms("IBlockData");
        Class<?> nmsItem = NMSUtil.nms("Item");
        Class<?> nmsItemStack = NMSUtil.nms("ItemStack");
        Class<?> craftItemStack = NMSUtil.obc("inventory.CraftItemStack");

        GET_WORLD_HANDLE = NMSUtil.obc("CraftWorld").getDeclaredMethod("getHandle");
        GET_BLOCK_TYPE = world.getDeclaredMethod("getType", blockPosition);
        GET_BLOCK = iBlockData.getDeclaredMethod("getBlock");
        GET_ITEM_FOR_BLOCK = block.getDeclaredMethod("i", iBlockData);
        GET_ITEM_FOR_BLOCK.setAccessible(true);

        GET_DROP_TYPE = block.getDeclaredMethod("getDropType", iBlockData, Random.class, int.class);

        TO_BUKKIT_ITEM_STACK = craftItemStack.getDeclaredMethod("asCraftMirror", nmsItemStack);
        NEW_BUKKIT_ITEM_STACK = craftItemStack.getDeclaredMethod("asNewCraftStack", nmsItem);

        SET_POSITION = mutableBlockPosition.getDeclaredMethod("c", int.class, int.class, int.class);
        BLOCK_POSITION = mutableBlockPosition.getConstructor().newInstance();
    }

    public static LegacyBlockToItem getInstance() {
        return INSTANCE;
    }

    public @Nullable ItemStack blockToItem(final Block block) {
        try {
            // World nmsWorld = block.getWorld().getHandle();
            Object nmsWorld = GET_WORLD_HANDLE.invoke(block.getWorld());

            // IBlockData blockData = nmsWorld.get(block.position());
            SET_POSITION.invoke(BLOCK_POSITION, block.getX(), block.getY(), block.getZ());
            Object blockData = GET_BLOCK_TYPE.invoke(nmsWorld, BLOCK_POSITION);

            // Block nmsBlock = blockData.getBlock();
            Object nmsBlock = GET_BLOCK.invoke(blockData);
            // ItemStack is = nmsBlock.getItemForBlock(blockData);
            Object nmsItemStack = GET_ITEM_FOR_BLOCK.invoke(nmsBlock, blockData);

            // CraftItemStack.asCraftMirror(nmsItemStack)
            ItemStack stack = (ItemStack) TO_BUKKIT_ITEM_STACK.invoke(null, nmsItemStack);
            if (stack.getType() != Material.AIR) return stack;

            // Couldn't find an item for the block (eg: redstone dust, sugarcane, doors, etc.), use the drop instead
            Object nmsItem = GET_DROP_TYPE.invoke(nmsBlock, blockData, RANDOM, 0);
            return (ItemStack) NEW_BUKKIT_ITEM_STACK.invoke(null, nmsItem);
        } catch (ReflectiveOperationException ex) {
            Via.getPlatform().getLogger().log(
                Level.WARNING, "Failed when trying to find the right item for pick-block.", ex);
            return null;
        }
    }

}
