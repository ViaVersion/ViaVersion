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
package com.viaversion.viaversion.protocols.v1_13_2to1_14.rewriter;

import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_13;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_14;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_13_2;
import com.viaversion.viaversion.api.type.types.version.Types1_14;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.Protocol1_13_2To1_14;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.metadata.MetadataRewriter1_14To1_13_2;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.packet.ClientboundPackets1_14;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.storage.EntityTracker1_14;
import java.util.LinkedList;
import java.util.List;

public class EntityPacketRewriter1_14 {

    public static void register(Protocol1_13_2To1_14 protocol) {
        MetadataRewriter1_14To1_13_2 metadataRewriter = protocol.get(MetadataRewriter1_14To1_13_2.class);

        protocol.registerClientbound(ClientboundPackets1_13.ADD_EXPERIENCE_ORB, wrapper -> {
            int entityId = wrapper.passthrough(Types.VAR_INT);
            metadataRewriter.tracker(wrapper.user()).addEntity(entityId, EntityTypes1_14.EXPERIENCE_ORB);
        });

        protocol.registerClientbound(ClientboundPackets1_13.ADD_GLOBAL_ENTITY, wrapper -> {
            int entityId = wrapper.passthrough(Types.VAR_INT);
            if (wrapper.passthrough(Types.BYTE) == 1) {
                metadataRewriter.tracker(wrapper.user()).addEntity(entityId, EntityTypes1_14.LIGHTNING_BOLT);
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.ADD_ENTITY, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Entity id
                map(Types.UUID); // 1 - UUID
                map(Types.BYTE, Types.VAR_INT); // 2 - Type
                map(Types.DOUBLE); // 3 - X
                map(Types.DOUBLE); // 4 - Y
                map(Types.DOUBLE); // 5 - Z
                map(Types.BYTE); // 6 - Pitch
                map(Types.BYTE); // 7 - Yaw
                map(Types.INT); // 8 - Data
                map(Types.SHORT); // 9 - Velocity X
                map(Types.SHORT); // 10 - Velocity Y
                map(Types.SHORT); // 11 - Velocity Z

                // Track Entity
                handler(wrapper -> {
                    int entityId = wrapper.get(Types.VAR_INT, 0);
                    int typeId = wrapper.get(Types.VAR_INT, 1);

                    EntityTypes1_13.EntityType type1_13 = EntityTypes1_13.getTypeFromId(typeId, true);
                    typeId = metadataRewriter.newEntityId(type1_13.getId());
                    EntityType type1_14 = EntityTypes1_14.getTypeFromId(typeId);

                    if (type1_14 != null) {
                        int data = wrapper.get(Types.INT, 0);
                        if (type1_14.is(EntityTypes1_14.FALLING_BLOCK)) {
                            wrapper.set(Types.INT, 0, protocol.getMappingData().getNewBlockStateId(data));
                        } else if (type1_14.is(EntityTypes1_14.MINECART)) {
                            typeId = switch (data) {
                                case 1 -> EntityTypes1_14.CHEST_MINECART.getId();
                                case 2 -> EntityTypes1_14.FURNACE_MINECART.getId();
                                case 3 -> EntityTypes1_14.TNT_MINECART.getId();
                                case 4 -> EntityTypes1_14.SPAWNER_MINECART.getId();
                                case 5 -> EntityTypes1_14.HOPPER_MINECART.getId();
                                case 6 -> EntityTypes1_14.COMMAND_BLOCK_MINECART.getId();
                                default -> typeId; // default 0 = rideable minecart
                            };
                        } else if ((type1_14.is(EntityTypes1_14.ITEM) && data > 0)
                                || type1_14.isOrHasParent(EntityTypes1_14.ABSTRACT_ARROW)) {
                            if (type1_14.isOrHasParent(EntityTypes1_14.ABSTRACT_ARROW)) {
                                wrapper.set(Types.INT, 0, data - 1);
                            }
                            // send velocity in separate packet, 1.14 is now ignoring the velocity
                            PacketWrapper velocity = wrapper.create(0x45);
                            velocity.write(Types.VAR_INT, entityId);
                            velocity.write(Types.SHORT, wrapper.get(Types.SHORT, 0));
                            velocity.write(Types.SHORT, wrapper.get(Types.SHORT, 1));
                            velocity.write(Types.SHORT, wrapper.get(Types.SHORT, 2));
                            velocity.scheduleSend(Protocol1_13_2To1_14.class);
                        }

                        // Register Type ID
                        wrapper.user().getEntityTracker(Protocol1_13_2To1_14.class).addEntity(entityId, type1_14);
                    }

                    wrapper.set(Types.VAR_INT, 1, typeId);
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.ADD_MOB, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Entity ID
                map(Types.UUID); // 1 - Entity UUID
                map(Types.VAR_INT); // 2 - Entity Type
                map(Types.DOUBLE); // 3 - X
                map(Types.DOUBLE); // 4 - Y
                map(Types.DOUBLE); // 5 - Z
                map(Types.BYTE); // 6 - Yaw
                map(Types.BYTE); // 7 - Pitch
                map(Types.BYTE); // 8 - Head Pitch
                map(Types.SHORT); // 9 - Velocity X
                map(Types.SHORT); // 10 - Velocity Y
                map(Types.SHORT); // 11 - Velocity Z
                map(Types1_13_2.METADATA_LIST, Types1_14.METADATA_LIST); // 12 - Metadata

                handler(metadataRewriter.trackerAndRewriterHandler(Types1_14.METADATA_LIST));
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.ADD_PAINTING, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT);
                map(Types.UUID);
                map(Types.VAR_INT);
                map(Types.BLOCK_POSITION1_8, Types.BLOCK_POSITION1_14);
                map(Types.BYTE);
                handler(wrapper -> metadataRewriter.tracker(wrapper.user()).addEntity(wrapper.get(Types.VAR_INT, 0), EntityTypes1_14.PAINTING));
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.ADD_PLAYER, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Entity ID
                map(Types.UUID); // 1 - Player UUID
                map(Types.DOUBLE); // 2 - X
                map(Types.DOUBLE); // 3 - Y
                map(Types.DOUBLE); // 4 - Z
                map(Types.BYTE); // 5 - Yaw
                map(Types.BYTE); // 6 - Pitch
                map(Types1_13_2.METADATA_LIST, Types1_14.METADATA_LIST); // 7 - Metadata

                handler(metadataRewriter.trackerAndRewriterHandler(Types1_14.METADATA_LIST, EntityTypes1_14.PLAYER));
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.ANIMATE, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT);
                handler(wrapper -> {
                    short animation = wrapper.passthrough(Types.UNSIGNED_BYTE);
                    if (animation == 2) {  //Leave bed
                        EntityTracker1_14 tracker = wrapper.user().getEntityTracker(Protocol1_13_2To1_14.class);
                        int entityId = wrapper.get(Types.VAR_INT, 0);
                        tracker.setSleeping(entityId, false);

                        PacketWrapper metadataPacket = wrapper.create(ClientboundPackets1_14.SET_ENTITY_DATA);
                        metadataPacket.write(Types.VAR_INT, entityId);
                        List<Metadata> metadataList = new LinkedList<>();
                        if (tracker.clientEntityId() != entityId) {
                            metadataList.add(new Metadata(6, Types1_14.META_TYPES.poseType, MetadataRewriter1_14To1_13_2.recalculatePlayerPose(entityId, tracker)));
                        }
                        metadataList.add(new Metadata(12, Types1_14.META_TYPES.optionalBlockPositionType, null));
                        metadataPacket.write(Types1_14.METADATA_LIST, metadataList);
                        metadataPacket.scheduleSend(Protocol1_13_2To1_14.class);
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.LOGIN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); // 0 - Entity ID
                map(Types.UNSIGNED_BYTE); // 1 - Gamemode
                map(Types.INT); // 2 - Dimension
                handler(wrapper -> {
                    // Store the player
                    ClientWorld clientChunks = wrapper.user().get(ClientWorld.class);
                    int dimensionId = wrapper.get(Types.INT, 1);
                    clientChunks.setEnvironment(dimensionId);
                });
                handler(metadataRewriter.playerTrackerHandler());
                handler(wrapper -> {
                    short difficulty = wrapper.read(Types.UNSIGNED_BYTE); // 19w11a removed difficulty from join game
                    PacketWrapper difficultyPacket = wrapper.create(ClientboundPackets1_14.CHANGE_DIFFICULTY);
                    difficultyPacket.write(Types.UNSIGNED_BYTE, difficulty);
                    difficultyPacket.write(Types.BOOLEAN, false); // Unknown value added in 19w11a
                    difficultyPacket.scheduleSend(protocol.getClass());

                    wrapper.passthrough(Types.UNSIGNED_BYTE); // Max Players
                    wrapper.passthrough(Types.STRING); // Level Type

                    wrapper.write(Types.VAR_INT, WorldPacketRewriter1_14.SERVERSIDE_VIEW_DISTANCE);  // Serverside view distance, added in 19w13a
                });
                handler(wrapper -> {
                    // Manually send the packet
                    wrapper.send(Protocol1_13_2To1_14.class);
                    wrapper.cancel();

                    // View distance has to be sent after the join packet
                    WorldPacketRewriter1_14.sendViewDistancePacket(wrapper.user());
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.PLAYER_SLEEP, ClientboundPackets1_14.SET_ENTITY_DATA, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT);
                handler(wrapper -> {
                    EntityTracker1_14 tracker = wrapper.user().getEntityTracker(Protocol1_13_2To1_14.class);
                    int entityId = wrapper.get(Types.VAR_INT, 0);
                    tracker.setSleeping(entityId, true);

                    Position position = wrapper.read(Types.BLOCK_POSITION1_8);
                    List<Metadata> metadataList = new LinkedList<>();
                    metadataList.add(new Metadata(12, Types1_14.META_TYPES.optionalBlockPositionType, position));
                    if (tracker.clientEntityId() != entityId) {
                        metadataList.add(new Metadata(6, Types1_14.META_TYPES.poseType, MetadataRewriter1_14To1_13_2.recalculatePlayerPose(entityId, tracker)));
                    }
                    wrapper.write(Types1_14.METADATA_LIST, metadataList);
                });
            }
        });

        metadataRewriter.registerRemoveEntities(ClientboundPackets1_13.REMOVE_ENTITIES);
        metadataRewriter.registerMetadataRewriter(ClientboundPackets1_13.SET_ENTITY_DATA, Types1_13_2.METADATA_LIST, Types1_14.METADATA_LIST);
    }
}
