/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_12_2to1_13.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.util.GsonUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RecipeData {
    public static Map<String, Recipe> recipes;

    public static void init() {
        JsonObject object;
        InputStream stream = MappingData1_13.class.getClassLoader()
            .getResourceAsStream("assets/viaversion/data/itemrecipes1_12_2to1_13.json");
        try (InputStreamReader reader = new InputStreamReader(stream)) {
            object = GsonUtil.getGson().fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        recipes = new LinkedHashMap<>();

        // Shaped and shapeless sections are keyed by their legacy id
        for (Map.Entry<String, JsonElement> entry : object.getAsJsonObject("shapeless").entrySet()) {
            JsonObject recipe = entry.getValue().getAsJsonObject();
            DataItem[][] ingredients = ingredients(recipe.getAsJsonArray("ingredients"));
            recipes.put("viaversion:legacy/" + entry.getKey(), new Recipe("crafting_shapeless", group(recipe), 0, 0, 0, 0, null, ingredients, result(recipe)));
        }

        // Ingredients are stored as grid rows, so the width and height are implicit
        for (Map.Entry<String, JsonElement> entry : object.getAsJsonObject("shaped").entrySet()) {
            JsonObject recipe = entry.getValue().getAsJsonObject();
            JsonArray grid = recipe.getAsJsonArray("grid");
            int height = grid.size();
            int width = grid.get(0).getAsJsonArray().size();
            DataItem[][] ingredients = new DataItem[width * height][];
            int slot = 0;
            for (JsonElement row : grid) {
                for (JsonElement element : row.getAsJsonArray()) {
                    ingredients[slot++] = ingredient(element);
                }
            }
            recipes.put("viaversion:legacy/" + entry.getKey(), new Recipe("crafting_shaped", group(recipe), width, height, 0, 0, null, ingredients, result(recipe)));
        }

        for (Map.Entry<String, JsonElement> entry : object.getAsJsonObject("smelting").entrySet()) {
            JsonObject recipe = entry.getValue().getAsJsonObject();
            float experience = recipe.get("experience").getAsFloat();
            recipes.put(entry.getKey(), new Recipe("smelting", group(recipe), 0, 0, experience, 200, ingredient(recipe.get("ingredient")), null, result(recipe)));
        }

        // Name to type suffix
        for (Map.Entry<String, JsonElement> entry : object.getAsJsonObject("special").entrySet()) {
            recipes.put(entry.getKey(), new Recipe("crafting_special_" + entry.getValue().getAsString(), "", 0, 0, 0, 0, null, null, null));
        }
    }

    private static String group(JsonObject recipe) {
        JsonElement group = recipe.get("group");
        return group != null ? group.getAsString() : "";
    }

    private static DataItem result(JsonObject recipe) {
        JsonElement count = recipe.get("count");
        return new DataItem(recipe.get("result").getAsInt(), count != null ? count.getAsByte() : (byte) 1, null);
    }

    // null for an empty slot, a single item id, or an array of item id choices
    private static DataItem[] ingredient(JsonElement element) {
        if (element.isJsonNull()) {
            return new DataItem[0];
        }
        if (element.isJsonArray()) {
            JsonArray choices = element.getAsJsonArray();
            DataItem[] items = new DataItem[choices.size()];
            for (int i = 0; i < items.length; i++) {
                items[i] = new DataItem(choices.get(i).getAsInt(), (byte) 1, null);
            }
            return items;
        }

        return new DataItem[]{new DataItem(element.getAsInt(), (byte) 1, null)};
    }

    private static DataItem[][] ingredients(JsonArray slots) {
        DataItem[][] items = new DataItem[slots.size()][];
        for (int i = 0; i < items.length; i++) {
            items[i] = ingredient(slots.get(i));
        }
        return items;
    }

    public record Recipe(String type, String group, int width, int height, float experience, int cookingTime,
                         DataItem[] ingredient, DataItem[][] ingredients, DataItem result) {
    }
}
