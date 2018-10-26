package us.myles.ViaVersion.api.minecraft.chunks;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class ChunkSection {
    /**
     * Size (dimensions) of blocks in a chunks section.
     */
    public static final int SIZE = 16 * 16 * 16; // width * depth * height
    /**
     * Length of the sky and block light nibble arrays.
     */
    public static final int LIGHT_LENGTH = 16 * 16 * 16 / 2; // size * size * size / 2 (nibble bit count)
    @Getter
	private final List<Integer> palette = Lists.newArrayList();
    private final int[] blocks;
    private NibbleArray blockLight;
    private NibbleArray skyLight;
    @Getter
    @Setter
    private int nonAirBlocksCount;

    public ChunkSection() {
        this.blocks = new int[SIZE];
        this.blockLight = new NibbleArray(SIZE);
        palette.add(0); // AIR
    }

    /**
     * Set a block in the chunks
     *
     * @param x    Block X
     * @param y    Block Y
     * @param z    Block Z
     * @param type The type of the block
     * @param data The data value of the block
     */
    public void setBlock(int x, int y, int z, int type, int data) {
        setFlatBlock(index(x, y, z), type << 4 | (data & 0xF));
    }

    public void setFlatBlock(int x, int y, int z, int type) {
        setFlatBlock(index(x, y, z), type);
    }

    public int getBlockId(int x, int y, int z) {
        return getFlatBlock(x, y, z) >> 4;
    }

    public int getBlockData(int x, int y, int z) {
        return getFlatBlock(x, y, z) & 0xF;
    }

    public int getFlatBlock(int x, int y, int z) {
        int index = blocks[index(x, y, z)];
        return palette.get(index);
    }

    public int getFlatBlock(int idx) {
        int index = blocks[idx];
        return palette.get(index);
    }

    public void setBlock(int idx, int type, int data) {
        setFlatBlock(idx, type << 4 | (data & 0xF));
    }

    public void setPaletteIndex(int idx, int index) {
    	blocks[idx] = index;
    }

    public int getPaletteIndex(int idx) {
    	return blocks[idx];
    }

    /**
     * Set a block in the chunks based on the index
     *
     * @param idx  Index
     * @param id The raw or flat id of the block
     */
    public void setFlatBlock(int idx, int id) {
        int index = palette.indexOf(id);
        if (index == -1) {
            index = palette.size();
            palette.add(id);
        }

        blocks[idx] = index;
    }

    /**
     * Set the block light array
     *
     * @param data The value to set the block light to
     */
    public void setBlockLight(byte[] data) {
        if (data.length != LIGHT_LENGTH) throw new IllegalArgumentException("Data length != " + LIGHT_LENGTH);
        if (this.blockLight == null) {
            this.blockLight = new NibbleArray(data);
        } else {
            this.blockLight.setHandle(data);
        }
    }

    /**
     * Set the sky light array
     *
     * @param data The value to set the sky light to
     */
    public void setSkyLight(byte[] data) {
        if (data.length != LIGHT_LENGTH) throw new IllegalArgumentException("Data length != " + LIGHT_LENGTH);
        if (this.skyLight == null) {
            this.skyLight = new NibbleArray(data);
        } else {
            this.skyLight.setHandle(data);
        }
    }

    public byte[] getBlockLight() {
        return blockLight == null ? null : blockLight.getHandle();
    }

    public byte[] getSkyLight() {
        return skyLight == null ? null : skyLight.getHandle();
    }

    public void readBlockLight(ByteBuf input) {
        if (this.blockLight == null) {
            this.blockLight = new NibbleArray(LIGHT_LENGTH * 2);
        }
        input.readBytes(this.blockLight.getHandle());
    }

    public void readSkyLight(ByteBuf input) {
        if (this.skyLight == null) {
            this.skyLight = new NibbleArray(LIGHT_LENGTH * 2);
        }
        input.readBytes(this.skyLight.getHandle());
    }

    private static int index(int x, int y, int z) {
        return y << 8 | z << 4 | x;
    }

    /**
     * Write the block light to a buffer
     *
     * @param output The buffer to write to
     */
    public void writeBlockLight(ByteBuf output) {
        output.writeBytes(blockLight.getHandle());
    }

    /**
     * Write the sky light to a buffer
     *
     * @param output The buffer to write to
     */
    public void writeSkyLight(ByteBuf output) {
        output.writeBytes(skyLight.getHandle());
    }

    /**
     * Check if sky light is present
     *
     * @return True if skylight is present
     */
    public boolean hasSkyLight() {
        return skyLight != null;
    }

    public boolean hasBlockLight() {
        return blockLight != null;
    }
}
