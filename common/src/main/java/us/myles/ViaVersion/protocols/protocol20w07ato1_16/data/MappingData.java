package us.myles.ViaVersion.protocols.protocol20w07ato1_16.data;

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
        JsonObject mapping1_16 = MappingDataLoader.loadData("mapping-1.16.json");
        JsonObject mapping20w07a = MappingDataLoader.loadData("mapping-20w07a.json");

        Via.getPlatform().getLogger().info("Loading 1.16 -> 20w07a blockstate mapping...");
        Via.getPlatform().getLogger().info("Loading 1.16 -> 20w07a block mapping...");
        blockMappings = new Mappings(mapping1_16.getAsJsonObject("blocks"), mapping20w07a.getAsJsonObject("blocks"));
        Via.getPlatform().getLogger().info("Loading 1.16 -> 20w07a item mapping...");
        MappingDataLoader.mapIdentifiers(oldToNewItems, mapping1_16.getAsJsonObject("items"), mapping20w07a.getAsJsonObject("items"));
        Via.getPlatform().getLogger().info("Loading 1.16 -> 20w07a sound mapping...");
        soundMappings = new Mappings(mapping1_16.getAsJsonArray("sounds"), mapping20w07a.getAsJsonArray("sounds"));
    }
}
