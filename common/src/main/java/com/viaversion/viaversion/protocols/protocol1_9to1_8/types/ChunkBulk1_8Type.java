/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2023 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.protocols.protocol1_9to1_8.types;

import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.type.PartialType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.minecraft.BaseChunkBulkType;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import io.netty.buffer.ByteBuf;

public class ChunkBulk1_8Type extends PartialType<Chunk[], ClientWorld> {

    private static final int BLOCKS_PER_SECTION = 16 * 16 * 16;
    private static final int BLOCKS_BYTES = BLOCKS_PER_SECTION * 2;
    private static final int LIGHT_BYTES = BLOCKS_PER_SECTION / 2;
    private static final int BIOME_BYTES = 16 * 16;

    public ChunkBulk1_8Type(final ClientWorld clientWorld) {
        super(clientWorld, Chunk[].class);
    }

    @Override
    public Class<? extends Type> getBaseClass() {
        return BaseChunkBulkType.class;
    }

    @Override
    public Chunk[] read(ByteBuf input, ClientWorld world) throws Exception {
        final boolean skyLight = input.readBoolean();
        final int count = Type.VAR_INT.readPrimitive(input);
        final Chunk[] chunks = new Chunk[count];
        final ChunkBulkSection[] chunkInfo = new ChunkBulkSection[count];

        // Read metadata
        for (int i = 0; i < chunkInfo.length; i++) {
            chunkInfo[i] = new ChunkBulkSection(input, skyLight);
        }
        // Read data
        for (int i = 0; i < chunks.length; i++) {
            final ChunkBulkSection chunkBulkSection = chunkInfo[i];
            chunkBulkSection.readData(input);
            chunks[i] = Chunk1_8Type.deserialize(chunkBulkSection.chunkX, chunkBulkSection.chunkZ, true, skyLight, chunkBulkSection.bitmask, chunkBulkSection.getData());
        }

        return chunks;
    }

    @Override
    public void write(ByteBuf output, ClientWorld world, Chunk[] chunks) throws Exception {
        boolean skyLight = false;
        loop1:
        for (Chunk c : chunks) {
            for (ChunkSection section : c.getSections()) {
                if (section != null && section.getLight().hasSkyLight()) {
                    skyLight = true;
                    break loop1;
                }
            }
        }
        output.writeBoolean(skyLight);
        Type.VAR_INT.writePrimitive(output, chunks.length);

        // Write metadata
        for (Chunk c : chunks) {
            output.writeInt(c.getX());
            output.writeInt(c.getZ());
            output.writeShort(c.getBitmask());
        }
        // Write data
        for (Chunk c : chunks) {
            output.writeBytes(Chunk1_8Type.serialize(c));
        }
    }

    public static final class ChunkBulkSection {
        private final int chunkX;
        private final int chunkZ;
        private final int bitmask;
        private final byte[] data;

        public ChunkBulkSection(final ByteBuf input, final boolean skyLight) {
            this.chunkX = input.readInt();
            this.chunkZ = input.readInt();
            this.bitmask = input.readUnsignedShort();
            final int setSections = Integer.bitCount(this.bitmask);
            this.data = new byte[setSections * (BLOCKS_BYTES + (skyLight ? 2 * LIGHT_BYTES : LIGHT_BYTES)) + BIOME_BYTES];
        }

        public void readData(final ByteBuf input) {
            input.readBytes(this.data);
        }

        public int getChunkX() {
            return this.chunkX;
        }

        public int getChunkZ() {
            return this.chunkZ;
        }

        public int getBitmask() {
            return this.bitmask;
        }

        public byte[] getData() {
            return this.data;
        }
    }

}
