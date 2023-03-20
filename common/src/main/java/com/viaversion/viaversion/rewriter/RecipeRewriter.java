/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2023 ViaVersion and contributors
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
package com.viaversion.viaversion.rewriter;

import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.Key;
import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public class RecipeRewriter<C extends ClientboundPacketType> {

    protected final Protocol<C, ?, ?, ?> protocol;
    protected final Map<String, RecipeConsumer> recipeHandlers = new HashMap<>();

    public RecipeRewriter(final Protocol<C, ?, ?, ?> protocol) {
        this.protocol = protocol;
        recipeHandlers.put("crafting_shapeless", this::handleCraftingShapeless);
        recipeHandlers.put("crafting_shaped", this::handleCraftingShaped);
        recipeHandlers.put("smelting", this::handleSmelting);

        // Added in 1.14
        recipeHandlers.put("blasting", this::handleSmelting);
        recipeHandlers.put("smoking", this::handleSmelting);
        recipeHandlers.put("campfire_cooking", this::handleSmelting);
        recipeHandlers.put("stonecutting", this::handleStonecutting);

        // Added in 1.16
        recipeHandlers.put("smithing", this::handleSmithing);

        // Added in 1.19.4
        recipeHandlers.put("smithing_transform", this::handleSmithingTransform);
        recipeHandlers.put("smithing_trim", this::handleSmithingTrim);
        recipeHandlers.put("crafting_decorated_pot", this::handleSimpleRecipe);
    }

    public void handleRecipeType(PacketWrapper wrapper, String type) throws Exception {
        RecipeConsumer handler = recipeHandlers.get(type);
        if (handler != null) {
            handler.accept(wrapper);
        }
    }

    /**
     * Registers a packet handler to rewrite recipe types, for 1.14+.
     *
     * @param packetType packet type
     */
    public void register(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            int size = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < size; i++) {
                String type = wrapper.passthrough(Type.STRING);
                wrapper.passthrough(Type.STRING); // Recipe Identifier
                handleRecipeType(wrapper, Key.stripMinecraftNamespace(type));
            }
        });
    }

    public void handleCraftingShaped(PacketWrapper wrapper) throws Exception {
        int ingredientsNo = wrapper.passthrough(Type.VAR_INT) * wrapper.passthrough(Type.VAR_INT);
        wrapper.passthrough(Type.STRING); // Group
        for (int i = 0; i < ingredientsNo; i++) {
            handleIngredient(wrapper);
        }
        rewrite(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Result
    }

    public void handleCraftingShapeless(PacketWrapper wrapper) throws Exception {
        wrapper.passthrough(Type.STRING); // Group
        handleIngredients(wrapper);
        rewrite(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Result
    }

    public void handleSmelting(PacketWrapper wrapper) throws Exception {
        wrapper.passthrough(Type.STRING); // Group
        handleIngredient(wrapper);
        rewrite(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Result
        wrapper.passthrough(Type.FLOAT); // EXP
        wrapper.passthrough(Type.VAR_INT); // Cooking time
    }

    public void handleStonecutting(PacketWrapper wrapper) throws Exception {
        wrapper.passthrough(Type.STRING); // Group
        handleIngredient(wrapper);
        rewrite(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Result
    }

    public void handleSmithing(PacketWrapper wrapper) throws Exception {
        handleIngredient(wrapper); // Base
        handleIngredient(wrapper); // Addition
        rewrite(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Result
    }

    public void handleSimpleRecipe(final PacketWrapper wrapper) throws Exception {
        wrapper.passthrough(Type.VAR_INT); // Crafting book category
    }

    public void handleSmithingTransform(final PacketWrapper wrapper) throws Exception {
        handleIngredient(wrapper); // Template
        handleIngredient(wrapper); // Base
        handleIngredient(wrapper); // Additions
        rewrite(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Result
    }

    public void handleSmithingTrim(final PacketWrapper wrapper) throws Exception {
        handleIngredient(wrapper); // Template
        handleIngredient(wrapper); // Base
        handleIngredient(wrapper); // Additions
    }

    protected void rewrite(@Nullable Item item) {
        if (protocol.getItemRewriter() != null) {
            protocol.getItemRewriter().handleItemToClient(item);
        }
    }

    protected void handleIngredient(final PacketWrapper wrapper) throws Exception {
        final Item[] items = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT);
        for (final Item item : items) {
            rewrite(item);
        }
    }

    protected void handleIngredients(final PacketWrapper wrapper) throws Exception {
        final int ingredients = wrapper.passthrough(Type.VAR_INT);
        for (int i = 0; i < ingredients; i++) {
            handleIngredient(wrapper);
        }
    }

    @FunctionalInterface
    public interface RecipeConsumer {

        void accept(PacketWrapper wrapper) throws Exception;
    }
}
