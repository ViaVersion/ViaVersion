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
package com.viaversion.viaversion.protocols.v26_1to26_2.rewriter;

import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes26_2;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes26_1;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPackets26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ServerboundPackets26_1;
import com.viaversion.viaversion.protocols.v26_1to26_2.Protocol26_1To26_2;
import com.viaversion.viaversion.protocols.v26_1to26_2.storage.Encrypted;
import com.viaversion.viaversion.rewriter.EntityRewriter;

public final class EntityPacketRewriter26_2 extends EntityRewriter<ClientboundPacket26_1, Protocol26_1To26_2> {

    public EntityPacketRewriter26_2(final Protocol26_1To26_2 protocol) {
        super(protocol);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        protocol.registerServerbound(ServerboundPackets26_1.SPECTATE_ENTITY, wrapper -> {
            final Integer entityId = wrapper.read(Types.OPTIONAL_VAR_INT);
            if (entityId != null) {
                wrapper.write(Types.VAR_INT, entityId);
            } else {
                // if entity id is null, they just left-clicked the air, which used to be handled by the swing packet in 1.21.10 and below (1.21.11 and 26.1.X does not send spectator left clicks at all)
                wrapper.setPacketType(ServerboundPackets26_1.SWING);
                wrapper.write(Types.VAR_INT, 0); // Hand to Swing, 0 = Main Hand
            }
        });

        protocol.appendClientbound(ClientboundPackets26_1.LOGIN, wrapper -> {
            // Continuing from the registered handler
            wrapper.passthrough(Types.BYTE); // Previous gamemode
            wrapper.passthrough(Types.BOOLEAN); // Debug
            wrapper.passthrough(Types.BOOLEAN); // Flat
            wrapper.passthrough(Types.OPTIONAL_GLOBAL_POSITION); // Last death location
            wrapper.passthrough(Types.VAR_INT); // Portal cooldown
            wrapper.passthrough(Types.VAR_INT); // Sea level
            final boolean onlineMode = wrapper.user().has(Encrypted.class);
            wrapper.write(Types.BOOLEAN, onlineMode);
        });
    }

    @Override
    protected void registerRewrites() {
        final EntityDataTypes26_1 entityDataTypes = protocol.mappedTypes().entityDataTypes();
        dataTypeMapper().register();
        registerEntityDataTypeHandler(
            entityDataTypes.itemType,
            entityDataTypes.blockStateType,
            entityDataTypes.optionalBlockStateType,
            entityDataTypes.particleType,
            entityDataTypes.particlesType,
            entityDataTypes.componentType,
            entityDataTypes.optionalComponentType
        );

        filter().type(EntityTypes26_2.ABSTRACT_CUBE_MOB).addIndex(16); // baby
        filter().type(EntityTypes26_2.ABSTRACT_CUBE_MOB).addIndex(17); // age locked
    }

    @Override
    public EntityType typeFromId(final int type) {
        return EntityTypes26_2.getTypeFromId(type);
    }
}
