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
package com.viaversion.viaversion.protocols.v1_9_3to1_10.rewriter;

import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_9;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_9_1to1_9_3.packet.ClientboundPackets1_9_3;
import com.viaversion.viaversion.protocols.v1_9_3to1_10.Protocol1_9_3To1_10;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import java.util.List;

public class EntityPacketRewriter1_10 extends EntityRewriter<ClientboundPackets1_9_3, Protocol1_9_3To1_10> {

    public EntityPacketRewriter1_10(Protocol1_9_3To1_10 protocol) {
        super(protocol);
    }

    @Override
    protected void registerPackets() {
        protocol.registerClientbound(ClientboundPackets1_9_3.ADD_ENTITY, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Entity id
            wrapper.passthrough(Types.UUID); // UUID
            wrapper.passthrough(Types.BYTE); // Type
            wrapper.passthrough(Types.DOUBLE); // X
            wrapper.passthrough(Types.DOUBLE); // Y
            wrapper.passthrough(Types.DOUBLE); // Z
            wrapper.passthrough(Types.BYTE); // Pitch
            wrapper.passthrough(Types.BYTE); // Yaw
            wrapper.passthrough(Types.INT); // Data
            objectTrackerHandler().handle(wrapper);
        });
        protocol.registerClientbound(ClientboundPackets1_9_3.ADD_MOB, wrapper -> {
            final int entityId = wrapper.passthrough(Types.VAR_INT);
            wrapper.passthrough(Types.UUID); // UUID
            final short entityType = wrapper.passthrough(Types.UNSIGNED_BYTE);
            wrapper.passthrough(Types.DOUBLE); // X
            wrapper.passthrough(Types.DOUBLE); // Y
            wrapper.passthrough(Types.DOUBLE); // Z
            wrapper.passthrough(Types.BYTE); // Yaw
            wrapper.passthrough(Types.BYTE); // Pitch
            wrapper.passthrough(Types.BYTE); // Head Pitch
            wrapper.passthrough(Types.SHORT); // Velocity X
            wrapper.passthrough(Types.SHORT); // Velocity Y
            wrapper.passthrough(Types.SHORT); // Velocity Z
            final List<EntityData> entityDataList = wrapper.passthrough(Types.ENTITY_DATA_LIST1_9);
            trackAndRewrite(wrapper, entityType, entityId);
            handleEntityData(entityId, entityDataList, wrapper.user());
        });
        protocol.registerClientbound(ClientboundPackets1_9_3.ADD_PLAYER, wrapper -> {
            final int entityId = wrapper.passthrough(Types.VAR_INT);
            wrapper.passthrough(Types.UUID); // Player UUID
            wrapper.passthrough(Types.DOUBLE); // X
            wrapper.passthrough(Types.DOUBLE); // Y
            wrapper.passthrough(Types.DOUBLE); // Z
            wrapper.passthrough(Types.BYTE); // Yaw
            wrapper.passthrough(Types.BYTE); // Pitch
            final List<EntityData> entityDataList = wrapper.passthrough(Types.ENTITY_DATA_LIST1_9);
            tracker(wrapper.user()).addEntity(entityId, EntityTypes1_9.EntityType.PLAYER);
            handleEntityData(entityId, entityDataList, wrapper.user());
        });
        registerSetEntityData(ClientboundPackets1_9_3.SET_ENTITY_DATA, Types.ENTITY_DATA_LIST1_9);
        registerRemoveEntities(ClientboundPackets1_9_3.REMOVE_ENTITIES);
    }

    @Override
    protected void registerRewrites() {
        // The item data slot was created via the wrong entity type class, using an index of 6 instead of 5 for the item
        filter().type(EntityTypes1_9.EntityType.POTION).removeIndex(5);
        filter().addIndex(5); // No gravity
    }

    @Override
    public EntityTypes1_9.EntityType typeFromId(int type) {
        return EntityTypes1_9.EntityType.findById(type);
    }

    @Override
    public EntityTypes1_9.EntityType objectTypeFromId(int type, int data) {
        return EntityTypes1_9.ObjectType.getEntityType(type, data);
    }
}
