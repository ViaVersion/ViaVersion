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
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data;

import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.rewriter.RecipeRewriter;

/**
 * For 1.13.2, not 1.13 (1.13 reads recipe type and id in swapped order)!
 */
public class RecipeRewriter1_13_2 extends RecipeRewriter {

    public RecipeRewriter1_13_2(Protocol protocol) {
        super(protocol);
        recipeHandlers.put("crafting_shapeless", this::handleCraftingShapeless);
        recipeHandlers.put("crafting_shaped", this::handleCraftingShaped);
        recipeHandlers.put("smelting", this::handleSmelting);
    }

    public void handleSmelting(PacketWrapper wrapper) throws Exception {
        wrapper.passthrough(Type.STRING); // Group
        Item[] items = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT); // Ingredients
        for (Item item : items) {
            rewrite(item);
        }

        rewrite(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Result

        wrapper.passthrough(Type.FLOAT); // EXP
        wrapper.passthrough(Type.VAR_INT); // Cooking time
    }

    public void handleCraftingShaped(PacketWrapper wrapper) throws Exception {
        int ingredientsNo = wrapper.passthrough(Type.VAR_INT) * wrapper.passthrough(Type.VAR_INT);
        wrapper.passthrough(Type.STRING); // Group
        for (int j = 0; j < ingredientsNo; j++) {
            Item[] items = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT); // Ingredients
            for (Item item : items) {
                rewrite(item);
            }
        }
        rewrite(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Result
    }

    public void handleCraftingShapeless(PacketWrapper wrapper) throws Exception {
        wrapper.passthrough(Type.STRING); // Group
        int ingredientsNo = wrapper.passthrough(Type.VAR_INT);
        for (int j = 0; j < ingredientsNo; j++) {
            Item[] items = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT); // Ingredients
            for (Item item : items) {
                rewrite(item);
            }
        }
        rewrite(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Result
    }
}
