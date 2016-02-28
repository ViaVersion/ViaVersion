package org.spacehq.mc.protocol.data.game.chunk;

public class Column {
    private int x;
    private int z;
    private Chunk chunks[];
    private byte biomeData[];

    private boolean skylight;

    public Column(int x, int z, Chunk chunks[]) {
        this(x, z, chunks, null);
    }

    public Column(int x, int z, Chunk chunks[], byte biomeData[]) {
        if(chunks.length != 16) {
            throw new IllegalArgumentException("Chunk array length must be 16.");
        }

        if(biomeData != null && biomeData.length != 256) {
            throw new IllegalArgumentException("Biome data array length must be 256.");
        }

        this.skylight = false;
        boolean noSkylight = false;
        for(int index = 0; index < chunks.length; index++) {
            if(chunks[index] != null) {
                if(chunks[index].getSkyLight() == null) {
                    noSkylight = true;
                } else {
                    this.skylight = true;
                }
            }
        }

        if(noSkylight && this.skylight) {
            throw new IllegalArgumentException("Either all chunks must have skylight values or none must have them.");
        }

        this.x = x;
        this.z = z;
        this.chunks = chunks;
        this.biomeData = biomeData;
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    public Chunk[] getChunks() {
        return this.chunks;
    }

    public boolean hasBiomeData() {
        return this.biomeData != null;
    }

    public byte[] getBiomeData() {
        return this.biomeData;
    }

    public boolean hasSkylight() {
        return this.skylight;
    }
}