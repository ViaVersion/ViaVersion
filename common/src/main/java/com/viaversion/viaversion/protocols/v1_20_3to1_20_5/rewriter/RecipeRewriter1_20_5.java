/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_20_3to1_20_5.rewriter;

import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.rewriter.RecipeRewriter1_20_3;
import java.util.ArrayList;
import java.util.List;

final class RecipeRewriter1_20_5<C extends ClientboundPacketType> extends RecipeRewriter1_20_3<C> {

    public RecipeRewriter1_20_5(final Protocol<C, ?, ?, ?> protocol) {
        super(protocol);
    }

    @Override
    protected void handleIngredient(final PacketWrapper wrapper) {
        final Item[] items = wrapper.read(itemArrayType());
        final List<Item> newItems = new ArrayList<>(items.length);
        for (final Item item : items) {
            if (item == null || item.isEmpty()) continue;
            newItems.add(rewrite(wrapper.user(), item));
        }
        wrapper.write(mappedItemArrayType(), newItems.toArray(new Item[0]));
    }

    @Override
    protected void handleResult(final PacketWrapper wrapper) {
        Item result = rewrite(wrapper.user(), wrapper.read(itemType()));
        if (result == null || result.isEmpty()) {
            result = new StructuredItem(1, 1);
        }
        wrapper.write(mappedItemType(), result);
    }
}
