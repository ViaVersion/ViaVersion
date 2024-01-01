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
package com.viaversion.viaversion.protocols.protocol1_9to1_8.storage;

import com.github.steveice10.opennbt.tag.builtin.ByteTag;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.util.Pair;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CommandBlockStorage implements StorableObject {
    private final Map<Pair<Integer, Integer>, Map<Position, CompoundTag>> storedCommandBlocks = new ConcurrentHashMap<>();
    private boolean permissions;

    public void unloadChunk(int x, int z) {
        Pair<Integer, Integer> chunkPos = new Pair<>(x, z);
        storedCommandBlocks.remove(chunkPos);
    }

    public void addOrUpdateBlock(Position position, CompoundTag tag) {
        Pair<Integer, Integer> chunkPos = getChunkCoords(position);

        if (!storedCommandBlocks.containsKey(chunkPos)) {
            storedCommandBlocks.put(chunkPos, new ConcurrentHashMap<>());
        }

        Map<Position, CompoundTag> blocks = storedCommandBlocks.get(chunkPos);

        if (blocks.containsKey(position) && blocks.get(position).equals(tag)) {
            return;
        }

        blocks.put(position, tag);
    }

    private Pair<Integer, Integer> getChunkCoords(Position position) {
        int chunkX = Math.floorDiv(position.x(), 16);
        int chunkZ = Math.floorDiv(position.z(), 16);

        return new Pair<>(chunkX, chunkZ);
    }

    public Optional<CompoundTag> getCommandBlock(Position position) {
        Pair<Integer, Integer> chunkCoords = getChunkCoords(position);

        Map<Position, CompoundTag> blocks = storedCommandBlocks.get(chunkCoords);
        if (blocks == null)
            return Optional.empty();

        CompoundTag tag = blocks.get(position);
        if (tag == null)
            return Optional.empty();

        tag = tag.copy();
        tag.put("powered", new ByteTag((byte) 0));
        tag.put("auto", new ByteTag((byte) 0));
        tag.put("conditionMet", new ByteTag((byte) 0));
        return Optional.of(tag);
    }

    public void unloadChunks() {
        storedCommandBlocks.clear();
    }

    public boolean isPermissions() {
        return permissions;
    }

    public void setPermissions(boolean permissions) {
        this.permissions = permissions;
    }
}
