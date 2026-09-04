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

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.TrackedEntity;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes26_3;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes26_3;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPackets26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ServerboundPackets26_1;
import com.viaversion.viaversion.protocols.v26_2to26_3.Protocol26_2To26_3;
import com.viaversion.viaversion.protocols.v26_2to26_3.packet.ServerboundPackets26_3;
import com.viaversion.viaversion.protocols.v26_2to26_3.storage.LastMovement;
import com.viaversion.viaversion.rewriter.EntityRewriter;

public final class EntityPacketRewriter26_3 extends EntityRewriter<ClientboundPacket26_1, Protocol26_2To26_3> {

    private static final int SINGLE_STEP = 1 << 1;
    private static final int MAX_TICK_OFFSET = 5;

    public EntityPacketRewriter26_3(final Protocol26_2To26_3 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        protocol.registerClientbound(ClientboundPackets26_1.MOVE_ENTITY_POS, wrapper -> {
            final int entityId = wrapper.passthrough(Types.VAR_INT);
            final short xa = wrapper.read(Types.SHORT);
            final short ya = wrapper.read(Types.SHORT);
            final short za = wrapper.read(Types.SHORT);
            final boolean onGround = wrapper.read(Types.BOOLEAN);
            wrapper.write(Types.VAR_INT, (onGround ? 1 : 0) | SINGLE_STEP); // Only the first bit set, otherwise empty = no step count
            wrapper.write(Types.VAR_INT, tickOffset(wrapper.user(), entityId));
            wrapper.write(Types.SHORT, xa);
            wrapper.write(Types.SHORT, ya);
            wrapper.write(Types.SHORT, za);
        });

        protocol.registerClientbound(ClientboundPackets26_1.MOVE_ENTITY_POS_ROT, wrapper -> {
            final int entityId = wrapper.passthrough(Types.VAR_INT);
            final short xa = wrapper.read(Types.SHORT);
            final short ya = wrapper.read(Types.SHORT);
            final short za = wrapper.read(Types.SHORT);
            final byte yRot = wrapper.read(Types.BYTE);
            final byte xRot = wrapper.read(Types.BYTE);
            final boolean onGround = wrapper.read(Types.BOOLEAN);
            wrapper.write(Types.VAR_INT, (onGround ? 1 : 0) | SINGLE_STEP); // Only the first bit set, otherwise empty = no extra steps
            wrapper.write(Types.VAR_INT, tickOffset(wrapper.user(), entityId));
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

        protocol.appendClientbound(ClientboundPackets26_1.LOGIN, wrapper -> {
            wrapper.rewindReader(1);
            handleGamemodes(wrapper);
        });
        protocol.appendClientbound(ClientboundPackets26_1.RESPAWN, wrapper -> {
            wrapper.rewindReader(1);
            handleGamemodes(wrapper);
        });

        protocol.registerServerbound(ServerboundPackets26_3.ACCEPT_TELEPORTATION, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // ID
            wrapper.read(Types.DOUBLE); // X
            wrapper.read(Types.DOUBLE); // Y
            wrapper.read(Types.DOUBLE); // Z
            wrapper.read(Types.FLOAT); // Y rot
            wrapper.read(Types.FLOAT); // X rot
        });

        protocol.registerServerbound(ServerboundPackets26_3.PUNCH, ServerboundPackets26_1.SWING, wrapper -> {
            wrapper.write(Types.VAR_INT, 0); // Main hand
        });

        protocol.registerClientbound(ClientboundPackets26_1.ANIMATE, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Entity ID
            int action = wrapper.read(Types.VAR_INT);
            action = switch (action) {
                case 2 -> 0; // wake up
                case 4 -> 1; // crit
                case 5 -> 2; // magic crit
                default -> -1; // arm swings
            };

            if (action == -1) {
                wrapper.cancel();
            } else {
                wrapper.write(Types.VAR_INT, action);
            }
        });

        protocol.registerServerbound(ServerboundPackets26_3.SPECTATOR_ACTION, ServerboundPackets26_1.SPECTATE_ENTITY);
    }

    private void handleGamemodes(final PacketWrapper wrapper) {
        final byte gamemode = wrapper.read(Types.BYTE);
        wrapper.write(Types.VAR_INT, (int) gamemode);

        final byte previousGamemode = wrapper.read(Types.BYTE);
        wrapper.write(Types.OPTIONAL_VAR_INT, previousGamemode == -1 ? null : (int) previousGamemode);
    }

    // To make the stepped interpolation handler work properly, the offset has to match the server's actual updateInterval (e.g. 2 for players).
    // An offset that's too small makes the client snap, a too large one makes it queue up steps and fall behind - this makes it match the actual rate.
    // Estimate it from the time between move packets; errors cancel out across consecutive packets as the timestamps telescope.
    private int tickOffset(final UserConnection connection, final int entityId) {
        final TrackedEntity entity = tracker(connection).entity(entityId);
        if (entity == null) {
            return 1;
        }

        final long now = System.nanoTime();
        final LastMovement timestamp = entity.get(LastMovement.class);
        if (timestamp == null) {
            entity.put(new LastMovement(now));
            return 1;
        }

        final long deltaMillis = (now - timestamp.lastMovementTime()) / 1_000_000L;
        timestamp.setLastMovementTime(now);
        if (deltaMillis > MAX_TICK_OFFSET * 50L) {
            return 1; // No recent movement. Don't stretch the first step of a new movement burst
        }
        return (int) Math.max(1, (deltaMillis + 25) / 50); // Round to the nearest tick count
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
