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
package com.viaversion.viaversion.protocols.v1_21_9to1_21_11;

import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocol.packet.PacketWrapperImpl;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class Protocol1_21_9To1_21_11Test {

    @Test
    void appendsCompleteTimelineTagBindingsWithoutReplacingExistingRegistries() {
        final PacketWrapper wrapper = new PacketWrapperImpl(0, null, null);
        wrapper.write(Types.VAR_INT, 2);
        wrapper.write(Types.STRING, "minecraft:item");
        wrapper.write(Types.VAR_INT, 0);
        wrapper.write(Types.STRING, "minecraft:block");
        wrapper.write(Types.VAR_INT, 0);

        Protocol1_21_9To1_21_11.appendTimelineTags(wrapper, Map.of(
            "day", 5,
            "early_game", 9,
            "moon", 7,
            "villager_schedule", 3
        ));

        assertEquals(3, wrapper.get(Types.VAR_INT, 0));
        assertEquals("minecraft:item", wrapper.get(Types.STRING, 0));
        assertEquals("minecraft:block", wrapper.get(Types.STRING, 1));
        assertEquals("minecraft:timeline", wrapper.get(Types.STRING, 2));
        assertEquals(4, wrapper.get(Types.VAR_INT, 3));
        assertEquals("minecraft:universal", wrapper.get(Types.STRING, 3));
        assertArrayEquals(new int[]{3}, wrapper.get(Types.VAR_INT_ARRAY_PRIMITIVE, 0));
        assertEquals("minecraft:in_overworld", wrapper.get(Types.STRING, 4));
        assertArrayEquals(new int[]{3, 5, 7, 9}, wrapper.get(Types.VAR_INT_ARRAY_PRIMITIVE, 1));
        assertEquals("minecraft:in_nether", wrapper.get(Types.STRING, 5));
        assertArrayEquals(new int[]{3}, wrapper.get(Types.VAR_INT_ARRAY_PRIMITIVE, 2));
        assertEquals("minecraft:in_end", wrapper.get(Types.STRING, 6));
        assertArrayEquals(new int[]{3}, wrapper.get(Types.VAR_INT_ARRAY_PRIMITIVE, 3));
    }
}
