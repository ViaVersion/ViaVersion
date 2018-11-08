package us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data;

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
import java.util.Map;

public class MappingData {
    public static BiMap<Integer, Integer> oldToNewItems = HashBiMap.create();
    public static BlockMappings blockStateMappings;
    public static BlockMappings blockMappings;
    public static SoundMappings soundMappings;

    public static void init() {
        JsonObject mapping1_13_2 = loadData("mapping-1.13.2.json");
        JsonObject mapping1_14 = loadData("mapping-1.14.json");

        Via.getPlatform().getLogger().info("Loading 1.13.2 -> 1.14 blockstate mapping...");
        blockStateMappings = new BlockMappingsShortArray(mapping1_13_2.getAsJsonObject("blockstates"), mapping1_14.getAsJsonObject("blockstates"));
        Via.getPlatform().getLogger().info("Loading 1.13.2 -> 1.14 block mapping...");
        blockMappings = new BlockMappingsShortArray(mapping1_13_2.getAsJsonObject("blocks"), mapping1_14.getAsJsonObject("blocks"));
        Via.getPlatform().getLogger().info("Loading 1.13.2 -> 1.14 item mapping...");
        mapIdentifiers(oldToNewItems, mapping1_13_2.getAsJsonObject("items"), mapping1_14.getAsJsonObject("items"));
        Via.getPlatform().getLogger().info("Loading 1.13.2 -> 1.14 sound mapping...");
        soundMappings = new SoundMappingShortArray(mapping1_13_2.getAsJsonArray("sounds"), mapping1_14.getAsJsonArray("sounds"));
    }

    public static JsonObject loadData(String name) {
        InputStream stream = MappingData.class.getClassLoader().getResourceAsStream("assets/viaversion/data/" + name);
        InputStreamReader reader = new InputStreamReader(stream);
        try {
            return GsonUtil.getGson().fromJson(reader, JsonObject.class);
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

    public interface SoundMappings {
        int getNewSound(int old);
    }

    private static class SoundMappingShortArray implements SoundMappings {
        private short[] oldToNew;

        private SoundMappingShortArray(JsonArray mapping1_13_2, JsonArray mapping1_14) {
            oldToNew = new short[mapping1_13_2.size()];
            Arrays.fill(oldToNew, (short) -1);
            mapIdentifiers(oldToNew, mapping1_13_2, mapping1_14);
        }

        @Override
        public int getNewSound(int old) {
            return old >= 0 && old < oldToNew.length ? oldToNew[old] : -1;
        }
    }

    public interface BlockMappings {
        int getNewBlock(int old);
    }

    private static class BlockMappingsShortArray implements BlockMappings {
        private short[] oldToNew;

        private BlockMappingsShortArray(JsonObject mapping1_13_2, JsonObject mapping1_14) {
            oldToNew = new short[mapping1_13_2.entrySet().size()];
            Arrays.fill(oldToNew, (short) -1);
            mapIdentifiers(oldToNew, mapping1_13_2, mapping1_14);
        }

        @Override
        public int getNewBlock(int old) {
            return old >= 0 && old < oldToNew.length ? oldToNew[old] : -1;
        }
    }
}
