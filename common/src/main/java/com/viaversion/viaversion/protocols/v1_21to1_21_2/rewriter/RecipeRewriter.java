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

import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.rewriter.RecipeRewriter1_20_3;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPacket1_21;

final class RecipeRewriter extends RecipeRewriter1_20_3<ClientboundPacket1_21> {

    RecipeRewriter(final Protocol<ClientboundPacket1_21, ?, ?, ?> protocol) {
        super(protocol);
    }

    @Override
    protected void handleIngredient(final PacketWrapper wrapper) {
        wrapper.write(Types.HOLDER_SET, ingredient(wrapper));
    }

    @Override
    public void handleCraftingShaped(final PacketWrapper wrapper) {
        wrapper.passthrough(Types.STRING); // Group
        wrapper.passthrough(Types.VAR_INT); // Crafting book category
        final int width = wrapper.passthrough(Types.VAR_INT);
        final int height = wrapper.passthrough(Types.VAR_INT);
        final int ingredients = width * height;

        wrapper.write(Types.VAR_INT, ingredients);
        for (int i = 0; i < ingredients; i++) {
            wrapper.write(Types.HOLDER_SET, ingredient(wrapper));
        }

        wrapper.write(mappedItemType(), rewrite(wrapper.user(), wrapper.read(itemType()))); // Result
        wrapper.passthrough(Types.BOOLEAN); // Show notification
    }

    @Override
    public void handleCraftingShapeless(final PacketWrapper wrapper) {
        wrapper.passthrough(Types.STRING); // Group
        wrapper.passthrough(Types.VAR_INT); // Crafting book category

        final int ingredients = wrapper.read(Types.VAR_INT);
        final HolderSet[] ingredient = new HolderSet[ingredients];
        for (int i = 0; i < ingredients; i++) {
            ingredient[i] = ingredient(wrapper);
        }

        wrapper.write(mappedItemType(), rewrite(wrapper.user(), wrapper.read(itemType())));

        // Also moved below here
        wrapper.write(Types.VAR_INT, ingredients);
        for (final HolderSet item : ingredient) {
            wrapper.write(Types.HOLDER_SET, item);
        }
    }

    private HolderSet ingredient(final PacketWrapper wrapper) {
        final Item[] items = wrapper.read(itemArrayType());
        final int[] ids = new int[items.length];
        for (int i = 0; i < items.length; i++) {
            final Item item = rewrite(wrapper.user(), items[i]);
            ids[i] = item.identifier();
        }
        return HolderSet.of(ids);
    }

    @Override
    public void handleRecipeType(final PacketWrapper wrapper, final String type) {
        if (type.equals("crafting_special_suspiciousstew") || type.equals("crafting_special_shulkerboxcoloring")) {
            wrapper.read(Types.VAR_INT); // Crafting book category
        } else {
            super.handleRecipeType(wrapper, type);
        }
    }
}
