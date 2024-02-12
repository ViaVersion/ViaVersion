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
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data;

import com.google.gson.reflect.TypeToken;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.util.GsonUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class RecipeData {
    public static Map<String, Recipe> recipes;

    public static void init() {
        InputStream stream = MappingData.class.getClassLoader()
                .getResourceAsStream("assets/viaversion/data/itemrecipes1_12_2to1_13.json");
        try (InputStreamReader reader = new InputStreamReader(stream)) {
            recipes = GsonUtil.getGson().fromJson(
                    reader,
                    new TypeToken<Map<String, Recipe>>() {
                    }.getType()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Ignored
    }

    public static class Recipe {
        private String type;
        private String group;
        private int width;
        private int height;
        private float experience;
        private int cookingTime;
        private DataItem[] ingredient;
        private DataItem[][] ingredients;
        private DataItem result;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public float getExperience() {
            return experience;
        }

        public void setExperience(float experience) {
            this.experience = experience;
        }

        public int getCookingTime() {
            return cookingTime;
        }

        public void setCookingTime(int cookingTime) {
            this.cookingTime = cookingTime;
        }

        public DataItem[] getIngredient() {
            return ingredient;
        }

        public void setIngredient(DataItem[] ingredient) {
            this.ingredient = ingredient;
        }

        public DataItem[][] getIngredients() {
            return ingredients;
        }

        public void setIngredients(DataItem[][] ingredients) {
            this.ingredients = ingredients;
        }

        public DataItem getResult() {
            return result;
        }

        public void setResult(DataItem result) {
            this.result = result;
        }
    }
}