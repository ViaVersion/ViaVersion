/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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

import com.viaversion.viaversion.api.minecraft.RegistryEntry;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_20_5;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundConfigurationPackets1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPacket1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.util.Key;

// Replace if needed
//  Types1_OLD
//  Types1_21
final class EntityPacketRewriter1_99 extends EntityRewriter<ClientboundPacket1_21, Protocol1_99To_98> {

    public EntityPacketRewriter1_99(final Protocol1_99To_98 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        // Tracks entities, applies entity data rewrites registered below, untracks entities
        registerTrackerWithData1_19(ClientboundPackets1_21.ADD_ENTITY, EntityTypes1_20_5.FALLING_BLOCK);
        registerSetEntityData(ClientboundPackets1_21.SET_ENTITY_DATA, /*Types1_OLD_ENTITY_DATA_LIST, */Types1_21.ENTITY_DATA_LIST); // Specify old and new entity data list if changed
        registerRemoveEntities(ClientboundPackets1_21.REMOVE_ENTITIES);

        protocol.registerClientbound(ClientboundConfigurationPackets1_21.REGISTRY_DATA, wrapper -> {
            final String registryKey = Key.stripMinecraftNamespace(wrapper.passthrough(Types.STRING));
            final RegistryEntry[] entries = wrapper.passthrough(Types.REGISTRY_ENTRY_ARRAY);
            handleRegistryData1_20_5(wrapper.user(), registryKey, entries); // Caches dimensions to access data like height later and tracks the amount of biomes sent for chunk data
        });

        protocol.registerClientbound(ClientboundPackets1_21.LOGIN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); // Entity id
                map(Types.BOOLEAN); // Hardcore
                map(Types.STRING_ARRAY); // World List
                map(Types.VAR_INT); // Max players
                map(Types.VAR_INT); // View distance
                map(Types.VAR_INT); // Simulation distance
                map(Types.BOOLEAN); // Reduced debug info
                map(Types.BOOLEAN); // Show death screen
                map(Types.BOOLEAN); // Limited crafting
                map(Types.VAR_INT); // Dimension id
                map(Types.STRING); // World
                handler(worldDataTrackerHandlerByKey1_20_5(3)); // Tracks world height and name for chunk data and entity (un)tracking
                handler(playerTrackerHandler());
            }
        });

        protocol.registerClientbound(ClientboundPackets1_21.RESPAWN, wrapper -> {
            final int dimensionId = wrapper.passthrough(Types.VAR_INT);
            final String world = wrapper.passthrough(Types.STRING);
            trackWorldDataByKey1_20_5(wrapper.user(), dimensionId, world); // Tracks world height and name for chunk data and entity (un)tracking
        });
    }

    @Override
    protected void registerRewrites() {
        /* Uncomment if entity data classes changed
        filter().mapDataType(typeId -> {
            int id = typeId;
            if (id >= SomeAddedIndex) {
                id++;
            }
            return Types1_21.ENTITY_DATA_TYPES.byId(id);
        });*/

        // Registers registry type id changes
        registerEntityDataTypeHandler(
            Types1_21.ENTITY_DATA_TYPES.itemType,
            Types1_21.ENTITY_DATA_TYPES.blockStateType,
            Types1_21.ENTITY_DATA_TYPES.optionalBlockStateType,
            Types1_21.ENTITY_DATA_TYPES.particleType,
            Types1_21.ENTITY_DATA_TYPES.particlesType,
            Types1_21.ENTITY_DATA_TYPES.componentType,
            Types1_21.ENTITY_DATA_TYPES.optionalComponentType
        );
        registerBlockStateHandler(EntityTypes1_20_5.ABSTRACT_MINECART, 11);
    }

    @Override
    public void onMappingDataLoaded() {
        // IF ENTITY TYPES CHANGED: Automatically map entity id changes AFTER entity ids have been loaded
        mapTypes();
    }

    @Override
    public EntityType typeFromId(final int type) {
        return EntityTypes1_20_5.getTypeFromId(type);
    }
}
