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
package com.viaversion.viaversion.common.protocol;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;

public class ProtocolVersionTest {

    @Test
    void testVersionWildcard() {
        Assertions.assertEquals(ProtocolVersion.v1_8, ProtocolVersion.getClosest("1.8.3"));
        Assertions.assertEquals(ProtocolVersion.v1_8, ProtocolVersion.getClosest("1.8"));
        Assertions.assertEquals(ProtocolVersion.v1_8, ProtocolVersion.getClosest("1.8.x"));
    }

    @Test
    void testVersionRange() {
        Assertions.assertEquals(ProtocolVersion.v1_7_1, ProtocolVersion.getClosest("1.7"));
        Assertions.assertEquals(ProtocolVersion.v1_7_1, ProtocolVersion.getClosest("1.7.0"));
        Assertions.assertEquals(ProtocolVersion.v1_7_1, ProtocolVersion.getClosest("1.7.1"));
        Assertions.assertEquals(ProtocolVersion.v1_7_1, ProtocolVersion.getClosest("1.7.5"));
    }

    @Test
    void testGet() {
        Assertions.assertEquals(ProtocolVersion.v1_16_3, ProtocolVersion.getProtocol(753));
    }
}
