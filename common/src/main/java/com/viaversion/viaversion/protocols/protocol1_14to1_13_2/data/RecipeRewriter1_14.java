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
package com.viaversion.viaversion.protocols.protocol1_14to1_13_2.data;

import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data.RecipeRewriter1_13_2;
import com.viaversion.viaversion.rewriter.ItemRewriter;

public class RecipeRewriter1_14 extends RecipeRewriter1_13_2 {

    public RecipeRewriter1_14(Protocol protocol) {
        super(protocol);
        recipeHandlers.put("stonecutting", this::handleStonecutting);

        recipeHandlers.put("blasting", this::handleSmelting);
        recipeHandlers.put("smoking", this::handleSmelting);
        recipeHandlers.put("campfire_cooking", this::handleSmelting);
    }

    public void handleStonecutting(PacketWrapper wrapper) throws Exception {
        wrapper.passthrough(Type.STRING);
        Item[] items = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT); // Ingredients
        for (Item item : items) {
            rewrite(item);
        }

        rewrite(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Result
    }
}
