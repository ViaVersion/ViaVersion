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
package com.viaversion.viaversion.protocols.protocol1_20to1_19_4.packets;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.FloatTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.api.minecraft.Quaternion;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_19_4;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_19_4;
import com.viaversion.viaversion.api.type.types.version.Types1_20;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.protocol1_20to1_19_4.Protocol1_20To1_19_4;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.util.TagUtil;

public final class EntityPackets extends EntityRewriter<ClientboundPackets1_19_4, Protocol1_20To1_19_4> {

    private static final Quaternion Y_FLIPPED_ROTATION = new Quaternion(0, 1, 0, 0);

    public EntityPackets(final Protocol1_20To1_19_4 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        registerTrackerWithData1_19(ClientboundPackets1_19_4.SPAWN_ENTITY, EntityTypes1_19_4.FALLING_BLOCK);
        registerMetadataRewriter(ClientboundPackets1_19_4.ENTITY_METADATA, Types1_19_4.METADATA_LIST, Types1_20.METADATA_LIST);
        registerRemoveEntities(ClientboundPackets1_19_4.REMOVE_ENTITIES);

        protocol.registerClientbound(ClientboundPackets1_19_4.JOIN_GAME, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); // Entity id
                map(Type.BOOLEAN); // Hardcore
                map(Type.BYTE); // Gamemode
                map(Type.BYTE); // Previous Gamemode
                map(Type.STRING_ARRAY); // World List
                map(Type.NAMED_COMPOUND_TAG); // Dimension registry
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
                    final CompoundTag registry = wrapper.get(Type.NAMED_COMPOUND_TAG, 0);
                    final ListTag<CompoundTag> damageTypes = TagUtil.getRegistryEntries(registry, "damage_type");
                    int highestId = -1;
                    for (final CompoundTag damageType : damageTypes) {
                        final IntTag id = damageType.getUnchecked("id");
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
        filter().mapMetaType(Types1_20.META_TYPES::byId);
        registerMetaTypeHandler(Types1_20.META_TYPES.itemType, Types1_20.META_TYPES.blockStateType, Types1_20.META_TYPES.optionalBlockStateType, Types1_20.META_TYPES.particleType, null);

        // Rotate item display by 180 degrees around the Y axis
        filter().type(EntityTypes1_19_4.ITEM_DISPLAY).handler((event, meta) -> {
            if (event.trackedEntity().hasSentMetadata() || event.hasExtraMeta()) {
                return;
            }

            if (event.metaAtIndex(12) == null) {
                event.createExtraMeta(new Metadata(12, Types1_20.META_TYPES.quaternionType, Y_FLIPPED_ROTATION));
            }
        });
        filter().type(EntityTypes1_19_4.ITEM_DISPLAY).index(12).handler((event, meta) -> {
            final Quaternion quaternion = meta.value();
            meta.setValue(rotateY180(quaternion));
        });

        filter().type(EntityTypes1_19_4.MINECART_ABSTRACT).index(11).handler((event, meta) -> {
            final int blockState = meta.value();
            meta.setValue(protocol.getMappingData().getNewBlockStateId(blockState));
        });
    }

    @Override
    public EntityType typeFromId(final int type) {
        return EntityTypes1_19_4.getTypeFromId(type);
    }

    private Quaternion rotateY180(final Quaternion quaternion) {
        return new Quaternion(-quaternion.z(), quaternion.w(), quaternion.x(), -quaternion.y());
    }
}
