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

import com.viaversion.viaversion.api.minecraft.Vector3d;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_9;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21_9;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundConfigurationPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundPacket1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.Protocol1_21_7To1_21_9;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.rewriter.RegistryDataRewriter;

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

        final RegistryDataRewriter registryDataRewriter = new RegistryDataRewriter(protocol);
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
        final EntityDataTypes1_21_9 entityDataTypes = protocol.mappedTypes().entityDataTypes();
        filter().mapDataType(typeId -> {
            int id = typeId;
            if (id > entityDataTypes.armadilloState.typeId()) {
                id += 2; // copper golem and weathering copper state
            }
            return entityDataTypes.byId(id);
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
