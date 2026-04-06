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
package com.viaversion.viaversion.protocols.v1_21_11to26_1;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.minecraft.RegistryEntry;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.data.version.StructuredDataKeys1_21_11;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_11;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21_11;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes26_1;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.packet.provider.SimplePacketTypesProvider;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_21_5;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType26_1;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import com.viaversion.viaversion.api.type.types.version.Types1_20_5;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.data.MappingData26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPackets26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ServerboundPacket26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ServerboundPackets26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.rewriter.BlockItemPacketRewriter26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.rewriter.ComponentRewriter26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.rewriter.EntityPacketRewriter26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.rewriter.RegistryDataRewriter26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.storage.PlayerSneaking;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.storage.TagsSent;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.rewriter.RecipeDisplayRewriter1_21_5;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundConfigurationPackets1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ServerboundConfigurationPackets1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ServerboundPacket1_21_9;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ClientboundPacket1_21_11;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ClientboundPackets1_21_11;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.RecipeDisplayRewriter;
import com.viaversion.viaversion.rewriter.RegistryDataRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.rewriter.block.BlockRewriter1_21_5;
import com.viaversion.viaversion.rewriter.text.NBTComponentRewriter;
import com.viaversion.viaversion.util.Key;
import java.util.Map;

import static com.viaversion.viaversion.util.ProtocolUtil.packetTypeMap;

public final class Protocol1_21_11To26_1 extends AbstractProtocol<ClientboundPacket1_21_11, ClientboundPacket26_1, ServerboundPacket1_21_9, ServerboundPacket26_1> {

    public static final MappingData26_1 MAPPINGS = new MappingData26_1();
    private final EntityPacketRewriter26_1 entityRewriter = new EntityPacketRewriter26_1(this);
    private final BlockItemPacketRewriter26_1 itemRewriter = new BlockItemPacketRewriter26_1(this);
    private final ParticleRewriter<ClientboundPacket1_21_11> particleRewriter = new ParticleRewriter<>(this);
    private final TagRewriter<ClientboundPacket1_21_11> tagRewriter = new TagRewriter<>(this);
    private final NBTComponentRewriter<ClientboundPacket1_21_11> componentRewriter = new ComponentRewriter26_1(this);
    private final RegistryDataRewriter registryDataRewriter = new RegistryDataRewriter26_1(this);
    private final RecipeDisplayRewriter<ClientboundPacket1_21_11> recipeRewriter = new RecipeDisplayRewriter1_21_5<>(this);
    private final BlockRewriter<ClientboundPacket1_21_11> blockRewriter = new BlockRewriter1_21_5<>(this, ChunkType1_21_5::new, ChunkType26_1::new);

