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
import java.util.HashMap;
import java.util.Map;

public class MappingData {
    public static final BiMap<Integer, Integer> oldToNewItems = HashBiMap.create();
    public static final Map<String, Integer[]> blockTags = new HashMap<>();
    public static final Map<String, Integer[]> itemTags = new HashMap<>();
    public static final Map<String, Integer[]> fluidTags = new HashMap<>();
    public static final BiMap<Short, String> oldEnchantmentsIds = HashBiMap.create();
    public static final Map<String, String> translateMapping = new HashMap<>();
    public static final Map<String, String> mojangTranslation = new HashMap<>();
    public static final BiMap<String, String> channelMappings = HashBiMap.create(); // 1.12->1.13
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
        enchantmentMappings = new Mappings(72, mapping1_12.getAsJsonObject("enchantments"), mapping1_13.getAsJsonObject("enchantments"));
        Via.getPlatform().getLogger().info("Loading 1.12.2 -> 1.13 sound mapping...");
        soundMappings = new Mappings(662, mapping1_12.getAsJsonArray("sounds"), mapping1_13.getAsJsonArray("sounds"));
        Via.getPlatform().getLogger().info("Loading 1.12.2 -> 1.13 plugin channel mappings...");

        JsonObject object = MappingDataLoader.loadFromDataDir("channelmappings-1.13.json");
        if (object != null) {
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                String oldChannel = entry.getKey();
                String newChannel = entry.getValue().getAsString();
                if (!isValid1_13Channel(newChannel)) {
                    Via.getPlatform().getLogger().warning("Channel '" + newChannel + "' is not a valid 1.13 plugin channel, please check your configuration!");
                    continue;
                }
                channelMappings.put(oldChannel, newChannel);
            }
        }

        Via.getPlatform().getLogger().info("Loading translation mappping");
        Map<String, String> translateData = GsonUtil.getGson().fromJson(
                new InputStreamReader(MappingData.class.getClassLoader().getResourceAsStream("assets/viaversion/data/mapping-lang-1.12-1.13.json")),
                new TypeToken<Map<String, String>>() {
                }.getType());
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
                if (!translateData.containsKey(key)) {
                    String translation = keyAndTranslation[1].replaceAll("%(\\d\\$)?d", "%$1s");
                    mojangTranslation.put(key, translation);
                } else {
                    String dataValue = translateData.get(key);
                    if (dataValue != null) {
                        translateMapping.put(key, dataValue);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String validateNewChannel(String newId) {
        if (!isValid1_13Channel(newId)) {
            return null; // Not valid
        }
        int separatorIndex = newId.indexOf(':');
        // Vanilla parses ``:`` and ```` as ``minecraft:`` (also ensure there's enough space)
        if ((separatorIndex == -1 || separatorIndex == 0) && newId.length() <= 10) {
            newId = "minecraft:" + newId;
        }
        return newId;
    }

    public static boolean isValid1_13Channel(String channelId) {
        return channelId.matches("([0-9a-z_.-]+):([0-9a-z_/.-]+)");
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

    private static class BlockMappingsShortArray extends Mappings {

        private BlockMappingsShortArray(JsonObject mapping1_12, JsonObject mapping1_13) {
            super(4084, mapping1_12, mapping1_13);

            // Map minecraft:snow[layers=1] of 1.12 to minecraft:snow[layers=2] in 1.13
            if (Via.getConfig().isSnowCollisionFix()) {
                oldToNew[1248] = 3416;
            }

            // Remap infested blocks, as they are instantly breakabale in 1.13+ and can't be broken by those clients on older servers
            if (Via.getConfig().isInfestedBlocksFix()) {
                oldToNew[1552] = 1; // stone
                oldToNew[1553] = 14; // cobblestone
                oldToNew[1554] = 3983; // stone bricks
                oldToNew[1555] = 3984; // mossy stone bricks
                oldToNew[1556] = 3985; // cracked stone bricks
                oldToNew[1557] = 3986; // chiseled stone bricks
            }
        }
    }
}
