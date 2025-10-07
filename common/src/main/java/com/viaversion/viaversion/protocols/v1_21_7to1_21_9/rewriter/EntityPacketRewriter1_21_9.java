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
package com.viaversion.viaversion.protocols.v1_21_7to1_21_9.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.GlobalBlockPosition;
import com.viaversion.viaversion.api.minecraft.RegistryEntry;
import com.viaversion.viaversion.api.minecraft.Vector3d;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_9;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21_5;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21_9;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundConfigurationPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundPacket1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.Protocol1_21_7To1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.storage.DimensionScaleStorage;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.rewriter.RegistryDataRewriter;
import com.viaversion.viaversion.rewriter.entitydata.EntityDataHandler;
import com.viaversion.viaversion.util.Key;

public final class EntityPacketRewriter1_21_9 extends EntityRewriter<ClientboundPacket1_21_6, Protocol1_21_7To1_21_9> {

    public EntityPacketRewriter1_21_9(final Protocol1_21_7To1_21_9 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        registerSetEntityData(ClientboundPackets1_21_6.SET_ENTITY_DATA);
        registerRemoveEntities(ClientboundPackets1_21_6.REMOVE_ENTITIES);
        registerPlayerAbilities(ClientboundPackets1_21_6.PLAYER_ABILITIES);
        registerGameEvent(ClientboundPackets1_21_6.GAME_EVENT);
        registerLogin1_20_5(ClientboundPackets1_21_6.LOGIN);
        registerRespawn1_20_5(ClientboundPackets1_21_6.RESPAWN);

        protocol.registerClientbound(ClientboundPackets1_21_6.ADD_ENTITY, wrapper -> {
            final int entityId = wrapper.passthrough(Types.VAR_INT);
            wrapper.passthrough(Types.UUID); // Entity UUID
            final int entityTypeId = wrapper.passthrough(Types.VAR_INT);
            wrapper.passthrough(Types.DOUBLE); // X
            wrapper.passthrough(Types.DOUBLE); // Y
            wrapper.passthrough(Types.DOUBLE); // Z

            wrapper.write(Types.MOVEMENT_VECTOR, Vector3d.ZERO); // Set at the end

            wrapper.passthrough(Types.BYTE); // Pitch
            wrapper.passthrough(Types.BYTE); // Yaw
            wrapper.passthrough(Types.BYTE); // Head yaw
            final int data = wrapper.passthrough(Types.VAR_INT);
            final EntityType entityType = trackAndRewrite(wrapper, entityTypeId, entityId);
            if (protocol.getMappingData() != null && entityType == EntityTypes1_21_9.FALLING_BLOCK) {
                final int mappedBlockStateId = protocol.getMappingData().getNewBlockStateId(data);
                wrapper.set(Types.VAR_INT, 2, mappedBlockStateId);
            }

            wrapper.set(Types.MOVEMENT_VECTOR, 0, readRelativeMovement(wrapper));
        });

        protocol.registerClientbound(ClientboundPackets1_21_6.SET_ENTITY_MOTION, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Entity ID
            wrapper.write(Types.MOVEMENT_VECTOR, readRelativeMovement(wrapper));
        });

        protocol.registerClientbound(ClientboundPackets1_21_6.PLAYER_ROTATION, wrapper -> {
            wrapper.passthrough(Types.FLOAT); // Y rotation
            wrapper.write(Types.BOOLEAN, false); // Relative Y rotation
            wrapper.passthrough(Types.FLOAT); // X rotation
            wrapper.write(Types.BOOLEAN, false); // Relative X rotation
        });

        protocol.registerClientbound(ClientboundPackets1_21_6.SET_DEFAULT_SPAWN_POSITION, wrapper -> {
            final BlockPosition pos = wrapper.read(Types.BLOCK_POSITION1_14);
            final String dimension = tracker(wrapper.user()).currentWorld();
            wrapper.write(Types.GLOBAL_POSITION, new GlobalBlockPosition(dimension, pos.x(), pos.y(), pos.z()));
            wrapper.passthrough(Types.FLOAT); // Yaw
            wrapper.write(Types.FLOAT, 0F); // Pitch
        });

