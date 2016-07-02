package us.myles.ViaVersion.protocols.protocol1_9to1_8.chunks;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;

@RequiredArgsConstructor
@Getter
@ToString
public class Chunk1_9to1_8 implements Chunk {
    private final int x;
    private final int z;
    private final boolean groundUp;
    private final int primaryBitmask;
    private final ChunkSection1_9to1_8[] sections;
    private final byte[] biomeData;
    private boolean unloadPacket = false;

    /**
     * Chunk unload.
     *
     * @param x coord
     * @param z coord
     */
    public Chunk1_9to1_8(int x, int z) {
        this(x, z, true, 0, new ChunkSection1_9to1_8[16], null);
        this.unloadPacket = true;
    }

    /**
     * Does this chunks have biome data
     *
     * @return True if the chunks has biome data
     */
    public boolean hasBiomeData() {
        return biomeData != null && groundUp;
    }

    @Override
    public boolean isBiomeData() {
        return biomeData != null;
    }

    @Override
    public int getBitmask() {
        return primaryBitmask;
    }
}
