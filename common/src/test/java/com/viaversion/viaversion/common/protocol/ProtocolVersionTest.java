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
package com.viaversion.viaversion.common.protocol;

import com.google.common.collect.Range;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersionRange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ProtocolVersionTest {

    @Test
    void testVersionWildcard() {
        Assertions.assertEquals(ProtocolVersion.v1_8, ProtocolVersion.getClosest("1.8.3"));
        Assertions.assertEquals(ProtocolVersion.v1_8, ProtocolVersion.getClosest("1.8"));
        Assertions.assertEquals(ProtocolVersion.v1_8, ProtocolVersion.getClosest("1.8.x"));
    }

    @Test
    void testVersionRange() {
        Assertions.assertEquals(ProtocolVersion.v1_20, ProtocolVersion.getClosest("1.20"));
        Assertions.assertEquals(ProtocolVersion.v1_20, ProtocolVersion.getClosest("1.20.0"));
        Assertions.assertEquals(ProtocolVersion.v1_20, ProtocolVersion.getClosest("1.20.1"));
        Assertions.assertEquals(ProtocolVersion.v1_7_2, ProtocolVersion.getClosest("1.7.2"));
        Assertions.assertEquals(ProtocolVersion.v1_7_2, ProtocolVersion.getClosest("1.7.5"));
    }

    @Test
    void testGet() {
        Assertions.assertEquals(ProtocolVersion.v1_16_3, ProtocolVersion.getProtocol(753));
    }

    @Test
    void testProtocolVersionRange() {
        Assertions.assertTrue(ProtocolVersionRange.of(Range.atLeast(ProtocolVersion.v1_8)).contains(ProtocolVersion.v1_10));
        Assertions.assertFalse(ProtocolVersionRange.of(Range.atLeast(ProtocolVersion.v1_8)).contains(ProtocolVersion.v1_7_2));
        Assertions.assertTrue(ProtocolVersionRange.of(ProtocolVersion.v1_8, ProtocolVersion.v1_10).contains(ProtocolVersion.v1_9));

        final ProtocolVersionRange complexRange = ProtocolVersionRange.of(Range.atLeast(ProtocolVersion.v1_11)).add(Range.singleton(ProtocolVersion.v1_8)).add(Range.lessThan(ProtocolVersion.v1_7_2));
        Assertions.assertEquals(complexRange.toString(), ProtocolVersionRange.fromString(complexRange.toString()).toString());
    }
}
