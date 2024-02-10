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
package com.viaversion.viaversion.protocols.protocol1_10to1_9_3;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.remapper.ValueTransformer;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_9_3;
import com.viaversion.viaversion.api.type.types.version.Types1_9;
import com.viaversion.viaversion.protocols.protocol1_10to1_9_3.packets.InventoryPackets;
import com.viaversion.viaversion.protocols.protocol1_10to1_9_3.storage.ResourcePackTracker;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.ClientboundPackets1_9_3;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.ServerboundPackets1_9_3;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Protocol1_10To1_9_3_4 extends AbstractProtocol<ClientboundPackets1_9_3, ClientboundPackets1_9_3, ServerboundPackets1_9_3, ServerboundPackets1_9_3> {

    public static final ValueTransformer<Short, Float> TO_NEW_PITCH = new ValueTransformer<Short, Float>(Type.FLOAT) {
        @Override
        public Float transform(PacketWrapper wrapper, Short inputValue) throws Exception {
            return inputValue / 63.0F;
        }
    };
    public static final ValueTransformer<List<Metadata>, List<Metadata>> TRANSFORM_METADATA = new ValueTransformer<List<Metadata>, List<Metadata>>(Types1_9.METADATA_LIST) {
        @Override
        public List<Metadata> transform(PacketWrapper wrapper, List<Metadata> inputValue) throws Exception {
            List<Metadata> metaList = new CopyOnWriteArrayList<>(inputValue);
            for (Metadata m : metaList) {
                if (m.id() >= 5)
                    m.setId(m.id() + 1);
            }
            return metaList;
        }
    };
    private final InventoryPackets itemRewriter = new InventoryPackets(this);

    public Protocol1_10To1_9_3_4() {
        super(ClientboundPackets1_9_3.class, ClientboundPackets1_9_3.class, ServerboundPackets1_9_3.class, ServerboundPackets1_9_3.class);
    }

    @Override
    protected void registerPackets() {
        itemRewriter.register();

        // Named sound effect
        registerClientbound(ClientboundPackets1_9_3.NAMED_SOUND, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // 0 - Sound name
                map(Type.VAR_INT); // 1 - Sound Category
                map(Type.INT); // 2 - x
                map(Type.INT); // 3 - y
                map(Type.INT); // 4 - z
                map(Type.FLOAT); // 5 - Volume
                map(Type.UNSIGNED_BYTE, TO_NEW_PITCH); // 6 - Pitch
            }
        });

        // Sound effect
        registerClientbound(ClientboundPackets1_9_3.SOUND, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Sound name
                map(Type.VAR_INT); // 1 - Sound Category
                map(Type.INT); // 2 - x
                map(Type.INT); // 3 - y
                map(Type.INT); // 4 - z
                map(Type.FLOAT); // 5 - Volume
                map(Type.UNSIGNED_BYTE, TO_NEW_PITCH); // 6 - Pitch

                handler(wrapper -> {
                    int id = wrapper.get(Type.VAR_INT, 0);
                    wrapper.set(Type.VAR_INT, 0, getNewSoundId(id));
                });
            }
        });

        // Metadata packet
        registerClientbound(ClientboundPackets1_9_3.ENTITY_METADATA, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Types1_9.METADATA_LIST, TRANSFORM_METADATA); // 1 - Metadata list
            }
        });

        // Spawn Mob
        registerClientbound(ClientboundPackets1_9_3.SPAWN_MOB, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity id
                map(Type.UUID); // 1 - UUID
                map(Type.UNSIGNED_BYTE); // 2 - Entity Type
                map(Type.DOUBLE); // 3 - X
                map(Type.DOUBLE); // 4 - Y
                map(Type.DOUBLE); // 5 - Z
                map(Type.BYTE); // 6 - Yaw
                map(Type.BYTE); // 7 - Pitch
                map(Type.BYTE); // 8 - Head Pitch
                map(Type.SHORT); // 9 - Velocity X
                map(Type.SHORT); // 10 - Velocity Y
                map(Type.SHORT); // 11 - Velocity Z
                map(Types1_9.METADATA_LIST, TRANSFORM_METADATA); // 12 - Metadata
            }
        });

        // Spawn Player
        registerClientbound(ClientboundPackets1_9_3.SPAWN_PLAYER, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.UUID); // 1 - Player UUID
                map(Type.DOUBLE); // 2 - X
                map(Type.DOUBLE); // 3 - Y
                map(Type.DOUBLE); // 4 - Z
                map(Type.BYTE); // 5 - Yaw
                map(Type.BYTE); // 6 - Pitch
                map(Types1_9.METADATA_LIST, TRANSFORM_METADATA); // 7 - Metadata list
            }
        });

        // Join Game
        registerClientbound(ClientboundPackets1_9_3.JOIN_GAME, new PacketHandlers() {
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

        // Respawn
        registerClientbound(ClientboundPackets1_9_3.RESPAWN, new PacketHandlers() {
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

        // Chunk Data
        registerClientbound(ClientboundPackets1_9_3.CHUNK_DATA, wrapper -> {
            ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
            Chunk chunk = wrapper.passthrough(ChunkType1_9_3.forEnvironment(clientWorld.getEnvironment()));

            if (Via.getConfig().isReplacePistons()) {
                int replacementId = Via.getConfig().getPistonReplacementId();
                for (ChunkSection section : chunk.getSections()) {
                    if (section == null) continue;
                    section.palette(PaletteType.BLOCKS).replaceId(36, replacementId);
                }
            }
        });

        // Packet Send ResourcePack
        registerClientbound(ClientboundPackets1_9_3.RESOURCE_PACK, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // 0 - URL
                map(Type.STRING); // 1 - Hash

                handler(wrapper -> {
                    ResourcePackTracker tracker = wrapper.user().get(ResourcePackTracker.class);
                    tracker.setLastHash(wrapper.get(Type.STRING, 1)); // Store the hash for resourcepack status
                });
            }
        });

        // Packet ResourcePack status
        registerServerbound(ServerboundPackets1_9_3.RESOURCE_PACK_STATUS, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    ResourcePackTracker tracker = wrapper.user().get(ResourcePackTracker.class);
                    wrapper.write(Type.STRING, tracker.getLastHash());
                    wrapper.write(Type.VAR_INT, wrapper.read(Type.VAR_INT));
                });
            }
        });
    }

    public int getNewSoundId(int id) {
        int newId = id;
        if (id >= 24) //Blame the enchantment table sound
            newId += 1;
        if (id >= 248) //Blame the husk
            newId += 4;
        if (id >= 296) //Blame the polar bear
            newId += 6;
        if (id >= 354) //Blame the stray
            newId += 4;
        if (id >= 372) //Blame the wither skeleton
            newId += 4;
        return newId;
    }

    @Override
    public void init(UserConnection userConnection) {
        userConnection.put(new ResourcePackTracker());

        if (!userConnection.has(ClientWorld.class)) {
            userConnection.put(new ClientWorld());
        }
    }

    @Override
    public InventoryPackets getItemRewriter() {
        return itemRewriter;
    }
}
