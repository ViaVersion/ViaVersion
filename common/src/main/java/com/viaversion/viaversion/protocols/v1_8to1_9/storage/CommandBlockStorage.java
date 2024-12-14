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
package com.viaversion.viaversion.protocols.v1_8to1_9.storage;

import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.ChunkPosition;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CommandBlockStorage implements StorableObject {
    private final Map<Long, Map<BlockPosition, CompoundTag>> storedCommandBlocks = new HashMap<>();
    private boolean permissions;

    public void unloadChunk(int x, int z) {
        storedCommandBlocks.remove(ChunkPosition.chunkKey(x, z));
    }

    public void addOrUpdateBlock(BlockPosition position, CompoundTag tag) {
        long chunkKey = ChunkPosition.chunkKeyForBlock(position.x(), position.z());
        Map<BlockPosition, CompoundTag> blocks = storedCommandBlocks.computeIfAbsent(chunkKey, k -> new HashMap<>());
        blocks.put(position, tag);
    }

    public Optional<CompoundTag> getCommandBlock(BlockPosition position) {
        Map<BlockPosition, CompoundTag> blocks = storedCommandBlocks.get(ChunkPosition.chunkKeyForBlock(position.x(), position.z()));
        if (blocks == null) {
            return Optional.empty();
        }

        CompoundTag tag = blocks.get(position);
        if (tag == null) {
            return Optional.empty();
        }

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
