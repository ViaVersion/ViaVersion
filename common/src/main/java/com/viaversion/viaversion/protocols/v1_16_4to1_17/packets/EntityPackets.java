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
package com.viaversion.viaversion.protocols.v1_16_4to1_17.packets;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_17;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_16;
import com.viaversion.viaversion.api.type.types.version.Types1_17;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.packet.ClientboundPackets1_16_2;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.Protocol1_16_4To1_17;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ClientboundPackets1_17;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.util.TagUtil;

public final class EntityPackets extends EntityRewriter<ClientboundPackets1_16_2, Protocol1_16_4To1_17> {

    public EntityPackets(Protocol1_16_4To1_17 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        registerTrackerWithData(ClientboundPackets1_16_2.ADD_ENTITY, EntityTypes1_17.FALLING_BLOCK);
        registerTracker(ClientboundPackets1_16_2.ADD_MOB);
        registerTracker(ClientboundPackets1_16_2.ADD_PLAYER, EntityTypes1_17.PLAYER);
        registerMetadataRewriter(ClientboundPackets1_16_2.SET_ENTITY_DATA, Types1_16.METADATA_LIST, Types1_17.METADATA_LIST);

        protocol.registerClientbound(ClientboundPackets1_16_2.REMOVE_ENTITIES, null, wrapper -> {
            int[] entityIds = wrapper.read(Type.VAR_INT_ARRAY_PRIMITIVE);
            wrapper.cancel();

            EntityTracker entityTracker = wrapper.user().getEntityTracker(Protocol1_16_4To1_17.class);
            for (int entityId : entityIds) {
                entityTracker.removeEntity(entityId);

                // Send individual remove packets
                PacketWrapper newPacket = wrapper.create(ClientboundPackets1_17.REMOVE_ENTITY);
                newPacket.write(Type.VAR_INT, entityId);
                newPacket.send(Protocol1_16_4To1_17.class);
            }
        });

        protocol.registerClientbound(ClientboundPackets1_16_2.LOGIN, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); // Entity ID
                map(Type.BOOLEAN); // Hardcore
                map(Type.BYTE); // Gamemode
                map(Type.BYTE); // Previous Gamemode
                map(Type.STRING_ARRAY); // World List
                map(Type.NAMED_COMPOUND_TAG); // Registry
                map(Type.NAMED_COMPOUND_TAG); // Current dimension
                handler(wrapper -> {
                    // Add new dimension fields
                    CompoundTag registry = wrapper.get(Type.NAMED_COMPOUND_TAG, 0);
                    ListTag<CompoundTag> dimensions = TagUtil.getRegistryEntries(registry, "dimension_type");
                    for (CompoundTag dimension : dimensions) {
                        CompoundTag dimensionCompound = dimension.getCompoundTag("element");
                        addNewDimensionData(dimensionCompound);
                    }

                    CompoundTag currentDimensionTag = wrapper.get(Type.NAMED_COMPOUND_TAG, 1);
                    addNewDimensionData(currentDimensionTag);
                });
                handler(playerTrackerHandler());
            }
        });

        protocol.registerClientbound(ClientboundPackets1_16_2.RESPAWN, wrapper -> {
            CompoundTag dimensionData = wrapper.passthrough(Type.NAMED_COMPOUND_TAG);
            addNewDimensionData(dimensionData);
        });

        protocol.registerClientbound(ClientboundPackets1_16_2.UPDATE_ATTRIBUTES, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // Entity id
                handler(wrapper -> wrapper.write(Type.VAR_INT, wrapper.read(Type.INT))); // Collection length is now a var int
            }
        });

        protocol.registerClientbound(ClientboundPackets1_16_2.PLAYER_POSITION, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.DOUBLE);
                map(Type.DOUBLE);
                map(Type.DOUBLE);
                map(Type.FLOAT);
                map(Type.FLOAT);
                map(Type.BYTE);
                map(Type.VAR_INT);
                create(Type.BOOLEAN, false); // Dismount vehicle
            }
        });

        protocol.registerClientbound(ClientboundPackets1_16_2.PLAYER_COMBAT, null, wrapper -> {
            // Combat packet actions have been split into individual packets (the content hasn't changed)
            int type = wrapper.read(Type.VAR_INT);
            ClientboundPacketType packetType = switch (type) {
                case 0 -> ClientboundPackets1_17.PLAYER_COMBAT_ENTER;
                case 1 -> ClientboundPackets1_17.PLAYER_COMBAT_END;
                case 2 -> ClientboundPackets1_17.PLAYER_COMBAT_KILL;
                default -> throw new IllegalArgumentException("Invalid combat type received: " + type);
            };

            wrapper.setPacketType(packetType);
        });

        // The parent class of the other entity move packets that is never actually used has finally been removed from the id list
        protocol.cancelClientbound(ClientboundPackets1_16_2.MOVE_ENTITY);
    }

    @Override
    protected void registerRewrites() {
        filter().mapMetaType(Types1_17.META_TYPES::byId);
        filter().metaType(Types1_17.META_TYPES.poseType).handler((event, meta) -> {
            int pose = meta.value();
            if (pose > 5) {
                // Added LONG_JUMP at 6
                meta.setValue(pose + 1);
            }
        });
        registerMetaTypeHandler(Types1_17.META_TYPES.itemType, Types1_17.META_TYPES.blockStateType, Types1_17.META_TYPES.particleType);

        // Ticks frozen added with id 7
        filter().type(EntityTypes1_17.ENTITY).addIndex(7);

        filter().type(EntityTypes1_17.ABSTRACT_MINECART).index(11).handler((event, meta) -> {
            // Convert to new block id
            int data = (int) meta.getValue();
            meta.setValue(protocol.getMappingData().getNewBlockStateId(data));
        });

        // Attachment position removed
        filter().type(EntityTypes1_17.SHULKER).removeIndex(17);
    }

    @Override
    public void onMappingDataLoaded() {
        mapTypes();
    }

    @Override
    public EntityType typeFromId(int type) {
        return EntityTypes1_17.getTypeFromId(type);
    }

    private static void addNewDimensionData(CompoundTag tag) {
        tag.put("min_y", new IntTag(0));
        tag.put("height", new IntTag(256));
    }
}
