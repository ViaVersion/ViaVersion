package us.myles.ViaVersion.protocols.protocol1_16to1_15_2.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.MappingDataLoader;
import us.myles.ViaVersion.api.data.Mappings;

public class MappingData {
    public static BiMap<Integer, Integer> oldToNewItems = HashBiMap.create();
    public static Mappings blockMappings;
    public static Mappings blockStateMappings;
    public static Mappings soundMappings;

    public static void init() {
        JsonObject diffmapping = MappingDataLoader.loadData("mappingdiff-1.15to1.16.json");
        JsonObject mapping1_15 = MappingDataLoader.loadData("mapping-1.15.json");
        JsonObject mapping1_16 = MappingDataLoader.loadData("mapping-1.16.json");

        Via.getPlatform().getLogger().info("Loading 1.15 -> 1.16 blockstate mapping...");
        blockStateMappings = new Mappings(mapping1_15.getAsJsonObject("blockstates"), mapping1_16.getAsJsonObject("blockstates"), diffmapping.getAsJsonObject("blockstates"));
        Via.getPlatform().getLogger().info("Loading 1.15 -> 1.16 block mapping...");
        blockMappings = new Mappings(mapping1_15.getAsJsonObject("blocks"), mapping1_16.getAsJsonObject("blocks"));
        Via.getPlatform().getLogger().info("Loading 1.15 -> 1.16 item mapping...");
        MappingDataLoader.mapIdentifiers(oldToNewItems, mapping1_15.getAsJsonObject("items"), mapping1_16.getAsJsonObject("items"), diffmapping.getAsJsonObject("items"));
        Via.getPlatform().getLogger().info("Loading 1.15 -> 1.16 sound mapping...");
        soundMappings = new Mappings(mapping1_15.getAsJsonArray("sounds"), mapping1_16.getAsJsonArray("sounds"), diffmapping.getAsJsonObject("sounds"));
    }
}
