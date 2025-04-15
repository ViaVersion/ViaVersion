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
package com.viaversion.viaversion.protocols.v1_21_5to1_22.rewriter;

import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_22;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_21_5;
import com.viaversion.viaversion.api.type.types.version.Types1_22;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundConfigurationPackets1_21;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ClientboundPacket1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ClientboundPackets1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ServerboundPackets1_21_5;
import com.viaversion.viaversion.protocols.v1_21_5to1_22.Protocol1_21_5To1_22;
import com.viaversion.viaversion.protocols.v1_21_5to1_22.storage.SneakStorage;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.rewriter.RegistryDataRewriter;

public final class EntityPacketRewriter1_22 extends EntityRewriter<ClientboundPacket1_21_5, Protocol1_21_5To1_22> {

    public EntityPacketRewriter1_22(final Protocol1_21_5To1_22 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        registerTrackerWithData1_19(ClientboundPackets1_21_5.ADD_ENTITY, EntityTypes1_22.FALLING_BLOCK);
        registerSetEntityData(ClientboundPackets1_21_5.SET_ENTITY_DATA, Types1_21_5.ENTITY_DATA_LIST, Types1_22.ENTITY_DATA_LIST);
        registerRemoveEntities(ClientboundPackets1_21_5.REMOVE_ENTITIES);
        registerPlayerAbilities(ClientboundPackets1_21_5.PLAYER_ABILITIES);
        registerGameEvent(ClientboundPackets1_21_5.GAME_EVENT);
        registerLogin1_20_5(ClientboundPackets1_21_5.LOGIN);
        registerRespawn1_20_5(ClientboundPackets1_21_5.RESPAWN);

        final RegistryDataRewriter registryDataRewriter = new RegistryDataRewriter(protocol);
        protocol.registerClientbound(ClientboundConfigurationPackets1_21.REGISTRY_DATA, registryDataRewriter::handle);

        protocol.appendClientbound(ClientboundPackets1_21_5.RESPAWN, wrapper -> {
            wrapper.user().get(SneakStorage.class).setSneaking(false);
        });

        protocol.registerServerbound(ServerboundPackets1_21_5.PLAYER_COMMAND, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Entity ID
            final int action = wrapper.read(Types.VAR_INT);
            wrapper.write(Types.VAR_INT, action + 2); // press_shift_key and release_shift_key gone
        });

        protocol.registerServerbound(ServerboundPackets1_21_5.PLAYER_INPUT, wrapper -> {
            final byte flags = wrapper.passthrough(Types.BYTE);
            final boolean pressingShift = (flags & 1 << 5) != 0;
            if (wrapper.user().get(SneakStorage.class).setSneaking(pressingShift)) {
                // Send the pressing/releasing shift action
                final PacketWrapper playerCommandPacket = wrapper.create(ServerboundPackets1_21_5.PLAYER_COMMAND);
                playerCommandPacket.write(Types.VAR_INT, tracker(wrapper.user()).clientEntityId());
                playerCommandPacket.write(Types.VAR_INT, pressingShift ? 0 : 1);
                playerCommandPacket.write(Types.VAR_INT, 0); // No data
                playerCommandPacket.sendToServer(Protocol1_21_5To1_22.class);
            }
        });
    }

    @Override
    protected void registerRewrites() {
        filter().mapDataType(Types1_22.ENTITY_DATA_TYPES::byId);
        registerEntityDataTypeHandler(
            Types1_22.ENTITY_DATA_TYPES.itemType,
            Types1_22.ENTITY_DATA_TYPES.blockStateType,
            Types1_22.ENTITY_DATA_TYPES.optionalBlockStateType,
            Types1_22.ENTITY_DATA_TYPES.particleType,
            Types1_22.ENTITY_DATA_TYPES.particlesType,
            Types1_22.ENTITY_DATA_TYPES.componentType,
            Types1_22.ENTITY_DATA_TYPES.optionalComponentType
        );
    }

    @Override
    public void onMappingDataLoaded() {
        mapTypes();
    }

    @Override
    public EntityType typeFromId(final int type) {
        return EntityTypes1_22.getTypeFromId(type);
    }
}
