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
package com.viaversion.viaversion.protocols.v1_19_1to1_19_3.rewriter;

import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.rewriter.RecipeRewriter;

public class RecipeRewriter1_19_3<C extends ClientboundPacketType> extends RecipeRewriter<C> {

    public RecipeRewriter1_19_3(final Protocol<C, ?, ?, ?> protocol) {
        super(protocol);
        // Existed before, but now have serialization data
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
    public void handleCraftingShapeless(final PacketWrapper wrapper) {
        wrapper.passthrough(Types.STRING); // Group
        wrapper.passthrough(Types.VAR_INT); // Crafting book category
        handleIngredients(wrapper);
        handleResult(wrapper);
    }

    @Override
    public void handleCraftingShaped(final PacketWrapper wrapper) {
        final int ingredients = wrapper.passthrough(Types.VAR_INT) * wrapper.passthrough(Types.VAR_INT);
        wrapper.passthrough(Types.STRING); // Group
        wrapper.passthrough(Types.VAR_INT); // Crafting book category
        for (int i = 0; i < ingredients; i++) {
            handleIngredient(wrapper);
        }
        handleResult(wrapper);
    }

    @Override
    public void handleSmelting(final PacketWrapper wrapper) {
        wrapper.passthrough(Types.STRING); // Group
        wrapper.passthrough(Types.VAR_INT); // Crafting book category
        handleIngredient(wrapper);
        handleResult(wrapper);
        wrapper.passthrough(Types.FLOAT); // EXP
        wrapper.passthrough(Types.VAR_INT); // Cooking time
    }
}
