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
package com.viaversion.viaversion.protocols.v1_13to1_13_1.rewriter;

import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_13;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_13to1_13_1.Protocol1_13To1_13_1;
import com.viaversion.viaversion.protocols.v1_13to1_13_1.metadata.MetadataRewriter1_13_1To1_13;

public class EntityPacketRewriter1_13_1 {

    public static void register(Protocol1_13To1_13_1 protocol) {
        MetadataRewriter1_13_1To1_13 metadataRewriter = protocol.get(MetadataRewriter1_13_1To1_13.class);

        protocol.registerClientbound(ClientboundPackets1_13.ADD_ENTITY, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Entity id
                map(Types.UUID); // 1 - UUID
                map(Types.BYTE); // 2 - Type
                map(Types.DOUBLE); // 3 - X
                map(Types.DOUBLE); // 4 - Y
                map(Types.DOUBLE); // 5 - Z
                map(Types.BYTE); // 6 - Pitch
                map(Types.BYTE); // 7 - Yaw
                map(Types.INT); // 8 - Data

                // Track Entity
                handler(wrapper -> {
                    int entityId = wrapper.get(Types.VAR_INT, 0);
                    byte type = wrapper.get(Types.BYTE, 0);
                    EntityTypes1_13.EntityType entType = EntityTypes1_13.getTypeFromId(type, true);

                    if (entType != null) {
                        if (entType.is(EntityTypes1_13.EntityType.FALLING_BLOCK)) {
                            int data = wrapper.get(Types.INT, 0);
                            wrapper.set(Types.INT, 0, protocol.getMappingData().getNewBlockStateId(data));
                        }
                        // Register Type ID
                        wrapper.user().getEntityTracker(Protocol1_13To1_13_1.class).addEntity(entityId, entType);
                    }
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
                map(Types1_13.METADATA_LIST); // 12 - Metadata

                handler(metadataRewriter.trackerAndRewriterHandler(Types1_13.METADATA_LIST));
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
                map(Types1_13.METADATA_LIST); // 7 - Metadata

                handler(metadataRewriter.trackerAndRewriterHandler(Types1_13.METADATA_LIST, EntityTypes1_13.EntityType.PLAYER));
            }
        });

        metadataRewriter.registerRemoveEntities(ClientboundPackets1_13.REMOVE_ENTITIES);
        metadataRewriter.registerMetadataRewriter(ClientboundPackets1_13.SET_ENTITY_DATA, Types1_13.METADATA_LIST);
    }
}
