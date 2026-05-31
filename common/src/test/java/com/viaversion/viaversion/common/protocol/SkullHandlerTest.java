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
package com.viaversion.viaversion.common.protocol;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.provider.blockentities.SkullHandler;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.storage.BlockStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SkullHandlerTest {

    private static final BlockPosition POSITION = new BlockPosition(1, 2, 3);

    @Test
    void testStandingPlayerHeadRotationWraps() {
        Assertions.assertEquals(5515, transform(5451, 3, 20));
    }

    @Test
    void testWallPlayerHeadIgnoresRotation() {
        Assertions.assertEquals(5507, transform(5447, 3, 20));
    }

    private int transform(final int blockState, final int skullType, final int rotation) {
        final UserConnectionImpl connection = new UserConnectionImpl(null);
        final BlockStorage storage = new BlockStorage();
        storage.store(POSITION, blockState);
        connection.put(storage);

        final CompoundTag tag = new CompoundTag();
        tag.putInt("x", POSITION.x());
        tag.putInt("y", POSITION.y());
        tag.putInt("z", POSITION.z());
        tag.putInt("SkullType", skullType);
        tag.putInt("Rot", rotation);
        return new SkullHandler().transform(connection, tag);
    }
}
