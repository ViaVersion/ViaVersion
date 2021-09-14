/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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
import com.viaversion.viaversion.api.minecraft.entities.Entity1_17Types;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_17_1to1_17.ClientboundPackets1_17_1;
import com.viaversion.viaversion.protocols.protocol1_18to1_17_1.Protocol1_18To1_17_1;
import com.viaversion.viaversion.protocols.protocol1_18to1_17_1.storage.ChunkLightStorage;
import com.viaversion.viaversion.rewriter.EntityRewriter;

public final class EntityPackets extends EntityRewriter<Protocol1_18To1_17_1> {

    public EntityPackets(final Protocol1_18To1_17_1 protocol) {
        super(protocol);
        //mapTypes(Entity1_17Types.values(), Entity1_18Types.class);
    }

    @Override
    public void registerPackets() {
        /*registerTrackerWithData(ClientboundPackets1_17_1.SPAWN_ENTITY, Entity1_18Types.FALLING_BLOCK);
        registerTracker(ClientboundPackets1_17_1.SPAWN_MOB);
        registerTracker(ClientboundPackets1_17_1.SPAWN_PLAYER, Entity1_18Types.PLAYER);
        registerMetadataRewriter(ClientboundPackets1_17_1.ENTITY_METADATA, Types1_17.METADATA_LIST);
        registerRemoveEntities(ClientboundPackets1_17_1.REMOVE_ENTITIES);*/

        protocol.registerClientbound(ClientboundPackets1_17_1.JOIN_GAME, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // Entity ID
                map(Type.BOOLEAN); // Hardcore
                map(Type.UNSIGNED_BYTE); // Gamemode
                map(Type.BYTE); // Previous Gamemode
                map(Type.STRING_ARRAY); // World List
                map(Type.NBT); // Registry
                map(Type.NBT); // Current dimension data
                map(Type.STRING); // World
                handler(worldDataTrackerHandler(1));
            }
        });

        protocol.registerClientbound(ClientboundPackets1_17_1.RESPAWN, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.NBT); // Current dimension data
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
        //registerMetaTypeHandler(MetaType1_17.ITEM, MetaType1_17.BLOCK_STATE, MetaType1_17.PARTICLE); //TODO with nulls if needed

        /*filter().filterFamily(Entity1_17Types.MINECART_ABSTRACT).index(11).handler((event, meta) -> { //TODO check id
            // Convert to new block id
            int data = (int) meta.getValue();
            meta.setValue(protocol.getMappingData().getNewBlockStateId(data));
        });*/
    }

    @Override
    public EntityType typeFromId(final int type) {
        //TODO Entity1_18Types
        return Entity1_17Types.getTypeFromId(type);
    }
}
