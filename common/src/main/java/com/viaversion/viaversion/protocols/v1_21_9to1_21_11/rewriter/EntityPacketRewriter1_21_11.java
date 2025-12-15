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
package com.viaversion.viaversion.protocols.v1_21_9to1_21_11.rewriter;

import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_11;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21_11;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundPacket1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundPackets1_21_9;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.Protocol1_21_9To1_21_11;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ClientboundPackets1_21_11;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.storage.GameTimeStorage;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.rewriter.entitydata.EntityDataHandlerEvent;

public final class EntityPacketRewriter1_21_11 extends EntityRewriter<ClientboundPacket1_21_9, Protocol1_21_9To1_21_11> {

    public EntityPacketRewriter1_21_11(final Protocol1_21_9To1_21_11 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        registerTrackerWithData1_21_9(ClientboundPackets1_21_9.ADD_ENTITY, EntityTypes1_21_11.FALLING_BLOCK);
        registerSetEntityData(ClientboundPackets1_21_9.SET_ENTITY_DATA);
        registerRemoveEntities(ClientboundPackets1_21_9.REMOVE_ENTITIES);
        registerPlayerAbilities(ClientboundPackets1_21_9.PLAYER_ABILITIES);
        registerGameEvent(ClientboundPackets1_21_9.GAME_EVENT);
        registerLogin1_20_5(ClientboundPackets1_21_9.LOGIN);
        registerRespawn1_20_5(ClientboundPackets1_21_9.RESPAWN);

        protocol.registerServerbound(ServerboundPackets1_21_6.PLAYER_ACTION, wrapper -> {
            final int action = wrapper.passthrough(Types.VAR_INT);
            // cancel spear "stab" packets sent by the client
            if (action == 7) {
                wrapper.cancel();
            }
        });

        protocol.registerClientbound(ClientboundPackets1_21_9.HORSE_SCREEN_OPEN, ClientboundPackets1_21_11.MOUNT_SCREEN_OPEN);
    }

    @Override
    protected void registerRewrites() {
        final EntityDataTypes1_21_11 entityDataTypes = protocol.mappedTypes().entityDataTypes();
        filter().mapDataType(id -> {
            if (id >= entityDataTypes.zombieNautilusVariantType.typeId()) {
                id++;
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

        filter().type(EntityTypes1_21_11.WOLF).index(21).handler(this::relativeToAbsoluteTicks);
        filter().type(EntityTypes1_21_11.BEE).index(18).handler(this::relativeToAbsoluteTicks);
        filter().type(EntityTypes1_21_11.AVATAR).index(15).handler(((event, data) -> {
            final byte arm = data.value();
            data.setTypeAndValue(entityDataTypes.humanoidArmType, (int) arm);
        }));
    }

    @Override
    public void onMappingDataLoaded() {
        mapTypes();
    }

    @Override
    public EntityType typeFromId(final int type) {
        return EntityTypes1_21_11.getTypeFromId(type);
    }

    private void relativeToAbsoluteTicks(final EntityDataHandlerEvent event, final EntityData data) {
        final long currentGameTime = event.user().get(GameTimeStorage.class).gameTime();
        final int remainingAngerTime = data.value();
        data.setTypeAndValue(protocol.mappedTypes().entityDataTypes().longType, currentGameTime + remainingAngerTime);
    }
}
