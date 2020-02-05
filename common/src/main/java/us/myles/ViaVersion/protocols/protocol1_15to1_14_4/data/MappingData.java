package us.myles.ViaVersion.protocols.protocol1_15to1_14_4.data;

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
        JsonObject diffmapping = MappingDataLoader.loadData("mappingdiff-1.14to1.15.json");
        JsonObject mapping1_14 = MappingDataLoader.loadData("mapping-1.14.json");
        JsonObject mapping1_15 = MappingDataLoader.loadData("mapping-1.15.json");

        Via.getPlatform().getLogger().info("Loading 1.14.4 -> 1.15 blockstate mapping...");
        blockStateMappings = new Mappings(mapping1_14.getAsJsonObject("blockstates"), mapping1_15.getAsJsonObject("blockstates"), diffmapping.getAsJsonObject("blockstates"));
        Via.getPlatform().getLogger().info("Loading 1.14.4 -> 1.15 block mapping...");
        blockMappings = new Mappings(mapping1_14.getAsJsonObject("blocks"), mapping1_15.getAsJsonObject("blocks"));
        Via.getPlatform().getLogger().info("Loading 1.14.4 -> 1.15 item mapping...");
        MappingDataLoader.mapIdentifiers(oldToNewItems, mapping1_14.getAsJsonObject("items"), mapping1_15.getAsJsonObject("items"));
        Via.getPlatform().getLogger().info("Loading 1.14.4 -> 1.15 sound mapping...");
        soundMappings = new Mappings(mapping1_14.getAsJsonArray("sounds"), mapping1_15.getAsJsonArray("sounds"), false);
    }
}
