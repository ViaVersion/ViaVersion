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
package com.viaversion.viaversion.protocols.v1_20to1_20_2.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_19_4;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_20;
import com.viaversion.viaversion.api.type.types.version.Types1_20_2;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.Protocol1_20To1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ClientboundPackets1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.storage.ConfigurationState;
import com.viaversion.viaversion.rewriter.EntityRewriter;

public final class EntityPacketRewriter1_20_2 extends EntityRewriter<ClientboundPackets1_19_4, Protocol1_20To1_20_2> {

    public EntityPacketRewriter1_20_2(final Protocol1_20To1_20_2 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        registerTrackerWithData1_19(ClientboundPackets1_19_4.ADD_ENTITY, EntityTypes1_19_4.FALLING_BLOCK);
        registerSetEntityData(ClientboundPackets1_19_4.SET_ENTITY_DATA, Types1_20.ENTITY_DATA_LIST, Types1_20_2.ENTITY_DATA_LIST);
        registerRemoveEntities(ClientboundPackets1_19_4.REMOVE_ENTITIES);

        protocol.registerClientbound(ClientboundPackets1_19_4.ADD_PLAYER, ClientboundPackets1_20_2.ADD_ENTITY, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Entity id
            wrapper.passthrough(Types.UUID); // UUID

            wrapper.write(Types.VAR_INT, EntityTypes1_19_4.PLAYER.getId()); // Entity type id

            wrapper.passthrough(Types.DOUBLE); // X
            wrapper.passthrough(Types.DOUBLE); // Y
            wrapper.passthrough(Types.DOUBLE); // Z

            final byte yaw = wrapper.read(Types.BYTE); // Yaw
            wrapper.passthrough(Types.BYTE); // Pitch
            wrapper.write(Types.BYTE, yaw);
            wrapper.write(Types.BYTE, yaw); // Head yaw
            wrapper.write(Types.VAR_INT, 0); // Data
            wrapper.write(Types.SHORT, (short) 0); // Velocity X
            wrapper.write(Types.SHORT, (short) 0); // Velocity Y
            wrapper.write(Types.SHORT, (short) 0); // Velocity Z
        });

