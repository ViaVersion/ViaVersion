package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.io.CharStreams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.MappingDataLoader;
import us.myles.ViaVersion.api.data.Mappings;
import us.myles.ViaVersion.util.GsonUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MappingData {
    public static BiMap<Integer, Integer> oldToNewItems = HashBiMap.create();
    public static Map<String, Integer[]> blockTags = new HashMap<>();
    public static Map<String, Integer[]> itemTags = new HashMap<>();
    public static Map<String, Integer[]> fluidTags = new HashMap<>();
    public static BiMap<Short, String> oldEnchantmentsIds = HashBiMap.create();
    public static Map<String, String> translateMapping = new HashMap<>();
    public static Map<String, String> mojangTranslation = new HashMap<>();
    public static Mappings enchantmentMappings;
    public static Mappings soundMappings;
    public static Mappings blockMappings;

    public static void init() {
        JsonObject mapping1_12 = MappingDataLoader.loadData("mapping-1.12.json");
        JsonObject mapping1_13 = MappingDataLoader.loadData("mapping-1.13.json");

        Via.getPlatform().getLogger().info("Loading 1.12.2 -> 1.13 block mapping...");
        blockMappings = new BlockMappingsShortArray(mapping1_12.getAsJsonObject("blocks"), mapping1_13.getAsJsonObject("blocks"));
        Via.getPlatform().getLogger().info("Loading 1.12.2 -> 1.13 item mapping...");
        MappingDataLoader.mapIdentifiers(oldToNewItems, mapping1_12.getAsJsonObject("items"), mapping1_13.getAsJsonObject("items"));
        Via.getPlatform().getLogger().info("Loading new 1.13 tags...");
        loadTags(blockTags, mapping1_13.getAsJsonObject("block_tags"));
        loadTags(itemTags, mapping1_13.getAsJsonObject("item_tags"));
        loadTags(fluidTags, mapping1_13.getAsJsonObject("fluid_tags"));
        Via.getPlatform().getLogger().info("Loading 1.12.2 -> 1.13 enchantment mapping...");
        loadEnchantments(oldEnchantmentsIds, mapping1_12.getAsJsonObject("enchantments"));
        enchantmentMappings = new EnchantmentMappingByteArray(mapping1_12.getAsJsonObject("enchantments"), mapping1_13.getAsJsonObject("enchantments"));
        Via.getPlatform().getLogger().info("Loading 1.12.2 -> 1.13 sound mapping...");
        soundMappings = new SoundMappingShortArray(mapping1_12.getAsJsonArray("sounds"), mapping1_13.getAsJsonArray("sounds"));
        Via.getPlatform().getLogger().info("Loading translation mappping");
        translateMapping = new HashMap<>();
        Map<String, String> translateData = GsonUtil.getGson().fromJson(
                new InputStreamReader(
                        MappingData.class.getClassLoader()
                                .getResourceAsStream("assets/viaversion/data/mapping-lang-1.12-1.13.json")
                ),
                (new TypeToken<Map<String, String>>() {
                }).getType());
        try {
            String[] lines;
            try (Reader reader = new InputStreamReader(MappingData.class.getClassLoader()
                    .getResourceAsStream("mojang-translations/en_US.properties"), StandardCharsets.UTF_8)) {
                lines = CharStreams.toString(reader).split("\n");
            }
            for (String line : lines) {
                if (line.isEmpty()) continue;
                String[] keyAndTranslation = line.split("=", 2);
                if (keyAndTranslation.length != 2) continue;
                String key = keyAndTranslation[0];
                String translation = keyAndTranslation[1].replaceAll("%(\\d\\$)?d", "%$1s");
                if (!translateData.containsKey(key)) {
                    translateMapping.put(key, translation);
                } else {
                    String dataValue = translateData.get(keyAndTranslation[0]);
                    if (dataValue != null) {
                        translateMapping.put(key, dataValue);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
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

    private static void loadEnchantments(Map<Short, String> output, JsonObject enchantments) {
        for (Map.Entry<String, JsonElement> enchantment : enchantments.entrySet()) {
            output.put(Short.parseShort(enchantment.getKey()), enchantment.getValue().getAsString());
        }
    }

    private static class BlockMappingsShortArray implements Mappings {
        private short[] oldToNew = new short[4084];

        private BlockMappingsShortArray(JsonObject mapping1_12, JsonObject mapping1_13) {
            Arrays.fill(oldToNew, (short) -1);
            MappingDataLoader.mapIdentifiers(oldToNew, mapping1_12, mapping1_13);
            // Map minecraft:snow[layers=1] of 1.12 to minecraft:snow[layers=2] in 1.13
            if (Via.getConfig().isSnowCollisionFix()) {
                oldToNew[1248] = 3416;
            }
        }

        @Override
        public int getNewId(int old) {
            return old >= 0 && old < oldToNew.length ? oldToNew[old] : -1;
        }
    }

    private static class SoundMappingShortArray implements Mappings {
        private short[] oldToNew = new short[662];

        private SoundMappingShortArray(JsonArray mapping1_12, JsonArray mapping1_13) {
            Arrays.fill(oldToNew, (short) -1);
            MappingDataLoader.mapIdentifiers(oldToNew, mapping1_12, mapping1_13);
        }

        @Override
        public int getNewId(int old) {
            return old >= 0 && old < oldToNew.length ? oldToNew[old] : -1;
        }
    }

    private static class EnchantmentMappingByteArray implements Mappings {
        private byte[] oldToNew = new byte[72];

        private EnchantmentMappingByteArray(JsonObject m1_12, JsonObject m1_13) {
            Arrays.fill(oldToNew, (byte) -1);
            MappingDataLoader.mapIdentifiers(oldToNew, m1_12, m1_13);
        }

        @Override
        public int getNewId(int old) {
            return old >= 0 && old < oldToNew.length ? oldToNew[old] : -1;
        }
    }
}
