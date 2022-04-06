/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2022 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_19to1_18_2.packets;

import com.viaversion.viaversion.api.minecraft.entities.Entity1_17Types;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_19Types;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_18;
import com.viaversion.viaversion.api.type.types.version.Types1_19;
import com.viaversion.viaversion.protocols.protocol1_18to1_17_1.ClientboundPackets1_18;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.ClientboundPackets1_19;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.Protocol1_19To1_18_2;
import com.viaversion.viaversion.rewriter.EntityRewriter;

public final class EntityPackets extends EntityRewriter<Protocol1_19To1_18_2> {

    public EntityPackets(final Protocol1_19To1_18_2 protocol) {
        super(protocol);
        mapTypes(Entity1_17Types.values(), Entity1_19Types.class);
    }

    @Override
    public void registerPackets() {
        registerTracker(ClientboundPackets1_18.SPAWN_PLAYER, Entity1_19Types.PLAYER);
        registerMetadataRewriter(ClientboundPackets1_18.ENTITY_METADATA, Types1_18.METADATA_LIST, Types1_19.METADATA_LIST);
        registerRemoveEntities(ClientboundPackets1_18.REMOVE_ENTITIES);

        protocol.registerClientbound(ClientboundPackets1_18.SPAWN_ENTITY, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // Entity id
                map(Type.UUID); // Entity UUID
                map(Type.VAR_INT); // Entity type
                map(Type.DOUBLE); // X
                map(Type.DOUBLE); // Y
                map(Type.DOUBLE); // Z
                map(Type.BYTE); // Pitch
                map(Type.BYTE); // Yaw
                create(Type.BYTE, (byte) 0); // Head yaw (moved over from mob spawn packet)
                map(Type.INT, Type.VAR_INT); // Data
                handler(trackerHandler());
                handler(wrapper -> {
                    final int entityId = wrapper.get(Type.VAR_INT, 0);
                    final EntityType entityType = tracker(wrapper.user()).entityType(entityId);
                    if (entityType == Entity1_19Types.FALLING_BLOCK) {
                        wrapper.set(Type.VAR_INT, 2, protocol.getMappingData().getNewBlockStateId(wrapper.get(Type.VAR_INT, 2)));
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_18.SPAWN_MOB, ClientboundPackets1_19.SPAWN_ENTITY, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // Entity ID
                map(Type.UUID); // Entity UUID
                map(Type.VAR_INT); // Entity Type
                map(Type.DOUBLE); // X
                map(Type.DOUBLE); // Y
                map(Type.DOUBLE); // Z
                map(Type.BYTE); // Yaw
                map(Type.BYTE); // Pitch
                map(Type.BYTE); // Head yaw
                create(Type.VAR_INT, 0); // Data
                map(Type.SHORT); // Velocity x
                map(Type.SHORT); // Velocity y
                map(Type.SHORT); // Velocity z
                handler(trackerHandler());
            }
        });

        protocol.registerClientbound(ClientboundPackets1_18.ENTITY_EFFECT, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // Entity id
                map(Type.VAR_INT); // Effect id
                map(Type.BYTE); // Amplifier
                map(Type.VAR_INT); // Duration
                map(Type.BYTE); // Flags
                create(Type.BOOLEAN, false); // No factor data
            }
        });

        protocol.registerClientbound(ClientboundPackets1_18.JOIN_GAME, new PacketRemapper() {
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
                map(Type.LONG); // Seed
                map(Type.VAR_INT); // Max players
                map(Type.VAR_INT); // Chunk radius
                map(Type.VAR_INT); // Simulation distance
                handler(playerTrackerHandler());
                handler(worldDataTrackerHandler(1));
                handler(biomeSizeTracker());
            }
        });
        protocol.registerClientbound(ClientboundPackets1_18.RESPAWN, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.NBT); // Current dimension data
                map(Type.STRING); // World
                handler(worldDataTrackerHandler(0));
            }
        });
    }

    @Override
    protected void registerRewrites() {
        filter().handler((event, meta) -> meta.setMetaType(Types1_19.META_TYPES.byId(meta.metaType().typeId())));

        registerMetaTypeHandler(Types1_19.META_TYPES.itemType, Types1_19.META_TYPES.blockStateType, Types1_19.META_TYPES.particleType);

        filter().filterFamily(Entity1_19Types.MINECART_ABSTRACT).index(11).handler((event, meta) -> {
            // Convert to new block id
            final int data = (int) meta.getValue();
            meta.setValue(protocol.getMappingData().getNewBlockStateId(data));
        });

        filter().type(Entity1_19Types.PLAYER).addIndex(19); // Last death location
        filter().type(Entity1_19Types.CAT).index(19).handler((event, meta) -> meta.setMetaType(Types1_19.META_TYPES.catVariantType));
    }

    @Override
    public EntityType typeFromId(final int type) {
        return Entity1_19Types.getTypeFromId(type);
    }
}
