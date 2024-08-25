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
package com.viaversion.viaversion.rewriter;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
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

    public void handleRecipeType(PacketWrapper wrapper, String type) {
        RecipeConsumer handler = recipeHandlers.get(Key.stripMinecraftNamespace(type));
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
            int size = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < size; i++) {
                String type = wrapper.passthrough(Types.STRING);
                wrapper.passthrough(Types.STRING); // Recipe Identifier
                handleRecipeType(wrapper, type);
            }
        });
    }

    public void register1_20_5(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            int size = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < size; i++) {
                wrapper.passthrough(Types.STRING); // Recipe Identifier

                final int typeId = wrapper.passthrough(Types.VAR_INT);
                final String type = protocol.getMappingData().getRecipeSerializerMappings().identifier(typeId);
                handleRecipeType(wrapper, type);
            }
        });
    }

    public void handleCraftingShaped(PacketWrapper wrapper) {
        int ingredientsNo = wrapper.passthrough(Types.VAR_INT) * wrapper.passthrough(Types.VAR_INT);
        wrapper.passthrough(Types.STRING); // Group
        for (int i = 0; i < ingredientsNo; i++) {
            handleIngredient(wrapper);
        }

        final Item result = rewrite(wrapper.user(), wrapper.read(itemType()));
        wrapper.write(mappedItemType(), result);
    }

    public void handleCraftingShapeless(PacketWrapper wrapper) {
        wrapper.passthrough(Types.STRING); // Group
        handleIngredients(wrapper);
        final Item result = rewrite(wrapper.user(), wrapper.read(itemType()));
        wrapper.write(mappedItemType(), result);
    }

    public void handleSmelting(PacketWrapper wrapper) {
        wrapper.passthrough(Types.STRING); // Group
        handleIngredient(wrapper);
        final Item result = rewrite(wrapper.user(), wrapper.read(itemType()));
        wrapper.write(mappedItemType(), result);
        wrapper.passthrough(Types.FLOAT); // EXP
        wrapper.passthrough(Types.VAR_INT); // Cooking time
    }

    public void handleStonecutting(PacketWrapper wrapper) {
        wrapper.passthrough(Types.STRING); // Group
        handleIngredient(wrapper);
        final Item result = rewrite(wrapper.user(), wrapper.read(itemType()));
        wrapper.write(mappedItemType(), result);
    }

    public void handleSmithing(PacketWrapper wrapper) {
        handleIngredient(wrapper); // Base
        handleIngredient(wrapper); // Addition
        final Item result = rewrite(wrapper.user(), wrapper.read(itemType()));
        wrapper.write(mappedItemType(), result);
    }

    public void handleSimpleRecipe(final PacketWrapper wrapper) {
        wrapper.passthrough(Types.VAR_INT); // Crafting book category
    }

    public void handleSmithingTransform(final PacketWrapper wrapper) {
        handleIngredient(wrapper); // Template
        handleIngredient(wrapper); // Base
        handleIngredient(wrapper); // Additions
        final Item result = rewrite(wrapper.user(), wrapper.read(itemType()));
        wrapper.write(mappedItemType(), result);
    }

    public void handleSmithingTrim(final PacketWrapper wrapper) {
        handleIngredient(wrapper); // Template
        handleIngredient(wrapper); // Base
        handleIngredient(wrapper); // Additions
    }

    protected @Nullable Item rewrite(UserConnection connection, @Nullable Item item) {
        if (protocol.getItemRewriter() != null) {
            return protocol.getItemRewriter().handleItemToClient(connection, item);
        }
        return item;
    }

    protected void handleIngredient(final PacketWrapper wrapper) {
        final Item[] items = wrapper.passthroughAndMap(itemArrayType(), mappedItemArrayType());
        for (int i = 0; i < items.length; i++) {
            Item item = items[i];
            items[i] = rewrite(wrapper.user(), item);
        }
    }

    protected void handleIngredients(final PacketWrapper wrapper) {
        final int ingredients = wrapper.passthrough(Types.VAR_INT);
        for (int i = 0; i < ingredients; i++) {
            handleIngredient(wrapper);
        }
    }

    @FunctionalInterface
    public interface RecipeConsumer {

        void accept(PacketWrapper wrapper);
    }

    protected Type<Item> itemType() {
        return Types.ITEM1_13_2;
    }

    protected Type<Item[]> itemArrayType() {
        return Types.ITEM1_13_2_ARRAY;
    }

    protected Type<Item> mappedItemType() {
        return itemType();
    }

    protected Type<Item[]> mappedItemArrayType() {
        return itemArrayType();
    }
}
