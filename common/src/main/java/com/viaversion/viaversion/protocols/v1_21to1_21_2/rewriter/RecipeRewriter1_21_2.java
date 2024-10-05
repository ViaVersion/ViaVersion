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

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_21_2;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.rewriter.RecipeRewriter1_20_3;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPacket1_21;
import com.viaversion.viaversion.util.Key;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

// Use directly as a connection storage. Slightly weird, but easiest and closed off from other packages
final class RecipeRewriter1_21_2 extends RecipeRewriter1_20_3<ClientboundPacket1_21> implements StorableObject {

    private static final int FALLBACK_CATEGORY = 3; // TODO ?
    private final List<ShapelessRecipe> shapelessRecipes = new ArrayList<>();
    private final List<ShapedRecipe> shapedRecipes = new ArrayList<>();
    private final List<SmithingRecipe> smithingRecipes = new ArrayList<>();
    private final List<StoneCutterRecipe> stoneCutterRecipes = new ArrayList<>();
    private final List<FurnaceRecipe> furnaceRecipes = new ArrayList<>();
    private final Object2IntMap<String> recipeGroups = new Object2IntOpenHashMap<>();
    private final Map<String, Recipe> recipesByKey = new HashMap<>();
    private final List<Recipe> recipes = new ArrayList<>();
    private String currentRecipeIdentifier;

    public void setCurrentRecipeIdentifier(final String recipeIdentifier) {
        this.currentRecipeIdentifier = Key.stripMinecraftNamespace(recipeIdentifier);
    }

    RecipeRewriter1_21_2(final Protocol<ClientboundPacket1_21, ?, ?, ?> protocol) {
        super(protocol);
        recipeGroups.defaultReturnValue(-1);
    }

    @Override
    public void handleSimpleRecipe(final PacketWrapper wrapper) {
        final int category = wrapper.read(Types.VAR_INT);
        // Special recipes aren't sent to the client
    }

    @Override
    public void handleStonecutting(final PacketWrapper wrapper) {
        final int group = readRecipeGroup(wrapper);
        final Item[] ingredient = readIngredient(wrapper);
        final Item result = rewrite(wrapper.user(), wrapper.read(itemType()));

        final StoneCutterRecipe recipe = new StoneCutterRecipe(recipesByKey.size(), currentRecipeIdentifier, group, ingredient, result);
        addRecipe(stoneCutterRecipes, recipe);
    }

    private <T extends Recipe> void addRecipe(final List<T> list, final T recipe) {
        list.add(recipe);
        recipes.add(recipe);
        recipesByKey.put(currentRecipeIdentifier, recipe);
    }

    @Override
    public void handleSmithingTransform(final PacketWrapper wrapper) {
        final IntList template = new IntArrayList();
        readIngredientToList(wrapper, template);
        //smithingTemplate.addAll(template);
        readIngredient(wrapper);
        readIngredient(wrapper);
        //readIngredientToList(wrapper, smithingBase);
        //readIngredientToList(wrapper, smithingAddition);
        final Item result = rewrite(wrapper.user(), wrapper.read(itemType()));

        // TODO Correct ingredients?
        final Item[] ingredients = new Item[template.size()];
        for (int i = 0; i < template.size(); i++) {
            ingredients[i] = new StructuredItem(template.getInt(i), 1);
        }

        final SmithingRecipe recipe = new SmithingRecipe(recipesByKey.size(), currentRecipeIdentifier, ingredients, result);
        addRecipe(smithingRecipes, recipe);
    }

    @Override
    public void handleSmithingTrim(final PacketWrapper wrapper) {
        readIngredient(wrapper);
        readIngredient(wrapper);
        readIngredient(wrapper);
        //readIngredientToList(wrapper, smithingTemplate);
        //readIngredientToList(wrapper, smithingBase);
        //readIngredientToList(wrapper, smithingAddition);
    }

