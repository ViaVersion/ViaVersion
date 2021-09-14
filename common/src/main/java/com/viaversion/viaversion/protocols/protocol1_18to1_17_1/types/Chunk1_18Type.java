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
package com.viaversion.viaversion.protocols.protocol1_18to1_17_1.types;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk1_18;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.minecraft.BaseChunkType;
import com.viaversion.viaversion.api.type.types.version.Types1_18;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

public final class Chunk1_18Type extends Type<Chunk> {
    private final int ySectionCount;

    public Chunk1_18Type(final int ySectionCount) {
        super(Chunk.class);
        Preconditions.checkArgument(ySectionCount > 0);
        this.ySectionCount = ySectionCount;
    }

    @Override
    public Chunk read(final ByteBuf input) throws Exception {
        final int chunkX = input.readInt();
        final int chunkZ = input.readInt();
        final CompoundTag heightMap = Type.NBT.read(input);

        Type.VAR_INT.readPrimitive(input); // Data size in bytes

        // Read sections
        final ChunkSection[] sections = new ChunkSection[ySectionCount];
        for (int i = 0; i < ySectionCount; i++) {
            sections[i] = Types1_18.CHUNK_SECTION.read(input);
        }

        final int blockEntitiesLength = Type.VAR_INT.readPrimitive(input);
        final List<BlockEntity> blockEntities = new ArrayList<>(blockEntitiesLength);
        for (int i = 0; i < blockEntitiesLength; i++) {
            blockEntities.add(Types1_18.BLOCK_ENTITY.read(input));
        }

        // Read all the remaining bytes (workaround for #681)
        if (input.readableBytes() > 0) {
            final byte[] array = Type.REMAINING_BYTES.read(input);
            if (Via.getManager().isDebug()) {
                Via.getPlatform().getLogger().warning("Found " + array.length + " more bytes than expected while reading the chunk: " + chunkX + "/" + chunkZ);
            }
        }

        return new Chunk1_18(chunkX, chunkZ, sections, heightMap, blockEntities);
    }

    @Override
    public void write(final ByteBuf output, final Chunk chunk) throws Exception {
        output.writeInt(chunk.getX());
        output.writeInt(chunk.getZ());

        Type.NBT.write(output, chunk.getHeightMap());

        final ByteBuf buf = output.alloc().buffer();
        try {
            for (final ChunkSection section : chunk.getSections()) {
                Types1_18.CHUNK_SECTION.write(buf, section);
            }
            buf.readerIndex(0);
            Type.VAR_INT.writePrimitive(output, buf.readableBytes());
            output.writeBytes(buf);
        } finally {
            buf.release(); // release buffer
        }

        for (final BlockEntity blockEntity : chunk.blockEntities()) {
            Types1_18.BLOCK_ENTITY.write(output, blockEntity);
        }
    }

    @Override
    public Class<? extends Type> getBaseClass() {
        return BaseChunkType.class;
    }
}
