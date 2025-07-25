/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_9_3to1_10;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_9;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.remapper.ValueTransformer;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_9_3;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.v1_9_1to1_9_3.packet.ClientboundPackets1_9_3;
import com.viaversion.viaversion.protocols.v1_9_1to1_9_3.packet.ServerboundPackets1_9_3;
import com.viaversion.viaversion.protocols.v1_9_3to1_10.rewriter.EntityPacketRewriter1_10;
import com.viaversion.viaversion.protocols.v1_9_3to1_10.rewriter.ItemPacketRewriter1_10;
import com.viaversion.viaversion.protocols.v1_9_3to1_10.storage.ResourcePackTracker;

public class Protocol1_9_3To1_10 extends AbstractProtocol<ClientboundPackets1_9_3, ClientboundPackets1_9_3, ServerboundPackets1_9_3, ServerboundPackets1_9_3> {

    public static final ValueTransformer<Short, Float> TO_NEW_PITCH = new ValueTransformer<>(Types.FLOAT) {
        @Override
        public Float transform(PacketWrapper wrapper, Short inputValue) {
            return inputValue / 63.0F;
        }
    };
    private final EntityPacketRewriter1_10 entityRewriter = new EntityPacketRewriter1_10(this);
    private final ItemPacketRewriter1_10 itemRewriter = new ItemPacketRewriter1_10(this);

    public Protocol1_9_3To1_10() {
        super(ClientboundPackets1_9_3.class, ClientboundPackets1_9_3.class, ServerboundPackets1_9_3.class, ServerboundPackets1_9_3.class);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        // Named sound effect
        registerClientbound(ClientboundPackets1_9_3.CUSTOM_SOUND, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); // 0 - Sound name
                map(Types.VAR_INT); // 1 - Sound Category
                map(Types.INT); // 2 - x
                map(Types.INT); // 3 - y
                map(Types.INT); // 4 - z
                map(Types.FLOAT); // 5 - Volume
                map(Types.UNSIGNED_BYTE, TO_NEW_PITCH); // 6 - Pitch
            }
        });

        // Sound effect
        registerClientbound(ClientboundPackets1_9_3.SOUND, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Sound name
                map(Types.VAR_INT); // 1 - Sound Category
                map(Types.INT); // 2 - x
                map(Types.INT); // 3 - y
                map(Types.INT); // 4 - z
                map(Types.FLOAT); // 5 - Volume
                map(Types.UNSIGNED_BYTE, TO_NEW_PITCH); // 6 - Pitch

                handler(wrapper -> {
                    int id = wrapper.get(Types.VAR_INT, 0);
                    wrapper.set(Types.VAR_INT, 0, getNewSoundId(id));
                });
            }
        });

        // Join Game
        registerClientbound(ClientboundPackets1_9_3.LOGIN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); // 0 - Entity ID
                map(Types.UNSIGNED_BYTE); // 1 - Gamemode
                map(Types.INT); // 2 - Dimension

                handler(wrapper -> {
                    ClientWorld clientWorld = wrapper.user().getClientWorld(Protocol1_9_3To1_10.class);

                    int dimensionId = wrapper.get(Types.INT, 1);
                    clientWorld.setEnvironment(dimensionId);
                });
            }
        });

        // Respawn
        registerClientbound(ClientboundPackets1_9_3.RESPAWN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); // 0 - Dimension ID

                handler(wrapper -> {
                    ClientWorld clientWorld = wrapper.user().getClientWorld(Protocol1_9_3To1_10.class);

                    int dimensionId = wrapper.get(Types.INT, 0);
                    clientWorld.setEnvironment(dimensionId);
                });
            }
        });

        // Chunk Data
        registerClientbound(ClientboundPackets1_9_3.LEVEL_CHUNK, wrapper -> {
            ClientWorld clientWorld = wrapper.user().getClientWorld(Protocol1_9_3To1_10.class);
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
                map(Types.STRING); // 0 - URL
                map(Types.STRING); // 1 - Hash

                handler(wrapper -> {
                    ResourcePackTracker tracker = wrapper.user().get(ResourcePackTracker.class);
                    tracker.setLastHash(wrapper.get(Types.STRING, 1)); // Store the hash for resourcepack status
                });
            }
        });

        // Packet ResourcePack status
        registerServerbound(ServerboundPackets1_9_3.RESOURCE_PACK, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    ResourcePackTracker tracker = wrapper.user().get(ResourcePackTracker.class);
                    wrapper.write(Types.STRING, tracker.getLastHash());
                    wrapper.write(Types.VAR_INT, wrapper.read(Types.VAR_INT));
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
        addEntityTracker(userConnection, new EntityTrackerBase(userConnection, EntityTypes1_9.EntityType.PLAYER));
        userConnection.addClientWorld(this.getClass(), new ClientWorld());

        userConnection.put(new ResourcePackTracker());
    }

    @Override
    public ItemPacketRewriter1_10 getItemRewriter() {
        return itemRewriter;
    }

    @Override
    public EntityPacketRewriter1_10 getEntityRewriter() {
        return entityRewriter;
    }
}
