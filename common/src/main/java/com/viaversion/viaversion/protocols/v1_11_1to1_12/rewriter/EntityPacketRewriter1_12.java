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
package com.viaversion.viaversion.protocols.v1_11_1to1_12.rewriter;

import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_12;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_11_1to1_12.Protocol1_11_1To1_12;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.Protocol1_12_2To1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_9_1to1_9_3.packet.ClientboundPackets1_9_3;
import com.viaversion.viaversion.rewriter.EntityRewriter;

public class EntityPacketRewriter1_12 extends EntityRewriter<ClientboundPackets1_9_3, Protocol1_11_1To1_12> {

    public EntityPacketRewriter1_12(Protocol1_11_1To1_12 protocol) {
        super(protocol);
    }

    @Override
    protected void registerPackets() {
        protocol.registerClientbound(ClientboundPackets1_9_3.LOGIN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT);
                map(Types.UNSIGNED_BYTE);
                map(Types.INT);
                handler(wrapper -> {
                    ClientWorld clientWorld = wrapper.user().getClientWorld(Protocol1_11_1To1_12.class);
                    int dimensionId = wrapper.get(Types.INT, 1);
                    clientWorld.setEnvironment(dimensionId);

                    // Reset recipes
                    if (wrapper.user().getProtocolInfo().protocolVersion().newerThanOrEqualTo(ProtocolVersion.v1_13)) {
                        wrapper.create(ClientboundPackets1_13.UPDATE_RECIPES, packetWrapper -> packetWrapper.write(Types.VAR_INT, 0))
                            .scheduleSend(Protocol1_12_2To1_13.class);
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_9_3.RESPAWN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT);
                handler(wrapper -> {
                    ClientWorld clientWorld = wrapper.user().getClientWorld(Protocol1_11_1To1_12.class);
                    int dimensionId = wrapper.get(Types.INT, 0);
                    if (clientWorld.setEnvironment(dimensionId)) {
                        tracker(wrapper.user()).clearEntities();
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_9_3.ADD_ENTITY, new PacketHandlers() {
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
                handler(objectTrackerHandler());
            }
        });

        protocol.registerClientbound(ClientboundPackets1_9_3.ADD_MOB, new PacketHandlers() {
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
                map(Types.ENTITY_DATA_LIST1_9, Types.ENTITY_DATA_LIST1_12); // 12 - Entity data

                // Track mob and rewrite entity data
                handler(trackerAndRewriterHandler(Types.ENTITY_DATA_LIST1_12));
            }
        });

        registerRemoveEntities(ClientboundPackets1_9_3.REMOVE_ENTITIES);
        registerSetEntityData(ClientboundPackets1_9_3.SET_ENTITY_DATA, Types.ENTITY_DATA_LIST1_9, Types.ENTITY_DATA_LIST1_12);
    }

    @Override
    protected void registerRewrites() {
        filter().handler((event, data) -> {
            if (data.getValue() instanceof Item) {
                data.setValue(protocol.getItemRewriter().handleItemToClient(event.user(), data.value()));
            }
        });

        filter().type(EntityTypes1_12.EntityType.ABSTRACT_ILLAGER).addIndex(12); // Aggressive
    }

    @Override
    public EntityType typeFromId(int type) {
        return EntityTypes1_12.EntityType.findById(type);
    }

    @Override
    public EntityType objectTypeFromId(int type, int data) {
        return EntityTypes1_12.ObjectType.getEntityType(type, data);
    }
}
