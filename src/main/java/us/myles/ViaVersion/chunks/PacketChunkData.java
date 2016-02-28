package us.myles.ViaVersion.chunks;

public class PacketChunkData {
    private short[] blocks = new short[4096];
    private byte[] blockLight = new byte[2048];
    private byte[] skyLight;

    public PacketChunkData(boolean isSkyLightData) {
        if(isSkyLightData){
            skyLight = new byte[2048];
        }
    }

    public short[] getBlocks() {
        return this.blocks;
    }

    public byte[] getBlockLight() {
        return this.blockLight;
    }

    public byte[] getSkyLight() {
        return this.skyLight;
    }
}
