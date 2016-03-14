package us.myles.ViaVersion.chunks;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
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
    protected Chunk(int x, int z) {
        this(x, z, true, 0, new ChunkSection[16], null);
        this.unloadPacket = true;
    }

    public boolean hasBiomeData() {
        return biomeData != null && groundUp;
    }
}
