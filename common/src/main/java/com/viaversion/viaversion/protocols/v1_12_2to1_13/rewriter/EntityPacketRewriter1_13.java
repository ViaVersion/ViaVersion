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
package com.viaversion.viaversion.protocols.v1_12_2to1_13.rewriter;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_13;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.Protocol1_12_2To1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.blockconnections.ConnectionData;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.data.EntityIdMappings1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.data.ParticleIdMappings1_13;
import com.viaversion.viaversion.protocols.v1_12to1_12_1.packet.ClientboundPackets1_12_1;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.util.ComponentUtil;

public class EntityPacketRewriter1_13 extends EntityRewriter<ClientboundPackets1_12_1, Protocol1_12_2To1_13> {

    public EntityPacketRewriter1_13(Protocol1_12_2To1_13 protocol) {
        super(protocol);
    }

    @Override
    protected void registerPackets() {
        protocol.registerClientbound(ClientboundPackets1_12_1.ADD_ENTITY, new PacketHandlers() {
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
                    int data = wrapper.get(Types.INT, 0);
                    EntityTypes1_13.EntityType entType = EntityTypes1_13.ObjectType.getEntityType(type, data);
                    if (entType == null) return;

                    // Register Type ID
                    wrapper.user().getEntityTracker(Protocol1_12_2To1_13.class).addEntity(entityId, entType);

                    if (entType.is(EntityTypes1_13.EntityType.FALLING_BLOCK)) {
                        int oldId = wrapper.get(Types.INT, 0);
                        int combined = (((oldId & 4095) << 4) | (oldId >> 12 & 15));
                        wrapper.set(Types.INT, 0, WorldPacketRewriter1_13.toNewId(combined));
                    }

                    // Fix ItemFrame hitbox
                    if (entType.is(EntityTypes1_13.EntityType.ITEM_FRAME)) {
                        switch (data) {
                            case 0 -> data = 3; // South
                            case 1 -> data = 4; // West
                            // North is the same
                            case 3 -> data = 5; // East
                        }

                        wrapper.set(Types.INT, 0, data);
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_12_1.ADD_MOB, new PacketHandlers() {
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
                map(Types.ENTITY_DATA_LIST1_12, Types1_13.ENTITY_DATA_LIST); // 12 - Entity data

                handler(trackerAndRewriterHandler(Types1_13.ENTITY_DATA_LIST));
            }
        });

        protocol.registerClientbound(ClientboundPackets1_12_1.ADD_PLAYER, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Entity ID
                map(Types.UUID); // 1 - Player UUID
                map(Types.DOUBLE); // 2 - X
                map(Types.DOUBLE); // 3 - Y
                map(Types.DOUBLE); // 4 - Z
                map(Types.BYTE); // 5 - Yaw
                map(Types.BYTE); // 6 - Pitch
                map(Types.ENTITY_DATA_LIST1_12, Types1_13.ENTITY_DATA_LIST); // 7 - Entity data

                handler(trackerAndRewriterHandler(Types1_13.ENTITY_DATA_LIST, EntityTypes1_13.EntityType.PLAYER));
            }
        });

        protocol.registerClientbound(ClientboundPackets1_12_1.LOGIN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); // 0 - Entity ID
                map(Types.UNSIGNED_BYTE); // 1 - Gamemode
                map(Types.INT); // 2 - Dimension

