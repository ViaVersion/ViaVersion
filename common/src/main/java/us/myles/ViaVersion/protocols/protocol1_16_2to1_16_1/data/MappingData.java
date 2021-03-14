package us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.data;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.google.gson.JsonObject;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.MappingDataLoader;
import us.myles.ViaVersion.api.minecraft.nbt.BinaryTagIO;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MappingData extends us.myles.ViaVersion.api.data.MappingData {
    private final Map<String, CompoundTag> dimensionDataMap = new HashMap<>();
    private CompoundTag dimensionRegistry;

    public MappingData() {
        super("1.16", "1.16.2", true);
    }

    @Override
    public void loadExtras(JsonObject oldMappings, JsonObject newMappings, JsonObject diffMappings) {
        try {
            dimensionRegistry = BinaryTagIO.readCompressedInputStream(MappingDataLoader.getResource("dimension-registry-1.16.2.nbt"));
        } catch (IOException e) {
            Via.getPlatform().getLogger().severe("Error loading dimension registry:");
            e.printStackTrace();
        }

        // Data of each dimension
        ListTag dimensions = ((CompoundTag) dimensionRegistry.get("minecraft:dimension_type")).get("value");
        for (Tag dimension : dimensions) {
            CompoundTag dimensionCompound = (CompoundTag) dimension;
            // Copy with an empty name
            CompoundTag dimensionData = new CompoundTag(((CompoundTag) dimensionCompound.get("element")).getValue());
            dimensionDataMap.put(((StringTag) dimensionCompound.get("name")).getValue(), dimensionData);
        }
    }

    public Map<String, CompoundTag> getDimensionDataMap() {
        return dimensionDataMap;
    }

    public CompoundTag getDimensionRegistry() {
        return dimensionRegistry;
    }
}
