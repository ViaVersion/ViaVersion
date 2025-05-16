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
package com.viaversion.viaversion.protocols.v1_21to1_21_2.rewriter;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.rewriter.RecipeRewriter1_20_3;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPacket1_21;
import com.viaversion.viaversion.util.Key;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

// Use directly as a connection storage. Slightly weird, but easiest and closed off from other packages
final class RecipeRewriter1_21_2 extends RecipeRewriter1_20_3<ClientboundPacket1_21> implements StorableObject {

    private static final int[] EMPTY_ARRAY = new int[0];
    private final List<StoneCutterRecipe> stoneCutterRecipes = new ArrayList<>();
    private final Object2IntMap<String> recipeGroups = new Object2IntOpenHashMap<>();
    private final Map<String, Recipe> recipesByKey = new HashMap<>();
    private final Map<String, IntSet> recipeInputs = new Object2ObjectArrayMap<>();
    private final List<Recipe> recipes = new ArrayList<>();
    private String currentRecipeIdentifier;

    public void setCurrentRecipeIdentifier(final String recipeIdentifier) {
        this.currentRecipeIdentifier = Key.stripMinecraftNamespace(recipeIdentifier);
    }

    RecipeRewriter1_21_2(final Protocol<ClientboundPacket1_21, ?, ?, ?> protocol) {
        super(protocol);
        recipeGroups.defaultReturnValue(-1);

        recipeHandlers.put("smelting", wrapper -> handleSmelting("furnace_input", wrapper));
        recipeHandlers.put("blasting", wrapper -> handleSmelting("blast_furnace_input", wrapper));
        recipeHandlers.put("smoking", wrapper -> handleSmelting("smoker_input", wrapper));
        recipeHandlers.put("campfire_cooking", wrapper -> handleSmelting("campfire_input", wrapper));
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

        final StoneCutterRecipe recipe = new StoneCutterRecipe(currentRecipeIdentifier, group, ingredient, result);
        stoneCutterRecipes.add(recipe); // Sent separately in update_recipes
    }

    private void addRecipe(final Recipe recipe) {
        recipes.add(recipe);
        recipesByKey.put(currentRecipeIdentifier, recipe);
    }

    @Override
    public void handleSmithingTransform(final PacketWrapper wrapper) {
        readRecipeInputs("smithing_template", wrapper);
        readRecipeInputs("smithing_base", wrapper);
        readRecipeInputs("smithing_addition", wrapper);
        wrapper.read(itemType()); // Result
    }

    @Override
    public void handleSmithingTrim(final PacketWrapper wrapper) {
        readRecipeInputs("smithing_template", wrapper);
        readRecipeInputs("smithing_base", wrapper);
        readRecipeInputs("smithing_addition", wrapper);
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
        final boolean showNotification = wrapper.read(Types.BOOLEAN);

        final ShapedRecipe recipe = new ShapedRecipe(recipesByKey.size(), currentRecipeIdentifier, group, category, width, height, ingredients, result, showNotification);
        addRecipe(recipe);
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
        addRecipe(recipe);
    }

    public void handleSmelting(final String key, final PacketWrapper wrapper) {
        final int group = readRecipeGroup(wrapper);
        final int category = wrapper.read(Types.VAR_INT);
        final Item[] ingredient = readRecipeInputs(key, wrapper);
        final Item result = rewrite(wrapper.user(), wrapper.read(itemType()));
        final float experience = wrapper.read(Types.FLOAT);
        final int cookingTime = wrapper.read(Types.VAR_INT);

        final FurnaceRecipe recipe = new FurnaceRecipe(recipesByKey.size(), currentRecipeIdentifier, group, category, ingredient, result, cookingTime, experience);
        addRecipe(recipe);
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

    public @Nullable Recipe recipe(final String key) {
        return recipesByKey.get(Key.stripMinecraftNamespace(key));
    }

    public @Nullable Recipe recipe(final int displayId) {
        return displayId >= 0 && displayId < recipes.size() ? recipes.get(displayId) : null;
    }

    public void finalizeRecipes() {
        // Need to be sorted alphabetically
        stoneCutterRecipes.sort(Comparator.comparing(recipe -> recipe.identifier));
    }

    public void writeUpdateRecipeInputs(final PacketWrapper wrapper) {
        // Smithing and smelting inputs
        wrapper.write(Types.VAR_INT, recipeInputs.size());
        for (final Map.Entry<String, IntSet> entry : recipeInputs.entrySet()) {
            wrapper.write(Types.STRING, entry.getKey());
            wrapper.write(Types.VAR_INT_ARRAY_PRIMITIVE, entry.getValue().toArray(EMPTY_ARRAY));
        }

        // Stonecutter recipes
        wrapper.write(Types.VAR_INT, stoneCutterRecipes.size());
        for (final StoneCutterRecipe recipe : stoneCutterRecipes) {
            final HolderSet ingredient = toHolderSet(recipe.ingredient());
            wrapper.write(Types.HOLDER_SET, ingredient);
            writeItemDisplay(wrapper, recipe.result());
        }
    }

    private Item[] readRecipeInputs(final String key, final PacketWrapper wrapper) {
        // Collect inputs for new UPDATE_RECIPE
        final Item[] ingredient = readIngredient(wrapper);
        final IntSet ids = recipeInputs.computeIfAbsent(key, $ -> new IntOpenHashSet(ingredient.length));
        for (final Item item : ingredient) {
            ids.add(item.identifier());
        }
        return ingredient;
    }

    interface Recipe {
        int SLOT_DISPLAY_EMPTY = 0;
        int SLOT_DISPLAY_ANY_FUEL = 1;
        int SLOT_DISPLAY_ITEM = 3;
        int SLOT_DISPLAY_COMPOSITE = 7;

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

        default boolean showNotification() {
            return true;
        }

        int category();

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
                        Item[][] ingredients, Item result, boolean showNotification) implements Recipe {
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
                         Item result, int duration, float experience) implements Recipe {
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
            wrapper.write(Types.VAR_INT, duration);
            wrapper.write(Types.FLOAT, experience);
        }
    }

    record StoneCutterRecipe(String identifier, int group, Item[] ingredient, Item result) {

        public int recipeDisplayId() {
            return 3;
        }

        public void writeRecipeDisplay(final PacketWrapper wrapper) {
            writeIngredientDisplay(wrapper, ingredient);
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
        if (ingredient.length == 1) {
            writeItemDisplay(wrapper, ingredient[0]);
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
        wrapper.write(VersionedTypes.V1_21_2.item, item.copy());
    }

    private static void writeCraftingStationDisplay(final PacketWrapper wrapper) {
        wrapper.write(Types.VAR_INT, Recipe.SLOT_DISPLAY_EMPTY);
    }
}
