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
import com.viaversion.viaversion.api.minecraft.item.data.EnumTypes;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPackets26_1;
import com.viaversion.viaversion.protocols.v26_2to26_3.Protocol26_2To26_3;
import com.viaversion.viaversion.protocols.v26_2to26_3.packet.ClientboundPackets26_3;
import com.viaversion.viaversion.rewriter.EntityRewriter;

public final class EntityPacketRewriter26_3 extends EntityRewriter<ClientboundPacket26_1, Protocol26_2To26_3> {

    private static final short SWING_MAIN_HAND = 0;
    private static final short WAKE_UP = 2;
    private static final short SWING_OFF_HAND = 3;
    private static final short CRITICAL_HIT = 4;
    private static final short MAGIC_CRITICAL_HIT = 5;
    private static final int DEFAULT_SWING_TYPE = EnumTypes.SWING_ANIMATION.idFromName("whack");
    private static final int DEFAULT_SWING_DURATION = 6;

    public EntityPacketRewriter26_3(final Protocol26_2To26_3 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        // Common entity registrations (including LOGIN, RESPAWN) are handled by SharedRegistrations; these continue from them.
        final PacketHandler previousGameModeHandler = wrapper -> {
            final byte previousGameMode = wrapper.read(Types.BYTE);
            wrapper.write(Types.OPTIONAL_VAR_INT, previousGameMode == -1 ? null : (int) previousGameMode);
        };
        protocol.appendClientbound(ClientboundPackets26_1.LOGIN, previousGameModeHandler);
        protocol.appendClientbound(ClientboundPackets26_1.RESPAWN, previousGameModeHandler);

        protocol.registerClientbound(ClientboundPackets26_1.MOVE_ENTITY_POS, relativeMoveHandler(false));
        protocol.registerClientbound(ClientboundPackets26_1.MOVE_ENTITY_POS_ROT, relativeMoveHandler(true));
        protocol.registerClientbound(ClientboundPackets26_1.MOVE_ENTITY_ROT, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Entity id
            final byte yRot = wrapper.read(Types.BYTE);
            final byte xRot = wrapper.read(Types.BYTE);
            wrapper.passthrough(Types.BOOLEAN); // On ground, moved in front of the rotation
            wrapper.write(Types.BYTE, yRot);
            wrapper.write(Types.BYTE, xRot);
        });

        protocol.registerClientbound(ClientboundPackets26_1.ENTITY_POSITION_SYNC, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Entity id
            final double x = wrapper.read(Types.DOUBLE);
            final double y = wrapper.read(Types.DOUBLE);
            final double z = wrapper.read(Types.DOUBLE);
            wrapper.read(Types.DOUBLE); // Delta movement is no longer sent
            wrapper.read(Types.DOUBLE);
            wrapper.read(Types.DOUBLE);

            wrapper.write(Types.VAR_INT, 0); // Position path type, 0 = linear
            wrapper.write(Types.DOUBLE, x);
            wrapper.write(Types.DOUBLE, y);
            wrapper.write(Types.DOUBLE, z);
        });

        protocol.registerClientbound(ClientboundPackets26_1.ANIMATE, wrapper -> {
            final int entityId = wrapper.passthrough(Types.VAR_INT);
            final short action = wrapper.read(Types.UNSIGNED_BYTE);
            final int hand = action == SWING_MAIN_HAND ? 0 : action == SWING_OFF_HAND ? 1 : -1;
            if (hand != -1) {
                // Swings moved into their own packet
                wrapper.setPacketType(ClientboundPackets26_3.SWING_ANIMATION);
                wrapper.write(Types.VAR_INT, hand);
                wrapper.write(Types.VAR_INT, DEFAULT_SWING_TYPE);
                wrapper.write(Types.VAR_INT, DEFAULT_SWING_DURATION);
                return;
            }

            final int mappedAction = switch (action) {
                case WAKE_UP -> 0;
                case CRITICAL_HIT -> 1;
                case MAGIC_CRITICAL_HIT -> 2;
                default -> -1;
            };
            if (mappedAction == -1) {
                wrapper.cancel();
                return;
            }
            wrapper.write(Types.UNSIGNED_BYTE, (short) mappedAction);
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

    private static PacketHandler relativeMoveHandler(final boolean withRotation) {
        return wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Entity id
            final short deltaX = wrapper.read(Types.SHORT);
            final short deltaY = wrapper.read(Types.SHORT);
            final short deltaZ = wrapper.read(Types.SHORT);
            final byte yRot = withRotation ? wrapper.read(Types.BYTE) : 0;
            final byte xRot = withRotation ? wrapper.read(Types.BYTE) : 0;
            final boolean onGround = wrapper.read(Types.BOOLEAN);

            wrapper.write(Types.VAR_INT, onGround ? 1 : 0); // Properties, step count of 0
            wrapper.write(Types.SHORT, deltaX);
            wrapper.write(Types.SHORT, deltaY);
            wrapper.write(Types.SHORT, deltaZ);
            if (withRotation) {
                wrapper.write(Types.BYTE, yRot);
                wrapper.write(Types.BYTE, xRot);
            }
        };
    }
}
