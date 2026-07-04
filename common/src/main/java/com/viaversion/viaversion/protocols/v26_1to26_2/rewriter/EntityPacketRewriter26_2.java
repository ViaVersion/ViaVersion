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
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPackets26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ServerboundPackets26_1;
import com.viaversion.viaversion.protocols.v26_1to26_2.Protocol26_1To26_2;
import com.viaversion.viaversion.protocols.v26_1to26_2.storage.Encrypted;
import com.viaversion.viaversion.protocols.v26_1to26_2.storage.FakeEntityId;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.rewriter.entitydata.EntityDataHandler;
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
                wrapper.write(Types.VAR_INT, toOriginalEntityId(wrapper, entityId));
            } else {
                // if entity id is null, they just left-clicked the air, which used to be handled by the swing packet in 1.21.10 and below (1.21.11 and 26.1.X does not send spectator left clicks at all)
                wrapper.setPacketType(ServerboundPackets26_1.SWING);
                wrapper.write(Types.VAR_INT, 0); // Hand to Swing, 0 = Main Hand
            }
        });

        protocol.appendClientbound(ClientboundPackets26_1.LOGIN, wrapper -> {
            wrapper.user().put(new FakeEntityId(ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, -1)));
            final int entityId = wrapper.get(Types.INT, 0);
            wrapper.set(Types.INT, 0, toFakeEntityId(wrapper, entityId));

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

        // Iterate through **all** packets containing an entity ID and replace it with a randomized negative value if the ID is zero,
        // since the client no longer accepts zero as a valid entity ID.
        final PacketHandler setFakeEntityId = wrapper -> wrapper.set(Types.VAR_INT, 0, toFakeEntityId(wrapper, wrapper.get(Types.VAR_INT, 0)));
        protocol.appendClientbound(ClientboundPackets26_1.ADD_ENTITY, wrapper -> {
            setFakeEntityId.handle(wrapper);
            final EntityType entityType = typeFromId(wrapper.get(Types.VAR_INT, 1));
            if (entityType != null && entityType.isOrHasParent(EntityTypes26_2.PROJECTILE)) {
                // For projectiles, the data field holds the owner entity id
                wrapper.set(Types.VAR_INT, 2, toFakeEntityId(wrapper, wrapper.get(Types.VAR_INT, 2)));
            }
        });
        protocol.appendClientbound(ClientboundPackets26_1.SET_ENTITY_DATA, setFakeEntityId);
        protocol.appendClientbound(ClientboundPackets26_1.SET_EQUIPMENT, setFakeEntityId);
        protocol.appendClientbound(ClientboundPackets26_1.PLAYER_COMBAT_KILL, setFakeEntityId);
        protocol.appendClientbound(ClientboundPackets26_1.UPDATE_ATTRIBUTES, setFakeEntityId);
        protocol.appendClientbound(ClientboundPackets26_1.REMOVE_ENTITIES, wrapper -> {
            final int[] entities = wrapper.get(Types.VAR_INT_ARRAY_PRIMITIVE, 0);
            for (int i = 0; i < entities.length; i++) {
                entities[i] = toFakeEntityId(wrapper, entities[i]);
            }
        });

        final PacketHandler toFakeEntityId = wrapper -> wrapper.write(Types.VAR_INT, toFakeEntityId(wrapper, wrapper.read(Types.VAR_INT)));
        final PacketHandler toOriginalEntityId = wrapper -> wrapper.write(Types.VAR_INT, toOriginalEntityId(wrapper, wrapper.read(Types.VAR_INT)));
        protocol.registerClientbound(ClientboundPackets26_1.ANIMATE, toFakeEntityId);
        protocol.registerClientbound(ClientboundPackets26_1.DEBUG_ENTITY_VALUE, toFakeEntityId);
        protocol.registerClientbound(ClientboundPackets26_1.ENTITY_POSITION_SYNC, toFakeEntityId);
        protocol.registerClientbound(ClientboundPackets26_1.HURT_ANIMATION, toFakeEntityId);
        protocol.registerClientbound(ClientboundPackets26_1.MOVE_ENTITY_POS, toFakeEntityId);
        protocol.registerClientbound(ClientboundPackets26_1.MOVE_ENTITY_ROT, toFakeEntityId);
        protocol.registerClientbound(ClientboundPackets26_1.MOVE_ENTITY_POS_ROT, toFakeEntityId);
        protocol.registerClientbound(ClientboundPackets26_1.MOVE_MINECART_ALONG_TRACK, toFakeEntityId);
        protocol.registerClientbound(ClientboundPackets26_1.REMOVE_MOB_EFFECT, toFakeEntityId);
        protocol.registerClientbound(ClientboundPackets26_1.ROTATE_HEAD, toFakeEntityId);
        protocol.registerClientbound(ClientboundPackets26_1.SET_CAMERA, toFakeEntityId);
        protocol.registerClientbound(ClientboundPackets26_1.SET_ENTITY_MOTION, toFakeEntityId);
        protocol.registerClientbound(ClientboundPackets26_1.UPDATE_MOB_EFFECT, toFakeEntityId);
        protocol.registerClientbound(ClientboundPackets26_1.TELEPORT_ENTITY, toFakeEntityId);
        protocol.registerClientbound(ClientboundPackets26_1.PROJECTILE_POWER, toFakeEntityId);
        protocol.registerClientbound(ClientboundPackets26_1.BLOCK_DESTRUCTION, toFakeEntityId);
        protocol.registerClientbound(ClientboundPackets26_1.ENTITY_EVENT, wrapper -> wrapper.write(Types.INT, toFakeEntityId(wrapper, wrapper.read(Types.INT))));
        protocol.registerClientbound(ClientboundPackets26_1.DAMAGE_EVENT, wrapper -> {
            toFakeEntityId.handle(wrapper); // Entity id
            wrapper.passthrough(Types.VAR_INT); // Source type
            final Integer sourceCauseId = wrapper.passthrough(Types.OPTIONAL_VAR_INT);
            if (sourceCauseId != null) {
                wrapper.set(Types.OPTIONAL_VAR_INT, 0, toFakeEntityId(wrapper, sourceCauseId));
            }
            final Integer sourceDirectId = wrapper.passthrough(Types.OPTIONAL_VAR_INT);
            if (sourceDirectId != null) {
                wrapper.set(Types.OPTIONAL_VAR_INT, 1, toFakeEntityId(wrapper, sourceDirectId));
            }
        });
        protocol.appendClientbound(ClientboundPackets26_1.SOUND_ENTITY, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Source
            toFakeEntityId.handle(wrapper);
        });
        protocol.registerClientbound(ClientboundPackets26_1.PLAYER_LOOK_AT, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // From anchor
            wrapper.passthrough(Types.DOUBLE); // X
            wrapper.passthrough(Types.DOUBLE); // Y
            wrapper.passthrough(Types.DOUBLE); // Z
            final boolean atEntity = wrapper.passthrough(Types.BOOLEAN);
            if (atEntity) {
                toFakeEntityId.handle(wrapper);
            }
        });
        protocol.registerClientbound(ClientboundPackets26_1.SET_PASSENGERS, wrapper -> {
            toFakeEntityId.handle(wrapper); // Vehicle
            final int[] passengers = wrapper.passthrough(Types.VAR_INT_ARRAY_PRIMITIVE);
            for (int i = 0; i < passengers.length; i++) {
                passengers[i] = toFakeEntityId(wrapper, passengers[i]);
            }
        });
        protocol.registerClientbound(ClientboundPackets26_1.MOUNT_SCREEN_OPEN, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Container id
            wrapper.passthrough(Types.VAR_INT); // Inventory columns
            wrapper.write(Types.INT, toFakeEntityId(wrapper, wrapper.read(Types.INT)));
        });
        protocol.registerClientbound(ClientboundPackets26_1.SET_ENTITY_LINK, wrapper -> {
            wrapper.write(Types.INT, toFakeEntityId(wrapper, wrapper.read(Types.INT))); // Source id
            // Destination id can be zero - ignore it
        });
        protocol.registerClientbound(ClientboundPackets26_1.TAKE_ITEM_ENTITY, wrapper -> {
            toFakeEntityId.handle(wrapper); // Item id
            toFakeEntityId.handle(wrapper); // Player id
        });

        protocol.registerServerbound(ServerboundPackets26_1.ATTACK, toOriginalEntityId);
        protocol.registerServerbound(ServerboundPackets26_1.INTERACT, toOriginalEntityId);
        protocol.registerServerbound(ServerboundPackets26_1.PICK_ITEM_FROM_ENTITY, toOriginalEntityId);
        protocol.registerServerbound(ServerboundPackets26_1.PLAYER_COMMAND, toOriginalEntityId);
        protocol.registerServerbound(ServerboundPackets26_1.SET_COMMAND_MINECART, toOriginalEntityId);
        protocol.registerServerbound(ServerboundPackets26_1.ENTITY_TAG_QUERY, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Transaction id
            toOriginalEntityId.handle(wrapper);
        });
    }

    private int toFakeEntityId(final PacketWrapper wrapper, final int entityId) {
        return entityId == 0 ? wrapper.user().get(FakeEntityId.class).id() : entityId;
    }

    private int toOriginalEntityId(final PacketWrapper wrapper, final int entityId) {
        return entityId == wrapper.user().get(FakeEntityId.class).id() ? 0 : entityId;
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

        final EntityDataHandler toFakeEntityId = (event, data) -> {
            final Integer target = data.value();
            if (target != null) {
                data.setValue(target == 0 ? event.user().get(FakeEntityId.class).id() : target);
            }
        };
        filter().type(EntityTypes26_2.FROG).index(19).handler(toFakeEntityId); // Tongue target
        filter().type(EntityTypes26_2.FIREWORK_ROCKET).index(9).handler(toFakeEntityId); // Attached to target
        // Guardian & Wither use zero as default - ignore them
    }

    @Override
    public EntityType typeFromId(final int type) {
        return EntityTypes26_2.getTypeFromId(type);
    }
}
