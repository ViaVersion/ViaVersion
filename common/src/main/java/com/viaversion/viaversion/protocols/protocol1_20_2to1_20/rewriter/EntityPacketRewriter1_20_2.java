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
package com.viaversion.viaversion.protocols.protocol1_20_2to1_20.rewriter;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_19_4;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_20;
import com.viaversion.viaversion.api.type.types.version.Types1_20_2;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.Protocol1_20_2To1_20;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ClientboundPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.storage.ConfigurationState;
import com.viaversion.viaversion.rewriter.EntityRewriter;

public final class EntityPacketRewriter1_20_2 extends EntityRewriter<ClientboundPackets1_19_4, Protocol1_20_2To1_20> {

    public EntityPacketRewriter1_20_2(final Protocol1_20_2To1_20 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        registerTrackerWithData1_19(ClientboundPackets1_19_4.SPAWN_ENTITY, EntityTypes1_19_4.FALLING_BLOCK);
        registerMetadataRewriter(ClientboundPackets1_19_4.ENTITY_METADATA, Types1_20.METADATA_LIST, Types1_20_2.METADATA_LIST);
        registerRemoveEntities(ClientboundPackets1_19_4.REMOVE_ENTITIES);

        protocol.registerClientbound(ClientboundPackets1_19_4.SPAWN_PLAYER, ClientboundPackets1_20_2.SPAWN_ENTITY, wrapper -> {
            wrapper.passthrough(Type.VAR_INT); // Entity id
            wrapper.passthrough(Type.UUID); // UUID

            wrapper.write(Type.VAR_INT, EntityTypes1_19_4.PLAYER.getId()); // Entity type id

            wrapper.passthrough(Type.DOUBLE); // X
            wrapper.passthrough(Type.DOUBLE); // Y
            wrapper.passthrough(Type.DOUBLE); // Z

            final byte yaw = wrapper.read(Type.BYTE); // Yaw
            wrapper.passthrough(Type.BYTE); // Pitch
            wrapper.write(Type.BYTE, yaw);
            wrapper.write(Type.BYTE, yaw); // Head yaw
            wrapper.write(Type.VAR_INT, 0); // Data
            wrapper.write(Type.SHORT, (short) 0); // Velocity X
            wrapper.write(Type.SHORT, (short) 0); // Velocity Y
            wrapper.write(Type.SHORT, (short) 0); // Velocity Z
        });

        protocol.registerClientbound(ClientboundPackets1_19_4.JOIN_GAME, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    // Just reorder written data, move dimension data to configuration phase
                    wrapper.passthrough(Type.INT); // Entity id
                    wrapper.passthrough(Type.BOOLEAN); // Hardcore

                    final byte gamemode = wrapper.read(Type.BYTE);
                    final byte previousGamemode = wrapper.read(Type.BYTE);

                    wrapper.passthrough(Type.STRING_ARRAY); // World List

                    final CompoundTag dimensionRegistry = wrapper.read(Type.NAMED_COMPOUND_TAG);
                    final String dimensionType = wrapper.read(Type.STRING);
                    final String world = wrapper.read(Type.STRING);
                    final long seed = wrapper.read(Type.LONG);
                    trackBiomeSize(wrapper.user(), dimensionRegistry); // Caches dimensions to access data like height later
                    cacheDimensionData(wrapper.user(), dimensionRegistry); // Tracks the amount of biomes sent for chunk data

                    wrapper.passthrough(Type.VAR_INT); // Max players
                    wrapper.passthrough(Type.VAR_INT); // View distance
                    wrapper.passthrough(Type.VAR_INT); // Simulation distance
                    wrapper.passthrough(Type.BOOLEAN); // Reduced debug info
                    wrapper.passthrough(Type.BOOLEAN); // Show death screen

                    wrapper.write(Type.BOOLEAN, false); // Limited crafting
                    wrapper.write(Type.STRING, dimensionType);
                    wrapper.write(Type.STRING, world);
                    wrapper.write(Type.LONG, seed);
                    wrapper.write(Type.BYTE, gamemode);
                    wrapper.write(Type.BYTE, previousGamemode);

                    // Debug, flat, last death pos, and portal cooldown at the end unchanged

                    final ConfigurationState configurationBridge = wrapper.user().get(ConfigurationState.class);
                    if (!configurationBridge.setLastDimensionRegistry(dimensionRegistry)) {
                        // No change, so no need to re-enter the configuration state - just let this one through
                        final PacketWrapper clientInformationPacket = configurationBridge.clientInformationPacket(wrapper.user());
                        if (clientInformationPacket != null) {
                            // Schedule the sending to ensure it arrives later, this fixes an issue where on
                            // servers running < 1.20.2 a client changing servers on a proxy lost skin layers
                            clientInformationPacket.scheduleSendToServer(Protocol1_20_2To1_20.class);
                        }
                        return;
                    }

                    if (configurationBridge.bridgePhase() == ConfigurationState.BridgePhase.NONE) {
                        // Reenter the configuration state
                        final PacketWrapper configurationPacket = wrapper.create(ClientboundPackets1_20_2.START_CONFIGURATION);
                        configurationPacket.send(Protocol1_20_2To1_20.class);

                        configurationBridge.setBridgePhase(ConfigurationState.BridgePhase.REENTERING_CONFIGURATION);
                        configurationBridge.setJoinGamePacket(wrapper);
                        wrapper.cancel();
                        return;
                    }

                    // Queue it and send it after the client acks the configuration finish
                    configurationBridge.setJoinGamePacket(wrapper);
                    wrapper.cancel();

                    Protocol1_20_2To1_20.sendConfigurationPackets(wrapper.user(), dimensionRegistry, null);
                });
                handler(worldDataTrackerHandlerByKey()); // Tracks world height and name for chunk data and entity (un)tracking
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_4.RESPAWN, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    wrapper.passthrough(Type.STRING); // Dimension type
                    wrapper.passthrough(Type.STRING); // World
                    wrapper.passthrough(Type.LONG); // Seed
                    wrapper.write(Type.BYTE, wrapper.read(Type.UNSIGNED_BYTE).byteValue()); // Gamemode
                    wrapper.passthrough(Type.BYTE); // Previous gamemode
                    wrapper.passthrough(Type.BOOLEAN); // Debug
                    wrapper.passthrough(Type.BOOLEAN); // Flat

