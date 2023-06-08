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
package com.viaversion.viaversion.protocols.protocol1_20to1_19_4.packets;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.FloatTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.api.data.entity.TrackedEntity;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_19_4Types;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_19_4;
import com.viaversion.viaversion.api.type.types.version.Types1_20;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.protocol1_20to1_19_4.Protocol1_20To1_19_4;
import com.viaversion.viaversion.rewriter.EntityRewriter;

public final class EntityPackets extends EntityRewriter<ClientboundPackets1_19_4, Protocol1_20To1_19_4> {

    public EntityPackets(final Protocol1_20To1_19_4 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        registerTrackerWithData1_20(ClientboundPackets1_19_4.SPAWN_ENTITY, Entity1_19_4Types.FALLING_BLOCK, Entity1_19_4Types.ITEM_DISPLAY);
        registerMetadataRewriter(ClientboundPackets1_19_4.ENTITY_METADATA, Types1_19_4.METADATA_LIST, Types1_20.METADATA_LIST);
        registerRemoveEntities(ClientboundPackets1_19_4.REMOVE_ENTITIES);

        protocol.registerClientbound(ClientboundPackets1_19_4.ENTITY_POSITION_AND_ROTATION, new PacketHandlers() {
            @Override
            protected void register() {
                map(Type.VAR_INT); // Entity id
                map(Type.SHORT);   // Delta X
                map(Type.SHORT);   // Delta Y
                map(Type.SHORT);   // Delta Z
                map(Type.BYTE);    // Yaw
                map(Type.BYTE);    // Pitch
                map(Type.BOOLEAN); // On Ground

                handler(wrapper -> {
                    TrackedEntity trackedEntity = tracker(wrapper.user()).entity(wrapper.get(Type.VAR_INT, 0));
                    if (trackedEntity == null) return;
                    if (trackedEntity.entityType() != Entity1_19_4Types.ITEM_DISPLAY) return;
                    wrapper.set(Type.BYTE, 0, (byte) (wrapper.get(Type.BYTE, 0) - 128));
                    wrapper.set(Type.BYTE, 1, (byte)-wrapper.get(Type.BYTE, 1));
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_4.ENTITY_ROTATION, new PacketHandlers() {
            @Override
            protected void register() {
                map(Type.VAR_INT); // Entity id
                map(Type.BYTE);    // Yaw
                map(Type.BYTE);    // Pitch
                map(Type.BOOLEAN); // On Ground

                handler(wrapper -> {
                    TrackedEntity trackedEntity = tracker(wrapper.user()).entity(wrapper.get(Type.VAR_INT, 0));
                    if (trackedEntity == null) return;
                    if (trackedEntity.entityType() != Entity1_19_4Types.ITEM_DISPLAY) return;
                    wrapper.set(Type.BYTE, 0, (byte) (wrapper.get(Type.BYTE, 0) - 128));
                    wrapper.set(Type.BYTE, 1, (byte)-wrapper.get(Type.BYTE, 1));
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_4.ENTITY_HEAD_LOOK, new PacketHandlers() {
            @Override
            protected void register() {
                map(Type.VAR_INT); // Entity id
                map(Type.BYTE);    // Head Yaw

                handler(wrapper -> {
                    TrackedEntity trackedEntity = tracker(wrapper.user()).entity(wrapper.get(Type.VAR_INT, 0));
                    if (trackedEntity == null) return;
                    if (trackedEntity.entityType() != Entity1_19_4Types.ITEM_DISPLAY) return;
                    wrapper.set(Type.BYTE, 0, (byte) (wrapper.get(Type.BYTE, 0) - 128));
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_4.ENTITY_TELEPORT, new PacketHandlers() {
            @Override
            protected void register() {
                map(Type.VAR_INT); // Entity id
                map(Type.DOUBLE);  // X
                map(Type.DOUBLE);  // Y
                map(Type.DOUBLE);  // Z
                map(Type.BYTE);    // Yaw
                map(Type.BYTE);    // Pitch
                map(Type.BOOLEAN); // On Ground

                handler(wrapper -> {
                    TrackedEntity trackedEntity = tracker(wrapper.user()).entity(wrapper.get(Type.VAR_INT, 0));
                    if (trackedEntity == null) return;
                    if (trackedEntity.entityType() != Entity1_19_4Types.ITEM_DISPLAY) return;
                    wrapper.set(Type.BYTE, 0, (byte) (wrapper.get(Type.BYTE, 0) - 128));
                    wrapper.set(Type.BYTE, 1, (byte)-wrapper.get(Type.BYTE, 1));
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_4.JOIN_GAME, new PacketHandlers() {
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
                map(Type.LONG); // Seed
                map(Type.VAR_INT); // Max players
                map(Type.VAR_INT); // Chunk radius
                map(Type.VAR_INT); // Simulation distance
                map(Type.BOOLEAN); // Reduced debug info
                map(Type.BOOLEAN); // Show death screen
                map(Type.BOOLEAN); // Debug
                map(Type.BOOLEAN); // Flat
                map(Type.OPTIONAL_GLOBAL_POSITION); // Last death location
                create(Type.VAR_INT, 0); // Portal cooldown

                handler(dimensionDataHandler()); // Caches dimensions to access data like height later
                handler(biomeSizeTracker()); // Tracks the amount of biomes sent for chunk data
                handler(worldDataTrackerHandlerByKey()); // Tracks world height and name for chunk data and entity (un)tracking
                handler(wrapper -> {
                    final CompoundTag registry = wrapper.get(Type.NBT, 0);
                    final CompoundTag damageTypeRegistry = registry.get("minecraft:damage_type");
                    final ListTag damageTypes = damageTypeRegistry.get("value");
                    int highestId = -1;
                    for (final Tag damageType : damageTypes) {
                        final IntTag id = ((CompoundTag) damageType).get("id");
                        highestId = Math.max(highestId, id.asInt());
                    }

                    // AaaaAAAaa
                    final CompoundTag outsideBorderReason = new CompoundTag();
                    final CompoundTag outsideBorderElement = new CompoundTag();
                    outsideBorderElement.put("scaling", new StringTag("always"));
                    outsideBorderElement.put("exhaustion", new FloatTag(0F));
                    outsideBorderElement.put("message_id", new StringTag("badRespawnPoint"));
                    outsideBorderReason.put("id", new IntTag(highestId + 1));
                    outsideBorderReason.put("name", new StringTag("minecraft:outside_border"));
                    outsideBorderReason.put("element", outsideBorderElement);
                    damageTypes.add(outsideBorderReason);

                    final CompoundTag genericKillReason = new CompoundTag();
                    final CompoundTag genericKillElement = new CompoundTag();
                    genericKillElement.put("scaling", new StringTag("always"));
                    genericKillElement.put("exhaustion", new FloatTag(0F));
                    genericKillElement.put("message_id", new StringTag("badRespawnPoint"));
                    genericKillReason.put("id", new IntTag(highestId + 2));
                    genericKillReason.put("name", new StringTag("minecraft:generic_kill"));
                    genericKillReason.put("element", genericKillElement);
                    damageTypes.add(genericKillReason);
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_4.RESPAWN, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // Dimension
                map(Type.STRING); // World
                map(Type.LONG); // Seed
                map(Type.UNSIGNED_BYTE); // Gamemode
                map(Type.BYTE); // Previous gamemode
                map(Type.BOOLEAN); // Debug
                map(Type.BOOLEAN); // Flat
                map(Type.BYTE); // Data to keep
                map(Type.OPTIONAL_GLOBAL_POSITION); // Last death location
                create(Type.VAR_INT, 0); // Portal cooldown
                handler(worldDataTrackerHandlerByKey()); // Tracks world height and name for chunk data and entity (un)tracking
            }
        });
    }

    @Override
    protected void registerRewrites() {
        filter().handler((event, meta) -> meta.setMetaType(Types1_20.META_TYPES.byId(meta.metaType().typeId())));
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