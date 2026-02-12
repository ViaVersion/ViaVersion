/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.template;

import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_11;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPackets26_1;
import com.viaversion.viaversion.rewriter.EntityRewriter;

// Replace if needed
//  VersionedTypes
//  EntityTypes1_21_11
final class EntityPacketRewriter99_1 extends EntityRewriter<ClientboundPacket26_1, Protocol98_1To99_1> {

    public EntityPacketRewriter99_1(final Protocol98_1To99_1 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        // Tracks entities, applies entity data rewrites registered below, untracks entities
        registerTrackerWithData1_21_9(ClientboundPackets26_1.ADD_ENTITY, EntityTypes1_21_11.FALLING_BLOCK);
        registerSetEntityData(ClientboundPackets26_1.SET_ENTITY_DATA);
        registerRemoveEntities(ClientboundPackets26_1.REMOVE_ENTITIES);
        registerPlayerAbilities(ClientboundPackets26_1.PLAYER_ABILITIES);
        registerGameEvent(ClientboundPackets26_1.GAME_EVENT);
        registerLogin1_20_5(ClientboundPackets26_1.LOGIN);
        registerRespawn1_20_5(ClientboundPackets26_1.RESPAWN);
    }

    @Override
    protected void registerRewrites() {
        final EntityDataTypes26_1 entityDataTypes = protocol.mappedTypes().entityDataTypes();
        filter().mapDataType(entityDataTypes::byId);
        /* ... or something this if entity data classes changed
        filter().mapDataType(typeId -> {
            int id = typeId;
            if (id >= SomeAddedIndex) {
                id++;
            }
            return entityDataTypes.byId(id);
        });*/

        // Registers registry type id changes
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
        // IF ENTITY TYPES CHANGED: Automatically map entity id changes AFTER entity ids have been loaded
        mapTypes();
    }

    @Override
    public EntityType typeFromId(final int type) {
        return EntityTypes1_21_11.getTypeFromId(type);
    }
}
