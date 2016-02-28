package us.myles.ViaVersion.chunks;

public class PacketChunk {
    private PacketChunkData[] chunkData;
    private byte[] biomeData;

    public PacketChunk(PacketChunkData[] chunkData, byte[] biomeData) {
        this.chunkData = chunkData;
        this.biomeData = biomeData;
    }

    public PacketChunkData[] getChunkData() {
        return chunkData;
    }

    public byte[] getBiomeData() {
        return biomeData;
    }
}
