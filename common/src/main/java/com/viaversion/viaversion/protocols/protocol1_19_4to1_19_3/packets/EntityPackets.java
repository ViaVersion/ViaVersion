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
package com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.packets;

import com.github.steveice10.opennbt.tag.builtin.ByteTag;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_19_4Types;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_19_3;
import com.viaversion.viaversion.api.type.types.version.Types1_19_4;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.ClientboundPackets1_19_3;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.Protocol1_19_4To1_19_3;
import com.viaversion.viaversion.rewriter.EntityRewriter;

public final class EntityPackets extends EntityRewriter<ClientboundPackets1_19_3, Protocol1_19_4To1_19_3> {

    public EntityPackets(final Protocol1_19_4To1_19_3 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        protocol.registerClientbound(ClientboundPackets1_19_3.JOIN_GAME, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); // Entity id
                map(Type.BOOLEAN); // Hardcore
                map(Type.UNSIGNED_BYTE); // Gamemode
                map(Type.BYTE); // Previous Gamemode
                map(Type.STRING_ARRAY); // World List
                map(Type.NBT); // Dimension registry
                map(Type.STRING); // Dimension key
                map(Type.STRING); // World
                handler(dimensionDataHandler());
                handler(biomeSizeTracker());
                handler(worldDataTrackerHandlerByKey());
                handler(wrapper -> {
                    final CompoundTag registry = wrapper.get(Type.NBT, 0);
                    final CompoundTag damageTypeRegistry = protocol.getMappingData().damageTypesRegistry();
                    registry.put("minecraft:damage_type", damageTypeRegistry);

                    final CompoundTag biomeRegistry = registry.get("minecraft:worldgen/biome");
                    final ListTag biomes = biomeRegistry.get("value");
                    for (final Tag biomeTag : biomes) {
                        final CompoundTag biomeData = ((CompoundTag) biomeTag).get("element");
                        final StringTag precipitation = biomeData.get("precipitation");
                        final byte precipitationByte = precipitation.getValue().equals("none") ? (byte) 0 : 1;
                        biomeData.put("has_precipitation", new ByteTag(precipitationByte));
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_3.PLAYER_POSITION, new PacketHandlers() {
            @Override
            protected void register() {
                map(Type.DOUBLE); // X
                map(Type.DOUBLE); // Y
                map(Type.DOUBLE); // Z
                map(Type.FLOAT); // Yaw
                map(Type.FLOAT); // Pitch
                map(Type.BYTE); // Relative arguments
                map(Type.VAR_INT); // Id
                read(Type.BOOLEAN); // Dismount vehicle
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_3.ENTITY_ANIMATION, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // Entity id
                handler(wrapper -> {
                    final short action = wrapper.read(Type.UNSIGNED_BYTE);
                    if (action != 1) {
                        wrapper.write(Type.UNSIGNED_BYTE, action);
                        return;
                    }

                    wrapper.setPacketType(ClientboundPackets1_19_4.HIT_ANIMATION);
                    wrapper.write(Type.FLOAT, 0F);
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_3.RESPAWN, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // Dimension
                map(Type.STRING); // World
                handler(worldDataTrackerHandlerByKey());
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_3.ENTITY_STATUS, wrapper -> {
            final int entityId = wrapper.read(Type.INT);
            final byte event = wrapper.read(Type.BYTE);

            final int damageType = damageTypeFromEntityEvent(event);
            if (damageType != -1) {
                wrapper.setPacketType(ClientboundPackets1_19_4.DAMAGE_EVENT);
                wrapper.write(Type.VAR_INT, entityId);
                wrapper.write(Type.VAR_INT, damageType);
                wrapper.write(Type.VAR_INT, 0); // No source entity
                wrapper.write(Type.VAR_INT, 0); // No direct source entity
                wrapper.write(Type.BOOLEAN, false); // No source position
                return;
            }

            wrapper.write(Type.INT, entityId);
            wrapper.write(Type.BYTE, event);
        });

        registerTrackerWithData1_19(ClientboundPackets1_19_3.SPAWN_ENTITY, Entity1_19_4Types.FALLING_BLOCK);
        registerRemoveEntities(ClientboundPackets1_19_3.REMOVE_ENTITIES);
        registerMetadataRewriter(ClientboundPackets1_19_3.ENTITY_METADATA, Types1_19_3.METADATA_LIST, Types1_19_4.METADATA_LIST);
    }

    private int damageTypeFromEntityEvent(byte entityEvent) {
        switch (entityEvent) {
            case 33: // Thorned
                return 36;
            case 36: // Drowned
                return 5;
            case 37: // Burned
                return 27;
            case 57: // Frozen
                return 15;
            case 44: // Poked
            case 2: // Generic hurt
                return 16;
        }
        return -1;
    }

    @Override
    protected void registerRewrites() {
        filter().handler((event, meta) -> {
            int id = meta.metaType().typeId();
            if (id >= 14) { // Optional block state (and map block state=14 to optional block state)
                id++;
            }
            meta.setMetaType(Types1_19_4.META_TYPES.byId(id));
        });
        registerMetaTypeHandler(Types1_19_4.META_TYPES.itemType, Types1_19_4.META_TYPES.blockStateType, Types1_19_4.META_TYPES.particleType);

        filter().filterFamily(Entity1_19_4Types.MINECART_ABSTRACT).index(11).handler((event, meta) -> {
            final int blockState = meta.value();
            meta.setValue(protocol.getMappingData().getNewBlockStateId(blockState));
        });

        filter().filterFamily(Entity1_19_4Types.BOAT).index(11).handler((event, meta) -> {
            final int boatType = meta.value();
            if (boatType > 4) { // Cherry added
                meta.setValue(boatType + 1);
            }
        });

        filter().filterFamily(Entity1_19_4Types.ABSTRACT_HORSE).removeIndex(18); // Owner UUID
    }

    @Override
    public void onMappingDataLoaded() {
        mapTypes();
    }

    @Override
    public EntityType typeFromId(final int type) {
        return Entity1_19_4Types.getTypeFromId(type);
    }
}