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
package com.viaversion.viaversion.common.entity;

import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes26_2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EntityTypeParentTest {

    @Test
    void knownFamilyRelations() {
        Assertions.assertTrue(EntityTypes26_2.ZOMBIE.isOrHasParent(EntityTypes26_2.ZOMBIE));
        Assertions.assertTrue(EntityTypes26_2.ZOMBIE.isOrHasParent(EntityTypes26_2.ABSTRACT_MONSTER));
        Assertions.assertTrue(EntityTypes26_2.ZOMBIE.isOrHasParent(EntityTypes26_2.ENTITY));
        Assertions.assertTrue(EntityTypes26_2.TRADER_LLAMA.isOrHasParent(EntityTypes26_2.LLAMA));
        Assertions.assertTrue(EntityTypes26_2.TRADER_LLAMA.isOrHasParent(EntityTypes26_2.ABSTRACT_HORSE));
        Assertions.assertFalse(EntityTypes26_2.ZOMBIE.isOrHasParent(EntityTypes26_2.SKELETON));
        Assertions.assertFalse(EntityTypes26_2.HORSE.isOrHasParent(EntityTypes26_2.LLAMA));
        Assertions.assertFalse(EntityTypes26_2.ENTITY.isOrHasParent(EntityTypes26_2.ZOMBIE));
    }

    @Test
    void cacheMatchesParentWalk() {
        for (final EntityTypes26_2 type : EntityTypes26_2.values()) {
            for (final EntityTypes26_2 candidate : EntityTypes26_2.values()) {
                Assertions.assertEquals(walk(type, candidate), type.isOrHasParent(candidate),
                    () -> type + " vs " + candidate);
            }
        }
    }

    private static boolean walk(final EntityType self, final EntityType type) {
        EntityType parent = self;
        do {
            if (parent == type) {
                return true;
            }
            parent = parent.getParent();
        } while (parent != null);
        return false;
    }
}
