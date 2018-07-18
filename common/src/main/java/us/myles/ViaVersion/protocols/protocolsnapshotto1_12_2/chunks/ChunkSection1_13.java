package us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.chunks;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import us.myles.ViaVersion.api.minecraft.chunks.NibbleArray;
import us.myles.ViaVersion.api.type.Type;

import java.util.List;

public class ChunkSection1_13 implements ChunkSection {
    /**
     * Size (dimensions) of blocks in a chunks section.
     */
    public static final int SIZE = 16 * 16 * 16; // width * depth * height
    /**
     * Length of the sky and block light nibble arrays.
     */
    public static final int LIGHT_LENGTH = 16 * 16 * 16 / 2; // size * size * size / 2 (nibble bit count)
    /**
     * Length of the block data array.
     */
    private final List<Integer> palette = Lists.newArrayList();
    private final int[] blocks;
    private final NibbleArray blockLight;
    private NibbleArray skyLight;

    public ChunkSection1_13() {
        this.blocks = new int[SIZE];
        this.blockLight = new NibbleArray(SIZE);
        palette.add(0); // AIR
    }

    public void setBlock(int x, int y, int z, int type, int data) {
        setBlock(index(x, y, z), type, data);
    }

    @Override
    public void setFlatBlock(int x, int y, int z, int type) {
        int index = palette.indexOf(type);
        if (index == -1) {
            index = palette.size();
            palette.add(type);
        }

        blocks[index(x, y, z)] = index;
    }

    public int getBlockId(int x, int y, int z) {
        return getBlock(x, y, z) >> 4;
    }

    public int getBlock(int x, int y, int z) {
        int index = blocks[index(x, y, z)];
        return palette.get(index);
    }

    /**
     * Set a block in the chunks based on the index
     *
     * @param idx  Index
     * @param type The type of the block
     * @param data The data value of the block
     */
    public void setBlock(int idx, int type, int data) {
        int hash = type << 4 | (data & 0xF);
        int index = palette.indexOf(hash);
        if (index == -1) {
            index = palette.size();
            palette.add(hash);
        }

        blocks[idx] = index;
    }

    /**
     * Set the block light array
     *
     * @param data The value to set the block light to
     */
    public void setBlockLight(byte[] data) {
        blockLight.setHandle(data);
    }

    /**
     * Set the sky light array
     *
     * @param data The value to set the sky light to
     */
    public void setSkyLight(byte[] data) {
        if (data.length != LIGHT_LENGTH) throw new IllegalArgumentException("Data length != " + LIGHT_LENGTH);
        this.skyLight = new NibbleArray(data);
    }

    private int index(int x, int y, int z) {
        return y << 8 | z << 4 | x;
    }

    /**
     * Read blocks from input stream.
     * This reads all the block related data:
     * <ul>
     * <li>Block length/palette type</li>
     * <li>Palette</li>
     * <li>Block hashes/palette reference</li>
     * </ul>
     *
     * @param input The buffer to read from.
     * @throws Exception If it fails to read
     */
    public void readBlocks(ByteBuf input) throws Exception {
        palette.clear();

        // Read bits per block
        int bitsPerBlock = input.readUnsignedByte();
        long maxEntryValue = (1L << bitsPerBlock) - 1;

        boolean directPalette = false;

        if (bitsPerBlock == 0) {
            bitsPerBlock = 13;
        }
        if (bitsPerBlock < 4) {
            bitsPerBlock = 4;
        }
        if (bitsPerBlock > 9) {
            directPalette = true;
            bitsPerBlock = 14;
        }

        int paletteLength = directPalette ? 0 : Type.VAR_INT.read(input);
        // Read palette
        for (int i = 0; i < paletteLength; i++) {
            palette.add(Type.VAR_INT.read(input));
        }

        // Read blocks
        Long[] blockData = Type.LONG_ARRAY.read(input);
        if (blockData.length > 0) {
            for (int i = 0; i < blocks.length; i++) {
                int bitIndex = i * bitsPerBlock;
                int startIndex = bitIndex >> 6; // /64
                int endIndex = ((i + 1) * bitsPerBlock - 1) >> 6; // /64
                int startBitSubIndex = bitIndex & 0x3F; // % 64
                int val;
                if (startIndex == endIndex) {
                    val = (int) (blockData[startIndex] >>> startBitSubIndex & maxEntryValue);
                } else {
                    int endBitSubIndex = 64 - startBitSubIndex;
                    val = (int) ((blockData[startIndex] >>> startBitSubIndex | blockData[endIndex] << endBitSubIndex) & maxEntryValue);
                }

                if (directPalette) {
                    int type = val >> 4;
                    int data = val & 0xF;

                    setBlock(i, type, data);
                } else {
                    blocks[i] = val;
                }
            }
        }
    }