    @Override
    public void handleCraftingShaped(final PacketWrapper wrapper) {
        final int group = readRecipeGroup(wrapper);
        final int category = wrapper.read(Types.VAR_INT);
        final int width = wrapper.read(Types.VAR_INT);
        final int height = wrapper.read(Types.VAR_INT);
        final int ingredientsSize = width * height;
        final Item[][] ingredients = new Item[ingredientsSize][];
        for (int i = 0; i < ingredientsSize; i++) {
            ingredients[i] = readIngredient(wrapper);
        }
        final Item result = rewrite(wrapper.user(), wrapper.read(itemType()));

        final ShapedRecipe recipe = new ShapedRecipe(recipesByKey.size(), currentRecipeIdentifier, group, category, width, height, ingredients, result);
        addRecipe(shapedRecipes, recipe);

        wrapper.read(Types.BOOLEAN); // Show notification
    }

    @Override
    public void handleCraftingShapeless(final PacketWrapper wrapper) {
        final int group = readRecipeGroup(wrapper);
        final int category = wrapper.read(Types.VAR_INT);
        final int ingredientsSize = wrapper.read(Types.VAR_INT);
        final Item[][] ingredients = new Item[ingredientsSize][];
        for (int i = 0; i < ingredientsSize; i++) {
            ingredients[i] = readIngredient(wrapper);
        }
        final Item result = rewrite(wrapper.user(), wrapper.read(itemType()));

        final ShapelessRecipe recipe = new ShapelessRecipe(recipesByKey.size(), currentRecipeIdentifier, group, category, ingredients, result);
        addRecipe(shapelessRecipes, recipe);
    }

    @Override
    public void handleSmelting(final PacketWrapper wrapper) {
        final int group = readRecipeGroup(wrapper);
        final int category = wrapper.read(Types.VAR_INT);
        final Item[] ingredient = readIngredient(wrapper);
        final Item result = rewrite(wrapper.user(), wrapper.read(itemType()));

        final FurnaceRecipe recipe = new FurnaceRecipe(recipesByKey.size(), currentRecipeIdentifier, group, category, ingredient, result);
        addRecipe(furnaceRecipes, recipe);

        wrapper.read(Types.FLOAT); // EXP
        wrapper.read(Types.VAR_INT); // Cooking time
    }

    private int readRecipeGroup(final PacketWrapper wrapper) {
        final String recipeGroup = Key.stripMinecraftNamespace(wrapper.read(Types.STRING));
        if (recipeGroup.isEmpty()) {
            return -1;
        }

        if (recipeGroups.containsKey(recipeGroup)) {
            return recipeGroups.getInt(recipeGroup);
        }

        final int size = recipeGroups.size();
        recipeGroups.put(recipeGroup, size);
        return size;
    }

    private Item[] readIngredient(final PacketWrapper wrapper) {
        final Item[] items = wrapper.read(itemArrayType());
        for (int i = 0; i < items.length; i++) {
            final Item item = items[i];
            items[i] = rewrite(wrapper.user(), item);
        }
        return items;
    }

    public HolderSet toHolderSet(final Item[] ingredient) {
        // Ingredients are no longer full items, already store them as just holder sets
        final int[] ids = new int[ingredient.length];
        for (int i = 0; i < ingredient.length; i++) {
            ids[i] = ingredient[i].identifier();
        }
        return HolderSet.of(ids);
    }

    private void readIngredientToList(final PacketWrapper wrapper, final IntCollection list) {
        final Item[] items = wrapper.read(itemArrayType());
        final MappingData mappings = protocol.getMappingData();
        for (final Item item : items) {
            final int mappedId = mappings.getNewItemId(item.identifier());
            list.add(mappedId);
        }
    }

    public @Nullable Recipe recipe(final String key) {
        return recipesByKey.get(Key.stripMinecraftNamespace(key));
    }

    public @Nullable Recipe recipe(final int displayId) {
        return displayId >= 0 && displayId < recipes.size() ? recipes.get(displayId) : null;
    }

    interface Recipe {
        int SLOT_DISPLAY_EMPTY = 0;
        int SLOT_DISPLAY_ANY_FUEL = 1;
        int SLOT_DISPLAY_ITEM = 3;
        int SLOT_DISPLAY_COMPOSITE = 6;

        int index();

        String identifier();

