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
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPackets26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ServerboundPackets26_1;
import com.viaversion.viaversion.protocols.v26_1to26_2.Protocol26_1To26_2;
import com.viaversion.viaversion.protocols.v26_1to26_2.storage.Encrypted;
import com.viaversion.viaversion.protocols.v26_1to26_2.storage.FakeEntityId;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import java.util.concurrent.ThreadLocalRandom;

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
                wrapper.write(Types.VAR_INT, fixEntityId(wrapper, entityId));
            } else {
                wrapper.cancel();
            }
        });

        protocol.appendClientbound(ClientboundPackets26_1.LOGIN, wrapper -> {
            // Generate a randomized negative id used as a replacement for the no longer allowed zero-entity id.
            wrapper.user().put(new FakeEntityId(ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, -1)));

            final int entityId = wrapper.get(Types.INT, 0);
            wrapper.set(Types.INT, 0, fixEntityId(wrapper, entityId));

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

        // Replace entity ids in a bunch of packets to make sure id zero is no longer used
        protocol.appendClientbound(ClientboundPackets26_1.ADD_ENTITY, this::setEntityId);
        protocol.appendClientbound(ClientboundPackets26_1.SET_ENTITY_DATA, this::setEntityId);
        protocol.appendClientbound(ClientboundPackets26_1.SET_EQUIPMENT, this::setEntityId);
        protocol.appendClientbound(ClientboundPackets26_1.PLAYER_COMBAT_KILL, this::setEntityId);
        protocol.appendClientbound(ClientboundPackets26_1.UPDATE_ATTRIBUTES, this::setEntityId);
        protocol.appendClientbound(ClientboundPackets26_1.REMOVE_ENTITIES, wrapper -> {
            final int[] entities = wrapper.get(Types.VAR_INT_ARRAY_PRIMITIVE, 0);
            for (int i = 0; i < entities.length; i++) {
                entities[i] = fixEntityId(wrapper, entities[i]);
            }
        });

        protocol.registerClientbound(ClientboundPackets26_1.ANIMATE, this::passEntityId);
        protocol.registerClientbound(ClientboundPackets26_1.DEBUG_ENTITY_VALUE, this::passEntityId);
        protocol.registerClientbound(ClientboundPackets26_1.ENTITY_POSITION_SYNC, this::passEntityId);
        protocol.registerClientbound(ClientboundPackets26_1.HURT_ANIMATION, this::passEntityId);
        protocol.registerClientbound(ClientboundPackets26_1.MOVE_ENTITY_POS, this::passEntityId);
        protocol.registerClientbound(ClientboundPackets26_1.MOVE_ENTITY_ROT, this::passEntityId);
        protocol.registerClientbound(ClientboundPackets26_1.MOVE_ENTITY_POS_ROT, this::passEntityId);
        protocol.registerClientbound(ClientboundPackets26_1.MOVE_MINECART_ALONG_TRACK, this::passEntityId);
        protocol.registerClientbound(ClientboundPackets26_1.REMOVE_MOB_EFFECT, this::passEntityId);
        protocol.registerClientbound(ClientboundPackets26_1.ROTATE_HEAD, this::passEntityId);
        protocol.registerClientbound(ClientboundPackets26_1.SET_CAMERA, this::passEntityId);
        protocol.registerClientbound(ClientboundPackets26_1.SET_ENTITY_MOTION, this::passEntityId);
        protocol.registerClientbound(ClientboundPackets26_1.UPDATE_MOB_EFFECT, this::passEntityId);
        protocol.registerClientbound(ClientboundPackets26_1.TELEPORT_ENTITY, this::passEntityId);
        protocol.registerClientbound(ClientboundPackets26_1.PROJECTILE_POWER, this::passEntityId);
        protocol.registerClientbound(ClientboundPackets26_1.BLOCK_DESTRUCTION, this::passEntityId);
        protocol.registerClientbound(ClientboundPackets26_1.ENTITY_EVENT, wrapper -> wrapper.write(Types.INT, fixEntityId(wrapper, wrapper.read(Types.INT))));
        protocol.registerClientbound(ClientboundPackets26_1.DAMAGE_EVENT, wrapper -> {
            this.passEntityId(wrapper); // Entity id
            wrapper.passthrough(Types.VAR_INT); // Source type
            final Integer sourceCauseId = wrapper.passthrough(Types.OPTIONAL_VAR_INT);
            if (sourceCauseId != null) {
                wrapper.set(Types.OPTIONAL_VAR_INT, 0, fixEntityId(wrapper, sourceCauseId));
            }
            final Integer sourceDirectId = wrapper.passthrough(Types.OPTIONAL_VAR_INT);
            if (sourceDirectId != null) {
                wrapper.set(Types.OPTIONAL_VAR_INT, 1, fixEntityId(wrapper, sourceDirectId));
            }
        });
        protocol.appendClientbound(ClientboundPackets26_1.SOUND_ENTITY, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Source
            passEntityId(wrapper);
        });
        protocol.registerClientbound(ClientboundPackets26_1.PLAYER_LOOK_AT, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // From anchor
            wrapper.passthrough(Types.DOUBLE); // X
            wrapper.passthrough(Types.DOUBLE); // Y
            wrapper.passthrough(Types.DOUBLE); // Z
            final boolean atEntity = wrapper.passthrough(Types.BOOLEAN);
            if (atEntity) {
                passEntityId(wrapper);
            }
        });
        protocol.registerClientbound(ClientboundPackets26_1.SET_PASSENGERS, wrapper -> {
            passEntityId(wrapper); // Vehicle
            final int[] passengers = wrapper.passthrough(Types.VAR_INT_ARRAY_PRIMITIVE);
            for (int i = 0; i < passengers.length; i++) {
                passengers[i] = fixEntityId(wrapper, passengers[i]);
            }
        });
        protocol.registerClientbound(ClientboundPackets26_1.MOUNT_SCREEN_OPEN, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Container id
            wrapper.passthrough(Types.VAR_INT); // Inventory columns
            wrapper.write(Types.INT, fixEntityId(wrapper, wrapper.read(Types.INT)));
        });
        protocol.registerClientbound(ClientboundPackets26_1.SET_ENTITY_LINK, wrapper -> {
            wrapper.write(Types.INT, fixEntityId(wrapper, wrapper.read(Types.INT))); // Source id
        });
        protocol.registerClientbound(ClientboundPackets26_1.TAKE_ITEM_ENTITY, wrapper -> {
            passEntityId(wrapper); // Item id
            passEntityId(wrapper); // Player id
        });

        protocol.registerServerbound(ServerboundPackets26_1.ATTACK, this::passEntityId);
        protocol.registerServerbound(ServerboundPackets26_1.INTERACT, this::passEntityId);
        protocol.registerServerbound(ServerboundPackets26_1.PICK_ITEM_FROM_ENTITY, this::passEntityId);
        protocol.registerServerbound(ServerboundPackets26_1.PLAYER_COMMAND, this::passEntityId);
        protocol.registerServerbound(ServerboundPackets26_1.SET_COMMAND_MINECART, this::passEntityId);
        protocol.registerServerbound(ServerboundPackets26_1.ENTITY_TAG_QUERY, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Transaction id
            passEntityId(wrapper);
        });
    }

    private void passEntityId(final PacketWrapper wrapper) {
        wrapper.write(Types.VAR_INT, fixEntityId(wrapper, wrapper.read(Types.VAR_INT)));
    }

    private void setEntityId(final PacketWrapper wrapper) {
        wrapper.set(Types.VAR_INT, 0, fixEntityId(wrapper, wrapper.get(Types.VAR_INT, 0)));
    }

    private int fixEntityId(final PacketWrapper wrapper, final int entityId) {
        return entityId == 0 ? wrapper.user().get(FakeEntityId.class).id() : entityId;
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
