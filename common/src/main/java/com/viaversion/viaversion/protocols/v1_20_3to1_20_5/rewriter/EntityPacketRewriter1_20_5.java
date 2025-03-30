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
package com.viaversion.viaversion.protocols.v1_20_3to1_20_5.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.DimensionData;
import com.viaversion.viaversion.api.minecraft.GameMode;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.RegistryEntry;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_20_5;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_20_3;
import com.viaversion.viaversion.api.type.types.version.Types1_20_5;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ClientboundConfigurationPackets1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ClientboundPacket1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ClientboundPackets1_20_3;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.Protocol1_20_3To1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.Attributes1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.BannerPatterns1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.storage.AcknowledgedMessagesStorage;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.storage.ArmorTrimStorage;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.KeyMappings;
import com.viaversion.viaversion.util.TagUtil;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class EntityPacketRewriter1_20_5 extends EntityRewriter<ClientboundPacket1_20_3, Protocol1_20_3To1_20_5> {

    private static final UUID CREATIVE_BLOCK_INTERACTION_RANGE = UUID.fromString("736565d2-e1a7-403d-a3f8-1aeb3e302542");
    private static final UUID CREATIVE_ENTITY_INTERACTION_RANGE = UUID.fromString("98491ef6-97b1-4584-ae82-71a8cc85cf73");

    public EntityPacketRewriter1_20_5(final Protocol1_20_3To1_20_5 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        registerTrackerWithData1_19(ClientboundPackets1_20_3.ADD_ENTITY, EntityTypes1_20_5.FALLING_BLOCK);
        registerSetEntityData(ClientboundPackets1_20_3.SET_ENTITY_DATA, Types1_20_3.ENTITY_DATA_LIST, Types1_20_5.ENTITY_DATA_LIST);
        registerRemoveEntities(ClientboundPackets1_20_3.REMOVE_ENTITIES);

        protocol.registerClientbound(ClientboundPackets1_20_3.SET_EQUIPMENT, wrapper -> {
            final int entityId = wrapper.passthrough(Types.VAR_INT); // Entity id
            final EntityType type = tracker(wrapper.user()).entityType(entityId);

            byte slot;
            do {
                slot = wrapper.read(Types.BYTE);

                final int rawSlot = slot & 0x7F;
                if (type != null && type.isOrHasParent(EntityTypes1_20_5.ABSTRACT_HORSE) && rawSlot == 4) {
                    final boolean lastSlot = (slot & 0xFFFFFF80) == 0;
                    slot = (byte) (lastSlot ? 6 : 6 | 0xFFFFFF80); // Map chest slot index to body slot index for horses
                }
                wrapper.write(Types.BYTE, slot);
                Item item = protocol.getItemRewriter().handleItemToClient(wrapper.user(), wrapper.read(Types.ITEM1_20_2));
                wrapper.write(Types1_20_5.ITEM, item);
            } while ((slot & 0xFFFFFF80) != 0);
        });

        protocol.registerClientbound(ClientboundConfigurationPackets1_20_3.REGISTRY_DATA, wrapper -> {
            final PacketWrapper knownPacksPacket = wrapper.create(ClientboundConfigurationPackets1_20_5.SELECT_KNOWN_PACKS);
            knownPacksPacket.write(Types.VAR_INT, 0); // No known packs, everything is sent here
            knownPacksPacket.send(Protocol1_20_3To1_20_5.class);

            final CompoundTag registryData = wrapper.read(Types.COMPOUND_TAG);
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

                // Calculate number of entries (exclude duplicated ids)
                RegistryEntry[] registryEntries = new RegistryEntry[valueTag.stream().map(e -> e.getInt("id")).distinct().toArray().length];
                boolean requiresDummyValues = false;
                int highestId = -1;
                final IntSet ids = new IntArraySet();
                for (final CompoundTag tag : valueTag) {
                    final String name = tag.getString("name");
                    final int id = tag.getInt("id");
                    if (ids.add(id)) { // Override duplicated id without incrementing entries length
                        highestId = Math.max(highestId, id);
                        if (id >= registryEntries.length) {
                            // It was previously possible to have arbitrary ids, increase array length if needed
                            registryEntries = Arrays.copyOf(registryEntries, Math.max(registryEntries.length * 2, id + 1));
                            requiresDummyValues = true;
                        }
                    }

                    registryEntries[id] = new RegistryEntry(name, tag.get("element"));
                }

                final String strippedKey = Key.stripMinecraftNamespace(type);
                if (strippedKey.equals("damage_type")) {
                    // Add spit damage type if not already present
                    if (Arrays.stream(registryEntries).noneMatch(e -> Key.namespaced(e.key()).equals("minecraft:spit"))) {
                        highestId++;
                        registryEntries = Arrays.copyOf(registryEntries, highestId + 1);
                        final CompoundTag spitData = new CompoundTag();
                        spitData.putString("scaling", "when_caused_by_living_non_player");
                        spitData.putString("message_id", "mob");
                        spitData.putFloat("exhaustion", 0.1F);
                        registryEntries[highestId] = new RegistryEntry("minecraft:spit", spitData);
                    }

                    // Fill in missing damage types with 1.20.3/4 defaults
                    final Set<String> registryEntryKeys = Arrays.stream(registryEntries).map(e -> Key.stripMinecraftNamespace(e.key())).collect(Collectors.toSet());
                    for (final String key : protocol.getMappingData().damageKeys()) {
                        if (registryEntryKeys.contains(key)) {
                            continue;
                        }

                        highestId++;
                        registryEntries = Arrays.copyOf(registryEntries, highestId + 1);
                        registryEntries[highestId] = new RegistryEntry(Key.namespaced(key), protocol.getMappingData().damageType(key));
                    }
                }

                if (requiresDummyValues) {
                    // Truncate and replace null values
                    final int finalLength = highestId + 1;
                    if (registryEntries.length != finalLength) {
                        registryEntries = Arrays.copyOf(registryEntries, finalLength);
                    }
                    replaceNullValues(registryEntries);
                }

                // Track custom armor trims
                if (strippedKey.equals("trim_pattern")) {
                    wrapper.user().get(ArmorTrimStorage.class).setTrimPatterns(toMappings(registryEntries));
                } else if (strippedKey.equals("trim_material")) {
                    wrapper.user().get(ArmorTrimStorage.class).setTrimMaterials(toMappings(registryEntries));
                }

                final PacketWrapper registryPacket = wrapper.create(ClientboundConfigurationPackets1_20_5.REGISTRY_DATA);
                registryPacket.write(Types.STRING, type);
                registryPacket.write(Types.REGISTRY_ENTRY_ARRAY, registryEntries);
                registryPacket.send(Protocol1_20_3To1_20_5.class);
            }

            wrapper.cancel();

            // Send banner patterns and default wolf variant
            final PacketWrapper wolfVariantsPacket = wrapper.create(ClientboundConfigurationPackets1_20_5.REGISTRY_DATA);
            wolfVariantsPacket.write(Types.STRING, "minecraft:wolf_variant");
            final CompoundTag paleWolf = new CompoundTag();
            paleWolf.putString("wild_texture", "entity/wolf/wolf");
            paleWolf.putString("tame_texture", "entity/wolf/wolf_tame");
            paleWolf.putString("angry_texture", "entity/wolf/wolf_angry");
            paleWolf.put("biomes", new ListTag<>(StringTag.class));
            wolfVariantsPacket.write(Types.REGISTRY_ENTRY_ARRAY, new RegistryEntry[]{new RegistryEntry("minecraft:pale", paleWolf)});
            wolfVariantsPacket.send(Protocol1_20_3To1_20_5.class);

            final PacketWrapper bannerPatternsPacket = wrapper.create(ClientboundConfigurationPackets1_20_5.REGISTRY_DATA);
            bannerPatternsPacket.write(Types.STRING, "minecraft:banner_pattern");
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
            bannerPatternsPacket.write(Types.REGISTRY_ENTRY_ARRAY, patternEntries);
            bannerPatternsPacket.send(Protocol1_20_3To1_20_5.class);
        });

        protocol.registerClientbound(ClientboundPackets1_20_3.LOGIN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); // Entity ID
                map(Types.BOOLEAN); // Hardcore
                map(Types.STRING_ARRAY); // World List
                map(Types.VAR_INT); // Max players
                map(Types.VAR_INT); // View distance
                map(Types.VAR_INT); // Simulation distance
                map(Types.BOOLEAN); // Reduced debug info
                map(Types.BOOLEAN); // Show death screen
                map(Types.BOOLEAN); // Limited crafting
                handler(wrapper -> {
                    final String dimensionKey = wrapper.read(Types.STRING);
                    final DimensionData data = tracker(wrapper.user()).dimensionData(dimensionKey);
                    wrapper.write(Types.VAR_INT, data.id());
                });
                map(Types.STRING); // World
                map(Types.LONG); // Seed
                map(Types.BYTE); // Gamemode
                map(Types.BYTE); // Previous gamemode
                map(Types.BOOLEAN); // Debug
                map(Types.BOOLEAN); // Flat
                map(Types.OPTIONAL_GLOBAL_POSITION); // Last death location
                map(Types.VAR_INT); // Portal cooldown
                handler(worldDataTrackerHandlerByKey1_20_5(3)); // Tracks world height and name for chunk data and entity (un)tracking
                handler(playerTrackerHandler());
                handler(wrapper -> {
                    // Enforces secure chat - moved from server data (which is unfortunately sent a while after this)
                    final AcknowledgedMessagesStorage storage = wrapper.user().get(AcknowledgedMessagesStorage.class);
                    if (storage.secureChatEnforced() != null) {
                        // Just put in what we know if this is sent multiple times
                        wrapper.write(Types.BOOLEAN, storage.isSecureChatEnforced());
                    } else {
                        wrapper.write(Types.BOOLEAN, Via.getConfig().enforceSecureChat());
                    }

                    storage.clear();

                    // Handle creative interaction range
                    final byte gamemode = wrapper.get(Types.BYTE, 0);
                    if (gamemode == GameMode.CREATIVE.id()) {
                        sendRangeAttributes(wrapper.user(), true);
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_20_3.RESPAWN, wrapper -> {
            final String dimensionKey = wrapper.read(Types.STRING);
            final DimensionData data = tracker(wrapper.user()).dimensionData(dimensionKey);
            wrapper.write(Types.VAR_INT, data.id());

            wrapper.passthrough(Types.STRING); // World
            worldDataTrackerHandlerByKey1_20_5(0).handle(wrapper); // Tracks world height and name for chunk data and entity (un)tracking
            wrapper.passthrough(Types.LONG); // Seed

            final byte gamemode = wrapper.passthrough(Types.BYTE);
            sendRangeAttributes(wrapper.user(), gamemode == GameMode.CREATIVE.id());
        });

        protocol.registerClientbound(ClientboundPackets1_20_3.UPDATE_MOB_EFFECT, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Entity ID
            wrapper.passthrough(Types.VAR_INT); // Effect ID

            final byte amplifier = wrapper.read(Types.BYTE);
            wrapper.write(Types.VAR_INT, (int) amplifier);

            wrapper.passthrough(Types.VAR_INT); // Duration
            wrapper.passthrough(Types.BYTE); // Flags
            wrapper.read(Types.OPTIONAL_COMPOUND_TAG); // Remove factor data
        });

        protocol.registerClientbound(ClientboundPackets1_20_3.UPDATE_ATTRIBUTES, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Entity ID
            final int size = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < size; i++) {
                // From a string to a registry int ID
                final String attributeIdentifier = wrapper.read(Types.STRING);
                final int mappedId = Attributes1_20_5.keyToId(attributeIdentifier);
                wrapper.write(Types.VAR_INT, mappedId != -1 ? mappedId : 0);

                wrapper.passthrough(Types.DOUBLE); // Base
                final int modifierSize = wrapper.passthrough(Types.VAR_INT);
                for (int j = 0; j < modifierSize; j++) {
                    wrapper.passthrough(Types.UUID); // ID
                    wrapper.passthrough(Types.DOUBLE); // Amount
                    wrapper.passthrough(Types.BYTE); // Operation
                }
            }
        });

        protocol.registerClientbound(ClientboundPackets1_20_3.GAME_EVENT, wrapper -> {
            // If the gamemode changed to/from creative, update the range attribute
            final short event = wrapper.passthrough(Types.UNSIGNED_BYTE);
            if (event != 3) {
                return;
            }

            // Resend attributes either with their original list or with the creative range modifier added
            final int value = (int) Math.floor(wrapper.passthrough(Types.FLOAT) + 0.5F);
            sendRangeAttributes(wrapper.user(), value == GameMode.CREATIVE.id());
        });
    }

    private KeyMappings toMappings(final RegistryEntry[] entries) {
        final String[] keys = new String[entries.length];
        for (int i = 0; i < entries.length; i++) {
            keys[i] = Key.stripMinecraftNamespace(entries[i].key());
        }
        return new KeyMappings(keys);
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

    private void sendRangeAttributes(final UserConnection connection, final boolean creativeMode) {
        final PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_20_5.UPDATE_ATTRIBUTES, connection);
        wrapper.write(Types.VAR_INT, tracker(connection).clientEntityId());
        wrapper.write(Types.VAR_INT, 2); // Number of attributes
        writeAttribute(wrapper, "player.block_interaction_range", 4.5, creativeMode ? CREATIVE_BLOCK_INTERACTION_RANGE : null, 0.5);
        writeAttribute(wrapper, "player.entity_interaction_range", 3.0, creativeMode ? CREATIVE_ENTITY_INTERACTION_RANGE : null, 2.0);
        wrapper.scheduleSend(Protocol1_20_3To1_20_5.class);
    }

    private void writeAttribute(final PacketWrapper wrapper, final String attributeId, final double base, @Nullable final UUID modifierId, final double amount) {
        wrapper.write(Types.VAR_INT, Attributes1_20_5.keyToId(attributeId));
        wrapper.write(Types.DOUBLE, base);
        if (modifierId != null) {
            // Single modifier
            wrapper.write(Types.VAR_INT, 1);
            wrapper.write(Types.UUID, modifierId);
            wrapper.write(Types.DOUBLE, amount);
            wrapper.write(Types.BYTE, (byte) 0); // Add
        } else {
            // No modifiers
            wrapper.write(Types.VAR_INT, 0);
        }
    }

    static int withAlpha(final int rgb) {
        return 255 << 24 | rgb & 0xffffff;
    }

    @Override
    protected void registerRewrites() {
        filter().mapDataType(typeId -> {
            int id = typeId;
            if (id >= Types1_20_5.ENTITY_DATA_TYPES.particlesType.typeId()) {
                id++;
            }
            if (id >= Types1_20_5.ENTITY_DATA_TYPES.wolfVariantType.typeId()) {
                id++;
            }
            if (id >= Types1_20_5.ENTITY_DATA_TYPES.armadilloState.typeId()) {
                id++;
            }
            return Types1_20_5.ENTITY_DATA_TYPES.byId(id);
        });

        registerEntityDataTypeHandler(
            Types1_20_5.ENTITY_DATA_TYPES.itemType,
            Types1_20_5.ENTITY_DATA_TYPES.blockStateType,
            Types1_20_5.ENTITY_DATA_TYPES.optionalBlockStateType,
            Types1_20_5.ENTITY_DATA_TYPES.particleType,
            null,
            Types1_20_5.ENTITY_DATA_TYPES.componentType,
            Types1_20_5.ENTITY_DATA_TYPES.optionalComponentType
        );
        registerBlockStateHandler(EntityTypes1_20_5.ABSTRACT_MINECART, 11);

        filter().type(EntityTypes1_20_5.LIVING_ENTITY).index(10).handler((event, data) -> {
            final int effectColor = data.value();
            if (effectColor == 0) {
                // No effect
                data.setTypeAndValue(Types1_20_5.ENTITY_DATA_TYPES.particlesType, new Particle[0]);
                return;
            }

            final Particle particle = new Particle(protocol.getMappingData().getParticleMappings().mappedId("entity_effect"));
            particle.add(Types.INT, withAlpha(effectColor));
            data.setTypeAndValue(Types1_20_5.ENTITY_DATA_TYPES.particlesType, new Particle[]{particle});
        });

        filter().type(EntityTypes1_20_5.LLAMA).handler((event, data) -> {
            // Carpet color removed - now set via the set equipment packet
            final int dataIndex = event.index();
            if (dataIndex == 20) {
                event.cancel();
                final int color = data.value();

                // Convert dyed color id to carpet item id
                final PacketWrapper setEquipment = PacketWrapper.create(ClientboundPackets1_20_5.SET_EQUIPMENT, event.user());
                setEquipment.write(Types.VAR_INT, event.entityId());
                setEquipment.write(Types.BYTE, (byte) 6);
                setEquipment.write(Types1_20_5.ITEM, new StructuredItem(color + 446, 1, new StructuredDataContainer()));
                setEquipment.scheduleSend(Protocol1_20_3To1_20_5.class);
            } else if (dataIndex > 20) {
                event.setIndex(dataIndex - 1);
            }
        });
        filter().type(EntityTypes1_20_5.AREA_EFFECT_CLOUD).handler((event, data) -> {
            // Color removed - Now put into the actual particle
            final int dataIndex = event.index();
            if (dataIndex == 9) {
                // If the color is found first
                final EntityData particleData = event.dataAtIndex(11);
                final int color = data.value();
                if (particleData == null) {
                    if (color != 0) {
                        // Add default particle with data
                        final Particle particle = new Particle(protocol.getMappingData().getParticleMappings().mappedId("entity_effect"));
                        particle.add(Types.INT, withAlpha(color));
                        event.createExtraData(new EntityData(10, Types1_20_5.ENTITY_DATA_TYPES.particleType, particle));
                    }
                } else {
                    addColor(particleData, color);
                }

                event.cancel();
                return;
            }

            if (dataIndex == 11) {
                // If the particle is found first
                final EntityData colorData = event.dataAtIndex(9);
                if (colorData != null && colorData.dataType() == Types1_20_5.ENTITY_DATA_TYPES.varIntType) {
                    addColor(data, colorData.value());
                }
            }

            if (dataIndex > 9) {
                event.setIndex(dataIndex - 1);
            }
        });

        filter().type(EntityTypes1_20_5.ARROW).index(10).handler((event, data) -> {
            final int color = data.value();
            if (color != -1) {
                data.setValue(withAlpha(color));
            }
        });

        filter().type(EntityTypes1_20_5.ITEM_PROJECTILE).index(8).handler((event, data) -> {
            final Item item = data.value();
            if (item == null || item.isEmpty()) {
                // The item is used for particles or projectile display and can no longer be empty
                event.cancel();
            }
        });
    }

    private void addColor(@Nullable final EntityData particleMeta, final int color) {
        if (particleMeta == null) {
            return;
        }

        final Particle particle = particleMeta.value();
        if (particle.id() == protocol.getMappingData().getParticleMappings().mappedId("entity_effect")) {
            particle.getArgument(0).setValue(withAlpha(color));
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
