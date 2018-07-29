package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.util.GsonUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class MappingData {
    public static BiMap<Integer, Integer> oldToNewItems = HashBiMap.create();
    public static Map<String, Integer[]> blockTags = new HashMap<>();
    public static Map<String, Integer[]> itemTags = new HashMap<>();
    public static Map<String, Integer[]> fluidTags = new HashMap<>();
    public static BiMap<Short, String> oldEnchantmentsIds = HashBiMap.create();
    public static Map<Integer, Integer> oldToNewSounds = new HashMap<>();
    public static BlockMappings blockMappings;

    public static void init() {
        JsonObject mapping1_12 = loadData("mapping-1.12.json");
        JsonObject mapping1_13 = loadData("mapping-1.13.json");

        Via.getPlatform().getLogger().info("Loading block mapping...");
        try {
            Class.forName("io.netty.util.collection.IntObjectMap");
            blockMappings = new BMNettyCollections();
        } catch (ClassNotFoundException e) {
            blockMappings = new BMJDKCollections();
        }
        blockMappings.init(mapping1_12, mapping1_13);
        Via.getPlatform().getLogger().info("Loading item mapping...");
        mapIdentifiers(oldToNewItems, mapping1_12.getAsJsonObject("items"), mapping1_13.getAsJsonObject("items"));
        Via.getPlatform().getLogger().info("Loading new tags...");
        loadTags(blockTags, mapping1_13.getAsJsonObject("block_tags"));
        loadTags(itemTags, mapping1_13.getAsJsonObject("item_tags"));
        loadTags(fluidTags, mapping1_13.getAsJsonObject("fluid_tags"));
        Via.getPlatform().getLogger().info("Loading enchantments...");
        loadEnchantments(oldEnchantmentsIds, mapping1_12.getAsJsonObject("enchantments"));
        Via.getPlatform().getLogger().info("Loading sound mapping...");
        mapIdentifiers(oldToNewSounds, mapping1_12.getAsJsonArray("sounds"), mapping1_13.getAsJsonArray("sounds"));
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

    private static void mapIdentifiers(Map<Integer, Integer> output, JsonObject oldIdentifiers, JsonObject newIdentifiers) {
        for (Map.Entry<String, JsonElement> entry : oldIdentifiers.entrySet()) {
            Map.Entry<String, JsonElement> value = findValue(newIdentifiers, entry.getValue().getAsString());
            if (value == null) {
                Via.getPlatform().getLogger().warning("No key for " + entry.getValue() + " :( ");
                continue;
            }
            output.put(Integer.parseInt(entry.getKey()), Integer.parseInt(value.getKey()));
        }
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

    private static void mapIdentifiers(Map<Integer, Integer> output, JsonArray oldIdentifiers, JsonArray newIdentifiers) {
        for (int i = 0; i < oldIdentifiers.size(); i++) {
            JsonElement v = oldIdentifiers.get(i);
            Integer index = findIndex(newIdentifiers, v.getAsString());
            if (index == null) {
                Via.getPlatform().getLogger().warning("No key for " + v + " :( ");
                continue;
            }
            output.put(i, index);
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

    private static Integer findIndex(JsonArray array, String value) {
        for (int i = 0; i < array.size(); i++) {
            JsonElement v = array.get(i);
            if (v.getAsString().equals(value)) {
                return i;
            }
        }
        return null;
    }

    public interface BlockMappings {
        void init(JsonObject mapping1_12, JsonObject mapping1_13);

        Integer getNewBlock(int old);
    }

    private static class BMJDKCollections implements BlockMappings {
        private Map<Integer, Integer> oldToNew = new HashMap<>();

        @Override
        public void init(JsonObject mapping1_12, JsonObject mapping1_13) {
            mapIdentifiers(oldToNew, mapping1_12.getAsJsonObject("blocks"), mapping1_13.getAsJsonObject("blocks"));
        }

        @Override
        public Integer getNewBlock(int old) {
            return oldToNew.get(old);
        }
    }

    private static class BMNettyCollections implements BlockMappings {
        private IntObjectMap<Integer> oldToNew = new IntObjectHashMap<>();

        @Override
        public void init(JsonObject mapping1_12, JsonObject mapping1_13) {
            mapIdentifiers(oldToNew, mapping1_12.getAsJsonObject("blocks"), mapping1_13.getAsJsonObject("blocks"));
        }

        @Override
        public Integer getNewBlock(int old) {
            return oldToNew.get(old);
        }

        private static void mapIdentifiers(IntObjectMap<Integer> output, JsonObject oldIdentifiers, JsonObject newIdentifiers) {
            for (Map.Entry<String, JsonElement> entry : oldIdentifiers.entrySet()) {
                Map.Entry<String, JsonElement> value = findValue(newIdentifiers, entry.getValue().getAsString());
                if (value == null) {
                    Via.getPlatform().getLogger().warning("No key for " + entry.getValue() + " :( ");
                    continue;
                }
                output.put(Integer.parseInt(entry.getKey()), Integer.parseInt(value.getKey()));
            }
        }
    }
}
