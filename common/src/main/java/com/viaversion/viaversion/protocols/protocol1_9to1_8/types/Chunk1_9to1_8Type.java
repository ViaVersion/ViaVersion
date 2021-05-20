/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk1_8;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSectionLight;
import com.viaversion.viaversion.api.type.PartialType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.minecraft.BaseChunkType;
import com.viaversion.viaversion.api.type.types.version.Types1_8;
import com.viaversion.viaversion.api.type.types.version.Types1_9;
import com.viaversion.viaversion.protocols.protocol1_10to1_9_3.Protocol1_10To1_9_3_4;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.storage.ClientChunks;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.logging.Level;

public class Chunk1_9to1_8Type extends PartialType<Chunk, ClientChunks> {
    /**
     * Amount of sections in a chunks.
     */
    public static final int SECTION_COUNT = 16;
    /**
     * size of each chunks section (16x16x16).
     */
    private static final int SECTION_SIZE = 16;
    /**
     * Length of biome data.
     */
    private static final int BIOME_DATA_LENGTH = 256;

    public Chunk1_9to1_8Type(ClientChunks chunks) {
        super(chunks, Chunk.class);
    }

    private static long toLong(int msw, int lsw) {
        return ((long) msw << 32) + lsw - -2147483648L;
    }

    @Override
    public Class<? extends Type> getBaseClass() {
        return BaseChunkType.class;
    }

    @Override
    public Chunk read(ByteBuf input, ClientChunks param) throws Exception {
        boolean replacePistons = param.getUser().getProtocolInfo().getPipeline().contains(Protocol1_10To1_9_3_4.class) && Via.getConfig().isReplacePistons();
        int replacementId = Via.getConfig().getPistonReplacementId();

        int chunkX = input.readInt();
        int chunkZ = input.readInt();
        long chunkHash = toLong(chunkX, chunkZ);
        boolean fullChunk = input.readByte() != 0;
        int bitmask = input.readUnsignedShort();
        int dataLength = Type.VAR_INT.readPrimitive(input);

        // Data to be read
        BitSet usedSections = new BitSet(16);
        ChunkSection[] sections = new ChunkSection[16];
        int[] biomeData = null;

        // Calculate section count from bitmask
        for (int i = 0; i < 16; i++) {
            if ((bitmask & (1 << i)) != 0) {
                usedSections.set(i);
            }
        }
        int sectionCount = usedSections.cardinality(); // the amount of sections set

        // If the chunks is from a chunks bulk, it is never an unload packet
        // Other wise, if it has no data, it is :)
        boolean isBulkPacket = param.getBulkChunks().remove(chunkHash);
        if (sectionCount == 0 && fullChunk && !isBulkPacket && param.getLoadedChunks().contains(chunkHash)) {
            // This is a chunks unload packet
            param.getLoadedChunks().remove(chunkHash);
            return new Chunk1_8(chunkX, chunkZ);
        }

        int startIndex = input.readerIndex();
        param.getLoadedChunks().add(chunkHash); // mark chunks as loaded

        // Read blocks
        for (int i = 0; i < SECTION_COUNT; i++) {
            if (!usedSections.get(i)) continue; // Section not set
            ChunkSection section = Types1_8.CHUNK_SECTION.read(input);
            sections[i] = section;

            if (replacePistons) {
                section.replacePaletteEntry(36, replacementId);
            }
        }

        // Read block light
        for (int i = 0; i < SECTION_COUNT; i++) {
            if (!usedSections.get(i)) continue; // Section not set, has no light
            sections[i].getLight().readBlockLight(input);
        }

        // Read sky light
        int bytesLeft = dataLength - (input.readerIndex() - startIndex);
        if (bytesLeft >= ChunkSectionLight.LIGHT_LENGTH) {
            for (int i = 0; i < SECTION_COUNT; i++) {
                if (!usedSections.get(i)) continue; // Section not set, has no light
                sections[i].getLight().readSkyLight(input);
                bytesLeft -= ChunkSectionLight.LIGHT_LENGTH;
            }
        }

        // Read biome data
        if (bytesLeft >= BIOME_DATA_LENGTH) {
            biomeData = new int[BIOME_DATA_LENGTH];
            for (int i = 0; i < BIOME_DATA_LENGTH; i++) {
                biomeData[i] = input.readByte() & 0xFF;
            }
            bytesLeft -= BIOME_DATA_LENGTH;
        }

        // Check remaining bytes
        if (bytesLeft > 0) {
            Via.getPlatform().getLogger().log(Level.WARNING, bytesLeft + " Bytes left after reading chunks! (" + fullChunk + ")");
        }

        // Return chunks
        return new Chunk1_8(chunkX, chunkZ, fullChunk, bitmask, sections, biomeData, new ArrayList<CompoundTag>());
    }

    @Override
    public void write(ByteBuf output, ClientChunks param, Chunk input) throws Exception {
        if (!(input instanceof Chunk1_8)) throw new Exception("Incompatible chunk, " + input.getClass());

        Chunk1_8 chunk = (Chunk1_8) input;
        // Write primary info
        output.writeInt(chunk.getX());
        output.writeInt(chunk.getZ());
        if (chunk.isUnloadPacket()) return;
        output.writeByte(chunk.isFullChunk() ? 0x01 : 0x00);
        Type.VAR_INT.writePrimitive(output, chunk.getBitmask());

        ByteBuf buf = output.alloc().buffer();
        try {
            for (int i = 0; i < SECTION_COUNT; i++) {
                ChunkSection section = chunk.getSections()[i];
                if (section == null) continue; // Section not set
                Types1_9.CHUNK_SECTION.write(buf, section);
                section.getLight().writeBlockLight(buf);

                if (!section.getLight().hasSkyLight()) continue; // No sky light, we're done here.
                section.getLight().writeSkyLight(buf);
            }
            buf.readerIndex(0);
            Type.VAR_INT.writePrimitive(output, buf.readableBytes() + (chunk.hasBiomeData() ? 256 : 0));
            output.writeBytes(buf);
        } finally {
            buf.release(); // release buffer
        }

        // Write biome data
        if (chunk.hasBiomeData()) {
            for (int biome : chunk.getBiomeData()) {
                output.writeByte((byte) biome);
            }
        }
    }
}
