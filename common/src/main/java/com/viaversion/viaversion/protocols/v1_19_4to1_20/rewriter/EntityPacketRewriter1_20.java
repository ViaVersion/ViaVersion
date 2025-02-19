/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_19_4to1_20.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.FloatTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.minecraft.Quaternion;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_19_4;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_19_4;
import com.viaversion.viaversion.api.type.types.version.Types1_20;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.v1_19_4to1_20.Protocol1_19_4To1_20;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.TagUtil;

public final class EntityPacketRewriter1_20 extends EntityRewriter<ClientboundPackets1_19_4, Protocol1_19_4To1_20> {

    private static final Quaternion Y_FLIPPED_ROTATION = new Quaternion(0, 1, 0, 0);

    public EntityPacketRewriter1_20(final Protocol1_19_4To1_20 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        registerTrackerWithData1_19(ClientboundPackets1_19_4.ADD_ENTITY, EntityTypes1_19_4.FALLING_BLOCK);
        registerSetEntityData(ClientboundPackets1_19_4.SET_ENTITY_DATA, Types1_19_4.ENTITY_DATA_LIST, Types1_20.ENTITY_DATA_LIST);
        registerRemoveEntities(ClientboundPackets1_19_4.REMOVE_ENTITIES);

        protocol.registerClientbound(ClientboundPackets1_19_4.LOGIN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); // Entity id
                map(Types.BOOLEAN); // Hardcore
                map(Types.BYTE); // Gamemode
                map(Types.BYTE); // Previous Gamemode
                map(Types.STRING_ARRAY); // World List
                map(Types.NAMED_COMPOUND_TAG); // Dimension registry
                map(Types.STRING); // Dimension key
                map(Types.STRING); // World
                map(Types.LONG); // Seed
                map(Types.VAR_INT); // Max players
                map(Types.VAR_INT); // Chunk radius
                map(Types.VAR_INT); // Simulation distance
                map(Types.BOOLEAN); // Reduced debug info
                map(Types.BOOLEAN); // Show death screen
                map(Types.BOOLEAN); // Debug
                map(Types.BOOLEAN); // Flat
                map(Types.OPTIONAL_GLOBAL_POSITION); // Last death location
                create(Types.VAR_INT, 0); // Portal cooldown

                handler(dimensionDataHandler()); // Caches dimensions to access data like height later
                handler(biomeSizeTracker()); // Tracks the amount of biomes sent for chunk data
                handler(worldDataTrackerHandlerByKey()); // Tracks world height and name for chunk data and entity (un)tracking
                handler(wrapper -> {
                    final CompoundTag registry = wrapper.get(Types.NAMED_COMPOUND_TAG, 0);
                    final ListTag<CompoundTag> damageTypes = TagUtil.getRegistryEntries(registry, "damage_type");
                    int highestId = -1;
                    boolean hasOutsideBorder = false;
                    boolean hasGenericKill = false;
                    for (final CompoundTag damageType : damageTypes) {
                        final int id = damageType.getInt("id");
                        highestId = Math.max(highestId, id);
                        final String name = Key.namespaced(damageType.getString("name"));
                        if (name.equals("minecraft:outside_border")) {
                            hasOutsideBorder = true;
                        } else if (name.equals("minecraft:generic_kill")) {
                            hasGenericKill = true;
                        }
                    }

                    // AaaaAAAaa
                    if (!hasOutsideBorder) {
                        final CompoundTag outsideBorderReason = new CompoundTag();
                        final CompoundTag outsideBorderElement = new CompoundTag();
                        outsideBorderElement.put("scaling", new StringTag("always"));
                        outsideBorderElement.put("exhaustion", new FloatTag(0F));
                        outsideBorderElement.put("message_id", new StringTag("badRespawnPoint"));
                        outsideBorderReason.put("id", new IntTag(highestId + 1));
                        outsideBorderReason.put("name", new StringTag("minecraft:outside_border"));
                        outsideBorderReason.put("element", outsideBorderElement);
                        damageTypes.add(outsideBorderReason);
                    }

                    if (!hasGenericKill) {
                        final CompoundTag genericKillReason = new CompoundTag();
                        final CompoundTag genericKillElement = new CompoundTag();
                        genericKillElement.put("scaling", new StringTag("always"));
                        genericKillElement.put("exhaustion", new FloatTag(0F));
                        genericKillElement.put("message_id", new StringTag("badRespawnPoint"));
                        genericKillReason.put("id", new IntTag(highestId + 2));
                        genericKillReason.put("name", new StringTag("minecraft:generic_kill"));
                        genericKillReason.put("element", genericKillElement);
                        damageTypes.add(genericKillReason);
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_4.RESPAWN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); // Dimension
                map(Types.STRING); // World
                map(Types.LONG); // Seed
                map(Types.UNSIGNED_BYTE); // Gamemode
                map(Types.BYTE); // Previous gamemode
                map(Types.BOOLEAN); // Debug
                map(Types.BOOLEAN); // Flat
                map(Types.BYTE); // Data to keep
                map(Types.OPTIONAL_GLOBAL_POSITION); // Last death location
                create(Types.VAR_INT, 0); // Portal cooldown
                handler(worldDataTrackerHandlerByKey()); // Tracks world height and name for chunk data and entity (un)tracking
            }
        });
    }

    @Override
    protected void registerRewrites() {
        filter().mapDataType(Types1_20.ENTITY_DATA_TYPES::byId);
        registerEntityDataTypeHandler(Types1_20.ENTITY_DATA_TYPES.itemType, Types1_20.ENTITY_DATA_TYPES.blockStateType, Types1_20.ENTITY_DATA_TYPES.optionalBlockStateType, Types1_20.ENTITY_DATA_TYPES.particleType, null);
        registerBlockStateHandler(EntityTypes1_19_4.ABSTRACT_MINECART, 11);

        // Rotate item display by 180 degrees around the Y axis
        filter().type(EntityTypes1_19_4.ITEM_DISPLAY).handler((event, data) -> {
            if (event.trackedEntity().hasSentEntityData() || event.hasExtraData()) {
                return;
            }

            if (event.dataAtIndex(12) == null) {
                event.createExtraData(new EntityData(12, Types1_20.ENTITY_DATA_TYPES.quaternionType, Y_FLIPPED_ROTATION));
            }
        });
        filter().type(EntityTypes1_19_4.ITEM_DISPLAY).index(12).handler((event, data) -> {
            final Quaternion quaternion = data.value();
            data.setValue(rotateY180(quaternion));
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
