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
package com.viaversion.viaversion.protocols.v1_21_11to26_1.rewriter;

import com.viaversion.viaversion.api.minecraft.Vector3d;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_11;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21_11;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.Protocol1_21_11To26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ServerboundPackets26_1;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ClientboundPacket1_21_11;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ClientboundPackets1_21_11;
import com.viaversion.viaversion.rewriter.EntityRewriter;

public final class EntityPacketRewriter26_1 extends EntityRewriter<ClientboundPacket1_21_11, Protocol1_21_11To26_1> {

    public EntityPacketRewriter26_1(final Protocol1_21_11To26_1 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        registerTrackerWithData1_21_9(ClientboundPackets1_21_11.ADD_ENTITY, EntityTypes1_21_11.FALLING_BLOCK);
        registerSetEntityData(ClientboundPackets1_21_11.SET_ENTITY_DATA);
        registerRemoveEntities(ClientboundPackets1_21_11.REMOVE_ENTITIES);
        registerPlayerAbilities(ClientboundPackets1_21_11.PLAYER_ABILITIES);
        registerGameEvent(ClientboundPackets1_21_11.GAME_EVENT);
        registerLogin1_20_5(ClientboundPackets1_21_11.LOGIN);
        registerRespawn1_20_5(ClientboundPackets1_21_11.RESPAWN);

        protocol.registerServerbound(ServerboundPackets26_1.INTERACT, wrapper -> {
            final int entityId = wrapper.passthrough(Types.VAR_INT);
            final int hand = wrapper.read(Types.VAR_INT);
            final Vector3d location = wrapper.read(Types.LOW_PRECISION_VECTOR);
            if (tracker(wrapper.user()).entityType(entityId) == EntityTypes1_21_11.ARMOR_STAND) {
                wrapper.write(Types.VAR_INT, 2); // Interact at
                wrapper.write(Types.FLOAT, (float) location.x());
                wrapper.write(Types.FLOAT, (float) location.y());
                wrapper.write(Types.FLOAT, (float) location.z());
                wrapper.write(Types.VAR_INT, hand);
                return;
            }

            wrapper.write(Types.VAR_INT, 0); // Interact
            wrapper.write(Types.VAR_INT, hand);
        });
        protocol.registerServerbound(ServerboundPackets26_1.ATTACK, ServerboundPackets1_21_6.INTERACT, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Entity ID
            wrapper.write(Types.VAR_INT, 1); // Attack
            wrapper.write(Types.BOOLEAN, false); // Secondary action
        });
        protocol.registerServerbound(ServerboundPackets26_1.SPECTATE_ENTITY, ServerboundPackets1_21_6.INTERACT, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Entity ID
            wrapper.write(Types.VAR_INT, 1); // Attack
            wrapper.write(Types.BOOLEAN, true); // Secondary action
        });
    }

    @Override
    protected void registerRewrites() {
        final EntityDataTypes1_21_11 entityDataTypes = protocol.mappedTypes().entityDataTypes();
        filter().mapDataType(entityDataTypes::byId);
        registerEntityDataTypeHandler(
            entityDataTypes.itemType,
            entityDataTypes.blockStateType,
            entityDataTypes.optionalBlockStateType,
            entityDataTypes.particleType,
            entityDataTypes.particlesType,
            entityDataTypes.componentType,
            entityDataTypes.optionalComponentType
        );

        filter().type(EntityTypes1_21_11.ABSTRACT_VILLAGER).addIndex(19); // Is villager data finalized
        filter().type(EntityTypes1_21_11.ZOMBIE_VILLAGER).addIndex(21); // Is villager data finalized
        filter().type(EntityTypes1_21_11.ABSTRACT_AGEABLE).addIndex(17); // Age locked
    }

    @Override
    public void onMappingDataLoaded() {
        mapTypes();
    }

    @Override
    public EntityType typeFromId(final int type) {
        return EntityTypes1_21_11.getTypeFromId(type);
    }
}
