package us.myles.ViaVersion.api.data;

import com.google.gson.JsonArray;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

public class ParticleMappings {
    private final Mappings mappings;
    private final int blockId;
    private final int fallingDustId;
    private final int itemId;

    public ParticleMappings(JsonArray oldMappings, Mappings mappings) {
        this.mappings = mappings;

        Object2IntMap<String> map = MappingDataLoader.arrayToMap(oldMappings);
        blockId = map.getInt("block");
        fallingDustId = map.getInt("dust");
        itemId = map.getInt("item");
    }

    public Mappings getMappings() {
        return mappings;
    }

    public int getBlockId() {
        return blockId;
    }

    public int getFallingDustId() {
        return fallingDustId;
    }

    public int getItemId() {
        return itemId;
    }
}
