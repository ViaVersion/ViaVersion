package us.myles.ViaVersion.protocols.protocol1_15to1_14_4.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;
import us.myles.ViaVersion.api.Via;

public class MappingData {
    public static BiMap<Integer, Integer> oldToNewItems = HashBiMap.create();
    public static us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data.MappingData.BlockMappings blockMappings;
    public static us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data.MappingData.BlockMappings blockStateMappings;
    public static us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data.MappingData.SoundMappings soundMappings;

    public static void init() {
        JsonObject mapping1_14_4 = us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data.MappingData.loadData("mapping-1.14.4.json");
        JsonObject mapping1_15 = us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data.MappingData.loadData("mapping-1.15.json");

        Via.getPlatform().getLogger().info("Loading 1.14.4 -> 1.15 blockstate mapping...");
        blockStateMappings = new us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data.MappingData.BlockMappingsShortArray(mapping1_14_4.getAsJsonObject("blockstates"), mapping1_15.getAsJsonObject("blockstates"));
        Via.getPlatform().getLogger().info("Loading 1.14.4 -> 1.15 block mapping...");
        blockMappings = new us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data.MappingData.BlockMappingsShortArray(mapping1_14_4.getAsJsonObject("blocks"), mapping1_15.getAsJsonObject("blocks"));
        Via.getPlatform().getLogger().info("Loading 1.14.4 -> 1.15 item mapping...");
        us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data.MappingData.mapIdentifiers(oldToNewItems, mapping1_14_4.getAsJsonObject("items"), mapping1_15.getAsJsonObject("items"));
        Via.getPlatform().getLogger().info("Loading 1.14.4 -> 1.15 sound mapping...");
        soundMappings = new us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data.MappingData.SoundMappingShortArray(mapping1_14_4.getAsJsonArray("sounds"), mapping1_15.getAsJsonArray("sounds"));
    }
}
