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
package com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.rewriter;

import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.rewriter.RecipeRewriter1_19_4;

public class RecipeRewriter1_20_3<C extends ClientboundPacketType> extends RecipeRewriter1_19_4<C> {

    public RecipeRewriter1_20_3(final Protocol<C, ?, ?, ?> protocol) {
        super(protocol);
    }

    @Override
    public void handleCraftingShaped(final PacketWrapper wrapper) throws Exception {
        wrapper.passthrough(Type.STRING); // Group
        wrapper.passthrough(Type.VAR_INT); // Crafting book category
        final int ingredients = wrapper.passthrough(Type.VAR_INT) * wrapper.passthrough(Type.VAR_INT);
        for (int i = 0; i < ingredients; i++) {
            handleIngredient(wrapper);
        }

        final Item item = rewrite(wrapper.user(), wrapper.read(itemType())); // Result
        wrapper.write(mappedItemType(), item);
        wrapper.passthrough(Type.BOOLEAN); // Show notification
    }

    @Override
    protected Type<Item> itemType() {
        return protocol.getItemRewriter().itemType();
    }

    @Override
    protected Type<Item[]> itemArrayType() {
        return protocol.getItemRewriter().itemArrayType();
    }

    @Override
    protected Type<Item> mappedItemType() {
        return protocol.getItemRewriter().mappedItemType();
    }

    @Override
    protected Type<Item[]> mappedItemArrayType() {
        return protocol.getItemRewriter().mappedItemArrayType();
    }
}