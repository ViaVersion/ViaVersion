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
package com.viaversion.viaversion.protocols.protocol1_18to1_17_1.packets;

import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_17;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_17;
import com.viaversion.viaversion.api.type.types.version.Types1_18;
import com.viaversion.viaversion.protocols.protocol1_17_1to1_17.ClientboundPackets1_17_1;
import com.viaversion.viaversion.protocols.protocol1_18to1_17_1.Protocol1_18To1_17_1;
import com.viaversion.viaversion.protocols.protocol1_18to1_17_1.storage.ChunkLightStorage;
import com.viaversion.viaversion.rewriter.EntityRewriter;

public final class EntityPackets extends EntityRewriter<ClientboundPackets1_17_1, Protocol1_18To1_17_1> {

    public EntityPackets(final Protocol1_18To1_17_1 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        registerMetadataRewriter(ClientboundPackets1_17_1.ENTITY_METADATA, Types1_17.METADATA_LIST, Types1_18.METADATA_LIST);

        protocol.registerClientbound(ClientboundPackets1_17_1.JOIN_GAME, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); // Entity ID
                map(Type.BOOLEAN); // Hardcore
                map(Type.BYTE); // Gamemode
                map(Type.BYTE); // Previous Gamemode
                map(Type.STRING_ARRAY); // World List
                map(Type.NAMED_COMPOUND_TAG); // Registry
                map(Type.NAMED_COMPOUND_TAG); // Current dimension data
                map(Type.STRING); // World
                map(Type.LONG); // Seed
                map(Type.VAR_INT); // Max players
                handler(wrapper -> {
                    int chunkRadius = wrapper.passthrough(Type.VAR_INT);
                    wrapper.write(Type.VAR_INT, chunkRadius); // Simulation distance
                });
                handler(worldDataTrackerHandler(1));
                handler(biomeSizeTracker());
            }
        });

        protocol.registerClientbound(ClientboundPackets1_17_1.RESPAWN, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.NAMED_COMPOUND_TAG); // Current dimension data
                map(Type.STRING); // World
                handler(wrapper -> {
                    final String world = wrapper.get(Type.STRING, 0);
                    final EntityTracker tracker = tracker(wrapper.user());
                    if (!world.equals(tracker.currentWorld())) {
                        wrapper.user().get(ChunkLightStorage.class).clear();
                    }
                });
                handler(worldDataTrackerHandler(0));
            }
        });
    }

    @Override
    protected void registerRewrites() {
        filter().mapMetaType(Types1_18.META_TYPES::byId);
        filter().metaType(Types1_18.META_TYPES.particleType).handler((event, meta) -> {
            final Particle particle = (Particle) meta.getValue();
            if (particle.id() == 2) { // Barrier
                particle.setId(3); // Block marker
                particle.add(Type.VAR_INT, 7754); // Barrier state
            } else if (particle.id() == 3) { // Light block
                particle.add(Type.VAR_INT, 7786); // Light block state
            } else {
                rewriteParticle(event.user(), particle);
            }
        });

        registerMetaTypeHandler(Types1_18.META_TYPES.itemType, null, null);
    }

    @Override
    public EntityType typeFromId(final int type) {
        return EntityTypes1_17.getTypeFromId(type);
    }
}
