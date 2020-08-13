package us.myles.ViaVersion.protocols.protocol1_15to1_14_4.data;

import com.google.gson.JsonObject;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.MappingDataLoader;
import us.myles.ViaVersion.api.data.Mappings;
import us.myles.ViaVersion.util.Int2IntBiMap;

public class MappingData {
    public static Int2IntBiMap oldToNewItems = new Int2IntBiMap();
    public static Mappings blockMappings;
    public static Mappings blockStateMappings;
    public static Mappings soundMappings;
    public static Mappings statisticsMappings;

    public static void init() {
        Via.getPlatform().getLogger().info("Loading 1.14.4 -> 1.15 mappings...");
        JsonObject diffmapping = MappingDataLoader.loadData("mappingdiff-1.14to1.15.json");
        JsonObject mapping1_14 = MappingDataLoader.loadData("mapping-1.14.json", true);
        JsonObject mapping1_15 = MappingDataLoader.loadData("mapping-1.15.json", true);

        oldToNewItems.defaultReturnValue(-1);
        blockStateMappings = new Mappings(mapping1_14.getAsJsonObject("blockstates"), mapping1_15.getAsJsonObject("blockstates"), diffmapping.getAsJsonObject("blockstates"));
        blockMappings = new Mappings(mapping1_14.getAsJsonObject("blocks"), mapping1_15.getAsJsonObject("blocks"));
        MappingDataLoader.mapIdentifiers(oldToNewItems, mapping1_14.getAsJsonObject("items"), mapping1_15.getAsJsonObject("items"));
        soundMappings = new Mappings(mapping1_14.getAsJsonArray("sounds"), mapping1_15.getAsJsonArray("sounds"), false);
        statisticsMappings = new Mappings(mapping1_14.getAsJsonArray("statistics"), mapping1_15.getAsJsonArray("statistics"));
    }
}
