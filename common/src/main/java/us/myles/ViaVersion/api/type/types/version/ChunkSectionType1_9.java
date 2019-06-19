package us.myles.ViaVersion.api.type.types.version;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import us.myles.ViaVersion.api.type.Type;

public class ChunkSectionType1_9 extends Type<ChunkSection> {
    private static final int GLOBAL_PALETTE = 13;

    public ChunkSectionType1_9() {
        super("Chunk Section Type", ChunkSection.class);
    }

    @Override
    public ChunkSection read(ByteBuf buffer) throws Exception {
        ChunkSection chunkSection = new ChunkSection();

        // Reaad bits per block
        int bitsPerBlock = buffer.readUnsignedByte();
        int originalBitsPerBlock = bitsPerBlock;
        long maxEntryValue = (1L << bitsPerBlock) - 1;

        if (bitsPerBlock == 0) {
            bitsPerBlock = GLOBAL_PALETTE;
        }
        if (bitsPerBlock < 4) {
            bitsPerBlock = 4;
        }
        if (bitsPerBlock > 8) {
            bitsPerBlock = GLOBAL_PALETTE;
        }
        int paletteLength = Type.VAR_INT.read(buffer);
        // Read palette
        chunkSection.clearPalette();
        for (int i = 0; i < paletteLength; i++) {
            if (bitsPerBlock != GLOBAL_PALETTE) {
                chunkSection.addPaletteEntry(Type.VAR_INT.read(buffer));
            } else {
                Type.VAR_INT.read(buffer);
            }
        }

        // Read blocks
        long[] blockData = new long[Type.VAR_INT.read(buffer)];
        if (blockData.length > 0) {
            int expectedLength = (int) Math.ceil(ChunkSection.SIZE * bitsPerBlock / 64.0);
            if (blockData.length != expectedLength) {
                throw new IllegalStateException("Block data length (" + blockData.length + ") does not match expected length (" + expectedLength + ")! bitsPerBlock=" + bitsPerBlock + ", originalBitsPerBlock=" + originalBitsPerBlock);
            }

            for (int i = 0; i < blockData.length; i++) {
                blockData[i] = buffer.readLong();
            }
            for (int i = 0; i < ChunkSection.SIZE; i++) {
                int bitIndex = i * bitsPerBlock;
                int startIndex = bitIndex / 64;
                int endIndex = ((i + 1) * bitsPerBlock - 1) / 64;
                int startBitSubIndex = bitIndex % 64;
                int val;
                if (startIndex == endIndex) {
                    val = (int) (blockData[startIndex] >>> startBitSubIndex & maxEntryValue);
                } else {
                    int endBitSubIndex = 64 - startBitSubIndex;
                    val = (int) ((blockData[startIndex] >>> startBitSubIndex | blockData[endIndex] << endBitSubIndex) & maxEntryValue);
                }

                if (bitsPerBlock == GLOBAL_PALETTE) {
                    chunkSection.setBlock(i, val >> 4, val & 0xF);
                } else {
                    chunkSection.setPaletteIndex(i, val);
                }
            }
        }

        return chunkSection;
    }

    @Override
    public void write(ByteBuf buffer, ChunkSection chunkSection) throws Exception {
        int bitsPerBlock = 4;
        while (chunkSection.getPaletteSize() > 1 << bitsPerBlock) {
            bitsPerBlock += 1;
        }

        if (bitsPerBlock > 8) {
            bitsPerBlock = GLOBAL_PALETTE;
        }

        long maxEntryValue = (1L << bitsPerBlock) - 1;
        buffer.writeByte(bitsPerBlock);

        // Write pallet (or not)
        if (bitsPerBlock != GLOBAL_PALETTE) {
            Type.VAR_INT.write(buffer, chunkSection.getPaletteSize());
            for (int i = 0; i < chunkSection.getPaletteSize(); i++) {
                Type.VAR_INT.write(buffer, chunkSection.getPaletteEntry(i));
            }
        } else {
            Type.VAR_INT.write(buffer, 0);
        }

        int length = (int) Math.ceil(ChunkSection.SIZE * bitsPerBlock / 64.0);
        Type.VAR_INT.write(buffer, length);
        long[] data = new long[length];
        for (int index = 0; index < ChunkSection.SIZE; index++) {
            int value = bitsPerBlock == GLOBAL_PALETTE ? chunkSection.getFlatBlock(index) : chunkSection.getPaletteIndex(index);
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
            buffer.writeLong(l);
        }
    }
}
