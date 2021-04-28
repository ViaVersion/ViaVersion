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
package com.viaversion.viaversion.protocols.protocol1_9_1_2to1_9_3_4;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_9_1_2to1_9_3_4.chunks.BlockEntity;
import com.viaversion.viaversion.protocols.protocol1_9_1_2to1_9_3_4.types.Chunk1_9_3_4Type;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.ClientboundPackets1_9_3;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.ServerboundPackets1_9_3;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.types.Chunk1_9_1_2Type;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ServerboundPackets1_9;

// Goes BACKWARDS from 1.9.3/4 to 1.9.1/2
public class Protocol1_9_1_2To1_9_3_4 extends AbstractProtocol<ClientboundPackets1_9_3, ClientboundPackets1_9, ServerboundPackets1_9_3, ServerboundPackets1_9> {

    public Protocol1_9_1_2To1_9_3_4() {
        super(ClientboundPackets1_9_3.class, ClientboundPackets1_9.class, ServerboundPackets1_9_3.class, ServerboundPackets1_9.class);
    }

    @Override
    protected void registerPackets() {
        registerClientbound(ClientboundPackets1_9_3.BLOCK_ENTITY_DATA, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION); //Position
                map(Type.UNSIGNED_BYTE); //Type
                map(Type.NBT); //NBT
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        if (wrapper.get(Type.UNSIGNED_BYTE, 0) == 9) {
                            Position position = wrapper.get(Type.POSITION, 0);
                            CompoundTag tag = wrapper.get(Type.NBT, 0);

                            wrapper.clearPacket(); //Clear the packet

                            wrapper.setId(ClientboundPackets1_9.UPDATE_SIGN.ordinal()); //Update sign packet
                            wrapper.write(Type.POSITION, position); // Position
                            for (int i = 1; i < 5; i++) {
                                // Should technically be written as COMPONENT, but left as String for simplification/to remove redundant wrapping for VR
                                wrapper.write(Type.STRING, (String) tag.get("Text" + i).getValue()); // Sign line
                            }
                        }
                    }
                });
            }
        });

        registerClientbound(ClientboundPackets1_9_3.CHUNK_DATA, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);

                        Chunk1_9_3_4Type newType = new Chunk1_9_3_4Type(clientWorld);
                        Chunk1_9_1_2Type oldType = new Chunk1_9_1_2Type(clientWorld); // Get the old type to not write Block Entities

                        Chunk chunk = wrapper.read(newType);
                        wrapper.write(oldType, chunk);
                        BlockEntity.handle(chunk.getBlockEntities(), wrapper.user());
                    }
                });
            }
        });

        registerClientbound(ClientboundPackets1_9_3.JOIN_GAME, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // 0 - Entity ID
                map(Type.UNSIGNED_BYTE); // 1 - Gamemode
                map(Type.INT); // 2 - Dimension

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        ClientWorld clientChunks = wrapper.user().get(ClientWorld.class);

                        int dimensionId = wrapper.get(Type.INT, 1);
                        clientChunks.setEnvironment(dimensionId);
                    }
                });
            }
        });

        registerClientbound(ClientboundPackets1_9_3.RESPAWN, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // 0 - Dimension ID

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);

                        int dimensionId = wrapper.get(Type.INT, 0);
                        clientWorld.setEnvironment(dimensionId);
                    }
                });
            }
        });
    }

    @Override
    public void init(UserConnection userConnection) {
        if (!userConnection.has(ClientWorld.class)) {
            userConnection.put(new ClientWorld(userConnection));
        }
    }
}
