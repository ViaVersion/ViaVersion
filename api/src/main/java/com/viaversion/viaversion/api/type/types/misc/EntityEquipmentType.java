/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.viaversion.viaversion.api.type.types.misc;

import com.viaversion.viaversion.api.minecraft.EntityEquipment;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;

public final class EntityEquipmentType extends Type<List<EntityEquipment>> {

    private final Type<Item> itemType;

    public EntityEquipmentType(final Type<Item> itemType) {
        super(List.class);
        this.itemType = itemType;
    }

    @Override
    public List<EntityEquipment> read(final ByteBuf buffer) {
        final List<EntityEquipment> list = new ArrayList<>();
        byte rawSlot;
        do {
            rawSlot = Types.BYTE.readPrimitive(buffer);
            final Item item = itemType.read(buffer);
            list.add(new EntityEquipment((byte) (rawSlot & 0x7F), item));
        } while (rawSlot < 0);
        return list;
    }

    @Override
    public void write(final ByteBuf buffer, final List<EntityEquipment> value) {
        for (int i = 0; i < value.size(); i++) {
            final EntityEquipment equipment = value.get(i);
            final boolean more = i < value.size() - 1;
            Types.BYTE.writePrimitive(buffer, more ? (byte) (equipment.slot() | -128) : (byte) equipment.slot());
            itemType.write(buffer, equipment.item());
        }
    }

    @Override
    public Class<? extends Type<?>> getBaseClass() {
        return EntityEquipmentType.class;
    }
}
