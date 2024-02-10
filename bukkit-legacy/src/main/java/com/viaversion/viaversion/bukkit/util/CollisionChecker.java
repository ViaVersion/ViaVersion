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
package com.viaversion.viaversion.bukkit.util;

import com.viaversion.viaversion.api.Via;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.List;
import java.util.logging.Level;

public class CollisionChecker {
    private static final CollisionChecker INSTANCE;
    static {
        CollisionChecker instance = null;
        try {
            instance = new CollisionChecker();
        } catch (ReflectiveOperationException ex) {
            Via.getPlatform().getLogger().log(
                    Level.WARNING,
                    "Couldn't find reflection methods/fields to calculate bounding boxes.\n" +
                            "Placing non-full blocks where the player stands may fail.", ex);
        }
        INSTANCE = instance;
    }

    private final Method GET_ENTITY_HANDLE;
    private final Method GET_ENTITY_BB;
    private final Method GET_BLOCK_BY_ID;
    private final Method GET_WORLD_HANDLE;
    private final Method GET_BLOCK_TYPE;
    private final Method GET_COLLISIONS;

    private final Method SET_POSITION;
    private final Object BLOCK_POSITION;

    private CollisionChecker() throws ReflectiveOperationException {
        Class<?> blockPosition = NMSUtil.nms("BlockPosition");
        Class<?> mutableBlockPosition = NMSUtil.nms("BlockPosition$MutableBlockPosition");
        Class<?> world = NMSUtil.nms("World");

        GET_ENTITY_HANDLE = NMSUtil.obc("entity.CraftEntity").getDeclaredMethod("getHandle");
        GET_ENTITY_BB = GET_ENTITY_HANDLE.getReturnType().getDeclaredMethod("getBoundingBox");

        GET_WORLD_HANDLE = NMSUtil.obc("CraftWorld").getDeclaredMethod("getHandle");
        GET_BLOCK_TYPE = world.getDeclaredMethod("getType", blockPosition);

        GET_BLOCK_BY_ID = NMSUtil.nms("Block").getDeclaredMethod("getById", int.class);

        GET_COLLISIONS = GET_BLOCK_BY_ID.getReturnType().getDeclaredMethod("a",
                world,
                blockPosition,
                GET_BLOCK_TYPE.getReturnType(),
                GET_ENTITY_BB.getReturnType(),
                List.class,
                GET_ENTITY_HANDLE.getReturnType());

        SET_POSITION = mutableBlockPosition.getDeclaredMethod("c", int.class, int.class, int.class);
        BLOCK_POSITION = mutableBlockPosition.getConstructor().newInstance();
    }

    public static CollisionChecker getInstance() {
        return INSTANCE;
    }

    public Boolean intersects(Block block, Entity entity) {
        try {
            Object nmsPlayer = GET_ENTITY_HANDLE.invoke(entity);

            Object nmsBlock = GET_BLOCK_BY_ID.invoke(null, block.getType().getId());
            Object nmsWorld = GET_WORLD_HANDLE.invoke(block.getWorld());

            SET_POSITION.invoke(BLOCK_POSITION, block.getX(), block.getY(), block.getZ());

            // Dummy list to avoid saving actual collision BB, we only care about if any collision happened
            List<?> collisions = new DummyList<>();

            GET_COLLISIONS.invoke(nmsBlock,
                    nmsWorld,
                    BLOCK_POSITION,
                    GET_BLOCK_TYPE.invoke(nmsWorld, BLOCK_POSITION),
                    GET_ENTITY_BB.invoke(nmsPlayer),
                    collisions,
                    nmsPlayer);
            return !collisions.isEmpty();
        } catch (ReflectiveOperationException ex) {
            return null;
        }
    }

    private static class DummyList<T> extends AbstractList<T> {
        private boolean any;

        @Override
        public T get(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(int idx, T el) {
            any = true;
        }

        @Override
        public int size() {
            return any ? 1 : 0;
        }
    }

}
