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
package com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.rewriter;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.DimensionData;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.RegistryEntry;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_20_5;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_20_3;
import com.viaversion.viaversion.api.type.types.version.Types1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundConfigurationPackets1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundPacket1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundPackets1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.Protocol1_20_5To1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.data.Attributes1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.data.BannerPatterns1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.packet.ClientboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.packet.ClientboundPackets1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.storage.AcknowledgedMessagesStorage;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.TagUtil;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class EntityPacketRewriter1_20_5 extends EntityRewriter<ClientboundPacket1_20_3, Protocol1_20_5To1_20_3> {

    private static final UUID CREATIVE_BLOCK_INTERACTION_RANGE = UUID.fromString("736565d2-e1a7-403d-a3f8-1aeb3e302542");
    private static final UUID CREATIVE_ENTITY_INTERACTION_RANGE = UUID.fromString("98491ef6-97b1-4584-ae82-71a8cc85cf73");
    private static final int CREATIVE_MODE_ID = 1;

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

            // Update format of height provider
            final ListTag<CompoundTag> dimensionTypes = TagUtil.getRegistryEntries(registryData, "dimension_type");
            for (final CompoundTag dimensionType : dimensionTypes) {
                final CompoundTag elementTag = dimensionType.getCompoundTag("element");
                final CompoundTag monsterSpawnLightLevel = elementTag.getCompoundTag("monster_spawn_light_level");
                if (monsterSpawnLightLevel != null) {
                    final CompoundTag value = monsterSpawnLightLevel.removeUnchecked("value");
                    monsterSpawnLightLevel.putInt("min_inclusive", value.getInt("min_inclusive"));
                    monsterSpawnLightLevel.putInt("max_inclusive", value.getInt("max_inclusive"));
                }
            }

            // Changes in biomes
            final ListTag<CompoundTag> biomes = TagUtil.getRegistryEntries(registryData, "worldgen/biome");
            for (final CompoundTag biome : biomes) {
                final CompoundTag effects = biome.getCompoundTag("element").getCompoundTag("effects");

                // Fixup sound ids that are now hard checked against the registry
                checkSoundTag(effects.getCompoundTag("mood_sound"), "sound");
                checkSoundTag(effects.getCompoundTag("additions_sound"), "sound");
                checkSoundTag(effects.getCompoundTag("music"), "sound");
                checkSoundTag(effects, "ambient_sound");

                // Particle format changes
                final CompoundTag particle = effects.getCompoundTag("particle");
                if (particle != null) {
                    final CompoundTag particleOptions = particle.getCompoundTag("options");
                    final String particleType = particleOptions.getString("type");
                    updateParticleFormat(particleOptions, Key.stripMinecraftNamespace(particleType));
                }
            }

            for (final Map.Entry<String, Tag> entry : registryData.entrySet()) {
                final CompoundTag entryTag = (CompoundTag) entry.getValue();
                final String type = entryTag.getString("type");
                final ListTag<CompoundTag> valueTag = entryTag.getListTag("value", CompoundTag.class);
                RegistryEntry[] registryEntries = new RegistryEntry[valueTag.size()];
                boolean requiresDummyValues = false;
                int entriesLength = registryEntries.length;
                for (final CompoundTag tag : valueTag) {
                    final String name = tag.getString("name");
                    final int id = tag.getInt("id");
                    entriesLength = Math.max(entriesLength, id + 1);
                    if (id >= registryEntries.length) {
                        // It was previously possible to have arbitrary ids
                        registryEntries = Arrays.copyOf(registryEntries, Math.max(registryEntries.length * 2, id + 1));
                        requiresDummyValues = true;
                    }

                    registryEntries[id] = new RegistryEntry(name, tag.get("element"));
                }

                // Add spit damage type
                if (Key.stripMinecraftNamespace(type).equals("damage_type")) {
                    final int length = registryEntries.length;
                    registryEntries = Arrays.copyOf(registryEntries, length + 1);
                    final CompoundTag spitData = new CompoundTag();
                    spitData.putString("scaling", "when_caused_by_living_non_player");
                    spitData.putString("message_id", "mob");
                    spitData.putFloat("exhaustion", 0.1F);
                    registryEntries[length] = new RegistryEntry("minecraft:spit", spitData);
                }

                if (requiresDummyValues) {
                    // Truncate and replace null values
                    if (registryEntries.length != entriesLength) {
                        registryEntries = Arrays.copyOf(registryEntries, entriesLength);
                    }
                    replaceNullValues(registryEntries);
                }

                final PacketWrapper registryPacket = wrapper.create(ClientboundConfigurationPackets1_20_5.REGISTRY_DATA);
                registryPacket.write(Type.STRING, type);
                registryPacket.write(Type.REGISTRY_ENTRY_ARRAY, registryEntries);
                registryPacket.send(Protocol1_20_5To1_20_3.class);
            }

            wrapper.cancel();

            // Send banner patterns and default wolf variant
            final PacketWrapper wolfVariantsPacket = wrapper.create(ClientboundConfigurationPackets1_20_5.REGISTRY_DATA);
            wolfVariantsPacket.write(Type.STRING, "minecraft:wolf_variant");
            final CompoundTag paleWolf = new CompoundTag();
            paleWolf.putString("wild_texture", "entity/wolf/wolf");
            paleWolf.putString("tame_texture", "entity/wolf/wolf_tame");
            paleWolf.putString("angry_texture", "entity/wolf/wolf_angry");
            paleWolf.put("biomes", new ListTag<>(StringTag.class));
            wolfVariantsPacket.write(Type.REGISTRY_ENTRY_ARRAY, new RegistryEntry[]{new RegistryEntry("minecraft:pale", paleWolf)});
            wolfVariantsPacket.send(Protocol1_20_5To1_20_3.class);

            final PacketWrapper bannerPatternsPacket = wrapper.create(ClientboundConfigurationPackets1_20_5.REGISTRY_DATA);
            bannerPatternsPacket.write(Type.STRING, "minecraft:banner_pattern");
            final RegistryEntry[] patternEntries = new RegistryEntry[BannerPatterns1_20_5.keys().length];
            final String[] keys = BannerPatterns1_20_5.keys();
            for (int i = 0; i < keys.length; i++) {
                final CompoundTag pattern = new CompoundTag();
                final String key = keys[i];
                final String resourceLocation = "minecraft:" + key;
                pattern.putString("asset_id", key);
                pattern.putString("translation_key", "block.minecraft.banner." + key);
                patternEntries[i] = new RegistryEntry(resourceLocation, pattern);
            }
            bannerPatternsPacket.write(Type.REGISTRY_ENTRY_ARRAY, patternEntries);
            bannerPatternsPacket.send(Protocol1_20_5To1_20_3.class);
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
                handler(worldDataTrackerHandlerByKey1_20_5(3)); // Tracks world height and name for chunk data and entity (un)tracking
                handler(playerTrackerHandler());
                handler(wrapper -> {
                    // Enforces secure chat - moved from server data (which is unfortunately sent a while after this)
                    final AcknowledgedMessagesStorage storage = wrapper.user().get(AcknowledgedMessagesStorage.class);
                    if (storage.secureChatEnforced() != null) {
                        // Just put in what we know if this is sent multiple times
                        wrapper.write(Type.BOOLEAN, storage.isSecureChatEnforced());
                    } else {
                        wrapper.write(Type.BOOLEAN, Via.getConfig().enforceSecureChat());
                    }

                    storage.clear();

                    // Handle creative interaction range
                    final byte gamemode = wrapper.get(Type.BYTE, 0);
                    if (gamemode == CREATIVE_MODE_ID) {
                        sendRangeAttributes(wrapper.user(), true);
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_20_3.RESPAWN, wrapper -> {
            final String dimensionKey = wrapper.read(Type.STRING);
            final DimensionData data = tracker(wrapper.user()).dimensionData(dimensionKey);
            wrapper.write(Type.VAR_INT, data.id());

            wrapper.passthrough(Type.STRING); // World
            worldDataTrackerHandlerByKey1_20_5(0).handle(wrapper); // Tracks world height and name for chunk data and entity (un)tracking
            wrapper.passthrough(Type.LONG); // Seed

            final byte gamemode = wrapper.passthrough(Type.BYTE);
            sendRangeAttributes(wrapper.user(), gamemode == CREATIVE_MODE_ID);
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
                final String attributeIdentifier = wrapper.read(Type.STRING);
                final int mappedId = Attributes1_20_5.keyToId(attributeIdentifier);
                wrapper.write(Type.VAR_INT, mappedId != -1 ? mappedId : 0);

                wrapper.passthrough(Type.DOUBLE); // Base
                final int modifierSize = wrapper.passthrough(Type.VAR_INT);
                for (int j = 0; j < modifierSize; j++) {
                    wrapper.passthrough(Type.UUID); // ID
                    wrapper.passthrough(Type.DOUBLE); // Amount
                    wrapper.passthrough(Type.BYTE); // Operation
                }
            }
        });

        protocol.registerClientbound(ClientboundPackets1_20_3.GAME_EVENT, wrapper -> {
            // If the gamemode changed to/from creative, update the range attribute
            final short event = wrapper.passthrough(Type.UNSIGNED_BYTE);
            if (event != 3) {
                return;
            }

            // Resend attributes either with their original list or with the creative range modifier added
            final float value = wrapper.passthrough(Type.FLOAT);
            sendRangeAttributes(wrapper.user(), value == CREATIVE_MODE_ID);
        });
    }

    private void updateParticleFormat(final CompoundTag options, final String particleType) {
        if ("block".equals(particleType) || "block_marker".equals(particleType) || "falling_dust".equals(particleType) || "dust_pillar".equals(particleType)) {
            moveTag(options, "value", "block_state");
        } else if ("item".equals(particleType)) {
            moveTag(options, "value", "item");
        } else if ("dust_color_transition".equals(particleType)) {
            moveTag(options, "fromColor", "from_color");
            moveTag(options, "toColor", "to_color");
        } else if ("entity_effect".equals(particleType)) {
            moveTag(options, "value", "color");
        }
    }

    private void moveTag(final CompoundTag compoundTag, final String from, final String to) {
        final Tag tag = compoundTag.remove(from);
        if (tag != null) {
            compoundTag.put(to, tag);
        }
    }

    private void checkSoundTag(@Nullable final CompoundTag tag, final String key) {
        if (tag == null) {
            return;
        }

        final String sound = tag.getString(key);
        if (sound != null && protocol.getMappingData().soundId(sound) == -1) {
            // Write as direct value
            final CompoundTag directSoundValue = new CompoundTag();
            directSoundValue.putString("sound_id", sound);
            tag.put(key, directSoundValue);
        }
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

    private void sendRangeAttributes(final UserConnection connection, final boolean creativeMode) throws Exception {
        final PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_20_5.ENTITY_PROPERTIES, connection);
        wrapper.write(Type.VAR_INT, tracker(connection).clientEntityId());
        wrapper.write(Type.VAR_INT, 2); // Number of attributes
        writeAttribute(wrapper, "player.block_interaction_range", 4.5, creativeMode ? CREATIVE_BLOCK_INTERACTION_RANGE : null, 0.5);
        writeAttribute(wrapper, "player.entity_interaction_range", 3.0, creativeMode ? CREATIVE_ENTITY_INTERACTION_RANGE : null, 2.0);
        wrapper.scheduleSend(Protocol1_20_5To1_20_3.class);
    }

    private void writeAttribute(final PacketWrapper wrapper, final String attributeId, final double base, @Nullable final UUID modifierId, final double amount) {
        wrapper.write(Type.VAR_INT, Attributes1_20_5.keyToId(attributeId));
        wrapper.write(Type.DOUBLE, base);
        if (modifierId != null) {
            // Single modifier
            wrapper.write(Type.VAR_INT, 1);
            wrapper.write(Type.UUID, modifierId);
            wrapper.write(Type.DOUBLE, amount);
            wrapper.write(Type.BYTE, (byte) 0); // Add
        } else {
            // No modifiers
            wrapper.write(Type.VAR_INT, 0);
        }
    }

    @Override
    protected void registerRewrites() {
        filter().mapMetaType(typeId -> {
            int id = typeId;
            if (id >= Types1_20_5.META_TYPES.particlesType.typeId()) {
                id++;
            }
            if (id >= Types1_20_5.META_TYPES.wolfVariantType.typeId()) {
                id++;
            }
            if (id >= Types1_20_5.META_TYPES.armadilloState.typeId()) {
                id++;
            }
            return Types1_20_5.META_TYPES.byId(id);
        });

        registerMetaTypeHandler(
            Types1_20_5.META_TYPES.itemType,
            Types1_20_5.META_TYPES.blockStateType,
            Types1_20_5.META_TYPES.optionalBlockStateType,
            Types1_20_5.META_TYPES.particleType,
            null
        );
        filter().metaType(Types1_20_5.META_TYPES.componentType).handler((event, meta) -> protocol.getComponentRewriter().processTag(event.user(), meta.value()));
        filter().metaType(Types1_20_5.META_TYPES.optionalComponentType).handler((event, meta) -> protocol.getComponentRewriter().processTag(event.user(), meta.value()));

        filter().type(EntityTypes1_20_5.LIVINGENTITY).index(10).handler((event, meta) -> {
            final int effectColor = meta.value();
            final Particle particle = new Particle(protocol.getMappingData().getParticleMappings().mappedId("entity_effect"));
            particle.add(Type.INT, effectColor);
            meta.setTypeAndValue(Types1_20_5.META_TYPES.particlesType, new Particle[]{particle});
        });

        filter().type(EntityTypes1_20_5.LLAMA).removeIndex(20); // Carpet color
        filter().type(EntityTypes1_20_5.AREA_EFFECT_CLOUD).handler((event, meta) -> {
            // Color removed - Now put into the actual particle
            final int metaIndex = event.index();
            if (metaIndex == 9) {
                // If the color is found first
                final Metadata particleData = event.metaAtIndex(11);
                addColor(particleData, meta.value());

                event.cancel();
                return;
            }

            if (metaIndex > 9) {
                event.setIndex(metaIndex - 1);
            }

            if (metaIndex == 11) {
                // If the particle is found first
                final Metadata colorData = event.metaAtIndex(9);
                if (colorData != null && colorData.metaType() == Types1_20_5.META_TYPES.varIntType) {
                    addColor(meta, colorData.value());
                }
            }
        });

        filter().type(EntityTypes1_20_5.MINECART_ABSTRACT).index(11).handler((event, meta) -> {
            final int blockState = meta.value();
            meta.setValue(protocol.getMappingData().getNewBlockStateId(blockState));
        });
    }

    private void addColor(@Nullable final Metadata particleMeta, final int color) {
        if (particleMeta == null) {
            return;
        }

        final Particle particle = particleMeta.value();
        if (particle.id() == protocol.getMappingData().getParticleMappings().mappedId("entity_effect")) {
            particle.getArgument(0).setValue(color);
        }
    }

    @Override
    public void rewriteParticle(final UserConnection connection, final Particle particle) {
        super.rewriteParticle(connection, particle);
        if (particle.id() == protocol.getMappingData().getParticleMappings().mappedId("entity_effect")) {
            particle.add(Type.INT, 0); // Default color, changed in the area effect handler
        }
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