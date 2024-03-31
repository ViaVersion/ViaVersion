/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.google.gson.JsonElement;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.remapper.ValueTransformer;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_9_1;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_9_3;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.data.FakeTileEntity;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ServerboundPackets1_9;
import java.util.List;

public class Protocol1_9_3To1_9_1_2 extends AbstractProtocol<ClientboundPackets1_9, ClientboundPackets1_9_3, ServerboundPackets1_9, ServerboundPackets1_9_3> {

    public static final ValueTransformer<Short, Short> ADJUST_PITCH = new ValueTransformer<Short, Short>(Type.UNSIGNED_BYTE, Type.UNSIGNED_BYTE) {
        @Override
        public Short transform(PacketWrapper wrapper, Short inputValue) throws Exception {
            return (short) Math.round(inputValue / 63.5F * 63.0F);
        }
    };

    public Protocol1_9_3To1_9_1_2() {
        super(ClientboundPackets1_9.class, ClientboundPackets1_9_3.class, ServerboundPackets1_9.class, ServerboundPackets1_9_3.class);
    }

    @Override
    protected void registerPackets() {
        // Sign update packet
        registerClientbound(ClientboundPackets1_9.UPDATE_SIGN, null, wrapper -> {
            //read data
            Position position = wrapper.read(Type.POSITION1_8);
            JsonElement[] lines = new JsonElement[4];
            for (int i = 0; i < 4; i++) {
                lines[i] = wrapper.read(Type.COMPONENT);
            }

            wrapper.clearInputBuffer();

            //write data
            wrapper.setPacketType(ClientboundPackets1_9_3.BLOCK_ENTITY_DATA);
            wrapper.write(Type.POSITION1_8, position); //Block location
            wrapper.write(Type.UNSIGNED_BYTE, (short) 9); //Action type (9 update sign)

            //Create nbt
            CompoundTag tag = new CompoundTag();
            tag.put("id", new StringTag("Sign"));
            tag.put("x", new IntTag(position.x()));
            tag.put("y", new IntTag(position.y()));
            tag.put("z", new IntTag(position.z()));
            for (int i = 0; i < lines.length; i++) {
                tag.put("Text" + (i + 1), new StringTag(lines[i].toString()));
            }

            wrapper.write(Type.NAMED_COMPOUND_TAG, tag);
        });

        registerClientbound(ClientboundPackets1_9.CHUNK_DATA, wrapper -> {
            ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);

            Chunk chunk = wrapper.read(ChunkType1_9_1.forEnvironment(clientWorld.getEnvironment()));
            wrapper.write(ChunkType1_9_3.forEnvironment(clientWorld.getEnvironment()), chunk);

            List<CompoundTag> tags = chunk.getBlockEntities();
            for (int s = 0; s < chunk.getSections().length; s++) {
                ChunkSection section = chunk.getSections()[s];
                if (section == null) continue;
                DataPalette blocks = section.palette(PaletteType.BLOCKS);

                for (int idx = 0; idx < ChunkSection.SIZE; idx++) {
                    int id = blocks.idAt(idx) >> 4;
                    if (FakeTileEntity.isTileEntity(id)) {
                        tags.add(FakeTileEntity.createTileEntity(
                                ChunkSection.xFromIndex(idx) + (chunk.getX() << 4),
                                ChunkSection.yFromIndex(idx) + (s << 4),
                                ChunkSection.zFromIndex(idx) + (chunk.getZ() << 4),
                                id
                        ));
                    }
                }
            }
        });

        registerClientbound(ClientboundPackets1_9.JOIN_GAME, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); // 0 - Entity ID
                map(Type.UNSIGNED_BYTE); // 1 - Gamemode
                map(Type.INT); // 2 - Dimension

                handler(wrapper -> {
                    ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
                    int dimensionId = wrapper.get(Type.INT, 1);
                    clientWorld.setEnvironment(dimensionId);
                });
            }
        });

        registerClientbound(ClientboundPackets1_9.RESPAWN, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); // 0 - Dimension ID
                handler(wrapper -> {
                    ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
                    int dimensionId = wrapper.get(Type.INT, 0);
                    clientWorld.setEnvironment(dimensionId);
                });
            }
        });

        // Sound effect
        registerClientbound(ClientboundPackets1_9.SOUND, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Sound name
                map(Type.VAR_INT); // 1 - Sound Category
                map(Type.INT); // 2 - x
                map(Type.INT); // 3 - y
                map(Type.INT); // 4 - z
                map(Type.FLOAT); // 5 - Volume
                map(ADJUST_PITCH); // 6 - Pitch
            }
        });
    }

    @Override
    public void init(UserConnection user) {
        if (!user.has(ClientWorld.class)) {
            user.put(new ClientWorld());
        }
    }
}