        int recipeDisplayId();

        default Item @Nullable [][] ingredients() {
            final Item[] ingredient = ingredient();
            return ingredient == null ? null : new Item[][]{ingredient};
        }

        default Item @Nullable [] ingredient() {
            return null;
        }

        default int group() {
            return -1;
        }

        default int category() {
            return FALLBACK_CATEGORY;
        }

        void writeRecipeDisplay(PacketWrapper wrapper);
    }

    record ShapelessRecipe(int index, String identifier, int group, int category, Item[][] ingredients,
                           Item result) implements Recipe {
        @Override
        public int recipeDisplayId() {
            return 0;
        }

        @Override
        public void writeRecipeDisplay(final PacketWrapper wrapper) {
            writeIngredientsDisplay(wrapper, ingredients);
            writeItemDisplay(wrapper, result);
            writeCraftingStationDisplay(wrapper);
        }
    }

    record ShapedRecipe(int index, String identifier, int group, int category, int width, int height,
                        Item[][] ingredients, Item result) implements Recipe {
        @Override
        public int recipeDisplayId() {
            return 1;
        }

        @Override
        public void writeRecipeDisplay(final PacketWrapper wrapper) {
            wrapper.write(Types.VAR_INT, width);
            wrapper.write(Types.VAR_INT, height);
            writeIngredientsDisplay(wrapper, ingredients);
            writeItemDisplay(wrapper, result);
            writeCraftingStationDisplay(wrapper);
        }
    }

    record FurnaceRecipe(int index, String identifier, int group, int category, Item[] ingredient,
                         Item result) implements Recipe {
        @Override
        public int recipeDisplayId() {
            return 2;
        }

        @Override
        public void writeRecipeDisplay(final PacketWrapper wrapper) {
            writeIngredientDisplay(wrapper, ingredient);
            wrapper.write(Types.VAR_INT, SLOT_DISPLAY_ANY_FUEL); // Fuel
            writeItemDisplay(wrapper, result);
            writeCraftingStationDisplay(wrapper);
        }
    }

    record StoneCutterRecipe(int index, String identifier, int group, Item[] ingredient,
                             Item result) implements Recipe {
        @Override
        public int recipeDisplayId() {
            return 3;
        }

        @Override
        public void writeRecipeDisplay(final PacketWrapper wrapper) {
            writeItemDisplay(wrapper, result);
            writeCraftingStationDisplay(wrapper);
        }
    }

    record SmithingRecipe(int index, String identifier, Item[] ingredient, Item result) implements Recipe {
        @Override
        public int recipeDisplayId() {
            return 4;
        }

        @Override
        public void writeRecipeDisplay(final PacketWrapper wrapper) {
            writeItemDisplay(wrapper, result);
            writeCraftingStationDisplay(wrapper);
        }
    }

    private static void writeIngredientsDisplay(final PacketWrapper wrapper, final Item[][] ingredients) {
        wrapper.write(Types.VAR_INT, ingredients.length);
        for (final Item[] ingredient : ingredients) {
            writeIngredientDisplay(wrapper, ingredient);
        }
    }

    private static void writeIngredientDisplay(final PacketWrapper wrapper, final Item[] ingredient) {
        if (ingredient.length == 0) {
            wrapper.write(Types.VAR_INT, Recipe.SLOT_DISPLAY_EMPTY);
            return;
        }

        wrapper.write(Types.VAR_INT, Recipe.SLOT_DISPLAY_COMPOSITE);
        wrapper.write(Types.VAR_INT, ingredient.length);
        for (final Item item : ingredient) {
            writeItemDisplay(wrapper, item);
        }
    }

    private static void writeItemDisplay(final PacketWrapper wrapper, final Item item) {
        wrapper.write(Types.VAR_INT, Recipe.SLOT_DISPLAY_ITEM);
        wrapper.write(Types1_21_2.ITEM, item);
    }

    private static void writeCraftingStationDisplay(final PacketWrapper wrapper) {
        wrapper.write(Types.VAR_INT, Recipe.SLOT_DISPLAY_EMPTY);
    }
}
