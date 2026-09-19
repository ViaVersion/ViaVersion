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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.common.storage;

import com.viaversion.viaversion.api.minecraft.Vector3d;
import com.viaversion.viaversion.protocols.v1_8to1_9.storage.TeleportPositionStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TeleportPositionStorageTest {

    @Test
    void testPollEmpty() {
        // Every position packet without a pending teleport polls the storage; must not throw
        Assertions.assertNull(new TeleportPositionStorage().pollPendingPosition());
    }

    @Test
    void testFifoOrder() {
        // Replies must be paired with their own teleport, in send order
        final TeleportPositionStorage storage = new TeleportPositionStorage();
        storage.addPendingPosition(new Vector3d(1, 2, 3));
        storage.addPendingPosition(new Vector3d(4, 5, 6));

        final Vector3d first = storage.pollPendingPosition();
        Assertions.assertEquals(1, first.x(), "oldest teleport should be polled first");
        Assertions.assertEquals(2, first.y(), "oldest teleport should be polled first");
        Assertions.assertEquals(3, first.z(), "oldest teleport should be polled first");

        final Vector3d second = storage.pollPendingPosition();
        Assertions.assertEquals(4, second.x(), "second teleport should be polled second");
        Assertions.assertEquals(5, second.y(), "second teleport should be polled second");
        Assertions.assertEquals(6, second.z(), "second teleport should be polled second");

        Assertions.assertNull(storage.pollPendingPosition());
    }

    @Test
    void testInterleavedAddAndPoll() {
        // Mounts can queue two teleports before the first reply arrives; later teleports
        // must still be paired correctly with their replies
        final TeleportPositionStorage storage = new TeleportPositionStorage();
        storage.addPendingPosition(new Vector3d(1, 0, 0));
        storage.addPendingPosition(new Vector3d(2, 0, 0));

        Assertions.assertEquals(1, storage.pollPendingPosition().x(), "first reply should get the first teleport");
        storage.addPendingPosition(new Vector3d(3, 0, 0));
        Assertions.assertEquals(2, storage.pollPendingPosition().x(), "second reply should get the second teleport");
        Assertions.assertEquals(3, storage.pollPendingPosition().x(), "third reply should get the third teleport");
        Assertions.assertNull(storage.pollPendingPosition());
    }

    @Test
    void testOldestPositionDroppedAtLimit() {
        // A server spamming teleports without replies must not grow the queue unboundedly
        final TeleportPositionStorage storage = new TeleportPositionStorage();
        final int overLimit = 6;
        for (int i = 0; i < 64 + overLimit; i++) {
            storage.addPendingPosition(new Vector3d(i, 0, 0));
        }

        Assertions.assertEquals(overLimit, storage.pollPendingPosition().x(), "oldest positions should have been dropped");
    }
}
