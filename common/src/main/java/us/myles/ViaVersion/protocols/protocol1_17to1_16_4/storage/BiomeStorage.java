package us.myles.ViaVersion.protocols.protocol1_17to1_16_4.storage;

import org.jetbrains.annotations.Nullable;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;

import java.util.HashMap;
import java.util.Map;

public class BiomeStorage extends StoredObject {

    private final Map<Long, int[]> chunkBiomes = new HashMap<>();
    private String world;

    public BiomeStorage(UserConnection user) {
        super(user);
    }

    @Nullable
    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    @Nullable
    public int[] getBiomes(int x, int z) {
        return chunkBiomes.get(getChunkSectionIndex(x, z));
    }

    public void setBiomes(int x, int z, int[] biomes) {
        chunkBiomes.put(getChunkSectionIndex(x, z), biomes);
    }

    public void clearBiomes(int x, int z) {
        chunkBiomes.remove(getChunkSectionIndex(x, z));
    }

    public void clearBiomes() {
        chunkBiomes.clear();
    }

    private long getChunkSectionIndex(int x, int z) {
        return ((x & 0x3FFFFFFL) << 38) | (z & 0x3FFFFFFL);
    }
}