        final RegistryDataRewriter registryDataRewriter = new RegistryDataRewriter(protocol) {
            @Override
            protected void handleParticleData(final CompoundTag particleData) {
                final String type = particleData.getString("type");
                if (type != null && Key.stripMinecraftNamespace(type).equals("flash")) {
                    particleData.putInt("color", -1);
                }
                super.handleParticleData(particleData);
            }

            @Override
            public void trackDimensionAndBiomes(final UserConnection connection, final String registryKey, final RegistryEntry[] entries) {
                super.trackDimensionAndBiomes(connection, registryKey, entries);
                if (!registryKey.equals("dimension_type")) {
                    return;
                }

                final DimensionScaleStorage dimensionScaleStorage = connection.get(DimensionScaleStorage.class);
                for (int i = 0; i < entries.length; i++) {
                    final RegistryEntry entry = entries[i];
                    final CompoundTag dimension = (CompoundTag) entry.tag();
                    if (dimension == null) {
                        continue;
                    }

                    final double coordinateScale = dimension.getDouble("coordinate_scale", 1);
                    dimensionScaleStorage.setScale(i, coordinateScale);
                }
            }
        };
        registryDataRewriter.addHandler("dimension_type", (key, dimension) -> {
            if (Key.equals(key, "minecraft:the_end")) {
                dimension.putFloat("ambient_light", 0.25F); // End now has actual skylight
            }
        });
        protocol.registerClientbound(ClientboundConfigurationPackets1_21_6.REGISTRY_DATA, registryDataRewriter::handle);
    }

    private Vector3d readRelativeMovement(final PacketWrapper wrapper) {
        final double movementX = wrapper.read(Types.SHORT) / 8000D;
        final double movementY = wrapper.read(Types.SHORT) / 8000D;
        final double movementZ = wrapper.read(Types.SHORT) / 8000D;
        return new Vector3d(movementX, movementY, movementZ);
    }

    @Override
    protected void registerRewrites() {
        final EntityDataTypes1_21_5 unmappedEntityDataTypes = protocol.types().entityDataTypes();
        final EntityDataTypes1_21_9 entityDataTypes = protocol.mappedTypes().entityDataTypes();
        filter().handler((event, data) -> {
            int id = data.dataType().typeId();
            if (id == unmappedEntityDataTypes.compoundTagType.typeId()) {
                if (event.entityType() == null) {
                    // Remove unhandled data from bad packets here
                    event.cancel();
                }
                return; // Handled below
            }
            if (id > unmappedEntityDataTypes.compoundTagType.typeId()) {
                id--;
            }
            if (id > entityDataTypes.armadilloState.typeId()) {
                id += 2; // copper golem and weathering copper state
            }
            data.setDataType(entityDataTypes.byId(id));
        });

        registerEntityDataTypeHandler(
            entityDataTypes.itemType,
            entityDataTypes.blockStateType,
            entityDataTypes.optionalBlockStateType,
            entityDataTypes.particleType,
            entityDataTypes.particlesType,
            entityDataTypes.componentType,
            entityDataTypes.optionalComponentType
        );

        final EntityDataHandler shoulderDataHandler = (event, data) -> {
            final CompoundTag value = data.value();
            if (value == null) {
                data.setTypeAndValue(protocol.mappedTypes().entityDataTypes.optionalVarIntType, null);
                return;
            }

            final int variant = value.getInt("Variant", -1);
            if (variant != -1) {
                data.setTypeAndValue(protocol.mappedTypes().entityDataTypes.optionalVarIntType, variant);
            } else {
                data.setTypeAndValue(protocol.mappedTypes().entityDataTypes.optionalVarIntType, null);
            }
        };
        filter().type(EntityTypes1_21_9.PLAYER).index(19).handler(shoulderDataHandler);
        filter().type(EntityTypes1_21_9.PLAYER).index(20).handler(shoulderDataHandler);
        filter().type(EntityTypes1_21_9.PLAYER).handler((event, data) -> {
            // Move model customization and main hand to avatar
            // except they've been swapped
            if (event.index() == 17) {
                event.setIndex(16);
            } else if (event.index() == 18) {
                event.setIndex(15);
            } else if (event.index() == 15 || event.index() == 16) {
                event.setIndex(event.index() + 2); // Move hearts and score up
            }
        });
    }

    @Override
    public void onMappingDataLoaded() {
        mapTypes();
    }

    @Override
    public EntityType typeFromId(final int type) {
        return EntityTypes1_21_9.getTypeFromId(type);
    }
}
