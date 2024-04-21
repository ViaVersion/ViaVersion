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
package com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.rewriter;

import com.viaversion.viaversion.api.data.ParticleMappings;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_20_3;
import com.viaversion.viaversion.api.minecraft.metadata.MetaType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_20_2;
import com.viaversion.viaversion.api.type.types.version.Types1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ClientboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ClientboundPacket1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ClientboundPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.Protocol1_20_3To1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundPackets1_20_3;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.Key;

public final class EntityPacketRewriter1_20_3 extends EntityRewriter<ClientboundPacket1_20_2, Protocol1_20_3To1_20_2> {

    public EntityPacketRewriter1_20_3(final Protocol1_20_3To1_20_2 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        registerTrackerWithData1_19(ClientboundPackets1_20_2.SPAWN_ENTITY, EntityTypes1_20_3.FALLING_BLOCK);
        registerMetadataRewriter(ClientboundPackets1_20_2.ENTITY_METADATA, Types1_20_2.METADATA_LIST, Types1_20_3.METADATA_LIST);
        registerRemoveEntities(ClientboundPackets1_20_2.REMOVE_ENTITIES);

        protocol.registerClientbound(ClientboundConfigurationPackets1_20_2.REGISTRY_DATA, new PacketHandlers() {
            @Override
            protected void register() {
                map(Type.COMPOUND_TAG); // Registry data
                handler(configurationDimensionDataHandler());
                handler(configurationBiomeSizeTracker());
            }
        });

        protocol.registerClientbound(ClientboundPackets1_20_2.JOIN_GAME, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); // Entity id
                map(Type.BOOLEAN); // Hardcore
                map(Type.STRING_ARRAY); // World List
                map(Type.VAR_INT); // Max players
                map(Type.VAR_INT); // View distance
                map(Type.VAR_INT); // Simulation distance
                map(Type.BOOLEAN); // Reduced debug info
                map(Type.BOOLEAN); // Show death screen
                map(Type.BOOLEAN); // Limited crafting
                map(Type.STRING); // Dimension key
                map(Type.STRING); // World
                handler(worldDataTrackerHandlerByKey());
                handler(wrapper -> sendChunksSentGameEvent(wrapper));
            }
        });

        protocol.registerClientbound(ClientboundPackets1_20_2.RESPAWN, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // Dimension
                map(Type.STRING); // World
                handler(worldDataTrackerHandlerByKey());
                handler(wrapper -> sendChunksSentGameEvent(wrapper));
            }
        });

        // https://github.com/ViaVersion/ViaVersion/issues/3630, should still investigate why sending it with respawn/login doesn't work on Velocity
        protocol.registerClientbound(ClientboundPackets1_20_2.WORLD_BORDER_INIT, this::sendChunksSentGameEvent);
    }

    private void sendChunksSentGameEvent(final PacketWrapper wrapper) throws Exception {
        wrapper.send(Protocol1_20_3To1_20_2.class);
        wrapper.cancel();

        // Make sure the loading screen is closed, continues old client behavior
        final PacketWrapper gameEventPacket = wrapper.create(ClientboundPackets1_20_3.GAME_EVENT);
        gameEventPacket.write(Type.UNSIGNED_BYTE, (short) 13);
        gameEventPacket.write(Type.FLOAT, 0F);
        gameEventPacket.send(Protocol1_20_3To1_20_2.class);
    }

    @Override
    protected void registerRewrites() {
        filter().handler((event, meta) -> {
            final MetaType type = meta.metaType();
            if (type == Types1_20_2.META_TYPES.componentType) {
                meta.setTypeAndValue(Types1_20_3.META_TYPES.componentType, ComponentUtil.jsonToTag(meta.value()));
            } else if (type == Types1_20_2.META_TYPES.optionalComponentType) {
                meta.setTypeAndValue(Types1_20_3.META_TYPES.optionalComponentType, ComponentUtil.jsonToTag(meta.value()));
            } else {
                meta.setMetaType(Types1_20_3.META_TYPES.byId(type.typeId()));
            }
        });
        filter().metaType(Types1_20_3.META_TYPES.particleType).handler((event, meta) -> {
            final Particle particle = meta.value();
            final ParticleMappings particleMappings = protocol.getMappingData().getParticleMappings();
            if (particle.getId() == particleMappings.id("vibration")) {
                // Change the type of the resource key argument
                final String resourceLocation = particle.<String>removeArgument(0).getValue();
                if (Key.stripMinecraftNamespace(resourceLocation).equals("block")) {
                    particle.add(0, Type.VAR_INT, 0);
                } else { // Entity
                    particle.add(0, Type.VAR_INT, 1);
                }
            }

            rewriteParticle(event.user(), particle);
        });

        registerMetaTypeHandler(
                Types1_20_3.META_TYPES.itemType,
                Types1_20_3.META_TYPES.blockStateType,
                Types1_20_3.META_TYPES.optionalBlockStateType,
                Types1_20_3.META_TYPES.particleType,
                null);

        filter().type(EntityTypes1_20_3.MINECART_ABSTRACT).index(11).handler((event, meta) -> {
            final int blockState = meta.value();
            meta.setValue(protocol.getMappingData().getNewBlockStateId(blockState));
        });
    }

    @Override
    public void onMappingDataLoaded() {
        mapTypes();
    }

    @Override
    public EntityType typeFromId(final int type) {
        return EntityTypes1_20_3.getTypeFromId(type);
    }
}