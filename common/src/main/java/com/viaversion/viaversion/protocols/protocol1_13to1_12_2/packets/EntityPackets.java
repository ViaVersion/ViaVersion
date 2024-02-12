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
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.packets;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_13;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_12;
import com.viaversion.viaversion.api.type.types.version.Types1_13;
import com.viaversion.viaversion.protocols.protocol1_12_1to1_12.ClientboundPackets1_12_1;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.metadata.MetadataRewriter1_13To1_12_2;

public class EntityPackets {

    public static void register(Protocol1_13To1_12_2 protocol) {
        MetadataRewriter1_13To1_12_2 metadataRewriter = protocol.get(MetadataRewriter1_13To1_12_2.class);

        protocol.registerClientbound(ClientboundPackets1_12_1.SPAWN_ENTITY, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity id
                map(Type.UUID); // 1 - UUID
                map(Type.BYTE); // 2 - Type
                map(Type.DOUBLE); // 3 - X
                map(Type.DOUBLE); // 4 - Y
                map(Type.DOUBLE); // 5 - Z
                map(Type.BYTE); // 6 - Pitch
                map(Type.BYTE); // 7 - Yaw
                map(Type.INT); // 8 - Data

                // Track Entity
                handler(wrapper -> {
                    int entityId = wrapper.get(Type.VAR_INT, 0);
                    byte type = wrapper.get(Type.BYTE, 0);
                    EntityTypes1_13.EntityType entType = EntityTypes1_13.getTypeFromId(type, true);
                    if (entType == null) return;

                    // Register Type ID
                    wrapper.user().getEntityTracker(Protocol1_13To1_12_2.class).addEntity(entityId, entType);

                    if (entType.is(EntityTypes1_13.EntityType.FALLING_BLOCK)) {
                        int oldId = wrapper.get(Type.INT, 0);
                        int combined = (((oldId & 4095) << 4) | (oldId >> 12 & 15));
                        wrapper.set(Type.INT, 0, WorldPackets.toNewId(combined));
                    }

                    // Fix ItemFrame hitbox
                    if (entType.is(EntityTypes1_13.EntityType.ITEM_FRAME)) {
                        int data = wrapper.get(Type.INT, 0);

                        switch (data) {
                            // South
                            case 0:
                                data = 3;
                                break;
                            // West
                            case 1:
                                data = 4;
                                break;
                            // North is the same
                            // East
                            case 3:
                                data = 5;
                                break;
                        }

                        wrapper.set(Type.INT, 0, data);
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_12_1.SPAWN_MOB, new PacketHandlers() {
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
                map(Types1_12.METADATA_LIST, Types1_13.METADATA_LIST); // 12 - Metadata

                handler(metadataRewriter.trackerAndRewriterHandler(Types1_13.METADATA_LIST));
            }
        });

        protocol.registerClientbound(ClientboundPackets1_12_1.SPAWN_PLAYER, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.UUID); // 1 - Player UUID
                map(Type.DOUBLE); // 2 - X
                map(Type.DOUBLE); // 3 - Y
                map(Type.DOUBLE); // 4 - Z
                map(Type.BYTE); // 5 - Yaw
                map(Type.BYTE); // 6 - Pitch
                map(Types1_12.METADATA_LIST, Types1_13.METADATA_LIST); // 7 - Metadata

                handler(metadataRewriter.trackerAndRewriterHandler(Types1_13.METADATA_LIST, EntityTypes1_13.EntityType.PLAYER));
            }
        });

        protocol.registerClientbound(ClientboundPackets1_12_1.JOIN_GAME, new PacketHandlers() {
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
                handler(metadataRewriter.playerTrackerHandler());
                handler(Protocol1_13To1_12_2.SEND_DECLARE_COMMANDS_AND_TAGS);
            }
        });

        protocol.registerClientbound(ClientboundPackets1_12_1.ENTITY_EFFECT, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // Entity id
                map(Type.BYTE); // Effect id
                map(Type.BYTE); // Amplifier
                map(Type.VAR_INT); // Duration

                handler(packetWrapper -> {
                    byte flags = packetWrapper.read(Type.BYTE); // Input Flags

                    if (Via.getConfig().isNewEffectIndicator())
                        flags |= 0x04;

                    packetWrapper.write(Type.BYTE, flags);
                });
            }
        });

        metadataRewriter.registerRemoveEntities(ClientboundPackets1_12_1.DESTROY_ENTITIES);
        metadataRewriter.registerMetadataRewriter(ClientboundPackets1_12_1.ENTITY_METADATA, Types1_12.METADATA_LIST, Types1_13.METADATA_LIST);
    }
}
