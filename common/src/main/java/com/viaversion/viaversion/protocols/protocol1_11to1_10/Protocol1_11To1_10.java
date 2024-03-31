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
package com.viaversion.viaversion.protocols.protocol1_11to1_10;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_11;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.remapper.ValueTransformer;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_9_3;
import com.viaversion.viaversion.api.type.types.version.Types1_9;
import com.viaversion.viaversion.protocols.protocol1_11to1_10.data.PotionColorMapping;
import com.viaversion.viaversion.protocols.protocol1_11to1_10.metadata.MetadataRewriter1_11To1_10;
import com.viaversion.viaversion.protocols.protocol1_11to1_10.packets.InventoryPackets;
import com.viaversion.viaversion.protocols.protocol1_11to1_10.rewriter.BlockEntityRewriter;
import com.viaversion.viaversion.protocols.protocol1_11to1_10.rewriter.EntityIdRewriter;
import com.viaversion.viaversion.protocols.protocol1_11to1_10.storage.EntityTracker1_11;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.ClientboundPackets1_9_3;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.ServerboundPackets1_9_3;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.util.Pair;

public class Protocol1_11To1_10 extends AbstractProtocol<ClientboundPackets1_9_3, ClientboundPackets1_9_3, ServerboundPackets1_9_3, ServerboundPackets1_9_3> {
    private static final ValueTransformer<Float, Short> toOldByte = new ValueTransformer<Float, Short>(Type.UNSIGNED_BYTE) {
        @Override
        public Short transform(PacketWrapper wrapper, Float inputValue) throws Exception {
            return (short) (inputValue * 16);
        }
    };

    private final MetadataRewriter1_11To1_10 entityRewriter = new MetadataRewriter1_11To1_10(this);
    private final InventoryPackets itemRewriter = new InventoryPackets(this);

