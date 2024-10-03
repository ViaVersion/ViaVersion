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

import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.rewriter.RecipeRewriter1_20_3;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPacket1_21;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.List;

final class RecipeRewriter1_21_2 extends RecipeRewriter1_20_3<ClientboundPacket1_21> {

    private final List<ShapelessRecipe> shapelessRecipes = new ArrayList<>();
    private final List<ShapedRecipe> shapedRecipes = new ArrayList<>();
    private final List<SmithingRecipe> smithingRecipes = new ArrayList<>();
    private final List<StoneCutterRecipe> stoneCutterRecipes = new ArrayList<>();
    private final List<SimpleRecipe> simpleRecipes = new ArrayList<>();
    private final Object2IntMap<String> recipeGroups = new Object2IntOpenHashMap<>();
    // TODO
    private final IntList smithingAddition = new IntArrayList();
    private final IntList smithingTemplate = new IntArrayList();
    private final IntList smithingBase = new IntArrayList();
    private final IntList furnaceInput = new IntArrayList();
    private final IntList smokerInput = new IntArrayList();
    private final IntList blastFurnaceInput = new IntArrayList();
    private final IntList campfireInput = new IntArrayList();

    record SimpleRecipe(int category) {
    }

    record ShapelessRecipe(int group, int category, HolderSet[] ingredients, Item result) {
    }

    record ShapedRecipe(int group, int category, int width, int height, HolderSet[] ingredients, Item result) {
    }

    record StoneCutterRecipe(int group, HolderSet ingredient, Item result) {
    }

    record SmithingRecipe(HolderSet ingredient, Item result) {
    }

    RecipeRewriter1_21_2(final Protocol<ClientboundPacket1_21, ?, ?, ?> protocol) {
        super(protocol);
        // TODO ?
        recipeHandlers.put("smelting", wrapper -> handleSmelting(wrapper, furnaceInput));
        recipeHandlers.put("blasting", wrapper -> handleSmelting(wrapper, blastFurnaceInput));
        recipeHandlers.put("smoking", wrapper -> handleSmelting(wrapper, smokerInput));
        recipeHandlers.put("campfire_cooking", wrapper -> handleSmelting(wrapper, campfireInput));
        recipeGroups.defaultReturnValue(-1);
    }

    private HolderSet readIngredient(final PacketWrapper wrapper) {
        // Ingredients are no longer full items, already store them as just holder sets
        final Item[] items = wrapper.read(itemArrayType());
        final int[] ids = new int[items.length];
        final MappingData mappings = protocol.getMappingData();
        for (int i = 0; i < items.length; i++) {
            final Item item = items[i];
            ids[i] = mappings.getNewItemId(item.identifier());
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

    @Override
    public void handleSimpleRecipe(final PacketWrapper wrapper) {
        final int category = wrapper.read(Types.VAR_INT);
        simpleRecipes.add(new SimpleRecipe(category));
    }

    @Override
    public void handleStonecutting(final PacketWrapper wrapper) {
        final int group = recipeGroupId(wrapper.read(Types.STRING));
        final HolderSet ingredient = readIngredient(wrapper);
        final Item result = rewrite(wrapper.user(), wrapper.read(itemType()));
        stoneCutterRecipes.add(new StoneCutterRecipe(group, ingredient, result));
    }

    private int recipeGroupId(final String recipeGroup) {
        final int size = recipeGroups.size();
        final int value = recipeGroups.putIfAbsent(recipeGroup, size);
        return value != -1 ? value : size;
    }

    @Override
    public void handleSmithingTransform(final PacketWrapper wrapper) {
        final IntList template = new IntArrayList();
        readIngredientToList(wrapper, template);
        smithingTemplate.addAll(template);
        readIngredientToList(wrapper, smithingBase);
        readIngredientToList(wrapper, smithingAddition);
        final Item result = rewrite(wrapper.user(), wrapper.read(itemType()));
        smithingRecipes.add(new SmithingRecipe(HolderSet.of(template.toIntArray()), result)); // TODO ?
    }

    @Override
    public void handleSmithingTrim(final PacketWrapper wrapper) {
        readIngredientToList(wrapper, smithingTemplate);
        readIngredientToList(wrapper, smithingBase);
        readIngredientToList(wrapper, smithingAddition);
    }

    @Override
    public void handleCraftingShaped(final PacketWrapper wrapper) {
        final int group = recipeGroupId(wrapper.read(Types.STRING));
        final int category = wrapper.read(Types.VAR_INT);
        final int width = wrapper.read(Types.VAR_INT);
        final int height = wrapper.read(Types.VAR_INT);
        final int ingredientsSize = width * height;
        final HolderSet[] ingredients = new HolderSet[ingredientsSize];
        for (int i = 0; i < ingredientsSize; i++) {
            ingredients[i] = readIngredient(wrapper);
        }
        final Item result = rewrite(wrapper.user(), wrapper.read(itemType()));
        shapedRecipes.add(new ShapedRecipe(group, category, width, height, ingredients, result));

        wrapper.read(Types.BOOLEAN); // Show notification
    }

    @Override
    public void handleCraftingShapeless(final PacketWrapper wrapper) {
        final int group = recipeGroupId(wrapper.read(Types.STRING));
        final int category = wrapper.read(Types.VAR_INT);
        final int ingredientsSize = wrapper.read(Types.VAR_INT);
        final HolderSet[] ingredients = new HolderSet[ingredientsSize];
        for (int i = 0; i < ingredientsSize; i++) {
            ingredients[i] = readIngredient(wrapper);
        }

        final Item result = rewrite(wrapper.user(), wrapper.read(itemType()));
        shapelessRecipes.add(new ShapelessRecipe(group, category, ingredients, result));
    }

    private void handleSmelting(final PacketWrapper wrapper, final IntCollection list) {
        final int group = recipeGroupId(wrapper.read(Types.STRING));
        final int category = wrapper.read(Types.VAR_INT);
        readIngredientToList(wrapper, list);
        wrapper.read(itemType()); // Result

        wrapper.read(Types.FLOAT); // EXP
        wrapper.read(Types.VAR_INT); // Cooking time
    }
}
