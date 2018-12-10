package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data;

import com.google.gson.reflect.TypeToken;
import lombok.Data;
import lombok.NonNull;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.util.GsonUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class RecipeData {
    public static Map<String, Recipe> recipes;

    public static void init() {
        InputStream stream = MappingData.class.getClassLoader()
                .getResourceAsStream("assets/viaversion/data/itemrecipes1_12_2to1_13.json");
        InputStreamReader reader = new InputStreamReader(stream);
        try {
            recipes = GsonUtil.getGson().fromJson(
                    reader,
                    new TypeToken<Map<String, Recipe>>() {
                    }.getType()
            );
        } finally {
            try {
                reader.close();
            } catch (IOException ignored) {
                // Ignored
            }
        }
    }

    @Data
    public static class Recipe {
        @NonNull
        private String type;
        private String group;
        private int width;
        private int height;
        private float experience;
        private int cookingTime;
        private Item[] ingredient;
        private Item[][] ingredients;
        private Item result;
    }
}