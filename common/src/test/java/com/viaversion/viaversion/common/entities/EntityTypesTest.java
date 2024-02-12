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
package com.viaversion.viaversion.common.entities;

import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_14;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_15;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_16;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_16_2;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_17;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test to make sure the array storage approach of entity types works correctly.
 */
public class EntityTypesTest {

    @Test
    void testArrayOrder() {
        testArrayOrder(EntityTypes1_14.values(), EntityTypes1_14::getTypeFromId);
        testArrayOrder(EntityTypes1_15.values(), EntityTypes1_15::getTypeFromId);
        testArrayOrder(EntityTypes1_16.values(), EntityTypes1_16::getTypeFromId);
        testArrayOrder(EntityTypes1_16_2.values(), EntityTypes1_16_2::getTypeFromId);
        testArrayOrder(EntityTypes1_17.values(), EntityTypes1_17::getTypeFromId);
        // Newer type enums are automatically filled using mappings
    }

    private void testArrayOrder(EntityType[] types, Function<Integer, EntityType> returnFunction) {
        for (EntityType type : types) {
            if (type.getId() != -1) {
                Assertions.assertEquals(type, returnFunction.apply(type.getId()));
            }
        }
    }
}