    public Protocol1_21_11To26_1() {
        super(ClientboundPacket1_21_11.class, ClientboundPacket26_1.class, ServerboundPacket1_21_9.class, ServerboundPacket26_1.class);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        registerFinishConfiguration(ClientboundConfigurationPackets1_21_9.FINISH_CONFIGURATION, wrapper -> {
            final PacketWrapper clocksPacket = wrapper.create(ClientboundConfigurationPackets1_21_9.REGISTRY_DATA);
            clocksPacket.write(Types.STRING, "world_clock");
            clocksPacket.write(Types.REGISTRY_ENTRY_ARRAY, new RegistryEntry[]{new RegistryEntry("minecraft:overworld", new CompoundTag())});
            clocksPacket.send(Protocol1_21_11To26_1.class);

            sendSoundVariants(wrapper, "cat_sound_variant", MAPPINGS.catSoundVariants());
            sendSoundVariants(wrapper, "cow_sound_variant", MAPPINGS.cowSoundVariants());
            sendSoundVariants(wrapper, "pig_sound_variant", MAPPINGS.pigSoundVariants());
            sendSoundVariants(wrapper, "chicken_sound_variant", MAPPINGS.chickenSoundVariants());

            // Make sure the client gets damage types and banner patterns, even if the server doesn't send tags
            if (!wrapper.user().has(TagsSent.class)) {
                final PacketWrapper tagsPacket = wrapper.create(ClientboundConfigurationPackets1_21_9.UPDATE_TAGS);
                tagsPacket.write(Types.VAR_INT, 0);
                tagsPacket.send(Protocol1_21_11To26_1.class, false);
            }
        });

        addRequiredRegistryEntries();
        registryDataRewriter.addHandler("timeline", (key, tag) -> tag.putString("clock", "overworld"));
        registryDataRewriter.addHandler("wolf_sound_variant", (key, tag) -> {
            final CompoundTag sounds = new CompoundTag();
            for (final Map.Entry<String, Tag> entry : tag.entrySet()) {
                sounds.put(entry.getKey(), entry.getValue());
            }
            tag.clear();

            sounds.putString("step_sound", "entity.wolf.step");
            tag.put("adult_sounds", sounds);
            tag.put("baby_sounds", sounds.copy());
        });
        registryDataRewriter.addHandler("wolf_variant", (key, tag) -> {
            final CompoundTag assets = tag.getCompoundTag("assets");
            final CompoundTag babyAssets = new CompoundTag();
            for (final Map.Entry<String, Tag> entry : assets.entrySet()) {
                babyAssets.putString(entry.getKey(), entry.getValue().getValue() + "_baby");
            }
            tag.put("baby_assets", babyAssets);
        });
        registryDataRewriter.addHandler("frog_variant", (key, tag) -> swapEntityNameAffix("frog", tag));
        swapAffixAndAddAssetId("chicken_variant", "chicken");
        swapAffixAndAddAssetId("cow_variant", "cow");
        swapAffixAndAddAssetId("pig_variant", "pig");
        registryDataRewriter.addHandler("cat_variant", (key, tag) -> {
            addEntityNamePrefix("cat", tag);
            addBabyAssetId(tag);
        });
        registryDataRewriter.addHandler("dimension_type", (key, tag) -> {
            tag.putBoolean("has_ender_dragon_fight", Key.equals(key, "the_end"));

            final CompoundTag attributes = tag.getCompoundTag("attributes");
            if (attributes != null) {
                final int ambientLightColor = switch (Key.stripMinecraftNamespace(key)) {
                    case "the_end" -> -12630209;
                    case "the_nether" -> -13621215;
                    case "overworld" -> -16119286;
                    default -> -16777216;
                };
                attributes.putInt("visual/ambient_light_color", ambientLightColor);
            }
        });
        registerClientbound(ClientboundPackets1_21_11.SET_TIME, wrapper -> {
            wrapper.passthrough(Types.LONG); // Game time

            final long dayTime = wrapper.read(Types.LONG);
            final boolean tickDayTime = wrapper.read(Types.BOOLEAN);

            wrapper.write(Types.VAR_INT, 1); // One!
            wrapper.write(Types.VAR_INT, 0); // Overworld clock
            wrapper.write(Types.VAR_LONG, dayTime); // Total ticks
            wrapper.write(Types.FLOAT, 0F); // Partial tick
            wrapper.write(Types.FLOAT, tickDayTime ? 1F : 0F); // Tick rate
        });

        replaceClientbound(ClientboundPackets1_21_11.UPDATE_TAGS, this::handleTags);
        replaceClientbound(ClientboundConfigurationPackets1_21_9.UPDATE_TAGS, this::handleTags);

        cancelServerbound(ServerboundPackets26_1.SET_GAME_RULE);
        registerServerbound(ServerboundPackets26_1.CLIENT_COMMAND, wrapper -> {
            final int action = wrapper.passthrough(Types.VAR_INT);
            if (action == 2) { // Request game rule values
                wrapper.cancel();
            }
        });
    }

    private void handleTags(final PacketWrapper wrapper) {
        tagRewriter.handleGeneric(wrapper);
        wrapper.user().put(new TagsSent());
    }

    private void sendSoundVariants(final PacketWrapper wrapper, final String key, final CompoundTag tag) {
        final PacketWrapper clocksPacket = wrapper.create(ClientboundConfigurationPackets1_21_9.REGISTRY_DATA);
        clocksPacket.write(Types.STRING, key);
        clocksPacket.write(Types.REGISTRY_ENTRY_ARRAY, registryDataRewriter.entriesFromTag(tag));
        clocksPacket.send(Protocol1_21_11To26_1.class);
    }

