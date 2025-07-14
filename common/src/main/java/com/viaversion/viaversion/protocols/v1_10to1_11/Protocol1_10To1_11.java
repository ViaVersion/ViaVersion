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
package com.viaversion.viaversion.protocols.v1_10to1_11;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.remapper.ValueTransformer;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_9_3;
import com.viaversion.viaversion.protocols.v1_10to1_11.data.BlockEntityMappings1_11;
import com.viaversion.viaversion.protocols.v1_10to1_11.data.EntityMappings1_11;
import com.viaversion.viaversion.protocols.v1_10to1_11.data.PotionColorMappings1_11;
import com.viaversion.viaversion.protocols.v1_10to1_11.rewriter.EntityPacketRewriter1_11;
import com.viaversion.viaversion.protocols.v1_10to1_11.rewriter.ItemPacketRewriter1_11;
import com.viaversion.viaversion.protocols.v1_10to1_11.storage.EntityTracker1_11;
import com.viaversion.viaversion.protocols.v1_9_1to1_9_3.packet.ClientboundPackets1_9_3;
import com.viaversion.viaversion.protocols.v1_9_1to1_9_3.packet.ServerboundPackets1_9_3;
import com.viaversion.viaversion.rewriter.SoundRewriter;

public class Protocol1_10To1_11 extends AbstractProtocol<ClientboundPackets1_9_3, ClientboundPackets1_9_3, ServerboundPackets1_9_3, ServerboundPackets1_9_3> {

    private static final ValueTransformer<Float, Short> toOldByte = new ValueTransformer<>(Types.UNSIGNED_BYTE) {
        @Override
        public Short transform(PacketWrapper wrapper, Float inputValue) {
            return (short) (inputValue * 16);
        }
    };

    private final EntityPacketRewriter1_11 entityRewriter = new EntityPacketRewriter1_11(this);
    private final ItemPacketRewriter1_11 itemRewriter = new ItemPacketRewriter1_11(this);

    public Protocol1_10To1_11() {
        super(ClientboundPackets1_9_3.class, ClientboundPackets1_9_3.class, ServerboundPackets1_9_3.class, ServerboundPackets1_9_3.class);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        new SoundRewriter<>(this, this::getNewSoundId).registerSound(ClientboundPackets1_9_3.SOUND);

        registerClientbound(ClientboundPackets1_9_3.SET_TITLES, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Action

                handler(wrapper -> {
                    int action = wrapper.get(Types.VAR_INT, 0);

                    // Handle the new ActionBar
                    if (action >= 2) {
                        wrapper.set(Types.VAR_INT, 0, action + 1);
                    }
                });
            }
        });

        registerClientbound(ClientboundPackets1_9_3.BLOCK_EVENT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.BLOCK_POSITION1_8); // 0 - Position
                map(Types.UNSIGNED_BYTE); // 1 - Action ID
                map(Types.UNSIGNED_BYTE); // 2 - Action Param
                map(Types.VAR_INT); // 3 - Block Type

                // Cheap hack to ensure it's always right block
                handler(actionWrapper -> {
                    if (Via.getConfig().isPistonAnimationPatch()) {
                        int id = actionWrapper.get(Types.VAR_INT, 0);
                        if (id == 33 || id == 29) {
                            actionWrapper.cancel();
                        }
                    }
                });
            }
        });

        registerClientbound(ClientboundPackets1_9_3.LEVEL_CHUNK, wrapper -> {
            ClientWorld clientWorld = wrapper.user().getClientWorld(Protocol1_10To1_11.class);

            Chunk chunk = wrapper.passthrough(ChunkType1_9_3.forEnvironment(clientWorld.getEnvironment()));

            if (chunk.getBlockEntities() == null) return;
            for (CompoundTag tag : chunk.getBlockEntities()) {
                StringTag idTag = tag.getStringTag("id");
                if (idTag == null) {
                    continue;
                }

                String identifier = idTag.getValue();
                if (identifier.equals("MobSpawner")) {
                    EntityMappings1_11.toClientSpawner(tag);
                }

                // Handle new identifier
                idTag.setValue(BlockEntityMappings1_11.toNewIdentifier(identifier));
            }
        });

        this.registerClientbound(ClientboundPackets1_9_3.LEVEL_EVENT, new PacketHandlers() {
            @Override
            public void register() {
                this.map(Types.INT); //effectID
                this.map(Types.BLOCK_POSITION1_8); //pos
                this.map(Types.INT); //effectData
                this.map(Types.BOOLEAN); //serverwide / global
                handler(packetWrapper -> {
                    int effectID = packetWrapper.get(Types.INT, 0);
                    if (effectID == 2002) {
                        int data = packetWrapper.get(Types.INT, 1);
                        boolean isInstant = false;
                        PotionColorMappings1_11.PotionData newData = PotionColorMappings1_11.getNewData(data);
                        if (newData == null) {
                            getLogger().warning("Received unknown potion data: " + data);
                            data = 0;
                        } else {
                            data = newData.data();
                            isInstant = newData.instant();
                        }
                        if (isInstant) {
                            packetWrapper.set(Types.INT, 0, 2007);
                        }
                        packetWrapper.set(Types.INT, 1, data);
                    }
                });
            }
        });

        /*
            INCOMING PACKETS
        */

        registerServerbound(ServerboundPackets1_9_3.USE_ITEM_ON, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.BLOCK_POSITION1_8); // 0 - Location
                map(Types.VAR_INT); // 1 - Face
                map(Types.VAR_INT); // 2 - Hand

                map(Types.FLOAT, toOldByte);
                map(Types.FLOAT, toOldByte);
                map(Types.FLOAT, toOldByte);
            }
        });

        registerServerbound(ServerboundPackets1_9_3.CHAT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); // 0 - Message
                handler(wrapper -> {
                    // 100-character limit on older servers
                    String msg = wrapper.get(Types.STRING, 0);
                    if (msg.length() > 100) {
                        wrapper.set(Types.STRING, 0, msg.substring(0, 100));
                    }
                });
            }
        });
    }

    private int getNewSoundId(int id) {
        if (id == 196) // Experience orb sound got removed
            return -1;

        if (id >= 85) // Shulker boxes
            id += 2;
        if (id >= 176) // Guardian flop
            id += 1;
        if (id >= 197) // evocation things
            id += 8;
        if (id >= 207) // Rip the Experience orb touch sound :'(
            id -= 1;
        if (id >= 279) // Liama's
            id += 9;
        if (id >= 296) // Mule chest
            id += 1;
        if (id >= 390) // Vex
            id += 4;
        if (id >= 400) // vindication
            id += 3;
        if (id >= 450) // Elytra
            id += 1;
        if (id >= 455) // Empty bottle
            id += 1;
        if (id >= 470) // Totem use
            id += 1;
        return id;
    }


    @Override
    public void init(UserConnection userConnection) {
        userConnection.addEntityTracker(this.getClass(), new EntityTracker1_11(userConnection));
        userConnection.addClientWorld(this.getClass(), new ClientWorld());
    }

    @Override
    public EntityPacketRewriter1_11 getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    public ItemPacketRewriter1_11 getItemRewriter() {
        return itemRewriter;
    }
}
