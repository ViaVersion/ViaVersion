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
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.blockconnections;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockFace;
import com.viaversion.viaversion.api.minecraft.Position;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.HashSet;
import java.util.Set;

public class SnowyGrassConnectionHandler extends ConnectionHandler {
    private static final Object2IntMap<GrassBlock> GRASS_BLOCKS = new Object2IntOpenHashMap<>();
    private static final IntSet SNOWY_GRASS_BLOCKS = new IntOpenHashSet();

    static ConnectionData.ConnectorInitAction init() {
        final Set<String> snowyGrassBlocks = new HashSet<>();
        snowyGrassBlocks.add("minecraft:grass_block");
        snowyGrassBlocks.add("minecraft:podzol");
        snowyGrassBlocks.add("minecraft:mycelium");

        GRASS_BLOCKS.defaultReturnValue(-1);
        final SnowyGrassConnectionHandler handler = new SnowyGrassConnectionHandler();
        return blockData -> {
            if (snowyGrassBlocks.contains(blockData.getMinecraftKey())) {
                ConnectionData.connectionHandlerMap.put(blockData.getSavedBlockStateId(), handler);
                blockData.set("snowy", "true");
                GRASS_BLOCKS.put(new GrassBlock(blockData.getSavedBlockStateId(), true), blockData.getBlockStateId());
                blockData.set("snowy", "false");
                GRASS_BLOCKS.put(new GrassBlock(blockData.getSavedBlockStateId(), false), blockData.getBlockStateId());
            }
            if (blockData.getMinecraftKey().equals("minecraft:snow") || blockData.getMinecraftKey().equals("minecraft:snow_block")) {
                ConnectionData.connectionHandlerMap.put(blockData.getSavedBlockStateId(), handler);
                SnowyGrassConnectionHandler.SNOWY_GRASS_BLOCKS.add(blockData.getSavedBlockStateId());
            }
        };
    }

    @Override
    public int connect(UserConnection user, Position position, int blockState) {
        int blockUpId = getBlockData(user, position.getRelative(BlockFace.TOP));
        int newId = GRASS_BLOCKS.getInt(new GrassBlock(blockState, SNOWY_GRASS_BLOCKS.contains(blockUpId)));
        return newId != -1 ? newId : blockState;
    }

    private static final class GrassBlock {
        private final int blockStateId;
        private final boolean snowy;

        private GrassBlock(final int blockStateId, final boolean snowy) {
            this.blockStateId = blockStateId;
            this.snowy = snowy;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final GrassBlock that = (GrassBlock) o;
            if (blockStateId != that.blockStateId) return false;
            return snowy == that.snowy;
        }

        @Override
        public int hashCode() {
            int result = blockStateId;
            result = 31 * result + (snowy ? 1 : 0);
            return result;
        }
    }
}