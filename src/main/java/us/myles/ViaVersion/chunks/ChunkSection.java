package us.myles.ViaVersion.chunks;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.Material;
import us.myles.ViaVersion.util.PacketUtil;

import java.util.List;

public class ChunkSection {
    /**
     * Size (dimensions) of blocks in a chunk section.
     */
    public static final int SIZE = 16 * 16 * 16; // width * depth * height
    /**
     * Length of the sky and block light nibble arrays.
     */
    public static final int LIGHT_LENGTH = 16 * 16 * 16 / 2; // size * size * size / 2 (nibble bit count)
    /**
     * Length of the block data array.
     */
//    public static final int BLOCK_LENGTH = 16 * 16 * 16 * 2; // size * size * size * 2 (char bit count)

    private final List<Integer> palette = Lists.newArrayList();
    private final int[] blocks;
    private final NibbleArray blockLight;
    private NibbleArray skyLight;

    public ChunkSection() {
        this.blocks = new int[SIZE];
        this.blockLight = new NibbleArray(SIZE);
        palette.add(0); // AIR
    }

    public void setBlock(int x, int y, int z, int type, int data) {
        setBlock(index(x, y, z), type, data);
    }

    public void setBlock(int idx, int type, int data) {
        int hash = type << 4 | (data & 0xF);
        int index = palette.indexOf(hash);
        if(index == -1) {
            index = palette.size();
            palette.add(hash);
        }

        blocks[idx] = index;
    }

    public void setBlockLight(byte[] data) {
        blockLight.setHandle(data);
    }

    public void setSkyLight(byte[] data) {
        if(data.length != LIGHT_LENGTH) throw new IllegalArgumentException("Data length != " + LIGHT_LENGTH);
        this.skyLight = new NibbleArray(data);
    }

    private int index(int x, int y, int z) {
        return z << 8 | y << 4 | x;
    }

    public void writeBlocks(ByteBuf output) {
        // Write bits per block
        int bitsPerBlock = 4;
        while(palette.size() > 1 << bitsPerBlock) {
            bitsPerBlock += 1;
        }
        long maxEntryValue = (1L << bitsPerBlock) - 1;
        output.writeByte(bitsPerBlock);

        // Write pallet (or not)
        PacketUtil.writeVarInt(palette.size(), output);
        for(int mappedId : palette) {
            PacketUtil.writeVarInt(mappedId, output);
        }

        int length = (int) Math.ceil(SIZE * bitsPerBlock / 64.0);
        PacketUtil.writeVarInt(length, output);
        long[] data = new long[length];
        for(int index = 0; index < blocks.length; index++) {
            int value = blocks[index];
            int bitIndex = index * bitsPerBlock;
            int startIndex = bitIndex / 64;
            int endIndex = ((index + 1) * bitsPerBlock - 1) / 64;
            int startBitSubIndex = bitIndex % 64;
            data[startIndex] = data[startIndex] & ~(maxEntryValue << startBitSubIndex) | ((long) value & maxEntryValue) << startBitSubIndex;
            if(startIndex != endIndex) {
                int endBitSubIndex = 64 - startBitSubIndex;
                data[endIndex] = data[endIndex] >>> endBitSubIndex << endBitSubIndex | ((long) value & maxEntryValue) >> endBitSubIndex;
            }
        }
        PacketUtil.writeLongs(data, output);
    }

    public void writeBlockLight(ByteBuf output) {
        output.writeBytes(blockLight.getHandle());
    }

    public void writeSkyLight(ByteBuf output) {
        output.writeBytes(skyLight.getHandle());
    }

    public boolean hasSkyLight() {
        return skyLight != null;
    }

    /**
     * Get expected size of this chunk section.
     *
     * @return Amount of bytes sent by this section
     */
    public int getExpectedSize() {
        int bitsPerBlock = palette.size() > 255 ? 16 : 8;
        int bytes = 1; // bits per block
        bytes += paletteBytes(); // palette
        bytes += countBytes(bitsPerBlock == 16 ? SIZE * 2 : SIZE); // block data length
        bytes += (palette.size() > 255 ? 2 : 1) * SIZE; // block data
        bytes += LIGHT_LENGTH; // block light
        bytes += hasSkyLight() ? LIGHT_LENGTH : 0; // sky light
        return bytes;
    }

    private int paletteBytes() {
        // Count bytes used by pallet
        int bytes = countBytes(palette.size());
        for(int mappedId : palette) {
            bytes += countBytes(mappedId);
        }
        return bytes;
    }

    private int countBytes(int value) {
        // Count amount of bytes that would be sent if the value were sent as a VarInt
        ByteBuf buf = Unpooled.buffer();
        PacketUtil.writeVarInt(value, buf);
        buf.readerIndex(0);
        int bitCount = buf.readableBytes();
        buf.release();
        return bitCount;
    }
}
