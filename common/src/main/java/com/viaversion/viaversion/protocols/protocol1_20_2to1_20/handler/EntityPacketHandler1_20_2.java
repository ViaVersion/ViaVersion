/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2023 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_20_2to1_20.handler;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_19_4Types;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_20;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.Protocol1_20_2To1_20;
import com.viaversion.viaversion.rewriter.EntityRewriter;

public final class EntityPacketHandler1_20_2 extends EntityRewriter<ClientboundPackets1_19_4, Protocol1_20_2To1_20> {

    public EntityPacketHandler1_20_2(final Protocol1_20_2To1_20 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        registerTrackerWithData1_19(ClientboundPackets1_19_4.SPAWN_ENTITY, Entity1_19_4Types.FALLING_BLOCK);
        registerMetadataRewriter(ClientboundPackets1_19_4.ENTITY_METADATA, Types1_20.METADATA_LIST);
        registerRemoveEntities(ClientboundPackets1_19_4.REMOVE_ENTITIES);

        protocol.registerClientbound(ClientboundPackets1_19_4.JOIN_GAME, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    // Just reorder written data, move dimension data to configuration phase
                    wrapper.passthrough(Type.INT); // Entity id
                    wrapper.passthrough(Type.BOOLEAN); // Hardcore

                    final byte gamemode = wrapper.read(Type.UNSIGNED_BYTE).byteValue();
                    final byte previousGamemode = wrapper.read(Type.BYTE);

                    wrapper.passthrough(Type.STRING_ARRAY); // World List

                    final CompoundTag dimensionRegistry = wrapper.read(Type.NBT); // TODO AAAAAAAAAAAAAAAAAAAAA
                    final String dimensionType = wrapper.read(Type.STRING);
                    final String world = wrapper.read(Type.STRING);
                    final long seed = wrapper.read(Type.LONG);

                    wrapper.passthrough(Type.VAR_INT); // Max players
                    wrapper.passthrough(Type.VAR_INT); // View distance
                    wrapper.passthrough(Type.VAR_INT); // Simulation distance
                    wrapper.passthrough(Type.BOOLEAN); // Reduced debug info
                    wrapper.passthrough(Type.BOOLEAN); // Show death screen

                    wrapper.write(Type.STRING, dimensionType);
                    wrapper.write(Type.STRING, world);
                    wrapper.write(Type.LONG, seed);
                    wrapper.write(Type.BYTE, gamemode);
                    wrapper.write(Type.BYTE, previousGamemode);

                    wrapper.passthrough(Type.BOOLEAN); // Debug
                    wrapper.passthrough(Type.BOOLEAN); // Flat
                    wrapper.passthrough(Type.OPTIONAL_GLOBAL_POSITION); // Last death position
                    wrapper.passthrough(Type.VAR_INT); // Portal cooldown
                });
                handler(dimensionDataHandler()); // Caches dimensions to access data like height later
                handler(biomeSizeTracker()); // Tracks the amount of biomes sent for chunk data
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
                    wrapper.passthrough(Type.BYTE); // Gamemode
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
    }

    @Override
    protected void registerRewrites() {
        registerMetaTypeHandler(Types1_20.META_TYPES.itemType, Types1_20.META_TYPES.blockStateType, Types1_20.META_TYPES.optionalBlockStateType, Types1_20.META_TYPES.particleType);

        filter().filterFamily(Entity1_19_4Types.MINECART_ABSTRACT).index(11).handler((event, meta) -> {
            final int blockState = meta.value();
            meta.setValue(protocol.getMappingData().getNewBlockStateId(blockState));
        });
    }

    @Override
    public EntityType typeFromId(final int type) {
        return Entity1_19_4Types.getTypeFromId(type);
    }
}