    private void addRequiredRegistryEntries() {
        // Add a few possibly missing registry entries required by delayed default item creation.
        // These are some of the things that brought us the glorious EitherHolderType before
        final CompoundTag spearDamageType = new CompoundTag();
        spearDamageType.putFloat("exhaustion", 0.1F);
        spearDamageType.putString("message_id", "spear");
        spearDamageType.putString("scaling", "when_caused_by_living_non_player");
        registryDataRewriter.addEntries("damage_type", new RegistryEntry("spear", spearDamageType));

        final CompoundTag coldChicken = new CompoundTag();
        coldChicken.putString("asset_id", "entity/chicken/chicken_cold");
        coldChicken.putString("baby_asset_id", "entity/chicken/chicken_cold_baby");
        coldChicken.putString("model", "cold");
        final CompoundTag warmChicken = new CompoundTag();
        warmChicken.putString("asset_id", "entity/chicken/chicken_warm");
        warmChicken.putString("baby_asset_id", "entity/chicken/chicken_warm_baby");
        registryDataRewriter.addEntries("chicken_variant", new RegistryEntry("cold", coldChicken), new RegistryEntry("warm", warmChicken));

        final CompoundTag ponderGoatHornInstrument = new CompoundTag();
        final CompoundTag ponderGoatHornDescription = new CompoundTag();
        ponderGoatHornDescription.putString("translate", "instrument.minecraft.ponder_goat_horn");
        ponderGoatHornInstrument.put("description", ponderGoatHornDescription);
        ponderGoatHornInstrument.putString("sound_event", "item.goat_horn.sound.0");
        ponderGoatHornInstrument.putFloat("use_duration", 7F);
        ponderGoatHornInstrument.putFloat("range", 256F);
        registryDataRewriter.addEntries("instrument", new RegistryEntry("spear", ponderGoatHornInstrument));

        addJukeboxPlayables("11", "13", "5", "blocks", "cat", "chirp", "far", "mall", "mellohi", "otherside", "pigstep", "relic", "stal", "strad", "wait", "ward", "creator", "creator_music_box", "lava_chicken", "tears", "precipice");
        addTrimMaterials("quartz", "iron", "netherite", "redstone", "copper", "gold", "emerald", "diamond", "lapis", "amethyst", "resin");
    }

    private void addTrimMaterials(final String... trimMaterials) {
        for (final String trimMaterial : trimMaterials) {
            final CompoundTag materialTag = new CompoundTag();
            final CompoundTag materialDescription = new CompoundTag();
            materialDescription.putString("translate", "trim_material.minecraft." + trimMaterial);
            materialTag.put("description", materialDescription);
            materialTag.putString("asset_name", trimMaterial);
            registryDataRewriter.addEntries("trim_material", new RegistryEntry(trimMaterial, materialTag));
        }
    }

    private void addJukeboxPlayables(final String... songs) {
        for (final String song : songs) {
            final CompoundTag songTag = new CompoundTag();
            final CompoundTag songDescription = new CompoundTag();
            songDescription.putString("translate", "jukebox_song.minecraft." + song);
            songTag.put("description", songDescription);
            songTag.putString("sound_event", "music_disc." + song);
            songTag.putFloat("length_in_seconds", 175F);
            songTag.putInt("comparator_output", 10);
            registryDataRewriter.addEntries("jukebox_song", new RegistryEntry(song, songTag));
        }
    }

    private void swapAffixAndAddAssetId(final String registryKey, final String affix) {
        registryDataRewriter.addHandler(registryKey, (key, tag) -> {
            swapEntityNameAffix(affix, tag);
            addBabyAssetId(tag);
        });
    }

    private void addBabyAssetId(final CompoundTag tag) {
        final String assetId = tag.getString("asset_id");
        tag.putString("baby_asset_id", assetId + "_baby");
    }

    private void addEntityNamePrefix(final String key, final CompoundTag tag) {
        // e.g. entity/cat/jellie -> entity/cat/cat_jellie
        final StringTag assetIdTag = tag.getStringTag("asset_id");
        final String assetId = assetIdTag.getValue();
        assetIdTag.setValue(assetId.replace(key + "/", key + "/" + key + "_"));
    }

    private void swapEntityNameAffix(final String key, final CompoundTag tag) {
        final StringTag assetIdTag = tag.getStringTag("asset_id");
        final String assetId = assetIdTag.getValue();
        if (assetId.endsWith("_" + key)) {
            // e.g. entity/frog/warm_frog -> entity/frog/frog_warm
            final String mappedAsset = assetId.substring(0, assetId.length() - key.length() - 1)
                .replace(key + "/", key + "/" + key + "_");
            assetIdTag.setValue(mappedAsset);
        }
    }

    @Override
    public void init(final UserConnection connection) {
        addEntityTracker(connection, new EntityTrackerBase(connection, EntityTypes1_21_11.PLAYER));
        addItemHasher(connection);
        connection.put(new PlayerSneaking());
    }

