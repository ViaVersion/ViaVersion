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
package com.viaversion.viaversion.common.type;

import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.api.type.types.item.ItemCostType1_20_5;
import com.viaversion.viaversion.api.type.types.item.StructuredDataType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ItemCostTypeTest {
    private final ItemCostType1_20_5 type = new ItemCostType1_20_5(new ArrayType<>(new StructuredDataType()));
    private final StructuredDataKey<Integer> blockTransformer = new StructuredDataKey<>("block_transformer", Types.VAR_INT);

    @Test
    void testRemovedComponentsAreNotWrittenAsPredicates() {
        final StructuredData<?> removed = StructuredData.empty(blockTransformer, 43);
        final StructuredData<?> present = StructuredData.of(StructuredDataKey.MAX_DAMAGE, 100, 2);
        final StructuredItem item = new StructuredItem(1053, 1, new StructuredDataContainer(new StructuredData<?>[]{removed, present}));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            type.write(buffer, item);
            Types.INT.writePrimitive(buffer, 123456);

            Assertions.assertEquals(1053, Types.VAR_INT.readPrimitive(buffer));
            Assertions.assertEquals(1, Types.VAR_INT.readPrimitive(buffer));
            Assertions.assertEquals(1, Types.VAR_INT.readPrimitive(buffer)); // Component count
            Assertions.assertEquals(2, Types.VAR_INT.readPrimitive(buffer)); // max_damage
            Assertions.assertEquals(100, Types.VAR_INT.readPrimitive(buffer));
            Assertions.assertEquals(123456, Types.INT.readPrimitive(buffer)); // Next packet field
            Assertions.assertFalse(buffer.isReadable());
            Assertions.assertSame(removed, item.dataContainer().data().get(blockTransformer));
            Assertions.assertSame(present, item.dataContainer().data().get(StructuredDataKey.MAX_DAMAGE));
        } finally {
            buffer.release();
        }
    }

    @Test
    void testOptionalCostWithOnlyRemovedComponents() {
        final ItemCostType1_20_5.OptionalItemCostType optionalType = new ItemCostType1_20_5.OptionalItemCostType(type);
        final StructuredData<?> removed = StructuredData.empty(blockTransformer, 43);
        final StructuredItem item = new StructuredItem(1053, 1, new StructuredDataContainer(new StructuredData<?>[]{removed}));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            optionalType.write(buffer, item);
            Types.INT.writePrimitive(buffer, 123456);

            Assertions.assertTrue(buffer.readBoolean());
            Assertions.assertEquals(1053, Types.VAR_INT.readPrimitive(buffer));
            Assertions.assertEquals(1, Types.VAR_INT.readPrimitive(buffer));
            Assertions.assertEquals(0, Types.VAR_INT.readPrimitive(buffer)); // No predicates remain
            Assertions.assertEquals(123456, Types.INT.readPrimitive(buffer));
            Assertions.assertFalse(buffer.isReadable());
            Assertions.assertSame(removed, item.dataContainer().data().get(blockTransformer));
        } finally {
            buffer.release();
        }
    }

    @Test
    void testAbsentOptionalCost() {
        final ByteBuf buffer = Unpooled.buffer();
        try {
            new ItemCostType1_20_5.OptionalItemCostType(type).write(buffer, null);
            Assertions.assertFalse(buffer.readBoolean());
            Assertions.assertFalse(buffer.isReadable());
        } finally {
            buffer.release();
        }
    }
}