                handler(wrapper -> {
                    ClientWorld clientChunks = wrapper.user().getClientWorld(Protocol1_12_2To1_13.class);
                    int dimensionId = wrapper.get(Types.INT, 1);
                    clientChunks.setEnvironment(dimensionId);
                });
                handler(playerTrackerHandler());
                handler(Protocol1_12_2To1_13.SEND_DECLARE_COMMANDS_AND_TAGS);
            }
        });

        protocol.registerClientbound(ClientboundPackets1_12_1.RESPAWN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); // 0 - Dimension ID
                handler(wrapper -> {
                    ClientWorld clientWorld = wrapper.user().getClientWorld(Protocol1_12_2To1_13.class);
                    int dimensionId = wrapper.get(Types.INT, 0);
                    if (clientWorld.setEnvironment(dimensionId)) {
                        if (Via.getConfig().isServersideBlockConnections()) {
                            ConnectionData.clearBlockStorage(wrapper.user());
                        }
                        tracker(wrapper.user()).clearEntities();
                    }
                });
                handler(Protocol1_12_2To1_13.SEND_DECLARE_COMMANDS_AND_TAGS);
            }
        });

        protocol.registerClientbound(ClientboundPackets1_12_1.UPDATE_MOB_EFFECT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // Entity id
                map(Types.BYTE); // Effect id
                map(Types.BYTE); // Amplifier
                map(Types.VAR_INT); // Duration

                handler(packetWrapper -> {
                    byte flags = packetWrapper.read(Types.BYTE); // Input Flags

                    if (Via.getConfig().isNewEffectIndicator())
                        flags |= 0x04;

                    packetWrapper.write(Types.BYTE, flags);
                });
            }
        });

        registerRemoveEntities(ClientboundPackets1_12_1.REMOVE_ENTITIES);
        registerSetEntityData(ClientboundPackets1_12_1.SET_ENTITY_DATA, Types.ENTITY_DATA_LIST1_12, Types1_13.ENTITY_DATA_LIST);
    }

    @Override
    protected void registerRewrites() {
        filter().mapDataType(typeId -> Types1_13.ENTITY_DATA_TYPES.byId(typeId > 4 ? typeId + 1 : typeId));
        filter().dataType(Types1_13.ENTITY_DATA_TYPES.itemType).handler(((event, data) -> protocol.getItemRewriter().handleItemToClient(event.user(), data.value())));
        filter().dataType(Types1_13.ENTITY_DATA_TYPES.optionalBlockStateType).handler(((event, data) -> {
            final int oldId = data.value();
            if (oldId != 0) {
                final int combined = (((oldId & 4095) << 4) | (oldId >> 12 & 15));
                final int newId = WorldPacketRewriter1_13.toNewId(combined);
                data.setValue(newId);
            }
        }));

        // Previously unused, now swimming
        filter().index(0).handler((event, data) -> data.setValue((byte) ((byte) data.getValue() & ~0x10)));

        filter().index(2).handler(((event, data) -> {
            if (data.getValue() != null && !((String) data.getValue()).isEmpty()) {
                data.setTypeAndValue(Types1_13.ENTITY_DATA_TYPES.optionalComponentType, ComponentUtil.legacyToJson((String) data.getValue()));
            } else {
                data.setTypeAndValue(Types1_13.ENTITY_DATA_TYPES.optionalComponentType, null);
            }
        }));

        filter().type(EntityTypes1_13.EntityType.WOLF).index(17).handler((event, data) -> {
            // Handle new colors
            data.setValue(15 - (int) data.getValue());
        });

        filter().type(EntityTypes1_13.EntityType.ZOMBIE).addIndex(15); // Shaking

        filter().type(EntityTypes1_13.EntityType.ABSTRACT_MINECART).index(9).handler((event, data) -> {
            final int oldId = data.value();
            final int combined = (((oldId & 4095) << 4) | (oldId >> 12 & 15));
            final int newId = WorldPacketRewriter1_13.toNewId(combined);
            data.setValue(newId);
        });

        filter().type(EntityTypes1_13.EntityType.AREA_EFFECT_CLOUD).handler((event, data) -> {
            if (data.id() == 9) {
                int particleId = data.value();
                EntityData parameter1Data = event.dataAtIndex(10);
                EntityData parameter2Data = event.dataAtIndex(11);
                int parameter1 = parameter1Data != null ? parameter1Data.value() : 0;
                int parameter2 = parameter2Data != null ? parameter2Data.value() : 0;

                Particle particle = ParticleIdMappings1_13.rewriteParticle(particleId, new Integer[]{parameter1, parameter2});
                if (particle != null && particle.id() != -1) {
                    event.createExtraData(new EntityData(9, Types1_13.ENTITY_DATA_TYPES.particleType, particle));
                }
            }
            if (data.id() >= 9) {
                event.cancel();
            }
        });
    }

    @Override
    public int newEntityId(final int id) {
        return EntityIdMappings1_13.getNewId(id);
    }

    @Override
    public EntityType typeFromId(int type) {
        return EntityTypes1_13.EntityType.findById(type);
    }

    @Override
    public EntityType objectTypeFromId(int type, int data) {
        return EntityTypes1_13.ObjectType.getEntityType(type, data);
    }
}
