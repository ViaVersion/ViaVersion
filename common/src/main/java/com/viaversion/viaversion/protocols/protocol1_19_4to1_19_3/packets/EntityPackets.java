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
package com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.packets;

import com.github.steveice10.opennbt.tag.builtin.ByteTag;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_19_4;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_19_3;
import com.viaversion.viaversion.api.type.types.version.Types1_19_4;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.ClientboundPackets1_19_3;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.Protocol1_19_4To1_19_3;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.storage.PlayerVehicleTracker;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.util.TagUtil;

public final class EntityPackets extends EntityRewriter<ClientboundPackets1_19_3, Protocol1_19_4To1_19_3> {

    public EntityPackets(final Protocol1_19_4To1_19_3 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        protocol.registerClientbound(ClientboundPackets1_19_3.JOIN_GAME, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); // Entity id
                map(Type.BOOLEAN); // Hardcore
                map(Type.BYTE); // Gamemode
                map(Type.BYTE); // Previous Gamemode
                map(Type.STRING_ARRAY); // World List
                map(Type.NAMED_COMPOUND_TAG); // Dimension registry
                map(Type.STRING); // Dimension key
                map(Type.STRING); // World
                handler(dimensionDataHandler());
                handler(biomeSizeTracker());
                handler(worldDataTrackerHandlerByKey());
                handler(playerTrackerHandler());
                handler(wrapper -> {
                    final CompoundTag registry = wrapper.get(Type.NAMED_COMPOUND_TAG, 0);
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
                map(Type.DOUBLE); // X
                map(Type.DOUBLE); // Y
                map(Type.DOUBLE); // Z
                map(Type.FLOAT); // Yaw
                map(Type.FLOAT); // Pitch
                map(Type.BYTE); // Relative arguments
                map(Type.VAR_INT); // Id
                handler(wrapper -> {
                    if (wrapper.read(Type.BOOLEAN)) { // Dismount vehicle
                        final PlayerVehicleTracker playerVehicleTracker = wrapper.user().get(PlayerVehicleTracker.class);
                        if (playerVehicleTracker.getVehicleId() != -1) {
                            final PacketWrapper bundleStart = wrapper.create(ClientboundPackets1_19_4.BUNDLE);
                            bundleStart.send(Protocol1_19_4To1_19_3.class);
                            final PacketWrapper setPassengers = wrapper.create(ClientboundPackets1_19_4.SET_PASSENGERS);
                            setPassengers.write(Type.VAR_INT, playerVehicleTracker.getVehicleId()); // vehicle id
                            setPassengers.write(Type.VAR_INT_ARRAY_PRIMITIVE, new int[0]); // passenger ids
                            setPassengers.send(Protocol1_19_4To1_19_3.class);
                            wrapper.send(Protocol1_19_4To1_19_3.class);
                            wrapper.cancel();
                            final PacketWrapper bundleEnd = wrapper.create(ClientboundPackets1_19_4.BUNDLE);
                            bundleEnd.send(Protocol1_19_4To1_19_3.class);

                            playerVehicleTracker.setVehicleId(-1);
                        }
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_3.SET_PASSENGERS, new PacketHandlers() {
            @Override
            protected void register() {
                map(Type.VAR_INT); // vehicle id
                map(Type.VAR_INT_ARRAY_PRIMITIVE); // passenger ids
                handler(wrapper -> {
                    final PlayerVehicleTracker playerVehicleTracker = wrapper.user().get(PlayerVehicleTracker.class);
                    final int clientEntityId = wrapper.user().getEntityTracker(Protocol1_19_4To1_19_3.class).clientEntityId();
                    final int vehicleId = wrapper.get(Type.VAR_INT, 0);

                    if (playerVehicleTracker.getVehicleId() == vehicleId) {
                        playerVehicleTracker.setVehicleId(-1);
                    }

                    final int[] passengerIds = wrapper.get(Type.VAR_INT_ARRAY_PRIMITIVE, 0);
                    for (int passengerId : passengerIds) {
                        if (passengerId == clientEntityId) {
                            playerVehicleTracker.setVehicleId(vehicleId);
                            break;
                        }
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_3.ENTITY_TELEPORT, new PacketHandlers() {
            @Override
            protected void register() {
                handler(wrapper -> {
                    final int entityId = wrapper.read(Type.VAR_INT); // entity id
                    final int clientEntityId = wrapper.user().getEntityTracker(Protocol1_19_4To1_19_3.class).clientEntityId();
                    if (entityId != clientEntityId) {
                        wrapper.write(Type.VAR_INT, entityId); // entity id
                        return;
                    }

                    wrapper.setPacketType(ClientboundPackets1_19_4.PLAYER_POSITION);
                    wrapper.passthrough(Type.DOUBLE); // x
                    wrapper.passthrough(Type.DOUBLE); // y
                    wrapper.passthrough(Type.DOUBLE); // z
                    wrapper.write(Type.FLOAT, wrapper.read(Type.BYTE) * 360F / 256F); // yaw
                    wrapper.write(Type.FLOAT, wrapper.read(Type.BYTE) * 360F / 256F); // pitch
                    wrapper.read(Type.BOOLEAN); // on ground
                    wrapper.write(Type.BYTE, (byte) 0); // flags
                    wrapper.write(Type.VAR_INT, -1); // teleport id
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_3.ENTITY_ANIMATION, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // Entity id
                handler(wrapper -> {
                    final short action = wrapper.read(Type.UNSIGNED_BYTE);
                    if (action != 1) {
                        wrapper.write(Type.UNSIGNED_BYTE, action);
                        return;
                    }

                    wrapper.setPacketType(ClientboundPackets1_19_4.HIT_ANIMATION);
                    wrapper.write(Type.FLOAT, 0F);
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_3.RESPAWN, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // Dimension
                map(Type.STRING); // World
                handler(worldDataTrackerHandlerByKey());
                handler(wrapper -> wrapper.user().put(new PlayerVehicleTracker()));
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_3.ENTITY_STATUS, wrapper -> {
            final int entityId = wrapper.read(Type.INT);
            final byte event = wrapper.read(Type.BYTE);

            final int damageType = damageTypeFromEntityEvent(event);
            if (damageType != -1) {
                wrapper.setPacketType(ClientboundPackets1_19_4.DAMAGE_EVENT);
                wrapper.write(Type.VAR_INT, entityId);
                wrapper.write(Type.VAR_INT, damageType);
                wrapper.write(Type.VAR_INT, 0); // No source entity
                wrapper.write(Type.VAR_INT, 0); // No direct source entity
                wrapper.write(Type.BOOLEAN, false); // No source position
                return;
            }

            wrapper.write(Type.INT, entityId);
            wrapper.write(Type.BYTE, event);
        });

        registerTrackerWithData1_19(ClientboundPackets1_19_3.SPAWN_ENTITY, EntityTypes1_19_4.FALLING_BLOCK);
        registerRemoveEntities(ClientboundPackets1_19_3.REMOVE_ENTITIES);
        registerMetadataRewriter(ClientboundPackets1_19_3.ENTITY_METADATA, Types1_19_3.METADATA_LIST, Types1_19_4.METADATA_LIST);
    }

    private int damageTypeFromEntityEvent(byte entityEvent) {
        switch (entityEvent) {
            case 33: // Thorned
                return 36;
            case 36: // Drowned
                return 5;
            case 37: // Burned
                return 27;
            case 57: // Frozen
                return 15;
            case 44: // Poked
            case 2: // Generic hurt
                return 16;
        }
        return -1;
    }

    @Override
    protected void registerRewrites() {
        filter().mapMetaType(typeId -> Types1_19_4.META_TYPES.byId(typeId >= 14 ? typeId + 1 : typeId)); // Optional block state (and map block state=14 to optional block state)
        registerMetaTypeHandler(Types1_19_4.META_TYPES.itemType, Types1_19_4.META_TYPES.blockStateType, Types1_19_4.META_TYPES.optionalBlockStateType, Types1_19_4.META_TYPES.particleType, null);

        filter().type(EntityTypes1_19_4.MINECART_ABSTRACT).index(11).handler((event, meta) -> {
            final int blockState = meta.value();
            meta.setValue(protocol.getMappingData().getNewBlockStateId(blockState));
        });

        filter().type(EntityTypes1_19_4.BOAT).index(11).handler((event, meta) -> {
            final int boatType = meta.value();
            if (boatType > 4) { // Cherry added
                meta.setValue(boatType + 1);
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
