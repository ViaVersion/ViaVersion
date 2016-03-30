package us.myles.ViaVersion.api.minecraft.chunks;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@ToString
public class Chunk {
    private final int x;
    private final int z;
    private final boolean groundUp;
    private final int primaryBitmask;
    private final ChunkSection[] sections;
    private final byte[] biomeData;
    private boolean unloadPacket = false;

    /**
     * Chunk unload.
     *
     * @param x coord
     * @param z coord
     */
    public Chunk(int x, int z) {
        this(x, z, true, 0, new ChunkSection[16], null);
        this.unloadPacket = true;
    }

    /**
     * Does this chunk have biome data
     *
     * @return True if the chunk has biome data
     */
    public boolean hasBiomeData() {
        return biomeData != null && groundUp;
    }
}