    /**
     * Read block light from buffer.
     *
     * @param input The buffer to read from
     */
    public void readBlockLight(ByteBuf input) {
        byte[] handle = new byte[LIGHT_LENGTH];
        input.readBytes(handle);
        blockLight.setHandle(handle);
    }

    /**
     * Read sky light from buffer.
     * Note: Only sent in overworld!
     *
     * @param input The buffer to read from
     */
    public void readSkyLight(ByteBuf input) {
        byte[] handle = new byte[LIGHT_LENGTH];
        input.readBytes(handle);
        if (skyLight != null) {
            skyLight.setHandle(handle);
            return;
        }

        this.skyLight = new NibbleArray(handle);
    }


    public void writeBlocks(ByteBuf output) throws Exception {
        // Write bits per block
        int bitsPerBlock = 4;
        while (palette.size() > 1 << bitsPerBlock) {
            bitsPerBlock += 1;
        }
        long maxEntryValue = (1L << bitsPerBlock) - 1;
        output.writeByte(bitsPerBlock);

        // Write pallet (or not)
        Type.VAR_INT.write(output, palette.size());
        for (int mappedId : palette) {
            Type.VAR_INT.write(output, mappedId);
        }

        int length = (int) Math.ceil(SIZE * bitsPerBlock / 64.0);
        Type.VAR_INT.write(output, length);
        long[] data = new long[length];
        for (int index = 0; index < blocks.length; index++) {
            int value = blocks[index];
            int bitIndex = index * bitsPerBlock;
            int startIndex = bitIndex / 64;
            int endIndex = ((index + 1) * bitsPerBlock - 1) / 64;
            int startBitSubIndex = bitIndex % 64;
            data[startIndex] = data[startIndex] & ~(maxEntryValue << startBitSubIndex) | ((long) value & maxEntryValue) << startBitSubIndex;
            if (startIndex != endIndex) {
                int endBitSubIndex = 64 - startBitSubIndex;
                data[endIndex] = data[endIndex] >>> endBitSubIndex << endBitSubIndex | ((long) value & maxEntryValue) >> endBitSubIndex;
            }
        }
        for (long l : data) {
            Type.LONG.write(output, l);
        }
    }

    @Override
    public void writeBlocks1_13(ByteBuf output) throws Exception {
        // Write bits per block
        int bitsPerBlock = 4;
        while (palette.size() > 1 << bitsPerBlock) {
            bitsPerBlock++;
        }
        boolean directPalette = false;
        if (bitsPerBlock > 9) {
            bitsPerBlock = 14;
            directPalette = true;
        }
        long maxEntryValue = (1L << bitsPerBlock) - 1;
        output.writeByte(bitsPerBlock);

        // Write pallet (or not)
        if (!directPalette) {
            Type.VAR_INT.write(output, palette.size());
            for (int mappedId : palette) {
                Type.VAR_INT.write(output, mappedId);
            }
        }

        int length = (int) Math.ceil(SIZE * bitsPerBlock / 64.0);
        Type.VAR_INT.write(output, length);
        long[] data = new long[length];
        for (int index = 0; index < blocks.length; index++) {
            int value = directPalette ? palette.get(blocks[index]) : blocks[index];
            int bitIndex = index * bitsPerBlock;
            int startIndex = bitIndex / 64;
            int endIndex = ((index + 1) * bitsPerBlock - 1) / 64;
            int startBitSubIndex = bitIndex % 64;
            data[startIndex] = data[startIndex] & ~(maxEntryValue << startBitSubIndex) | ((long) value & maxEntryValue) << startBitSubIndex;
            if (startIndex != endIndex) {
                int endBitSubIndex = 64 - startBitSubIndex;
                data[endIndex] = data[endIndex] >>> endBitSubIndex << endBitSubIndex | ((long) value & maxEntryValue) >> endBitSubIndex;
            }
        }
        for (long l : data) {
            Type.LONG.write(output, l);
        }
    }

    /**
     * Write the block light to a buffer
     *
     * @param output The buffer to write to
     */
    @Override
    public void writeBlockLight(ByteBuf output) {
        output.writeBytes(blockLight.getHandle());
    }

    /**
     * Write the sky light to a buffer
     *
     * @param output The buffer to write to
     */
    @Override
    public void writeSkyLight(ByteBuf output) {
        output.writeBytes(skyLight.getHandle());
    }

    /**
     * Check if sky light is present
     *
     * @return True if skylight is present
     */
    @Override
    public boolean hasSkyLight() {
        return skyLight != null;
    }

    @Override
    public List<Integer> getPalette() {
        return palette;
    }
}
