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
package com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.rewriter;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.api.data.entity.DimensionData;
import com.viaversion.viaversion.api.minecraft.RegistryEntry;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_20_5;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_20_3;
import com.viaversion.viaversion.api.type.types.version.Types1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundConfigurationPackets1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundPacket1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundPackets1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.Protocol1_20_5To1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.data.Attributes1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.packet.ClientboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.util.Key;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public final class EntityPacketRewriter1_20_5 extends EntityRewriter<ClientboundPacket1_20_3, Protocol1_20_5To1_20_3> {

    public EntityPacketRewriter1_20_5(final Protocol1_20_5To1_20_3 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        registerTrackerWithData1_19(ClientboundPackets1_20_3.SPAWN_ENTITY, EntityTypes1_20_5.FALLING_BLOCK);
        registerMetadataRewriter(ClientboundPackets1_20_3.ENTITY_METADATA, Types1_20_3.METADATA_LIST, Types1_20_5.METADATA_LIST);
        registerRemoveEntities(ClientboundPackets1_20_3.REMOVE_ENTITIES);

        protocol.registerClientbound(ClientboundConfigurationPackets1_20_3.REGISTRY_DATA, wrapper -> {
            final PacketWrapper knownPacksPacket = wrapper.create(ClientboundConfigurationPackets1_20_5.SELECT_KNOWN_PACKS);
            knownPacksPacket.write(Type.VAR_INT, 0); // No known packs, everything is sent here
            knownPacksPacket.send(Protocol1_20_5To1_20_3.class);

            final CompoundTag registryData = wrapper.read(Type.COMPOUND_TAG);
            cacheDimensionData(wrapper.user(), registryData);
            trackBiomeSize(wrapper.user(), registryData);

            for (final Map.Entry<String, Tag> entry : registryData.entrySet()) {
                final CompoundTag entryTag = (CompoundTag) entry.getValue();
                final StringTag typeTag = entryTag.getStringTag("type");
                final ListTag<CompoundTag> valueTag = entryTag.getListTag("value", CompoundTag.class);
                RegistryEntry[] registryEntries = new RegistryEntry[valueTag.size()];
                boolean requiresDummyValues = false;
                int entriesLength = registryEntries.length;
                for (final CompoundTag tag : valueTag) {
                    final StringTag nameTag = tag.getStringTag("name");
                    final int id = tag.getNumberTag("id").asInt();
                    entriesLength = Math.max(entriesLength, id + 1);
                    if (id >= registryEntries.length) {
                        // It was previously possible to have arbitrary ids
                        registryEntries = Arrays.copyOf(registryEntries, Math.max(registryEntries.length * 2, id + 1));
                        requiresDummyValues = true;
                    }

                    registryEntries[id] = new RegistryEntry(nameTag.getValue(), tag.get("element"));
                }

                if (requiresDummyValues) {
                    // Truncate and replace null values
                    if (registryEntries.length != entriesLength) {
                        registryEntries = Arrays.copyOf(registryEntries, entriesLength);
                    }
                    replaceNullValues(registryEntries);
                }

                final PacketWrapper registryPacket = wrapper.create(ClientboundConfigurationPackets1_20_5.REGISTRY_DATA);
                registryPacket.write(Type.STRING, typeTag.getValue());
                registryPacket.write(Type.REGISTRY_ENTRY_ARRAY, registryEntries);
                registryPacket.send(Protocol1_20_5To1_20_3.class);
            }

            wrapper.cancel();
        });

        protocol.registerClientbound(ClientboundPackets1_20_3.JOIN_GAME, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); // Entity ID
                map(Type.BOOLEAN); // Hardcore
                map(Type.STRING_ARRAY); // World List
                map(Type.VAR_INT); // Max players
                map(Type.VAR_INT); // View distance
                map(Type.VAR_INT); // Simulation distance
                map(Type.BOOLEAN); // Reduced debug info
                map(Type.BOOLEAN); // Show death screen
                map(Type.BOOLEAN); // Limited crafting
                handler(wrapper -> {
                    final String dimensionKey = wrapper.read(Type.STRING);
                    final DimensionData data = tracker(wrapper.user()).dimensionData(dimensionKey);
                    wrapper.write(Type.VAR_INT, data.id());
                });
                map(Type.STRING); // World
                map(Type.LONG); // Seed
                map(Type.BYTE); // Gamemode
                map(Type.BYTE); // Previous gamemode
                map(Type.BOOLEAN); // Debug
                map(Type.BOOLEAN); // Flat
                map(Type.OPTIONAL_GLOBAL_POSITION); // Last death location
                map(Type.VAR_INT); // Portal cooldown
                create(Type.BOOLEAN, false); // Enforces secure chat - moved from server data (which is unfortunately sent a while after this)
                handler(worldDataTrackerHandlerByKey1_20_5(3)); // Tracks world height and name for chunk data and entity (un)tracking
            }
        });

        protocol.registerClientbound(ClientboundPackets1_20_3.RESPAWN, wrapper -> {
            final String dimensionKey = wrapper.read(Type.STRING);
            final DimensionData data = tracker(wrapper.user()).dimensionData(dimensionKey);
            wrapper.write(Type.VAR_INT, data.id());
            wrapper.passthrough(Type.STRING); // World
            worldDataTrackerHandlerByKey1_20_5(0).handle(wrapper); // Tracks world height and name for chunk data and entity (un)tracking
        });

        protocol.registerClientbound(ClientboundPackets1_20_3.ENTITY_EFFECT, wrapper -> {
            wrapper.passthrough(Type.VAR_INT); // Entity ID
            wrapper.passthrough(Type.VAR_INT); // Effect ID

            final byte amplifier = wrapper.read(Type.BYTE);
            wrapper.write(Type.VAR_INT, (int) amplifier);

            wrapper.passthrough(Type.VAR_INT); // Duration
            wrapper.passthrough(Type.BYTE); // Flags
            wrapper.read(Type.OPTIONAL_COMPOUND_TAG); // Remove factor data
        });

        protocol.registerClientbound(ClientboundPackets1_20_3.ENTITY_PROPERTIES, wrapper -> {
            wrapper.passthrough(Type.VAR_INT); // Entity ID

            final int size = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < size; i++) {
                // From a string to a registry int ID
                final String attributeIdentifier = Key.stripMinecraftNamespace(wrapper.read(Type.STRING));
                final int id = Attributes1_20_3.keyToId(attributeIdentifier);
                wrapper.write(Type.VAR_INT, protocol.getMappingData().getNewAttributeId(id));

                wrapper.passthrough(Type.DOUBLE); // Base
                final int modifierSize = wrapper.passthrough(Type.VAR_INT);
                for (int j = 0; j < modifierSize; j++) {
                    wrapper.passthrough(Type.UUID); // ID
                    wrapper.passthrough(Type.DOUBLE); // Amount
                    wrapper.passthrough(Type.BYTE); // Operation
                }
            }
        });
    }

    private void replaceNullValues(final RegistryEntry[] entries) {
        // Find the first non-null entry and fill the array with dummy values where needed (which is easier than remapping them to different ids in a new, smaller array)
        RegistryEntry first = null;
        for (final RegistryEntry registryEntry : entries) {
            if (registryEntry != null) {
                first = registryEntry;
                break;
            }
        }

        for (int i = 0; i < entries.length; i++) {
            if (entries[i] == null) {
                entries[i] = first.withKey(UUID.randomUUID().toString());
            }
        }
    }

    @Override
    protected void registerRewrites() {
        filter().mapMetaType(typeId -> {
            int id = typeId;
            if (typeId >= Types1_20_5.META_TYPES.armadilloState.typeId()) {
                id++;
            }
            if (typeId >= Types1_20_5.META_TYPES.wolfVariantType.typeId()) {
                id++;
            }
            return Types1_20_5.META_TYPES.byId(id);
        });

        registerMetaTypeHandler(
                Types1_20_5.META_TYPES.itemType,
                Types1_20_5.META_TYPES.blockStateType,
                Types1_20_5.META_TYPES.optionalBlockStateType,
                Types1_20_5.META_TYPES.particleType
        );

        filter().type(EntityTypes1_20_5.LLAMA).removeIndex(20); // Carpet color

        filter().type(EntityTypes1_20_5.MINECART_ABSTRACT).index(11).handler((event, meta) -> {
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
        return EntityTypes1_20_5.getTypeFromId(type);
    }
}