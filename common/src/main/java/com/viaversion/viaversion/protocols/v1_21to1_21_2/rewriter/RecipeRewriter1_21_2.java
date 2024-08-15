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
package com.viaversion.viaversion.protocols.v1_21to1_21_2.rewriter;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.rewriter.RecipeRewriter1_20_3;

public class RecipeRewriter1_21_2<C extends ClientboundPacketType> extends RecipeRewriter1_20_3<C> {

    public RecipeRewriter1_21_2(final Protocol<C, ?, ?, ?> protocol) {
        super(protocol);
    }

    @Override
    public void handleCraftingShaped(final PacketWrapper wrapper) {
        wrapper.passthrough(Types.STRING); // Group
        wrapper.passthrough(Types.VAR_INT); // Crafting book category
        wrapper.passthrough(Types.VAR_INT); // Width
        wrapper.passthrough(Types.VAR_INT); // Height

        final int ingredients = wrapper.passthrough(Types.VAR_INT);
        for (int i = 0; i < ingredients; i++) {
            handleIngredient(wrapper);
        }

        wrapper.write(mappedItemType(), rewrite(wrapper.user(), wrapper.read(itemType()))); // Result
        wrapper.passthrough(Types.BOOLEAN); // Show notification
    }

    @Override
    public void handleCraftingShapeless(final PacketWrapper wrapper) {
        wrapper.passthrough(Types.STRING); // Group
        wrapper.passthrough(Types.VAR_INT); // Crafting book category
        wrapper.write(mappedItemType(), rewrite(wrapper.user(), wrapper.read(itemType()))); // Result
        handleIngredients(wrapper);
    }

    @Override
    protected void handleIngredient(final PacketWrapper wrapper) {
        final HolderSet items = wrapper.passthrough(Types.HOLDER_SET);
        if (items.hasTagKey()) {
            return;
        }

        final int[] ids = items.ids();
        for (int i = 0; i < ids.length; i++) {
            ids[i] = rewriteItemId(wrapper.user(), ids[i]);
        }
    }

    protected int rewriteItemId(final UserConnection connection, final int id) {
        if (protocol.getMappingData() != null && protocol.getMappingData().getItemMappings() != null) {
            return protocol.getMappingData().getItemMappings().getNewIdOrDefault(id, id);
        }
        return id;
    }
}
