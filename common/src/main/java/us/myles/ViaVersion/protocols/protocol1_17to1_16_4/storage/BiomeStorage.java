/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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
package us.myles.ViaVersion.protocols.protocol1_17to1_16_4.storage;

import org.jetbrains.annotations.Nullable;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;

import java.util.HashMap;
import java.util.Map;

public class BiomeStorage extends StoredObject {

    private final Map<Long, int[]> chunkBiomes = new HashMap<>();
    private String world;

    public BiomeStorage(UserConnection user) {
        super(user);
    }

    @Nullable
    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    @Nullable
    public int[] getBiomes(int x, int z) {
        return chunkBiomes.get(getChunkSectionIndex(x, z));
    }

    public void setBiomes(int x, int z, int[] biomes) {
        chunkBiomes.put(getChunkSectionIndex(x, z), biomes);
    }

    public void clearBiomes(int x, int z) {
        chunkBiomes.remove(getChunkSectionIndex(x, z));
    }

    public void clearBiomes() {
        chunkBiomes.clear();
    }

    private long getChunkSectionIndex(int x, int z) {
        return ((x & 0x3FFFFFFL) << 38) | (z & 0x3FFFFFFL);
    }
}
