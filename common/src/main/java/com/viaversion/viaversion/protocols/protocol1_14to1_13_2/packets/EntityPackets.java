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
package com.viaversion.viaversion.protocols.protocol1_14to1_13_2.packets;

import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_13Types;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_14Types;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_14;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
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

        protocol.registerClientbound(ClientboundPackets1_13.SPAWN_ENTITY, new PacketRemapper() {
            @Override
            public void registerMap() {
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
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int entityId = wrapper.get(Type.VAR_INT, 0);
                        int typeId = wrapper.get(Type.VAR_INT, 1);

                        Entity1_13Types.EntityType type1_13 = Entity1_13Types.getTypeFromId(typeId, true);
                        typeId = metadataRewriter.newEntityId(type1_13.getId());
                        EntityType type1_14 = Entity1_14Types.getTypeFromId(typeId);

                        if (type1_14 != null) {
                            int data = wrapper.get(Type.INT, 0);
                            if (type1_14.is(Entity1_14Types.FALLING_BLOCK)) {
                                wrapper.set(Type.INT, 0, protocol.getMappingData().getNewBlockStateId(data));
                            } else if (type1_14.is(Entity1_14Types.MINECART)) {
                                // default is 0 = rideable minecart
                                switch (data) {
                                    case 1:
                                        typeId = Entity1_14Types.CHEST_MINECART.getId();
                                        break;
                                    case 2:
                                        typeId = Entity1_14Types.FURNACE_MINECART.getId();
                                        break;
                                    case 3:
                                        typeId = Entity1_14Types.TNT_MINECART.getId();
                                        break;
                                    case 4:
                                        typeId = Entity1_14Types.SPAWNER_MINECART.getId();
                                        break;
                                    case 5:
                                        typeId = Entity1_14Types.HOPPER_MINECART.getId();
                                        break;
                                    case 6:
                                        typeId = Entity1_14Types.COMMAND_BLOCK_MINECART.getId();
                                        break;
                                }
                            } else if ((type1_14.is(Entity1_14Types.ITEM) && data > 0)
                                    || type1_14.isOrHasParent(Entity1_14Types.ABSTRACT_ARROW)) {
                                if (type1_14.isOrHasParent(Entity1_14Types.ABSTRACT_ARROW)) {
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
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.SPAWN_MOB, new PacketRemapper() {
            @Override
            public void registerMap() {
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

        protocol.registerClientbound(ClientboundPackets1_13.SPAWN_PAINTING, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT);
                map(Type.UUID);
                map(Type.VAR_INT);
                map(Type.POSITION, Type.POSITION1_14);
                map(Type.BYTE);
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.SPAWN_PLAYER, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.UUID); // 1 - Player UUID
                map(Type.DOUBLE); // 2 - X
                map(Type.DOUBLE); // 3 - Y
                map(Type.DOUBLE); // 4 - Z
                map(Type.BYTE); // 5 - Yaw
                map(Type.BYTE); // 6 - Pitch
                map(Types1_13_2.METADATA_LIST, Types1_14.METADATA_LIST); // 7 - Metadata

                handler(metadataRewriter.trackerAndRewriterHandler(Types1_14.METADATA_LIST, Entity1_14Types.PLAYER));
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.ENTITY_ANIMATION, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT);
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        short animation = wrapper.passthrough(Type.UNSIGNED_BYTE);
                        if (animation == 2) {  //Leave bed
                            EntityTracker1_14 tracker = wrapper.user().getEntityTracker(Protocol1_14To1_13_2.class);
                            int entityId = wrapper.get(Type.VAR_INT, 0);
                            tracker.setSleeping(entityId, false);

                            PacketWrapper metadataPacket = wrapper.create(ClientboundPackets1_14.ENTITY_METADATA);
                            metadataPacket.write(Type.VAR_INT, entityId);
                            List<Metadata> metadataList = new LinkedList<>();
                            if (tracker.clientEntityId() != entityId) {
                                metadataList.add(new Metadata(6, MetaType1_14.Pose, MetadataRewriter1_14To1_13_2.recalculatePlayerPose(entityId, tracker)));
                            }
                            metadataList.add(new Metadata(12, MetaType1_14.OptPosition, null));
                            metadataPacket.write(Types1_14.METADATA_LIST, metadataList);
                            metadataPacket.scheduleSend(Protocol1_14To1_13_2.class);
                        }
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.USE_BED, ClientboundPackets1_14.ENTITY_METADATA, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT);
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        EntityTracker1_14 tracker = wrapper.user().getEntityTracker(Protocol1_14To1_13_2.class);
                        int entityId = wrapper.get(Type.VAR_INT, 0);
                        tracker.setSleeping(entityId, true);

                        Position position = wrapper.read(Type.POSITION);
                        List<Metadata> metadataList = new LinkedList<>();
                        metadataList.add(new Metadata(12, MetaType1_14.OptPosition, position));
                        if (tracker.clientEntityId() != entityId) {
                            metadataList.add(new Metadata(6, MetaType1_14.Pose, MetadataRewriter1_14To1_13_2.recalculatePlayerPose(entityId, tracker)));
                        }
                        wrapper.write(Types1_14.METADATA_LIST, metadataList);
                    }
                });
            }
        });

        metadataRewriter.registerRemoveEntities(ClientboundPackets1_13.DESTROY_ENTITIES);
        metadataRewriter.registerMetadataRewriter(ClientboundPackets1_13.ENTITY_METADATA, Types1_13_2.METADATA_LIST, Types1_14.METADATA_LIST);
    }
}
