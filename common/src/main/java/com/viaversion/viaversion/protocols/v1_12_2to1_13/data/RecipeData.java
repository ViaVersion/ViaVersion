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

import com.google.gson.reflect.TypeToken;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.util.GsonUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public final class RecipeData {
    public static Map<String, Recipe> recipes;

    public static void init() {
        InputStream stream = MappingData1_13.class.getClassLoader()
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

    public record Recipe(String type, String group, int width, int height, float experience, int cookingTime,
                         DataItem[] ingredient, DataItem[][] ingredients, DataItem result) {
    }
}
