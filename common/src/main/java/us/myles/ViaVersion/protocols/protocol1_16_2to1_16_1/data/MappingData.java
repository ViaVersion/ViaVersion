package us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.data;

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

    public static void init() {
        Via.getPlatform().getLogger().info("Loading 1.16.1 -> 1.16.2 mappings...");
        JsonObject mapping1_16 = MappingDataLoader.loadData("mapping-1.16.json", true);
        JsonObject mapping1_16_2 = MappingDataLoader.loadData("mapping-1.16.2.json", true);

        oldToNewItems.defaultReturnValue(-1);
        blockStateMappings = new Mappings(mapping1_16.getAsJsonObject("blockstates"), mapping1_16_2.getAsJsonObject("blockstates"));
        blockMappings = new Mappings(mapping1_16.getAsJsonObject("blocks"), mapping1_16_2.getAsJsonObject("blocks"));
        MappingDataLoader.mapIdentifiers(oldToNewItems, mapping1_16.getAsJsonObject("items"), mapping1_16_2.getAsJsonObject("items"));
        soundMappings = new Mappings(mapping1_16.getAsJsonArray("sounds"), mapping1_16_2.getAsJsonArray("sounds"));
    }
}
