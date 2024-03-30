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
package com.viaversion.viaversion.protocols.protocol1_9to1_8.packets;

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
import com.viaversion.viaversion.api.type.types.version.Types1_8;
import com.viaversion.viaversion.api.type.types.version.Types1_9;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ItemRewriter;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.metadata.MetadataRewriter1_9To1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.storage.EntityTracker1_9;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class SpawnPackets {
    public static final ValueTransformer<Integer, Double> toNewDouble = new ValueTransformer<Integer, Double>(Type.DOUBLE) {
        @Override
        public Double transform(PacketWrapper wrapper, Integer inputValue) {
            return inputValue / 32D;
        }
    };

    public static void register(Protocol1_9To1_8 protocol) {
        protocol.registerClientbound(ClientboundPackets1_8.SPAWN_ENTITY, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity ID

                handler(wrapper -> {
                    int entityID = wrapper.get(Type.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                    wrapper.write(Type.UUID, tracker.getEntityUUID(entityID)); // 1 - UUID
                });
                map(Type.BYTE); // 2 - Type

                // Parse this info
                handler(wrapper -> {
                    int entityID = wrapper.get(Type.VAR_INT, 0);
                    int typeID = wrapper.get(Type.BYTE, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                    tracker.addEntity(entityID, EntityTypes1_10.getTypeFromId(typeID, true));
                });

                map(Type.INT, toNewDouble); // 3 - X - Needs to be divided by 32
                map(Type.INT, toNewDouble); // 4 - Y - Needs to be divided by 32
                map(Type.INT, toNewDouble); // 5 - Z - Needs to be divided by 32

                map(Type.BYTE); // 6 - Pitch
                map(Type.BYTE); // 7 - Yaw

                map(Type.INT); // 8 - Data

                // Create last 3 shorts
                handler(wrapper -> {
                    int data = wrapper.get(Type.INT, 0); // Data (1st Integer)

                    short vX = 0;
                    short vY = 0;
                    short vZ = 0;
                    if (data > 0) {
                        vX = wrapper.read(Type.SHORT);
                        vY = wrapper.read(Type.SHORT);
                        vZ = wrapper.read(Type.SHORT);
                    }

                    wrapper.write(Type.SHORT, vX);
                    wrapper.write(Type.SHORT, vY);
                    wrapper.write(Type.SHORT, vZ);
                });

                // Handle potions
                handler(wrapper -> {
                    final int entityID = wrapper.get(Type.VAR_INT, 0);
                    final int data = wrapper.get(Type.INT, 0); // Data

                    int typeID = wrapper.get(Type.BYTE, 0);
                    if (EntityTypes1_10.getTypeFromId(typeID, true) == EntityTypes1_10.EntityType.SPLASH_POTION) {
                        // Convert this to meta data, woo!
                        PacketWrapper metaPacket = wrapper.create(ClientboundPackets1_9.ENTITY_METADATA, wrapper1 -> {
                            wrapper1.write(Type.VAR_INT, entityID);
                            List<Metadata> meta = new ArrayList<>();
                            Item item = new DataItem(373, (byte) 1, (short) data, null); // Potion
                            ItemRewriter.toClient(item); // Rewrite so that it gets the right nbt
                            // TEMP FIX FOR POTIONS UNTIL WE FIGURE OUT HOW TO TRANSFORM SENT PACKETS
                            Metadata potion = new Metadata(5, MetaType1_9.Slot, item);
                            meta.add(potion);
                            wrapper1.write(Types1_9.METADATA_LIST, meta);
                        });
                        // Fix packet order
                        wrapper.send(Protocol1_9To1_8.class);
                        metaPacket.send(Protocol1_9To1_8.class);
                        wrapper.cancel();
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.SPAWN_EXPERIENCE_ORB, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity ID

                // Parse this info
                handler(wrapper -> {
                    int entityID = wrapper.get(Type.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                    tracker.addEntity(entityID, EntityTypes1_10.EntityType.EXPERIENCE_ORB);
                });

                map(Type.INT, toNewDouble); // 1 - X - Needs to be divided by 32
                map(Type.INT, toNewDouble); // 2 - Y - Needs to be divided by 32
                map(Type.INT, toNewDouble); // 3 - Z - Needs to be divided by 32

                map(Type.SHORT); // 4 - Amount to spawn
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.SPAWN_GLOBAL_ENTITY, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.BYTE); // 1 - Type
                // Parse this info
                handler(wrapper -> {
                    // Currently only lightning uses this
                    int entityID = wrapper.get(Type.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                    tracker.addEntity(entityID, EntityTypes1_10.EntityType.LIGHTNING);
                });

                map(Type.INT, toNewDouble); // 2 - X - Needs to be divided by 32
                map(Type.INT, toNewDouble); // 3 - Y - Needs to be divided by 32
                map(Type.INT, toNewDouble); // 4 - Z - Needs to be divided by 32
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.SPAWN_MOB, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity ID

                handler(wrapper -> {
                    int entityID = wrapper.get(Type.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                    wrapper.write(Type.UUID, tracker.getEntityUUID(entityID)); // 1 - UUID
                });
                map(Type.UNSIGNED_BYTE); // 2 - Type

                // Parse this info
                handler(wrapper -> {
                    int entityID = wrapper.get(Type.VAR_INT, 0);
                    int typeID = wrapper.get(Type.UNSIGNED_BYTE, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                    tracker.addEntity(entityID, EntityTypes1_10.getTypeFromId(typeID, false));
                });

                map(Type.INT, toNewDouble); // 3 - X - Needs to be divided by 32
                map(Type.INT, toNewDouble); // 4 - Y - Needs to be divided by 32
                map(Type.INT, toNewDouble); // 5 - Z - Needs to be divided by 32

                map(Type.BYTE); // 6 - Yaw
                map(Type.BYTE); // 7 - Pitch
                map(Type.BYTE); // 8 - Head Pitch

                map(Type.SHORT); // 9 - Velocity X
                map(Type.SHORT); // 10 - Velocity Y
                map(Type.SHORT); // 11 - Velocity Z

                map(Types1_8.METADATA_LIST, Types1_9.METADATA_LIST);
                handler(wrapper -> {
                    List<Metadata> metadataList = wrapper.get(Types1_9.METADATA_LIST, 0);
                    int entityId = wrapper.get(Type.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
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
                    int entityID = wrapper.get(Type.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                    tracker.handleMetadata(entityID, metadataList);
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.SPAWN_PAINTING, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity ID

                // Parse this info
                handler(wrapper -> {
                    int entityID = wrapper.get(Type.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                    tracker.addEntity(entityID, EntityTypes1_10.EntityType.PAINTING);
                });
                handler(wrapper -> {
                    int entityID = wrapper.get(Type.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                    wrapper.write(Type.UUID, tracker.getEntityUUID(entityID)); // 1 - UUID
                });

                map(Type.STRING); // 2 - Title
                map(Type.POSITION1_8); // 3 - Position
                map(Type.BYTE); // 4 - Direction
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.SPAWN_PLAYER, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.UUID); // 1 - Player UUID

                // Parse this info
                handler(wrapper -> {
                    int entityID = wrapper.get(Type.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                    tracker.addEntity(entityID, EntityTypes1_10.EntityType.PLAYER);
                });

                map(Type.INT, toNewDouble); // 2 - X - Needs to be divided by 32
                map(Type.INT, toNewDouble); // 3 - Y - Needs to be divided by 32
                map(Type.INT, toNewDouble); // 4 - Z - Needs to be divided by 32

                map(Type.BYTE); // 5 - Yaw
                map(Type.BYTE); // 6 - Pitch

                //Handle discontinued player hand item
                handler(wrapper -> {
                    short item = wrapper.read(Type.SHORT);
                    if (item != 0) {
                        PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_9.ENTITY_EQUIPMENT, null, wrapper.user());
                        packet.write(Type.VAR_INT, wrapper.get(Type.VAR_INT, 0));
                        packet.write(Type.VAR_INT, 0);
                        packet.write(Type.ITEM1_8, new DataItem(item, (byte) 1, (short) 0, null));
                        try {
                            packet.send(Protocol1_9To1_8.class);
                        } catch (Exception e) {
                            Via.getPlatform().getLogger().log(Level.SEVERE, "Failed to send entity equipment packet", e);
                        }
                    }
                });

                map(Types1_8.METADATA_LIST, Types1_9.METADATA_LIST);

                handler(wrapper -> {
                    List<Metadata> metadataList = wrapper.get(Types1_9.METADATA_LIST, 0);
                    int entityId = wrapper.get(Type.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
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
                    int entityID = wrapper.get(Type.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                    tracker.handleMetadata(entityID, metadataList);
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.DESTROY_ENTITIES, new PacketHandlers() {

            @Override
            public void register() {
                map(Type.VAR_INT_ARRAY_PRIMITIVE); // 0 - Entities to destroy

                handler(wrapper -> {
                    int[] entities = wrapper.get(Type.VAR_INT_ARRAY_PRIMITIVE, 0);
                    EntityTracker tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                    for (int entity : entities) {
                        // EntityTracker
                        tracker.removeEntity(entity);
                    }
                });
            }
        });
    }
}
