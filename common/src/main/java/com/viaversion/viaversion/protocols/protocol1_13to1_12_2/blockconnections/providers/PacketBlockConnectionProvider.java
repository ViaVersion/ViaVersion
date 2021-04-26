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
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.blockconnections.providers;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.storage.BlockConnectionStorage;

public class PacketBlockConnectionProvider extends BlockConnectionProvider {

    @Override
    public void storeBlock(UserConnection connection, int x, int y, int z, int blockState) {
        connection.get(BlockConnectionStorage.class).store(x, y, z, blockState);
    }

    @Override
    public void removeBlock(UserConnection connection, int x, int y, int z) {
        connection.get(BlockConnectionStorage.class).remove(x, y, z);
    }

    @Override
    public int getBlockData(UserConnection connection, int x, int y, int z) {
        return connection.get(BlockConnectionStorage.class).get(x, y, z);
    }

    @Override
    public void clearStorage(UserConnection connection) {
        connection.get(BlockConnectionStorage.class).clear();
    }

    @Override
    public void unloadChunk(UserConnection connection, int x, int z) {
        connection.get(BlockConnectionStorage.class).unloadChunk(x, z);
    }

    @Override
    public boolean storesBlocks() {
        return true;
    }
}
