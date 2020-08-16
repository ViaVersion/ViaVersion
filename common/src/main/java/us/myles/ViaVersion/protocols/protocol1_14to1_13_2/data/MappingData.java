package us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.MappingDataLoader;

import java.util.HashMap;
import java.util.Map;

public class MappingData extends us.myles.ViaVersion.api.data.MappingData {
    private IntSet motionBlocking;
    private IntSet nonFullBlocks;

    public MappingData() {
        super("1.13.2", "1.14");
    }

    @Override
    public void loadExtras(JsonObject oldMappings, JsonObject newMappings, JsonObject diffMappings) {
        JsonObject blockStates = newMappings.getAsJsonObject("blockstates");
        Map<String, Integer> blockStateMap = new HashMap<>(blockStates.entrySet().size());
        for (Map.Entry<String, JsonElement> entry : blockStates.entrySet()) {
            blockStateMap.put(entry.getValue().getAsString(), Integer.parseInt(entry.getKey()));
        }

        JsonObject heightMapData = MappingDataLoader.loadData("heightMapData-1.14.json");
        JsonArray motionBlocking = heightMapData.getAsJsonArray("MOTION_BLOCKING");
        this.motionBlocking = new IntOpenHashSet(motionBlocking.size(), 1F);
        for (JsonElement blockState : motionBlocking) {
            String key = blockState.getAsString();
            Integer id = blockStateMap.get(key);
            if (id == null) {
                Via.getPlatform().getLogger().warning("Unknown blockstate " + key + " :(");
            } else {
                this.motionBlocking.add(id.intValue());
            }
        }

        if (Via.getConfig().isNonFullBlockLightFix()) {
            nonFullBlocks = new IntOpenHashSet(1611, 1F);
            for (Map.Entry<String, JsonElement> blockstates : oldMappings.getAsJsonObject("blockstates").entrySet()) {
                final String state = blockstates.getValue().getAsString();
                if (state.contains("_slab") || state.contains("_stairs") || state.contains("_wall[")) {
                    nonFullBlocks.add(blockStateMappings.getNewId(Integer.parseInt(blockstates.getKey())));
                }
            }
            nonFullBlocks.add(blockStateMappings.getNewId(8163)); // grass path
            for (int i = 3060; i <= 3067; i++) { // farmland
                nonFullBlocks.add(blockStateMappings.getNewId(i));
            }
        }
    }

    public IntSet getMotionBlocking() {
        return motionBlocking;
    }

    public IntSet getNonFullBlocks() {
        return nonFullBlocks;
    }
}
