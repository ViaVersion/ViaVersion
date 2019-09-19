package us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.MappingDataLoader;
import us.myles.ViaVersion.api.data.Mappings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MappingData {
    public static final BiMap<Integer, Integer> oldToNewItems = HashBiMap.create();
    public static Mappings blockStateMappings;
    public static Mappings blockMappings;
    public static Mappings soundMappings;
    public static Set<Integer> motionBlocking;
    public static Set<Integer> nonFullBlocks;

    public static void init() {
        JsonObject mapping1_13_2 = MappingDataLoader.loadData("mapping-1.13.2.json");
        JsonObject mapping1_14 = MappingDataLoader.loadData("mapping-1.14.json");

        Via.getPlatform().getLogger().info("Loading 1.13.2 -> 1.14 blockstate mapping...");
        blockStateMappings = new Mappings(mapping1_13_2.getAsJsonObject("blockstates"), mapping1_14.getAsJsonObject("blockstates"));
        Via.getPlatform().getLogger().info("Loading 1.13.2 -> 1.14 block mapping...");
        blockMappings = new Mappings(mapping1_13_2.getAsJsonObject("blocks"), mapping1_14.getAsJsonObject("blocks"));
        Via.getPlatform().getLogger().info("Loading 1.13.2 -> 1.14 item mapping...");
        MappingDataLoader.mapIdentifiers(oldToNewItems, mapping1_13_2.getAsJsonObject("items"), mapping1_14.getAsJsonObject("items"));
        Via.getPlatform().getLogger().info("Loading 1.13.2 -> 1.14 sound mapping...");
        soundMappings = new Mappings(mapping1_13_2.getAsJsonArray("sounds"), mapping1_14.getAsJsonArray("sounds"));

        Via.getPlatform().getLogger().info("Loading 1.14 blockstates...");
        JsonObject blockStates = mapping1_14.getAsJsonObject("blockstates");
        Map<String, Integer> blockStateMap = new HashMap<>(blockStates.entrySet().size());
        for (Map.Entry<String, JsonElement> entry : blockStates.entrySet()) {
            blockStateMap.put(entry.getValue().getAsString(), Integer.parseInt(entry.getKey()));
        }

        Via.getPlatform().getLogger().info("Loading 1.14 heightmap data...");
        JsonObject heightMapData = MappingDataLoader.loadData("heightMapData-1.14.json");
        JsonArray motionBlocking = heightMapData.getAsJsonArray("MOTION_BLOCKING");
        us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data.MappingData.motionBlocking = new HashSet<>(motionBlocking.size());
        for (JsonElement blockState : motionBlocking) {
            String key = blockState.getAsString();
            Integer id = blockStateMap.get(key);
            if (id == null) {
                Via.getPlatform().getLogger().warning("Unknown blockstate " + key + " :(");
            } else {
                us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data.MappingData.motionBlocking.add(id);
            }
        }

        if (Via.getConfig().isNonFullBlockLightFix()) {
            nonFullBlocks = new HashSet<>();
            for (Map.Entry<String, JsonElement> blockstates : mapping1_13_2.getAsJsonObject("blockstates").entrySet()) {
                final String state = blockstates.getValue().getAsString();
                if (state.contains("_slab") || state.contains("_stairs") || state.contains("_wall["))
                    nonFullBlocks.add(blockStateMappings.getNewId(Integer.parseInt(blockstates.getKey())));
            }
            nonFullBlocks.add(blockStateMappings.getNewId(8163)); // grass path
            for (int i = 3060; i <= 3067; i++) { // farmland
                nonFullBlocks.add(blockStateMappings.getNewId(i));
            }
        }
    }
}