    @Override
    protected void onMappingDataLoaded() {
        ParticleType.Fillers.fill1_21_9(this);
        mappedTypes().structuredData.filler(this).add(StructuredDataKey.CUSTOM_DATA, StructuredDataKey.MAX_STACK_SIZE, StructuredDataKey.MAX_DAMAGE,
            StructuredDataKey.UNBREAKABLE1_21_5, StructuredDataKey.RARITY, StructuredDataKey.TOOLTIP_DISPLAY, StructuredDataKey.DAMAGE_RESISTANT26_1,
            StructuredDataKey.CUSTOM_NAME, StructuredDataKey.LORE, StructuredDataKey.ENCHANTMENTS1_21_5,
            StructuredDataKey.CUSTOM_MODEL_DATA1_21_4, StructuredDataKey.BLOCKS_ATTACKS26_1, StructuredDataKey.PROVIDES_BANNER_PATTERNS26_1,
            StructuredDataKey.REPAIR_COST, StructuredDataKey.CREATIVE_SLOT_LOCK, StructuredDataKey.ENCHANTMENT_GLINT_OVERRIDE,
            StructuredDataKey.INTANGIBLE_PROJECTILE, StructuredDataKey.STORED_ENCHANTMENTS1_21_5, StructuredDataKey.DYED_COLOR1_21_5,
            StructuredDataKey.MAP_COLOR, StructuredDataKey.MAP_ID, StructuredDataKey.MAP_DECORATIONS, StructuredDataKey.MAP_POST_PROCESSING,
            StructuredDataKey.POTION_CONTENTS1_21_2, StructuredDataKey.SUSPICIOUS_STEW_EFFECTS, StructuredDataKey.WRITABLE_BOOK_CONTENT,
            StructuredDataKey.WRITTEN_BOOK_CONTENT, StructuredDataKey.TRIM1_21_5, StructuredDataKey.DEBUG_STICK_STATE, StructuredDataKey.ENTITY_DATA1_21_9,
            StructuredDataKey.BUCKET_ENTITY_DATA, StructuredDataKey.BLOCK_ENTITY_DATA1_21_9, StructuredDataKey.INSTRUMENT26_1,
            StructuredDataKey.RECIPES, StructuredDataKey.LODESTONE_TRACKER, StructuredDataKey.FIREWORK_EXPLOSION, StructuredDataKey.FIREWORKS,
            StructuredDataKey.PROFILE1_21_9, StructuredDataKey.NOTE_BLOCK_SOUND, StructuredDataKey.BANNER_PATTERNS, StructuredDataKey.BASE_COLOR,
            StructuredDataKey.POT_DECORATIONS, StructuredDataKey.BLOCK_STATE, StructuredDataKey.BEES1_21_9, StructuredDataKey.LOCK1_21_2,
            StructuredDataKey.CONTAINER_LOOT, StructuredDataKey.TOOL1_21_5, StructuredDataKey.ITEM_NAME, StructuredDataKey.OMINOUS_BOTTLE_AMPLIFIER,
            StructuredDataKey.FOOD1_21_2, StructuredDataKey.JUKEBOX_PLAYABLE26_1, StructuredDataKey.ATTRIBUTE_MODIFIERS1_21_6,
            StructuredDataKey.REPAIRABLE, StructuredDataKey.ENCHANTABLE, StructuredDataKey.CONSUMABLE1_21_2, StructuredDataKey.ATTACK_RANGE,
            StructuredDataKey.USE_COOLDOWN, StructuredDataKey.DAMAGE, StructuredDataKey.EQUIPPABLE1_21_6, StructuredDataKey.ITEM_MODEL,
            StructuredDataKey.GLIDER, StructuredDataKey.TOOLTIP_STYLE, StructuredDataKey.DEATH_PROTECTION, StructuredDataKey.WEAPON,
            StructuredDataKey.POTION_DURATION_SCALE, StructuredDataKey.VILLAGER_VARIANT, StructuredDataKey.WOLF_VARIANT, StructuredDataKey.WOLF_COLLAR,
            StructuredDataKey.FOX_VARIANT, StructuredDataKey.SALMON_SIZE, StructuredDataKey.PARROT_VARIANT, StructuredDataKey.TROPICAL_FISH_PATTERN,
            StructuredDataKey.TROPICAL_FISH_BASE_COLOR, StructuredDataKey.TROPICAL_FISH_PATTERN_COLOR, StructuredDataKey.MOOSHROOM_VARIANT,
            StructuredDataKey.RABBIT_VARIANT, StructuredDataKey.PIG_VARIANT, StructuredDataKey.FROG_VARIANT, StructuredDataKey.HORSE_VARIANT,
            StructuredDataKey.PAINTING_VARIANT, StructuredDataKey.LLAMA_VARIANT, StructuredDataKey.AXOLOTL_VARIANT, StructuredDataKey.CAT_VARIANT,
            StructuredDataKey.CAT_COLLAR, StructuredDataKey.SHEEP_COLOR, StructuredDataKey.SHULKER_COLOR, StructuredDataKey.PROVIDES_TRIM_MATERIAL26_1,
            StructuredDataKey.BREAK_SOUND, StructuredDataKey.COW_VARIANT, StructuredDataKey.CHICKEN_VARIANT26_1, StructuredDataKey.WOLF_SOUND_VARIANT,
            StructuredDataKey.USE_EFFECTS, StructuredDataKey.MINIMUM_ATTACK_CHARGE, StructuredDataKey.DAMAGE_TYPE26_1, StructuredDataKey.PIERCING_WEAPON,
            StructuredDataKey.KINETIC_WEAPON, StructuredDataKey.SWING_ANIMATION, StructuredDataKey.ZOMBIE_NAUTILUS_VARIANT26_1, StructuredDataKey.ADDITIONAL_TRADE_COST,
            StructuredDataKey.DYE, StructuredDataKey.PIG_SOUND_VARIANT, StructuredDataKey.COW_SOUND_VARIANT, StructuredDataKey.CHICKEN_SOUND_VARIANT, StructuredDataKey.CAT_SOUND_VARIANT);

        tagRewriter.addEmptyTags(RegistryType.DAMAGE_TYPE, "damages_helmet", "bypasses_armor", "bypasses_shield", "bypasses_invulnerability", "bypasses_cooldown", "bypasses_effects",
            "bypasses_resistance", "bypasses_enchantments", "is_fire", "is_projectile", "witch_resistant_to", "is_explosion", "is_fall", "is_drowning", "is_freezing", "is_lightning", "no_anger",
            "no_impact", "always_most_significant_fall", "wither_immune_to", "ignites_armor_stands", "burns_armor_stands", "avoids_guardian_thorns", "always_triggers_silverfish",
            "always_hurts_ender_dragons", "no_knockback", "always_kills_armor_stands", "can_break_armor_stand", "bypasses_wolf_armor", "is_player_attack",
            "burn_from_stepping", "panic_causes", "panic_environmental_causes", "mace_smash");
        tagRewriter.addEmptyTags(RegistryType.BANNER_PATTERN, "no_item_required", "pattern_item/flower", "pattern_item/creeper", "pattern_item/skull", "pattern_item/mojang",
            "pattern_item/globe", "pattern_item/piglin", "pattern_item/flow", "pattern_item/guster", "pattern_item/field_masoned", "pattern_item/bordure_indented");
        super.onMappingDataLoaded();
    }

