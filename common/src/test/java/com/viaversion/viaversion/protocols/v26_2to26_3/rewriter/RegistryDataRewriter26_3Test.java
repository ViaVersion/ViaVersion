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
package com.viaversion.viaversion.protocols.v26_2to26_3.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.protocols.v26_2to26_3.Protocol26_2To26_3;
import java.io.IOException;
import org.junit.jupiter.api.Test;

import static com.viaversion.nbt.stringified.SNBT.deserializeCompoundTag;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

final class RegistryDataRewriter26_3Test {

    @Test
    void rewritesEnchantmentLevelValueCheck() throws IOException {
        final CompoundTag term = deserializeCompoundTag(
            "{condition:\"minecraft:value_check\",value:{type:\"minecraft:enchantment_level\",amount:{base:1.0f,per_level_above_first:1.0f,type:\"minecraft:linear\"}},range:2}"
        );

        new RegistryDataRewriter26_3(new Protocol26_2To26_3()).updateEnchantmentTerm(term);

        assertEquals("minecraft:int_value_check", term.getString("type"));
        assertEquals(2, term.getNumberTag("test").asInt());
        assertNull(term.get("range"));

        final CompoundTag intValue = term.getCompoundTag("value");
        assertEquals("minecraft:from_float", intValue.getString("type"));

        final CompoundTag roundedValue = intValue.getCompoundTag("input");
        assertEquals("minecraft:round", roundedValue.getString("type"));

        final CompoundTag enchantmentLevel = roundedValue.getCompoundTag("input");
        assertEquals("minecraft:enchantment_level", enchantmentLevel.getString("type"));
        assertEquals(1.0F, enchantmentLevel.getCompoundTag("amount").getNumberTag("base").asFloat());
    }
}
