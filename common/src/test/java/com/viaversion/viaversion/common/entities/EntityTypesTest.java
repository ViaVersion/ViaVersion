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
package com.viaversion.viaversion.common.entities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_14Types;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_15Types;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_16Types;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_16_2Types;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_17Types;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;

import java.util.function.Function;

/**
 * Test to make sure the array storage approach of entity types works correctly.
 */
public class EntityTypesTest {

    @Test
    void testArrayOrder() {
        testArrayOrder(Entity1_14Types.values(), Entity1_14Types::getTypeFromId);
        testArrayOrder(Entity1_15Types.values(), Entity1_15Types::getTypeFromId);
        testArrayOrder(Entity1_16Types.values(), Entity1_16Types::getTypeFromId);
        testArrayOrder(Entity1_16_2Types.values(), Entity1_16_2Types::getTypeFromId);
        testArrayOrder(Entity1_17Types.values(), Entity1_17Types::getTypeFromId);
    }

    private void testArrayOrder(EntityType[] types, Function<Integer, EntityType> returnFunction) {
        for (EntityType type : types) {
            if (type.getId() != -1) {
                Assertions.assertEquals(type, returnFunction.apply(type.getId()));
            }
        }
    }
}
