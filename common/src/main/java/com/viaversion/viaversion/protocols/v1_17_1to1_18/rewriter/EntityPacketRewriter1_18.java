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
package com.viaversion.viaversion.protocols.v1_17_1to1_18.rewriter;

import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_17;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_17;
import com.viaversion.viaversion.api.type.types.version.Types1_18;
import com.viaversion.viaversion.protocols.v1_17_1to1_18.Protocol1_17_1To1_18;
import com.viaversion.viaversion.protocols.v1_17_1to1_18.storage.ChunkLightStorage;
import com.viaversion.viaversion.protocols.v1_17to1_17_1.packet.ClientboundPackets1_17_1;
import com.viaversion.viaversion.rewriter.EntityRewriter;

public final class EntityPacketRewriter1_18 extends EntityRewriter<ClientboundPackets1_17_1, Protocol1_17_1To1_18> {

    public EntityPacketRewriter1_18(final Protocol1_17_1To1_18 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        registerSetEntityData(ClientboundPackets1_17_1.SET_ENTITY_DATA, Types1_17.ENTITY_DATA_LIST, Types1_18.ENTITY_DATA_LIST);

        protocol.registerClientbound(ClientboundPackets1_17_1.LOGIN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); // Entity ID
                map(Types.BOOLEAN); // Hardcore
                map(Types.BYTE); // Gamemode
                map(Types.BYTE); // Previous Gamemode
                map(Types.STRING_ARRAY); // World List
                map(Types.NAMED_COMPOUND_TAG); // Registry
                map(Types.NAMED_COMPOUND_TAG); // Current dimension data
                map(Types.STRING); // World
                map(Types.LONG); // Seed
                map(Types.VAR_INT); // Max players
                handler(wrapper -> {
                    int chunkRadius = wrapper.passthrough(Types.VAR_INT);
                    wrapper.write(Types.VAR_INT, chunkRadius); // Simulation distance
                });
                handler(worldDataTrackerHandler(1));
                handler(biomeSizeTracker());
            }
        });

        protocol.registerClientbound(ClientboundPackets1_17_1.RESPAWN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.NAMED_COMPOUND_TAG); // Current dimension data
                map(Types.STRING); // World
                handler(wrapper -> {
                    final String world = wrapper.get(Types.STRING, 0);
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
        filter().mapDataType(Types1_18.ENTITY_DATA_TYPES::byId);
        filter().dataType(Types1_18.ENTITY_DATA_TYPES.particleType).handler((event, data) -> {
            final Particle particle = (Particle) data.getValue();
            if (particle.id() == 2) { // Barrier
                particle.setId(3); // Block marker
                particle.add(Types.VAR_INT, 7754); // Barrier state
            } else if (particle.id() == 3) { // Light block
                particle.add(Types.VAR_INT, 7786); // Light block state
            } else {
                rewriteParticle(event.user(), particle);
            }
        });

        registerEntityDataTypeHandler(Types1_18.ENTITY_DATA_TYPES.itemType, null, null);
    }

    @Override
    public EntityType typeFromId(final int type) {
        return EntityTypes1_17.getTypeFromId(type);
    }
}
