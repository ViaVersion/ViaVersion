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
package com.viaversion.viaversion.protocols.v26_2to26_3.rewriter;

import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes26_3;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes26_3;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPackets26_1;
import com.viaversion.viaversion.protocols.v26_2to26_3.Protocol26_2To26_3;
import com.viaversion.viaversion.rewriter.EntityRewriter;

public final class EntityPacketRewriter26_3 extends EntityRewriter<ClientboundPacket26_1, Protocol26_2To26_3> {

    private static final int SINGLE_STEP = 1 << 1;

    public EntityPacketRewriter26_3(final Protocol26_2To26_3 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        // TODO jittery movement, likely need to interpolate it ourselves...
        protocol.registerClientbound(ClientboundPackets26_1.MOVE_ENTITY_POS, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Entity ID
            final short xa = wrapper.read(Types.SHORT);
            final short ya = wrapper.read(Types.SHORT);
            final short za = wrapper.read(Types.SHORT);
            final boolean onGround = wrapper.read(Types.BOOLEAN);
            wrapper.write(Types.VAR_INT, (onGround ? 1 : 0) | SINGLE_STEP); // Only the first bit set, otherwise empty = no step count
            wrapper.write(Types.VAR_INT, 0); // No tick offset
            wrapper.write(Types.SHORT, xa);
            wrapper.write(Types.SHORT, ya);
            wrapper.write(Types.SHORT, za);
        });

        protocol.registerClientbound(ClientboundPackets26_1.MOVE_ENTITY_POS_ROT, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Entity ID
            final short xa = wrapper.read(Types.SHORT);
            final short ya = wrapper.read(Types.SHORT);
            final short za = wrapper.read(Types.SHORT);
            final byte yRot = wrapper.read(Types.BYTE);
            final byte xRot = wrapper.read(Types.BYTE);
            final boolean onGround = wrapper.read(Types.BOOLEAN);
            wrapper.write(Types.VAR_INT, (onGround ? 1 : 0) | SINGLE_STEP); // Only the first bit set, otherwise empty = no extra steps
            wrapper.write(Types.VAR_INT, 0); // No tick offset
            wrapper.write(Types.SHORT, xa);
            wrapper.write(Types.SHORT, ya);
            wrapper.write(Types.SHORT, za);
            wrapper.write(Types.BYTE, yRot);
            wrapper.write(Types.BYTE, xRot);
        });

        protocol.registerClientbound(ClientboundPackets26_1.MOVE_ENTITY_ROT, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Entity ID
            final byte yRot = wrapper.read(Types.BYTE);
            final byte xRot = wrapper.read(Types.BYTE);
            wrapper.passthrough(Types.BOOLEAN); // on ground stays as boolean; moved up
            wrapper.write(Types.BYTE, yRot);
            wrapper.write(Types.BYTE, xRot);
        });

        protocol.registerClientbound(ClientboundPackets26_1.ENTITY_POSITION_SYNC, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Entity ID

            wrapper.write(Types.VAR_INT, 0); // Linear
            wrapper.passthrough(Types.DOUBLE); // X
            wrapper.passthrough(Types.DOUBLE); // Y
            wrapper.passthrough(Types.DOUBLE); // Z

            wrapper.read(Types.DOUBLE); // Delta x
            wrapper.read(Types.DOUBLE); // Delta y
            wrapper.read(Types.DOUBLE); // Delta z

            wrapper.passthrough(Types.FLOAT); // Y rot
            wrapper.passthrough(Types.FLOAT); // X rot
        });
    }

    @Override
    protected void registerRewrites() {
        final EntityDataTypes26_3 entityDataTypes = protocol.mappedTypes().entityDataTypes();
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
    }

    @Override
    public EntityType typeFromId(final int type) {
        return EntityTypes26_3.getTypeFromId(type);
    }
}
