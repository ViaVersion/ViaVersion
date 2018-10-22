package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.util.GsonUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MappingData {
    public static BiMap<Integer, Integer> oldToNewItems = HashBiMap.create();
    public static Map<String, Integer[]> blockTags = new HashMap<>();
    public static Map<String, Integer[]> itemTags = new HashMap<>();
    public static Map<String, Integer[]> fluidTags = new HashMap<>();
    public static BiMap<Short, String> oldEnchantmentsIds = HashBiMap.create();
    public static EnchantmentMappings enchantmentMappings;
    public static SoundMappings soundMappings;
    public static BlockMappings blockMappings;

    public static void init() {
        JsonObject mapping1_12 = loadData("mapping-1.12.json");
        JsonObject mapping1_13 = loadData("mapping-1.13.json");

        Via.getPlatform().getLogger().info("Loading block mapping...");
        blockMappings = new BlockMappingsShortArray(mapping1_12.getAsJsonObject("blocks"), mapping1_13.getAsJsonObject("blocks"));
        Via.getPlatform().getLogger().info("Loading item mapping...");
        mapIdentifiers(oldToNewItems, mapping1_12.getAsJsonObject("items"), mapping1_13.getAsJsonObject("items"));
        Via.getPlatform().getLogger().info("Loading new tags...");
        loadTags(blockTags, mapping1_13.getAsJsonObject("block_tags"));
        loadTags(itemTags, mapping1_13.getAsJsonObject("item_tags"));
        loadTags(fluidTags, mapping1_13.getAsJsonObject("fluid_tags"));
        Via.getPlatform().getLogger().info("Loading enchantments...");
        loadEnchantments(oldEnchantmentsIds, mapping1_12.getAsJsonObject("enchantments"));
        enchantmentMappings = new EnchantmentMappingByteArray(mapping1_12.getAsJsonObject("enchantments"), mapping1_13.getAsJsonObject("enchantments"));
        Via.getPlatform().getLogger().info("Loading sound mapping...");
        soundMappings = new SoundMappingShortArray(mapping1_12.getAsJsonArray("sounds"), mapping1_13.getAsJsonArray("sounds"));
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
                if (!Via.getConfig().isSuppress1_13ConversionErrors() || Via.getManager().isDebug()) {
                    Via.getPlatform().getLogger().warning("No key for " + entry.getValue() + " :( ");
                }
                continue;
            }
            output.put(Integer.parseInt(entry.getKey()), Integer.parseInt(value.getKey()));
        }
    }

    private static void mapIdentifiers(short[] output, JsonObject oldIdentifiers, JsonObject newIdentifiers) {
        for (Map.Entry<String, JsonElement> entry : oldIdentifiers.entrySet()) {
            Map.Entry<String, JsonElement> value = findValue(newIdentifiers, entry.getValue().getAsString());
            if (value == null) {
                if (!Via.getConfig().isSuppress1_13ConversionErrors() || Via.getManager().isDebug()) {
                    Via.getPlatform().getLogger().warning("No key for " + entry.getValue() + " :( ");
                }
                continue;
            }
            output[Integer.parseInt(entry.getKey())] = Short.parseShort(value.getKey());
        }
    }

    private static void mapIdentifiers(byte[] output, JsonObject oldIdentifiers, JsonObject newIdentifiers) {
        for (Map.Entry<String, JsonElement> entry : oldIdentifiers.entrySet()) {
            Map.Entry<String, JsonElement> value = findValue(newIdentifiers, entry.getValue().getAsString());
            if (value == null) {
                Via.getPlatform().getLogger().warning("No key for " + entry.getValue() + " :( ");
                continue;
            }
            output[Integer.parseInt(entry.getKey())] = Byte.parseByte(value.getKey());
        }
    }

    private static void mapIdentifiers(short[] output, JsonArray oldIdentifiers, JsonArray newIdentifiers) {
        for (int i = 0; i < oldIdentifiers.size(); i++) {
            JsonElement v = oldIdentifiers.get(i);
            Integer index = findIndex(newIdentifiers, v.getAsString());
            if (index == null) {
                if (!Via.getConfig().isSuppress1_13ConversionErrors() || Via.getManager().isDebug()) {
                    Via.getPlatform().getLogger().warning("No key for " + v + " :( ");
                }
                continue;
            }
            output[i] = index.shortValue();
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
        int getNewBlock(int old);
    }

    private static class BlockMappingsShortArray implements BlockMappings {
        private short[] oldToNew = new short[4084];

        private BlockMappingsShortArray(JsonObject mapping1_12, JsonObject mapping1_13) {
            Arrays.fill(oldToNew, (short) -1);
            mapIdentifiers(oldToNew, mapping1_12, mapping1_13);
        }

        @Override
        public int getNewBlock(int old) {
            return old >= 0 && old < oldToNew.length ? oldToNew[old] : -1;
        }
    }

    public interface SoundMappings {
        int getNewSound(int old);
    }

    private static class SoundMappingShortArray implements SoundMappings {
        private short[] oldToNew = new short[662];

        private SoundMappingShortArray(JsonArray mapping1_12, JsonArray mapping1_13) {
            Arrays.fill(oldToNew, (short) -1);
            mapIdentifiers(oldToNew, mapping1_12, mapping1_13);
        }

        @Override
        public int getNewSound(int old) {
            return old >= 0 && old < oldToNew.length ? oldToNew[old] : -1;
        }
    }

    public interface EnchantmentMappings {
        int getNewEnchantment(int old);
    }

    private static class EnchantmentMappingByteArray implements EnchantmentMappings {
        private byte[] oldToNew = new byte[72];

        private EnchantmentMappingByteArray(JsonObject m1_12, JsonObject m1_13) {
            Arrays.fill(oldToNew, (byte) -1);
            mapIdentifiers(oldToNew, m1_12, m1_13);
        }

        @Override
        public int getNewEnchantment(int old) {
            return old >= 0 && old < oldToNew.length ? oldToNew[old] : -1;
        }
    }
}
