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

import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.common.PlatformTestBase;
import com.viaversion.viaversion.protocols.v26_2to26_3.Protocol26_2To26_3;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StructuredDataCoverageTest extends PlatformTestBase {

    @Test
    void testAllDataComponentsCovered26_3() {
        final int size = Protocol26_2To26_3.MAPPINGS.getDataComponentSerializerMappings().mappedSize();
        Assertions.assertTrue(size > 0, "No data component mappings loaded");

        for (int id = 0; id < size; id++) {
            Assertions.assertNotNull(VersionedTypes.V26_3.structuredData.key(id), "No data component serializer for id " + id);
        }
    }
}
