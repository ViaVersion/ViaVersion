package us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.data;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.google.gson.JsonObject;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.MappingDataLoader;
import us.myles.ViaVersion.api.data.Mappings;
import us.myles.ViaVersion.api.minecraft.nbt.BinaryTagIO;
import us.myles.ViaVersion.util.Int2IntBiMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MappingData {
    public static CompoundTag dimensionRegistry;
    public static Map<String, CompoundTag> dimensionDataMap = new HashMap<>();
    public static Int2IntBiMap oldToNewItems = new Int2IntBiMap();
    public static Mappings blockMappings;
    public static Mappings blockStateMappings;
    public static Mappings soundMappings;

    public static void init() {
        Via.getPlatform().getLogger().info("Loading 1.16.1 -> 1.16.2 mappings...");
        JsonObject diffmapping = MappingDataLoader.loadData("mappingdiff-1.16.1to1.16.2.json");
        JsonObject mapping1_16 = MappingDataLoader.loadData("mapping-1.16.json", true);
        JsonObject mapping1_16_2 = MappingDataLoader.loadData("mapping-1.16.2.json", true);

        try {
            dimensionRegistry = BinaryTagIO.readCompressedInputStream(MappingDataLoader.getResource("dimension-registry-1.16.2.nbt"));
        } catch (IOException e) {
            Via.getPlatform().getLogger().severe("Error loading dimension registry:");
            e.printStackTrace();
        }

        oldToNewItems.defaultReturnValue(-1);
        blockStateMappings = new Mappings(mapping1_16.getAsJsonObject("blockstates"), mapping1_16_2.getAsJsonObject("blockstates"), diffmapping.getAsJsonObject("blockstates"));
        blockMappings = new Mappings(mapping1_16.getAsJsonObject("blocks"), mapping1_16_2.getAsJsonObject("blocks"));
        MappingDataLoader.mapIdentifiers(oldToNewItems, mapping1_16.getAsJsonObject("items"), mapping1_16_2.getAsJsonObject("items"));
        soundMappings = new Mappings(mapping1_16.getAsJsonArray("sounds"), mapping1_16_2.getAsJsonArray("sounds"));

        // Data of each dimension
        ListTag dimensions = ((CompoundTag) dimensionRegistry.get("minecraft:dimension_type")).get("value");
        for (Tag dimension : dimensions) {
            CompoundTag dimensionCompound = (CompoundTag) dimension;
            // Copy with an empty name
            CompoundTag dimensionData = new CompoundTag("", ((CompoundTag) dimensionCompound.get("element")).getValue());
            dimensionDataMap.put(((StringTag) dimensionCompound.get("name")).getValue(), dimensionData);
        }
    }
}
