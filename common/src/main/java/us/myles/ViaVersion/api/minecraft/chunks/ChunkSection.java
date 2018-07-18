package us.myles.ViaVersion.api.minecraft.chunks;

import io.netty.buffer.ByteBuf;

import java.util.List;

public interface ChunkSection {
    /**
     * Gets a block state id (&lt; 1.13: block_id &lt;&lt; 4 | data &amp; 0xF)
     *
     * @param x Block X
     * @param y Block Y
     * @param z Block Z
     * @return Block raw id
     */
    int getBlock(int x, int y, int z);

    /**
     * Set a block in the chunks
     *
     * @param x    Block X
     * @param y    Block Y
     * @param z    Block Z
     * @param type The block id
     * @param data The data value of the block
     */
    void setBlock(int x, int y, int z, int type, int data);

    /**
     * Set a block state in the chunk
     *
     * @param x          Block X
     * @param y          Block Y
     * @param z          Block Z
     * @param blockState The block state id
     */
    void setFlatBlock(int x, int y, int z, int blockState);

    /**
     * Gets a block id (without data)
     * /!\ YOU SHOULD NOT USE THIS ON 1.13
     *
     * @param x Block X
     * @param y Block Y
     * @param z Block Z
     * @return Block id (without data)
     */
    int getBlockId(int x, int y, int z);

    /**
     * Write the blocks in &lt; 1.13 format to a buffer.
     *
     * @param output The buffer to write to.
     * @throws Exception Throws if it failed to write.
     */
    void writeBlocks(ByteBuf output) throws Exception;

    /**
     * Write the blocks in 1.13 format to a buffer.
     *
     * @param output The buffer to write to.
     * @throws Exception Throws if it failed to write.
     */
    void writeBlocks1_13(ByteBuf output) throws Exception;

    void writeBlockLight(ByteBuf output) throws Exception;

    boolean hasSkyLight();

    void writeSkyLight(ByteBuf output) throws Exception;

    List<Integer> getPalette();
}
