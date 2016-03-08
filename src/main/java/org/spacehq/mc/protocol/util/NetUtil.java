package org.spacehq.mc.protocol.util;

import io.netty.buffer.ByteBuf;
import org.spacehq.mc.protocol.data.game.chunk.Chunk;
import org.spacehq.mc.protocol.data.game.chunk.Column;
import org.spacehq.mc.protocol.data.game.chunk.NibbleArray3d;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/* From https://github.com/Steveice10/MCProtocolLib/ */
/* No credit taken for writing this code, and used accordance to it's license
   Original by Steveice10, modified to suit this plugin.
 */
public class NetUtil {
    public static int writeNewColumn(ByteBuf out, Column column, boolean fullChunk, boolean hasSkylight) throws IOException {
        int mask = 0;
        Chunk chunks[] = column.getChunks();
        for (int index = 0; index < chunks.length; index++) {
            Chunk chunk = chunks[index];
            if (chunk != null && (!fullChunk || !chunk.isEmpty())) {
                mask |= 1 << index;
                chunk.getBlocks().write(out);
                chunk.getBlockLight().write(out);
                if (hasSkylight || column.hasSkylight()) {
                    chunk.getSkyLight().write(out); // TODO: Make a PR to original lib to correct this
                }
            }
        }

        if (fullChunk && column.getBiomeData() != null) {
            out.writeBytes(column.getBiomeData());
        }

        return mask;
    }

    public static Column readOldChunkData(int x, int z, boolean isFullChunk, int bitmask, byte[] input, boolean checkForSky, boolean hasSkyLight) {
        int pos = 0;
        int expected = isFullChunk ? 256 : 0;
        boolean sky = false;
        ShortBuffer buf = ByteBuffer.wrap(input).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        // 0 = Calculate expected length and determine if the packet has skylight.
        // 1 = Create chunks from mask and get blocks.
        // 2 = Get block light.
        // 3 = Get sky light.
        Chunk[] chunks = new Chunk[16];
        int chunkCount = 0;
        for (int pass = 0; pass < 4; pass++) {
            if(pass == 1){
                if(chunkCount == 0) return null;
            }
            for (int ind = 0; ind < 16; ind++) {
                if ((bitmask & 1 << ind) != 0) {
                    if (pass == 0) {
                        chunkCount++;
                        // Block length + Blocklight length
                        expected += (4096 * 2) + 2048;
                    }

                    if (pass == 1) {
                        chunks[ind] = new Chunk(sky || hasSkyLight);
                        buf.position(pos / 2);
                        int buffPos = buf.position();
                        // convert short array to new one

                        for (int index = 0; index < 4096; index++) {
                            short ss = buf.get(buffPos + index);
                            // s is 16 bits, 12 bits id and 4 bits data
                            int data = ss & 0xF;
                            int id = (ss >> 4) << 4 | data;

                            int newCombined = id; // test

                            chunks[ind].getBlocks().set(index, newCombined);
                        }
                        pos += 4096 * 2;

                    }

                    if (pass == 2) {
                        NibbleArray3d blocklight = chunks[ind].getBlockLight();
                        System.arraycopy(input, pos, blocklight.getData(), 0, blocklight.getData().length);
                        pos += blocklight.getData().length;
                    }

                    if (pass == 3) {
                        if (sky) {
                            NibbleArray3d skylight = chunks[ind].getSkyLight();
                            System.arraycopy(input, pos, skylight.getData(), 0, skylight.getData().length);
                            pos += skylight.getData().length;
                        }
                    }
                }
            }

            if (pass == 0) {
                // If we have more data than blocks and blocklight combined, there must be skylight data as well.
                if (input.length > expected) {
                    sky = checkForSky;
                }
            }
        }

        byte biomeData[] = null;
        if (isFullChunk && (pos + 256 <= input.length)) {

            biomeData = new byte[256];
            System.arraycopy(input, pos, biomeData, 0, biomeData.length);
        }

        Column column = new Column(x, z, chunks, biomeData);
        return column;
    }
}