        protocol.registerClientbound(ClientboundPackets1_19_4.LOGIN, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    // Just reorder written data, move dimension data to configuration phase
                    wrapper.passthrough(Types.INT); // Entity id
                    wrapper.passthrough(Types.BOOLEAN); // Hardcore

                    final byte gamemode = wrapper.read(Types.BYTE);
                    final byte previousGamemode = wrapper.read(Types.BYTE);

                    wrapper.passthrough(Types.STRING_ARRAY); // World List

                    final CompoundTag dimensionRegistry = wrapper.read(Types.NAMED_COMPOUND_TAG);
                    final String dimensionType = wrapper.read(Types.STRING);
                    final String world = wrapper.read(Types.STRING);
                    final long seed = wrapper.read(Types.LONG);
                    trackBiomeSize(wrapper.user(), dimensionRegistry); // Caches dimensions to access data like height later
                    cacheDimensionData(wrapper.user(), dimensionRegistry); // Tracks the amount of biomes sent for chunk data

                    wrapper.passthrough(Types.VAR_INT); // Max players
                    wrapper.passthrough(Types.VAR_INT); // View distance
                    wrapper.passthrough(Types.VAR_INT); // Simulation distance
                    wrapper.passthrough(Types.BOOLEAN); // Reduced debug info
                    wrapper.passthrough(Types.BOOLEAN); // Show death screen

                    wrapper.write(Types.BOOLEAN, false); // Limited crafting
                    wrapper.write(Types.STRING, dimensionType);
                    wrapper.write(Types.STRING, world);
                    wrapper.write(Types.LONG, seed);
                    wrapper.write(Types.BYTE, gamemode);
                    wrapper.write(Types.BYTE, previousGamemode);

                    // Debug, flat, last death pos, and portal cooldown at the end unchanged

                    final ConfigurationState configurationBridge = wrapper.user().get(ConfigurationState.class);
                    if (!configurationBridge.setLastDimensionRegistry(dimensionRegistry)) {
                        // No change, so no need to re-enter the configuration state - just let this one through
                        final PacketWrapper clientInformationPacket = configurationBridge.clientInformationPacket(wrapper.user());
                        if (clientInformationPacket != null) {
                            // Schedule the sending to ensure it arrives later, this fixes an issue where on
                            // servers running < 1.20.2 a client changing servers on a proxy lost skin layers
                            clientInformationPacket.scheduleSendToServer(Protocol1_20To1_20_2.class);
                        }
                        return;
                    }

                    if (configurationBridge.bridgePhase() == ConfigurationState.BridgePhase.NONE) {
                        // Reenter the configuration state
                        final PacketWrapper configurationPacket = wrapper.create(ClientboundPackets1_20_2.START_CONFIGURATION);
                        configurationPacket.send(Protocol1_20To1_20_2.class);

                        configurationBridge.setBridgePhase(ConfigurationState.BridgePhase.REENTERING_CONFIGURATION);
                        configurationBridge.setJoinGamePacket(wrapper);
                        wrapper.cancel();
                        return;
                    }

                    // Queue it and send it after the client acks the configuration finish
                    configurationBridge.setJoinGamePacket(wrapper);
                    wrapper.cancel();

                    Protocol1_20To1_20_2.sendConfigurationPackets(wrapper.user(), dimensionRegistry, null);
                });
                handler(worldDataTrackerHandlerByKey()); // Tracks world height and name for chunk data and entity (un)tracking
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_4.RESPAWN, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    wrapper.passthrough(Types.STRING); // Dimension type
                    wrapper.passthrough(Types.STRING); // World
                    wrapper.passthrough(Types.LONG); // Seed
                    wrapper.write(Types.BYTE, wrapper.read(Types.UNSIGNED_BYTE).byteValue()); // Gamemode
                    wrapper.passthrough(Types.BYTE); // Previous gamemode
                    wrapper.passthrough(Types.BOOLEAN); // Debug
                    wrapper.passthrough(Types.BOOLEAN); // Flat

                    // Move this to the end
                    final byte dataToKeep = wrapper.read(Types.BYTE);

                    wrapper.passthrough(Types.OPTIONAL_GLOBAL_POSITION); // Last death position
                    wrapper.passthrough(Types.VAR_INT); // Portal cooldown

                    wrapper.write(Types.BYTE, dataToKeep);
                });
                handler(worldDataTrackerHandlerByKey()); // Tracks world height and name for chunk data and entity (un)tracking
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_4.UPDATE_MOB_EFFECT, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Entity id
            wrapper.write(Types.VAR_INT, wrapper.read(Types.VAR_INT) - 1); // Effect id
            wrapper.passthrough(Types.BYTE); // Amplifier
            wrapper.passthrough(Types.VAR_INT); // Duration
            wrapper.passthrough(Types.BYTE); // Flags
            wrapper.write(Types.OPTIONAL_COMPOUND_TAG, wrapper.read(Types.OPTIONAL_NAMED_COMPOUND_TAG)); // Factor data
        });

        protocol.registerClientbound(ClientboundPackets1_19_4.REMOVE_MOB_EFFECT, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Entity id
            wrapper.write(Types.VAR_INT, wrapper.read(Types.VAR_INT) - 1); // Effect id
        });
    }

    @Override
    protected void registerRewrites() {
        filter().mapDataType(Types1_20_2.ENTITY_DATA_TYPES::byId);
        registerEntityDataTypeHandler(Types1_20_2.ENTITY_DATA_TYPES.itemType, Types1_20_2.ENTITY_DATA_TYPES.blockStateType, Types1_20_2.ENTITY_DATA_TYPES.optionalBlockStateType, Types1_20_2.ENTITY_DATA_TYPES.particleType, null);
        registerMinecartBlockStateHandler(EntityTypes1_19_4.ABSTRACT_MINECART);

        filter().type(EntityTypes1_19_4.DISPLAY).addIndex(10);
    }

    @Override
    public EntityType typeFromId(final int type) {
        return EntityTypes1_19_4.getTypeFromId(type);
    }
}
