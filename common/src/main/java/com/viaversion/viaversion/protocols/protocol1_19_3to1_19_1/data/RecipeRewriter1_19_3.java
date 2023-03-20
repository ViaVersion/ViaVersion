/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2022 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.data;

import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_16to1_15_2.data.RecipeRewriter1_16;

public class RecipeRewriter1_19_3 extends RecipeRewriter1_16 {

    public RecipeRewriter1_19_3(final Protocol protocol) {
        super(protocol);
        recipeHandlers.put("crafting_special_armordye", this::handleSimpleRecipe);
        recipeHandlers.put("crafting_special_bookcloning", this::handleSimpleRecipe);
        recipeHandlers.put("crafting_special_mapcloning", this::handleSimpleRecipe);
        recipeHandlers.put("crafting_special_mapextending", this::handleSimpleRecipe);
        recipeHandlers.put("crafting_special_firework_rocket", this::handleSimpleRecipe);
        recipeHandlers.put("crafting_special_firework_star", this::handleSimpleRecipe);
        recipeHandlers.put("crafting_special_firework_star_fade", this::handleSimpleRecipe);
        recipeHandlers.put("crafting_special_tippedarrow", this::handleSimpleRecipe);
        recipeHandlers.put("crafting_special_bannerduplicate", this::handleSimpleRecipe);
        recipeHandlers.put("crafting_special_shielddecoration", this::handleSimpleRecipe);
        recipeHandlers.put("crafting_special_shulkerboxcoloring", this::handleSimpleRecipe);
        recipeHandlers.put("crafting_special_suspiciousstew", this::handleSimpleRecipe);
        recipeHandlers.put("crafting_special_repairitem", this::handleSimpleRecipe);
    }

    @Override
    public void handleCraftingShapeless(final PacketWrapper wrapper) throws Exception {
        wrapper.passthrough(Type.STRING); // Group
        wrapper.passthrough(Type.VAR_INT); // Crafting book category
        final int ingredients = wrapper.passthrough(Type.VAR_INT);
        for (int j = 0; j < ingredients; j++) {
            final Item[] items = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT); // Ingredients
            for (final Item item : items) {
                rewrite(item);
            }
        }
        rewrite(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Result
    }

    @Override
    public void handleCraftingShaped(final PacketWrapper wrapper) throws Exception {
        final int ingredients = wrapper.passthrough(Type.VAR_INT) * wrapper.passthrough(Type.VAR_INT);
        wrapper.passthrough(Type.STRING); // Group
        wrapper.passthrough(Type.VAR_INT); // Crafting book category
        for (int j = 0; j < ingredients; j++) {
            final Item[] items = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT); // Ingredients
            for (final Item item : items) {
                rewrite(item);
            }
        }
        rewrite(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Result
    }

    @Override
    public void handleSmelting(final PacketWrapper wrapper) throws Exception {
        wrapper.passthrough(Type.STRING); // Group
        wrapper.passthrough(Type.VAR_INT); // Crafting book category
        final Item[] items = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT); // Ingredients
        for (final Item item : items) {
            rewrite(item);
        }
        rewrite(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Result
        wrapper.passthrough(Type.FLOAT); // EXP
        wrapper.passthrough(Type.VAR_INT); // Cooking time
    }

    public void handleSimpleRecipe(final PacketWrapper wrapper) throws Exception {
        wrapper.passthrough(Type.VAR_INT); // Crafting book category
    }
}