    public Protocol1_11To1_10() {
        super(ClientboundPackets1_9_3.class, ClientboundPackets1_9_3.class, ServerboundPackets1_9_3.class, ServerboundPackets1_9_3.class);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        registerClientbound(ClientboundPackets1_9_3.SPAWN_ENTITY, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity id
                map(Type.UUID); // 1 - UUID
                map(Type.BYTE); // 2 - Type

                // Track Entity
                handler(entityRewriter.objectTrackerHandler());
            }
        });

        registerClientbound(ClientboundPackets1_9_3.SPAWN_MOB, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.UUID); // 1 - Entity UUID
                map(Type.UNSIGNED_BYTE, Type.VAR_INT); // 2 - Entity Type
                map(Type.DOUBLE); // 3 - X
                map(Type.DOUBLE); // 4 - Y
                map(Type.DOUBLE); // 5 - Z
                map(Type.BYTE); // 6 - Yaw
                map(Type.BYTE); // 7 - Pitch
                map(Type.BYTE); // 8 - Head Pitch
                map(Type.SHORT); // 9 - Velocity X
                map(Type.SHORT); // 10 - Velocity Y
                map(Type.SHORT); // 11 - Velocity Z
                map(Types1_9.METADATA_LIST); // 12 - Metadata

                handler(wrapper -> {
                    int entityId = wrapper.get(Type.VAR_INT, 0);
                    // Change Type :)
                    int type = wrapper.get(Type.VAR_INT, 1);

                    EntityTypes1_11.EntityType entType = MetadataRewriter1_11To1_10.rewriteEntityType(type, wrapper.get(Types1_9.METADATA_LIST, 0));
                    if (entType != null) {
                        wrapper.set(Type.VAR_INT, 1, entType.getId());

                        // Register Type ID
                        wrapper.user().getEntityTracker(Protocol1_11To1_10.class).addEntity(entityId, entType);
                        entityRewriter.handleMetadata(entityId, wrapper.get(Types1_9.METADATA_LIST, 0), wrapper.user());
                    }
                });
            }
        });

        new SoundRewriter<>(this, this::getNewSoundId).registerSound(ClientboundPackets1_9_3.SOUND);

        registerClientbound(ClientboundPackets1_9_3.COLLECT_ITEM, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Collected entity id
                map(Type.VAR_INT); // 1 - Collector entity id

                handler(wrapper -> {
                    wrapper.write(Type.VAR_INT, 1); // 2 - Pickup Count
                });
            }
        });

        entityRewriter.registerMetadataRewriter(ClientboundPackets1_9_3.ENTITY_METADATA, Types1_9.METADATA_LIST);

        registerClientbound(ClientboundPackets1_9_3.ENTITY_TELEPORT, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity id
                map(Type.DOUBLE); // 1 - x
                map(Type.DOUBLE); // 2 - y
                map(Type.DOUBLE); // 3 - z
                map(Type.BYTE); // 4 - yaw
                map(Type.BYTE); // 5 - pitch
                map(Type.BOOLEAN); // 6 - onGround

                handler(wrapper -> {
                    int entityID = wrapper.get(Type.VAR_INT, 0);
                    if (Via.getConfig().isHologramPatch()) {
                        EntityTracker1_11 tracker = wrapper.user().getEntityTracker(Protocol1_11To1_10.class);
                        if (tracker.isHologram(entityID)) {
                            Double newValue = wrapper.get(Type.DOUBLE, 1);
                            newValue -= (Via.getConfig().getHologramYOffset());
                            wrapper.set(Type.DOUBLE, 1, newValue);
                        }
                    }
                });
            }
        });

        entityRewriter.registerRemoveEntities(ClientboundPackets1_9_3.DESTROY_ENTITIES);

        registerClientbound(ClientboundPackets1_9_3.TITLE, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Action

                handler(wrapper -> {
                    int action = wrapper.get(Type.VAR_INT, 0);

                    // Handle the new ActionBar
                    if (action >= 2) {
                        wrapper.set(Type.VAR_INT, 0, action + 1);
                    }
                });
            }
        });

        registerClientbound(ClientboundPackets1_9_3.BLOCK_ACTION, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.POSITION1_8); // 0 - Position
                map(Type.UNSIGNED_BYTE); // 1 - Action ID
                map(Type.UNSIGNED_BYTE); // 2 - Action Param
                map(Type.VAR_INT); // 3 - Block Type

                // Cheap hack to ensure it's always right block
                handler(actionWrapper -> {
                    if (Via.getConfig().isPistonAnimationPatch()) {
                        int id = actionWrapper.get(Type.VAR_INT, 0);
                        if (id == 33 || id == 29) {
                            actionWrapper.cancel();
                        }
                    }
                });
            }
        });

        registerClientbound(ClientboundPackets1_9_3.BLOCK_ENTITY_DATA, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.POSITION1_8); // 0 - Position
                map(Type.UNSIGNED_BYTE); // 1 - Action
                map(Type.NAMED_COMPOUND_TAG); // 2 - NBT data

                handler(wrapper -> {
                    CompoundTag tag = wrapper.get(Type.NAMED_COMPOUND_TAG, 0);
                    if (wrapper.get(Type.UNSIGNED_BYTE, 0) == 1) {
                        EntityIdRewriter.toClientSpawner(tag);
                    }

                    StringTag idTag = tag.getStringTag("id");
                    if (idTag != null) {
                        // Handle new identifier
                        idTag.setValue(BlockEntityRewriter.toNewIdentifier(idTag.getValue()));
                    }
                });
            }
        });

        registerClientbound(ClientboundPackets1_9_3.CHUNK_DATA, wrapper -> {
            ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);

            Chunk chunk = wrapper.passthrough(ChunkType1_9_3.forEnvironment(clientWorld.getEnvironment()));

            if (chunk.getBlockEntities() == null) return;
            for (CompoundTag tag : chunk.getBlockEntities()) {
                StringTag idTag = tag.getStringTag("id");
                if (idTag == null) {
                    continue;
                }

                String identifier = idTag.getValue();
                if (identifier.equals("MobSpawner")) {
                    EntityIdRewriter.toClientSpawner(tag);
                }

                // Handle new identifier
                idTag.setValue(BlockEntityRewriter.toNewIdentifier(identifier));
            }
        });

        registerClientbound(ClientboundPackets1_9_3.JOIN_GAME, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); // 0 - Entity ID
                map(Type.UNSIGNED_BYTE); // 1 - Gamemode
                map(Type.INT); // 2 - Dimension
                handler(wrapper -> {
                    ClientWorld clientChunks = wrapper.user().get(ClientWorld.class);
                    int dimensionId = wrapper.get(Type.INT, 1);
                    clientChunks.setEnvironment(dimensionId);
                });
            }
        });
        registerClientbound(ClientboundPackets1_9_3.RESPAWN, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT);
                handler(wrapper -> {
                    ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
                    int dimensionId = wrapper.get(Type.INT, 0);
                    clientWorld.setEnvironment(dimensionId);
                });
            }
        });

        this.registerClientbound(ClientboundPackets1_9_3.EFFECT, new PacketHandlers() {
            @Override
            public void register() {
                this.map(Type.INT); //effectID
                this.map(Type.POSITION1_8); //pos
                this.map(Type.INT); //effectData
                this.map(Type.BOOLEAN); //serverwide / global
                handler(packetWrapper -> {
                    int effectID = packetWrapper.get(Type.INT, 0);
                    if (effectID == 2002) {
                        int data = packetWrapper.get(Type.INT, 1);
                        boolean isInstant = false;
                        Pair<Integer, Boolean> newData = PotionColorMapping.getNewData(data);
                        if (newData == null) {
                            Via.getPlatform().getLogger().warning("Received unknown 1.11 -> 1.10.2 potion data (" + data + ")");
                            data = 0;
                        } else {
                            data = newData.key();
                            isInstant = newData.value();
                        }
                        if (isInstant) {
                            packetWrapper.set(Type.INT, 0, 2007);
                        }
                        packetWrapper.set(Type.INT, 1, data);
                    }
                });
            }
        });

        /*
            INCOMING PACKETS
        */

        registerServerbound(ServerboundPackets1_9_3.PLAYER_BLOCK_PLACEMENT, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.POSITION1_8); // 0 - Location
                map(Type.VAR_INT); // 1 - Face
                map(Type.VAR_INT); // 2 - Hand

                map(Type.FLOAT, toOldByte);
                map(Type.FLOAT, toOldByte);
                map(Type.FLOAT, toOldByte);
            }
        });

        registerServerbound(ServerboundPackets1_9_3.CHAT_MESSAGE, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // 0 - Message
                handler(wrapper -> {
                    // 100 character limit on older servers
                    String msg = wrapper.get(Type.STRING, 0);
                    if (msg.length() > 100) {
                        wrapper.set(Type.STRING, 0, msg.substring(0, 100));
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
        // Entity tracker
        userConnection.addEntityTracker(this.getClass(), new EntityTracker1_11(userConnection));

        if (!userConnection.has(ClientWorld.class))
            userConnection.put(new ClientWorld());
    }

    @Override
    public MetadataRewriter1_11To1_10 getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    public InventoryPackets getItemRewriter() {
        return itemRewriter;
    }
}
