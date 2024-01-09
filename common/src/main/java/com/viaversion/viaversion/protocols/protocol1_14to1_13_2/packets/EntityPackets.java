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
package com.viaversion.viaversion.protocols.protocol1_14to1_13_2.packets;

import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_13;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_14;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_13_2;
import com.viaversion.viaversion.api.type.types.version.Types1_14;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.ClientboundPackets1_14;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.Protocol1_14To1_13_2;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.metadata.MetadataRewriter1_14To1_13_2;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.storage.EntityTracker1_14;
import java.util.LinkedList;
import java.util.List;

public class EntityPackets {

    public static void register(Protocol1_14To1_13_2 protocol) {
        MetadataRewriter1_14To1_13_2 metadataRewriter = protocol.get(MetadataRewriter1_14To1_13_2.class);

        protocol.registerClientbound(ClientboundPackets1_13.SPAWN_EXPERIENCE_ORB, wrapper -> {
            int entityId = wrapper.passthrough(Type.VAR_INT);
            metadataRewriter.tracker(wrapper.user()).addEntity(entityId, EntityTypes1_14.EXPERIENCE_ORB);
        });

        protocol.registerClientbound(ClientboundPackets1_13.SPAWN_GLOBAL_ENTITY, wrapper -> {
            int entityId = wrapper.passthrough(Type.VAR_INT);
            if (wrapper.passthrough(Type.BYTE) == 1) {
                metadataRewriter.tracker(wrapper.user()).addEntity(entityId, EntityTypes1_14.LIGHTNING_BOLT);
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.SPAWN_ENTITY, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity id
                map(Type.UUID); // 1 - UUID
                map(Type.BYTE, Type.VAR_INT); // 2 - Type
                map(Type.DOUBLE); // 3 - X
                map(Type.DOUBLE); // 4 - Y
                map(Type.DOUBLE); // 5 - Z
                map(Type.BYTE); // 6 - Pitch
                map(Type.BYTE); // 7 - Yaw
                map(Type.INT); // 8 - Data
                map(Type.SHORT); // 9 - Velocity X
                map(Type.SHORT); // 10 - Velocity Y
                map(Type.SHORT); // 11 - Velocity Z

                // Track Entity
                handler(wrapper -> {
                    int entityId = wrapper.get(Type.VAR_INT, 0);
                    int typeId = wrapper.get(Type.VAR_INT, 1);

                    EntityTypes1_13.EntityType type1_13 = EntityTypes1_13.getTypeFromId(typeId, true);
                    typeId = metadataRewriter.newEntityId(type1_13.getId());
                    EntityType type1_14 = EntityTypes1_14.getTypeFromId(typeId);

                    if (type1_14 != null) {
                        int data = wrapper.get(Type.INT, 0);
                        if (type1_14.is(EntityTypes1_14.FALLING_BLOCK)) {
                            wrapper.set(Type.INT, 0, protocol.getMappingData().getNewBlockStateId(data));
                        } else if (type1_14.is(EntityTypes1_14.MINECART)) {
                            // default is 0 = rideable minecart
                            switch (data) {
                                case 1:
                                    typeId = EntityTypes1_14.CHEST_MINECART.getId();
                                    break;
                                case 2:
                                    typeId = EntityTypes1_14.FURNACE_MINECART.getId();
                                    break;
                                case 3:
                                    typeId = EntityTypes1_14.TNT_MINECART.getId();
                                    break;
                                case 4:
                                    typeId = EntityTypes1_14.SPAWNER_MINECART.getId();
                                    break;
                                case 5:
                                    typeId = EntityTypes1_14.HOPPER_MINECART.getId();
                                    break;
                                case 6:
                                    typeId = EntityTypes1_14.COMMAND_BLOCK_MINECART.getId();
                                    break;
                            }
                        } else if ((type1_14.is(EntityTypes1_14.ITEM) && data > 0)
                                || type1_14.isOrHasParent(EntityTypes1_14.ABSTRACT_ARROW)) {
                            if (type1_14.isOrHasParent(EntityTypes1_14.ABSTRACT_ARROW)) {
                                wrapper.set(Type.INT, 0, data - 1);
                            }
                            // send velocity in separate packet, 1.14 is now ignoring the velocity
                            PacketWrapper velocity = wrapper.create(0x45);
                            velocity.write(Type.VAR_INT, entityId);
                            velocity.write(Type.SHORT, wrapper.get(Type.SHORT, 0));
                            velocity.write(Type.SHORT, wrapper.get(Type.SHORT, 1));
                            velocity.write(Type.SHORT, wrapper.get(Type.SHORT, 2));
                            velocity.scheduleSend(Protocol1_14To1_13_2.class);
                        }

                        // Register Type ID
                        wrapper.user().getEntityTracker(Protocol1_14To1_13_2.class).addEntity(entityId, type1_14);
                    }

                    wrapper.set(Type.VAR_INT, 1, typeId);
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.SPAWN_MOB, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.UUID); // 1 - Entity UUID
                map(Type.VAR_INT); // 2 - Entity Type
                map(Type.DOUBLE); // 3 - X
                map(Type.DOUBLE); // 4 - Y
                map(Type.DOUBLE); // 5 - Z
                map(Type.BYTE); // 6 - Yaw
                map(Type.BYTE); // 7 - Pitch
                map(Type.BYTE); // 8 - Head Pitch
                map(Type.SHORT); // 9 - Velocity X
                map(Type.SHORT); // 10 - Velocity Y
                map(Type.SHORT); // 11 - Velocity Z
                map(Types1_13_2.METADATA_LIST, Types1_14.METADATA_LIST); // 12 - Metadata

                handler(metadataRewriter.trackerAndRewriterHandler(Types1_14.METADATA_LIST));
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.SPAWN_PAINTING, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT);
                map(Type.UUID);
                map(Type.VAR_INT);
                map(Type.POSITION1_8, Type.POSITION1_14);
                map(Type.BYTE);
                handler(wrapper -> metadataRewriter.tracker(wrapper.user()).addEntity(wrapper.get(Type.VAR_INT, 0), EntityTypes1_14.PAINTING));
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.SPAWN_PLAYER, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.UUID); // 1 - Player UUID
                map(Type.DOUBLE); // 2 - X
                map(Type.DOUBLE); // 3 - Y
                map(Type.DOUBLE); // 4 - Z
                map(Type.BYTE); // 5 - Yaw
                map(Type.BYTE); // 6 - Pitch
                map(Types1_13_2.METADATA_LIST, Types1_14.METADATA_LIST); // 7 - Metadata

                handler(metadataRewriter.trackerAndRewriterHandler(Types1_14.METADATA_LIST, EntityTypes1_14.PLAYER));
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.ENTITY_ANIMATION, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT);
                handler(wrapper -> {
                    short animation = wrapper.passthrough(Type.UNSIGNED_BYTE);
                    if (animation == 2) {  //Leave bed
                        EntityTracker1_14 tracker = wrapper.user().getEntityTracker(Protocol1_14To1_13_2.class);
                        int entityId = wrapper.get(Type.VAR_INT, 0);
                        tracker.setSleeping(entityId, false);

                        PacketWrapper metadataPacket = wrapper.create(ClientboundPackets1_14.ENTITY_METADATA);
                        metadataPacket.write(Type.VAR_INT, entityId);
                        List<Metadata> metadataList = new LinkedList<>();
                        if (tracker.clientEntityId() != entityId) {
                            metadataList.add(new Metadata(6, Types1_14.META_TYPES.poseType, MetadataRewriter1_14To1_13_2.recalculatePlayerPose(entityId, tracker)));
                        }
                        metadataList.add(new Metadata(12, Types1_14.META_TYPES.optionalPositionType, null));
                        metadataPacket.write(Types1_14.METADATA_LIST, metadataList);
                        metadataPacket.scheduleSend(Protocol1_14To1_13_2.class);
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.JOIN_GAME, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); // 0 - Entity ID
                map(Type.UNSIGNED_BYTE); // 1 - Gamemode
                map(Type.INT); // 2 - Dimension
                handler(wrapper -> {
                    // Store the player
                    ClientWorld clientChunks = wrapper.user().get(ClientWorld.class);
                    int dimensionId = wrapper.get(Type.INT, 1);
                    clientChunks.setEnvironment(dimensionId);
                });
                handler(metadataRewriter.playerTrackerHandler());
                handler(wrapper -> {
                    short difficulty = wrapper.read(Type.UNSIGNED_BYTE); // 19w11a removed difficulty from join game
                    PacketWrapper difficultyPacket = wrapper.create(ClientboundPackets1_14.SERVER_DIFFICULTY);
                    difficultyPacket.write(Type.UNSIGNED_BYTE, difficulty);
                    difficultyPacket.write(Type.BOOLEAN, false); // Unknown value added in 19w11a
                    difficultyPacket.scheduleSend(protocol.getClass());

                    wrapper.passthrough(Type.UNSIGNED_BYTE); // Max Players
                    wrapper.passthrough(Type.STRING); // Level Type

                    wrapper.write(Type.VAR_INT, WorldPackets.SERVERSIDE_VIEW_DISTANCE);  // Serverside view distance, added in 19w13a
                });
                handler(wrapper -> {
                    // Manually send the packet
                    wrapper.send(Protocol1_14To1_13_2.class);
                    wrapper.cancel();

                    // View distance has to be sent after the join packet
                    WorldPackets.sendViewDistancePacket(wrapper.user());
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.USE_BED, ClientboundPackets1_14.ENTITY_METADATA, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT);
                handler(wrapper -> {
                    EntityTracker1_14 tracker = wrapper.user().getEntityTracker(Protocol1_14To1_13_2.class);
                    int entityId = wrapper.get(Type.VAR_INT, 0);
                    tracker.setSleeping(entityId, true);

                    Position position = wrapper.read(Type.POSITION1_8);
                    List<Metadata> metadataList = new LinkedList<>();
                    metadataList.add(new Metadata(12, Types1_14.META_TYPES.optionalPositionType, position));
                    if (tracker.clientEntityId() != entityId) {
                        metadataList.add(new Metadata(6, Types1_14.META_TYPES.poseType, MetadataRewriter1_14To1_13_2.recalculatePlayerPose(entityId, tracker)));
                    }
                    wrapper.write(Types1_14.METADATA_LIST, metadataList);
                });
            }
        });

        metadataRewriter.registerRemoveEntities(ClientboundPackets1_13.DESTROY_ENTITIES);
        metadataRewriter.registerMetadataRewriter(ClientboundPackets1_13.ENTITY_METADATA, Types1_13_2.METADATA_LIST, Types1_14.METADATA_LIST);
    }
}