    @Override
    public MappingData getMappingData() {
        return MAPPINGS;
    }

    @Override
    public EntityPacketRewriter26_1 getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    public BlockItemPacketRewriter26_1 getItemRewriter() {
        return itemRewriter;
    }

    @Override
    public BlockRewriter<ClientboundPacket1_21_11> getBlockRewriter() {
        return blockRewriter;
    }

    @Override
    public RecipeDisplayRewriter<ClientboundPacket1_21_11> getRecipeRewriter() {
        return recipeRewriter;
    }

    @Override
    public RegistryDataRewriter getRegistryDataRewriter() {
        return registryDataRewriter;
    }

    @Override
    public ParticleRewriter<ClientboundPacket1_21_11> getParticleRewriter() {
        return particleRewriter;
    }

    @Override
    public TagRewriter<ClientboundPacket1_21_11> getTagRewriter() {
        return tagRewriter;
    }

    @Override
    public NBTComponentRewriter<ClientboundPacket1_21_11> getComponentRewriter() {
        return componentRewriter;
    }

    @Override
    public Types1_20_5<StructuredDataKeys1_21_11, EntityDataTypes1_21_11> types() {
        return VersionedTypes.V1_21_11;
    }

    @Override
    public Types1_20_5<StructuredDataKeys1_21_11, EntityDataTypes26_1> mappedTypes() {
        return VersionedTypes.V26_1;
    }

    @Override
    protected PacketTypesProvider<ClientboundPacket1_21_11, ClientboundPacket26_1, ServerboundPacket1_21_9, ServerboundPacket26_1> createPacketTypesProvider() {
        return new SimplePacketTypesProvider<>(
            packetTypeMap(unmappedClientboundPacketType, ClientboundPackets1_21_11.class, ClientboundConfigurationPackets1_21_9.class),
            packetTypeMap(mappedClientboundPacketType, ClientboundPackets26_1.class, ClientboundConfigurationPackets1_21_9.class),
            packetTypeMap(mappedServerboundPacketType, ServerboundPackets1_21_6.class, ServerboundConfigurationPackets1_21_9.class),
            packetTypeMap(unmappedServerboundPacketType, ServerboundPackets26_1.class, ServerboundConfigurationPackets1_21_9.class)
        );
    }
}
