package us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.MappingDataLoader;
import us.myles.ViaVersion.api.data.Mappings;
import us.myles.ViaVersion.util.Int2IntBiMap;

import java.util.HashMap;
import java.util.Map;

public class MappingData {
    public static final Int2IntBiMap oldToNewItems = new Int2IntBiMap();
    public static Mappings blockStateMappings;
    public static Mappings blockMappings;
    public static Mappings soundMappings;
    public static IntSet motionBlocking;
    public static IntSet nonFullBlocks;

    public static void init() {
        Via.getPlatform().getLogger().info("Loading 1.13.2 -> 1.14 mappings...");
        JsonObject mapping1_13_2 = MappingDataLoader.loadData("mapping-1.13.2.json", true);
        JsonObject mapping1_14 = MappingDataLoader.loadData("mapping-1.14.json", true);

        oldToNewItems.defaultReturnValue(-1);
        blockStateMappings = new Mappings(mapping1_13_2.getAsJsonObject("blockstates"), mapping1_14.getAsJsonObject("blockstates"));
        blockMappings = new Mappings(mapping1_13_2.getAsJsonObject("blocks"), mapping1_14.getAsJsonObject("blocks"));
        MappingDataLoader.mapIdentifiers(oldToNewItems, mapping1_13_2.getAsJsonObject("items"), mapping1_14.getAsJsonObject("items"));
        soundMappings = new Mappings(mapping1_13_2.getAsJsonArray("sounds"), mapping1_14.getAsJsonArray("sounds"));

        JsonObject blockStates = mapping1_14.getAsJsonObject("blockstates");
        Map<String, Integer> blockStateMap = new HashMap<>(blockStates.entrySet().size());
        for (Map.Entry<String, JsonElement> entry : blockStates.entrySet()) {
            blockStateMap.put(entry.getValue().getAsString(), Integer.parseInt(entry.getKey()));
        }

        JsonObject heightMapData = MappingDataLoader.loadData("heightMapData-1.14.json");
        JsonArray motionBlocking = heightMapData.getAsJsonArray("MOTION_BLOCKING");
        MappingData.motionBlocking = new IntOpenHashSet(motionBlocking.size());
        for (JsonElement blockState : motionBlocking) {
            String key = blockState.getAsString();
            Integer id = blockStateMap.get(key);
            if (id == null) {
                Via.getPlatform().getLogger().warning("Unknown blockstate " + key + " :(");
            } else {
                MappingData.motionBlocking.add(id.intValue());
            }
        }

        if (Via.getConfig().isNonFullBlockLightFix()) {
            nonFullBlocks = new IntOpenHashSet(1611);
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
