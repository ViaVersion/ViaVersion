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
package com.viaversion.viaversion.protocols.v1_8to1_9.rewriter;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_9;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.remapper.ValueTransformer;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_8;
import com.viaversion.viaversion.api.type.types.version.Types1_9;
import com.viaversion.viaversion.protocols.v1_8.packet.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.v1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.metadata.MetadataRewriter1_9To1_8;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.storage.EntityTracker1_9;
import java.util.ArrayList;
import java.util.List;

public class SpawnPackets1_9 {
    public static final ValueTransformer<Integer, Double> toNewDouble = new ValueTransformer<>(Types.DOUBLE) {
        @Override
        public Double transform(PacketWrapper wrapper, Integer inputValue) {
            return inputValue / 32D;
        }
    };

    public static void register(Protocol1_8To1_9 protocol) {
        protocol.registerClientbound(ClientboundPackets1_8.ADD_ENTITY, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Entity ID

                handler(wrapper -> {
                    int entityID = wrapper.get(Types.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    wrapper.write(Types.UUID, tracker.getEntityUUID(entityID)); // 1 - UUID
                });
                map(Types.BYTE); // 2 - Type

                // Parse this info
                handler(wrapper -> {
                    int entityID = wrapper.get(Types.VAR_INT, 0);
                    int typeID = wrapper.get(Types.BYTE, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    tracker.addEntity(entityID, EntityTypes1_10.getTypeFromId(typeID, true));
                });

                map(Types.INT, toNewDouble); // 3 - X - Needs to be divided by 32
                map(Types.INT, toNewDouble); // 4 - Y - Needs to be divided by 32
                map(Types.INT, toNewDouble); // 5 - Z - Needs to be divided by 32

                map(Types.BYTE); // 6 - Pitch
                map(Types.BYTE); // 7 - Yaw

                map(Types.INT); // 8 - Data

                // Create last 3 shorts
                handler(wrapper -> {
                    int data = wrapper.get(Types.INT, 0); // Data (1st Integer)

                    short vX = 0;
                    short vY = 0;
                    short vZ = 0;
                    if (data > 0) {
                        vX = wrapper.read(Types.SHORT);
                        vY = wrapper.read(Types.SHORT);
                        vZ = wrapper.read(Types.SHORT);
                    }

                    wrapper.write(Types.SHORT, vX);
                    wrapper.write(Types.SHORT, vY);
                    wrapper.write(Types.SHORT, vZ);
                });

                // Handle potions
                handler(wrapper -> {
                    final int entityID = wrapper.get(Types.VAR_INT, 0);
                    final int data = wrapper.get(Types.INT, 0); // Data

                    int typeID = wrapper.get(Types.BYTE, 0);
                    if (EntityTypes1_10.getTypeFromId(typeID, true) == EntityTypes1_10.EntityType.SPLASH_POTION) {
                        // Convert this to meta data, woo!
                        PacketWrapper metaPacket = wrapper.create(ClientboundPackets1_9.SET_ENTITY_DATA, wrapper1 -> {
                            wrapper1.write(Types.VAR_INT, entityID);
                            List<Metadata> meta = new ArrayList<>();
                            Item item = new DataItem(373, (byte) 1, (short) data, null); // Potion
                            ItemRewriter.toClient(item); // Rewrite so that it gets the right nbt
                            // TEMP FIX FOR POTIONS UNTIL WE FIGURE OUT HOW TO TRANSFORM SENT PACKETS
                            Metadata potion = new Metadata(5, MetaType1_9.ITEM, item);
                            meta.add(potion);
                            wrapper1.write(Types1_9.METADATA_LIST, meta);
                        });
                        // Fix packet order
                        wrapper.send(Protocol1_8To1_9.class);
                        metaPacket.send(Protocol1_8To1_9.class);
                        wrapper.cancel();
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.ADD_EXPERIENCE_ORB, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Entity ID

                // Parse this info
                handler(wrapper -> {
                    int entityID = wrapper.get(Types.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    tracker.addEntity(entityID, EntityTypes1_10.EntityType.EXPERIENCE_ORB);
                });

                map(Types.INT, toNewDouble); // 1 - X - Needs to be divided by 32
                map(Types.INT, toNewDouble); // 2 - Y - Needs to be divided by 32
                map(Types.INT, toNewDouble); // 3 - Z - Needs to be divided by 32

                map(Types.SHORT); // 4 - Amount to spawn
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.ADD_GLOBAL_ENTITY, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Entity ID
                map(Types.BYTE); // 1 - Type
                // Parse this info
                handler(wrapper -> {
                    // Currently only lightning uses this
                    int entityID = wrapper.get(Types.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    tracker.addEntity(entityID, EntityTypes1_10.EntityType.LIGHTNING);
                });

                map(Types.INT, toNewDouble); // 2 - X - Needs to be divided by 32
                map(Types.INT, toNewDouble); // 3 - Y - Needs to be divided by 32
                map(Types.INT, toNewDouble); // 4 - Z - Needs to be divided by 32
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.ADD_MOB, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Entity ID

                handler(wrapper -> {
                    int entityID = wrapper.get(Types.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    wrapper.write(Types.UUID, tracker.getEntityUUID(entityID)); // 1 - UUID
                });
                map(Types.UNSIGNED_BYTE); // 2 - Type

                // Parse this info
                handler(wrapper -> {
                    int entityID = wrapper.get(Types.VAR_INT, 0);
                    int typeID = wrapper.get(Types.UNSIGNED_BYTE, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    tracker.addEntity(entityID, EntityTypes1_10.getTypeFromId(typeID, false));
                });

                map(Types.INT, toNewDouble); // 3 - X - Needs to be divided by 32
                map(Types.INT, toNewDouble); // 4 - Y - Needs to be divided by 32
                map(Types.INT, toNewDouble); // 5 - Z - Needs to be divided by 32

                map(Types.BYTE); // 6 - Yaw
                map(Types.BYTE); // 7 - Pitch
                map(Types.BYTE); // 8 - Head Pitch

                map(Types.SHORT); // 9 - Velocity X
                map(Types.SHORT); // 10 - Velocity Y
                map(Types.SHORT); // 11 - Velocity Z

                map(Types1_8.METADATA_LIST, Types1_9.METADATA_LIST);
                handler(wrapper -> {
                    List<Metadata> metadataList = wrapper.get(Types1_9.METADATA_LIST, 0);
                    int entityId = wrapper.get(Types.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    if (tracker.hasEntity(entityId)) {
                        protocol.get(MetadataRewriter1_9To1_8.class).handleMetadata(entityId, metadataList, wrapper.user());
                    } else {
                        Via.getPlatform().getLogger().warning("Unable to find entity for metadata, entity ID: " + entityId);
                        metadataList.clear();
                    }
                });
                // Handler for meta data
                handler(wrapper -> {
                    List<Metadata> metadataList = wrapper.get(Types1_9.METADATA_LIST, 0);
                    int entityID = wrapper.get(Types.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    tracker.handleMetadata(entityID, metadataList);
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.ADD_PAINTING, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Entity ID

                // Parse this info
                handler(wrapper -> {
                    int entityID = wrapper.get(Types.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    tracker.addEntity(entityID, EntityTypes1_10.EntityType.PAINTING);
                });
                handler(wrapper -> {
                    int entityID = wrapper.get(Types.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    wrapper.write(Types.UUID, tracker.getEntityUUID(entityID)); // 1 - UUID
                });

                map(Types.STRING); // 2 - Title
                map(Types.BLOCK_POSITION1_8); // 3 - Position
                map(Types.BYTE); // 4 - Direction
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.ADD_PLAYER, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Entity ID
                map(Types.UUID); // 1 - Player UUID

                // Parse this info
                handler(wrapper -> {
                    int entityID = wrapper.get(Types.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    tracker.addEntity(entityID, EntityTypes1_10.EntityType.PLAYER);
                });

                map(Types.INT, toNewDouble); // 2 - X - Needs to be divided by 32
                map(Types.INT, toNewDouble); // 3 - Y - Needs to be divided by 32
                map(Types.INT, toNewDouble); // 4 - Z - Needs to be divided by 32

                map(Types.BYTE); // 5 - Yaw
                map(Types.BYTE); // 6 - Pitch

                //Handle discontinued player hand item
                handler(wrapper -> {
                    short item = wrapper.read(Types.SHORT);
                    if (item != 0) {
                        PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_9.SET_EQUIPPED_ITEM, null, wrapper.user());
                        packet.write(Types.VAR_INT, wrapper.get(Types.VAR_INT, 0));
                        packet.write(Types.VAR_INT, 0);
                        packet.write(Types.ITEM1_8, new DataItem(item, (byte) 1, (short) 0, null));
                        packet.send(Protocol1_8To1_9.class);
                    }
                });

                map(Types1_8.METADATA_LIST, Types1_9.METADATA_LIST);

                handler(wrapper -> {
                    List<Metadata> metadataList = wrapper.get(Types1_9.METADATA_LIST, 0);
                    int entityId = wrapper.get(Types.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    if (tracker.hasEntity(entityId)) {
                        protocol.get(MetadataRewriter1_9To1_8.class).handleMetadata(entityId, metadataList, wrapper.user());
                    } else {
                        Via.getPlatform().getLogger().warning("Unable to find entity for metadata, entity ID: " + entityId);
                        metadataList.clear();
                    }
                });

                // Handler for meta data
                handler(wrapper -> {
                    List<Metadata> metadataList = wrapper.get(Types1_9.METADATA_LIST, 0);
                    int entityID = wrapper.get(Types.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    tracker.handleMetadata(entityID, metadataList);
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.REMOVE_ENTITIES, new PacketHandlers() {

            @Override
            public void register() {
                map(Types.VAR_INT_ARRAY_PRIMITIVE); // 0 - Entities to destroy

                handler(wrapper -> {
                    int[] entities = wrapper.get(Types.VAR_INT_ARRAY_PRIMITIVE, 0);
                    EntityTracker tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    for (int entity : entities) {
                        // EntityTracker
                        tracker.removeEntity(entity);
                    }
                });
            }
        });
    }
}