                    // Move this to the end
                    final byte dataToKeep = wrapper.read(Type.BYTE);

                    wrapper.passthrough(Type.OPTIONAL_GLOBAL_POSITION); // Last death position
                    wrapper.passthrough(Type.VAR_INT); // Portal cooldown

                    wrapper.write(Type.BYTE, dataToKeep);
                });
                handler(worldDataTrackerHandlerByKey()); // Tracks world height and name for chunk data and entity (un)tracking
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_4.ENTITY_EFFECT, wrapper -> {
            wrapper.passthrough(Type.VAR_INT); // Entity id
            wrapper.write(Type.VAR_INT, wrapper.read(Type.VAR_INT) - 1); // Effect id
            wrapper.passthrough(Type.BYTE); // Amplifier
            wrapper.passthrough(Type.VAR_INT); // Duration
            wrapper.passthrough(Type.BYTE); // Flags
            wrapper.write(Type.OPTIONAL_COMPOUND_TAG, wrapper.read(Type.OPTIONAL_NAMED_COMPOUND_TAG)); // Factor data
        });

        protocol.registerClientbound(ClientboundPackets1_19_4.REMOVE_ENTITY_EFFECT, wrapper -> {
            wrapper.passthrough(Type.VAR_INT); // Entity id
            wrapper.write(Type.VAR_INT, wrapper.read(Type.VAR_INT) - 1); // Effect id
        });
    }

    @Override
    protected void registerRewrites() {
        filter().mapMetaType(Types1_20_2.META_TYPES::byId);
        registerMetaTypeHandler(Types1_20_2.META_TYPES.itemType, Types1_20_2.META_TYPES.blockStateType, Types1_20_2.META_TYPES.optionalBlockStateType, Types1_20_2.META_TYPES.particleType, null);

        filter().type(EntityTypes1_19_4.DISPLAY).addIndex(10);

        filter().type(EntityTypes1_19_4.MINECART_ABSTRACT).index(11).handler((event, meta) -> {
            final int blockState = meta.value();
            meta.setValue(protocol.getMappingData().getNewBlockStateId(blockState));
        });
    }

    @Override
    public EntityType typeFromId(final int type) {
        return EntityTypes1_19_4.getTypeFromId(type);
    }
}
