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
package com.viaversion.viaversion.protocols.protocol1_18to1_17_1.data;

import com.viaversion.viaversion.api.Via;
import java.util.Arrays;

public final class BlockEntityIds {

    private static final int[] IDS = new int[14];

    static {
        // Only fills the ids that actually have block entity update packets sent
        Arrays.fill(IDS, -1);
        IDS[1] = 8; // Spawner
        IDS[2] = 21; // Command block
        IDS[3] = 13; // Beacon
        IDS[4] = 14; // Skull
        IDS[5] = 24; // Conduit
        IDS[6] = 18; // Banner
        IDS[7] = 19; // Structure block
        IDS[8] = 20; // End gateway
        IDS[9] = 7; // Sign
        IDS[10] = 22; // Shulker box
        IDS[11] = 23; // Bed
        IDS[12] = 30; // Jigsaw
        IDS[13] = 31; // Campfire
    }

    public static int newId(final int id) {
        final int newId;
        if (id < 0 || id > IDS.length || (newId = IDS[id]) == -1) {
            Via.getPlatform().getLogger().warning("Received out of bounds block entity id: " + id);
            return -1;
        }
        return newId;
    }

    public static int[] getIds() {
        return IDS;
    }
}
