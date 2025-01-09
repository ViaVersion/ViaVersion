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
package com.viaversion.viaversion.protocols.v1_19_3to1_19_4.rewriter;

import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_19_4;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_19_3;
import com.viaversion.viaversion.api.type.types.version.Types1_19_4;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.packet.ClientboundPackets1_19_3;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.Protocol1_19_3To1_19_4;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.storage.PlayerVehicleTracker;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.util.TagUtil;

public final class EntityPacketRewriter1_19_4 extends EntityRewriter<ClientboundPackets1_19_3, Protocol1_19_3To1_19_4> {

    public EntityPacketRewriter1_19_4(final Protocol1_19_3To1_19_4 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        protocol.registerClientbound(ClientboundPackets1_19_3.LOGIN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); // Entity id
                map(Types.BOOLEAN); // Hardcore
                map(Types.BYTE); // Gamemode
                map(Types.BYTE); // Previous Gamemode
                map(Types.STRING_ARRAY); // World List
                map(Types.NAMED_COMPOUND_TAG); // Dimension registry
                map(Types.STRING); // Dimension key
                map(Types.STRING); // World
                handler(dimensionDataHandler());
                handler(biomeSizeTracker());
                handler(worldDataTrackerHandlerByKey());
                handler(playerTrackerHandler());
                handler(wrapper -> {
                    final CompoundTag registry = wrapper.get(Types.NAMED_COMPOUND_TAG, 0);
                    final CompoundTag damageTypeRegistry = protocol.getMappingData().damageTypesRegistry();
                    registry.put("minecraft:damage_type", damageTypeRegistry);

                    final ListTag<CompoundTag> biomes = TagUtil.getRegistryEntries(registry, "worldgen/biome");
                    for (final CompoundTag biomeTag : biomes) {
                        final CompoundTag biomeData = biomeTag.getCompoundTag("element");
                        final StringTag precipitation = biomeData.getStringTag("precipitation");
                        final byte precipitationByte = precipitation.getValue().equals("none") ? (byte) 0 : 1;
                        biomeData.put("has_precipitation", new ByteTag(precipitationByte));
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_3.PLAYER_POSITION, new PacketHandlers() {
            @Override
            protected void register() {
                map(Types.DOUBLE); // X
                map(Types.DOUBLE); // Y
                map(Types.DOUBLE); // Z
                map(Types.FLOAT); // Yaw
                map(Types.FLOAT); // Pitch
                map(Types.BYTE); // Relative arguments
                map(Types.VAR_INT); // Id
                handler(wrapper -> {
                    if (wrapper.read(Types.BOOLEAN)) { // Dismount vehicle
                        final PlayerVehicleTracker playerVehicleTracker = wrapper.user().get(PlayerVehicleTracker.class);
                        if (playerVehicleTracker.getVehicleId() != -1) {
                            final PacketWrapper bundleStart = wrapper.create(ClientboundPackets1_19_4.BUNDLE_DELIMITER);
                            bundleStart.send(Protocol1_19_3To1_19_4.class);
                            final PacketWrapper setPassengers = wrapper.create(ClientboundPackets1_19_4.SET_PASSENGERS);
                            setPassengers.write(Types.VAR_INT, playerVehicleTracker.getVehicleId()); // vehicle id
                            setPassengers.write(Types.VAR_INT_ARRAY_PRIMITIVE, new int[0]); // passenger ids
                            setPassengers.send(Protocol1_19_3To1_19_4.class);
                            wrapper.send(Protocol1_19_3To1_19_4.class);
                            wrapper.cancel();
                            final PacketWrapper bundleEnd = wrapper.create(ClientboundPackets1_19_4.BUNDLE_DELIMITER);
                            bundleEnd.send(Protocol1_19_3To1_19_4.class);

                            playerVehicleTracker.setVehicleId(-1);
                        }
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_3.SET_PASSENGERS, new PacketHandlers() {
            @Override
            protected void register() {
                map(Types.VAR_INT); // vehicle id
                map(Types.VAR_INT_ARRAY_PRIMITIVE); // passenger ids
                handler(wrapper -> {
                    final PlayerVehicleTracker playerVehicleTracker = wrapper.user().get(PlayerVehicleTracker.class);
                    final int clientEntityId = wrapper.user().getEntityTracker(Protocol1_19_3To1_19_4.class).clientEntityId();
                    final int vehicleId = wrapper.get(Types.VAR_INT, 0);

                    if (playerVehicleTracker.getVehicleId() == vehicleId) {
                        playerVehicleTracker.setVehicleId(-1);
                    }

                    final int[] passengerIds = wrapper.get(Types.VAR_INT_ARRAY_PRIMITIVE, 0);
                    for (int passengerId : passengerIds) {
                        if (passengerId == clientEntityId) {
                            playerVehicleTracker.setVehicleId(vehicleId);
                            break;
                        }
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_3.TELEPORT_ENTITY, new PacketHandlers() {
            @Override
            protected void register() {
                handler(wrapper -> {
                    final int entityId = wrapper.read(Types.VAR_INT); // entity id
                    final int clientEntityId = wrapper.user().getEntityTracker(Protocol1_19_3To1_19_4.class).clientEntityId();
                    if (entityId != clientEntityId) {
                        wrapper.write(Types.VAR_INT, entityId); // entity id
                        return;
                    }

                    wrapper.setPacketType(ClientboundPackets1_19_4.PLAYER_POSITION);
                    wrapper.passthrough(Types.DOUBLE); // x
                    wrapper.passthrough(Types.DOUBLE); // y
                    wrapper.passthrough(Types.DOUBLE); // z
                    wrapper.write(Types.FLOAT, wrapper.read(Types.BYTE) * 360F / 256F); // yaw
                    wrapper.write(Types.FLOAT, wrapper.read(Types.BYTE) * 360F / 256F); // pitch
                    wrapper.read(Types.BOOLEAN); // on ground
                    wrapper.write(Types.BYTE, (byte) 0); // flags
                    wrapper.write(Types.VAR_INT, -1); // teleport id
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_3.ANIMATE, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // Entity id
                handler(wrapper -> {
                    final short action = wrapper.read(Types.UNSIGNED_BYTE);
                    if (action != 1) {
                        wrapper.write(Types.UNSIGNED_BYTE, action);
                        return;
                    }

                    wrapper.setPacketType(ClientboundPackets1_19_4.HURT_ANIMATION);
                    wrapper.write(Types.FLOAT, 0F);
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_3.RESPAWN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); // Dimension
                map(Types.STRING); // World
                handler(worldDataTrackerHandlerByKey());
                handler(wrapper -> wrapper.user().put(new PlayerVehicleTracker()));
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_3.ENTITY_EVENT, wrapper -> {
            final int entityId = wrapper.read(Types.INT);
            final byte event = wrapper.read(Types.BYTE);

            final int damageType = damageTypeFromEntityEvent(event);
            if (damageType != -1) {
                wrapper.setPacketType(ClientboundPackets1_19_4.DAMAGE_EVENT);
                wrapper.write(Types.VAR_INT, entityId);
                wrapper.write(Types.VAR_INT, damageType);
                wrapper.write(Types.VAR_INT, 0); // No source entity
                wrapper.write(Types.VAR_INT, 0); // No direct source entity
                wrapper.write(Types.BOOLEAN, false); // No source position
                return;
            }

            wrapper.write(Types.INT, entityId);
            wrapper.write(Types.BYTE, event);
        });

        registerTrackerWithData1_19(ClientboundPackets1_19_3.ADD_ENTITY, EntityTypes1_19_4.FALLING_BLOCK);
        registerRemoveEntities(ClientboundPackets1_19_3.REMOVE_ENTITIES);
        registerSetEntityData(ClientboundPackets1_19_3.SET_ENTITY_DATA, Types1_19_3.ENTITY_DATA_LIST, Types1_19_4.ENTITY_DATA_LIST);
    }

    private int damageTypeFromEntityEvent(byte entityEvent) {
        return switch (entityEvent) {
            case 33 -> 36; // Thorned
            case 36 -> 5; // Drowned
            case 37 -> 27; // Burned
            case 57 -> 15; // Frozen -> Poked
            case 44, 2 -> 16; // Generic hurt
            default -> -1;
        };
    }

    @Override
    protected void registerRewrites() {
        filter().mapDataType(typeId -> Types1_19_4.ENTITY_DATA_TYPES.byId(typeId >= 14 ? typeId + 1 : typeId)); // Optional block state (and map block state=14 to optional block state)
        registerEntityDataTypeHandler(Types1_19_4.ENTITY_DATA_TYPES.itemType, Types1_19_4.ENTITY_DATA_TYPES.blockStateType, Types1_19_4.ENTITY_DATA_TYPES.optionalBlockStateType, Types1_19_4.ENTITY_DATA_TYPES.particleType, null);
        registerBlockStateHandler(EntityTypes1_19_4.ABSTRACT_MINECART, 11);

        filter().type(EntityTypes1_19_4.BOAT).index(11).handler((event, data) -> {
            final int boatType = data.value();
            if (boatType > 4) { // Cherry added
                data.setValue(boatType + 1);
            }
        });

        filter().type(EntityTypes1_19_4.ABSTRACT_HORSE).removeIndex(18); // Owner UUID
    }

    @Override
    public void onMappingDataLoaded() {
        mapTypes();
    }

    @Override
    public EntityType typeFromId(final int type) {
        return EntityTypes1_19_4.getTypeFromId(type);
    }
}
