package us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import us.myles.ViaVersion.util.GsonUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class MappingData {
    public static Map<Integer, Integer> oldToNewBlocks = new HashMap<>();
    public static Map<Integer, Integer> oldToNewItems = new HashMap<>();
    public static Map<Integer, Integer> newToOldItems = new HashMap<>();
    public static Map<String, Integer[]> blockTags = new HashMap<>();
    public static Map<String, Integer[]> itemTags = new HashMap<>();
    public static Map<String, Integer[]> fluidTags = new HashMap<>();
    public static BiMap<Short, String> oldEnchantmentsIds = HashBiMap.create();
    public static Map<Integer, Integer> oldToNewSounds = new HashMap<>();

    public static void init() {
        JsonObject mapping1_12 = loadData("mapping-1.12.json");
        JsonObject mapping1_13 = loadData("mapping-1.13.json");

        // TODO: Remove how verbose this is
        System.out.println("Loading block mapping...");
        mapIdentifiers(oldToNewBlocks, mapping1_12.getAsJsonObject("blocks"), mapping1_13.getAsJsonObject("blocks"));
        System.out.println("Loading item mapping...");
        mapIdentifiers(oldToNewItems, mapping1_12.getAsJsonObject("items"), mapping1_13.getAsJsonObject("items"));
        System.out.println("Loading new to old item mapping...");
        mapIdentifiers(newToOldItems, mapping1_13.getAsJsonObject("items"), mapping1_12.getAsJsonObject("items"));
        System.out.println("Loading new tags...");
        loadTags(blockTags, mapping1_13.getAsJsonObject("block_tags"));
        loadTags(itemTags, mapping1_13.getAsJsonObject("item_tags"));
        loadTags(fluidTags, mapping1_13.getAsJsonObject("fluid_tags"));
        System.out.println("Loading enchantments...");
        loadEnchantments(oldEnchantmentsIds, mapping1_12.getAsJsonObject("enchantments"));
        System.out.println("Loading sound mapping...");
        mapIdentifiers(oldToNewSounds, mapping1_12.getAsJsonArray("sounds"), mapping1_13.getAsJsonArray("sounds"));
    }

    private static void mapIdentifiers(Map<Integer, Integer> output, JsonObject oldIdentifiers, JsonObject newIdentifiers) {
        for (Map.Entry<String, JsonElement> entry : oldIdentifiers.entrySet()) {
            Map.Entry<String, JsonElement> value = findValue(newIdentifiers, entry.getValue().getAsString());
            if (value == null) {
                System.out.println("No key for " + entry.getValue() + " :( ");
                continue;
            }
            output.put(Integer.parseInt(entry.getKey()), Integer.parseInt(value.getKey()));
        }
    }

    private static Map.Entry<String, JsonElement> findValue(JsonObject object, String needle) {
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            String value = entry.getValue().getAsString();
            if (value.equals(needle)) {
                return entry;
            }
        }
        return null;
    }

    private static void mapIdentifiers(Map<Integer, Integer> output, JsonArray oldIdentifiers, JsonArray newIdentifiers) {
        for (int i = 0; i < oldIdentifiers.size(); i++) {
            JsonElement v = oldIdentifiers.get(i);
            Integer index = findIndex(newIdentifiers, v.getAsString());
            if (index == null) {
                System.out.println("No key for " + v + " :( ");
                continue;
            }
            output.put(i, index);
        }
    }

    private static Integer findIndex(JsonArray array, String value) {
        for (int i = 0; i < array.size(); i++) {
            JsonElement v = array.get(i);
            if (v.getAsString().equals(value)) {
                return i;
            }
        }
        return null;
    }

    private static void loadTags(Map<String, Integer[]> output, JsonObject newTags) {
        for (Map.Entry<String, JsonElement> entry : newTags.entrySet()) {
            JsonArray ids = entry.getValue().getAsJsonArray();
            Integer[] idsArray = new Integer[ids.size()];
            for (int i = 0; i < ids.size(); i++) {
                idsArray[i] = ids.get(i).getAsInt();
            }
            output.put(entry.getKey(), idsArray);
        }
    }

    public static void loadEnchantments(Map<Short, String> output, JsonObject enchantments) {
        for (Map.Entry<String, JsonElement> enchantment : enchantments.entrySet()) {
            output.put(Short.parseShort(enchantment.getKey()), enchantment.getValue().getAsString());
        }
    }

    public static JsonObject loadData(String name) {
        InputStream stream = MappingData.class.getClassLoader().getResourceAsStream("assets/viaversion/data/" + name);
        InputStreamReader reader = new InputStreamReader(stream);
        try {
            JsonObject jsonObject = GsonUtil.getGson().fromJson(reader, JsonObject.class);
            return jsonObject;
        } finally {
            try {
                reader.close();
            } catch (IOException ignored) {
                // Ignored
            }
        }
    }
}
