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
package com.viaversion.viaversion.protocols.v1_21_9to1_21_11;

import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.FloatTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.minecraft.RegistryEntry;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.data.version.StructuredDataKeys1_21_11;
import com.viaversion.viaversion.api.minecraft.data.version.StructuredDataKeys1_21_5;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_11;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21_11;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21_9;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.packet.provider.SimplePacketTypesProvider;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import com.viaversion.viaversion.api.type.types.version.Types1_20_5;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.data.item.ItemHasherBase;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundConfigurationPackets1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundPacket1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundPackets1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ServerboundConfigurationPackets1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ServerboundPacket1_21_9;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.data.MappingData1_21_11;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ClientboundPacket1_21_11;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ClientboundPackets1_21_11;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.rewriter.BlockItemPacketRewriter1_21_11;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.rewriter.ComponentRewriter1_21_11;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.rewriter.EntityPacketRewriter1_21_11;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.storage.GameTimeStorage;
import com.viaversion.viaversion.rewriter.AttributeRewriter;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.RegistryDataRewriter;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.rewriter.text.NBTComponentRewriter;
import com.viaversion.viaversion.util.Key;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.viaversion.viaversion.util.ProtocolUtil.packetTypeMap;

public final class Protocol1_21_9To1_21_11 extends AbstractProtocol<ClientboundPacket1_21_9, ClientboundPacket1_21_11, ServerboundPacket1_21_9, ServerboundPacket1_21_9> {

    public static final MappingData1_21_11 MAPPINGS = new MappingData1_21_11();
    private final EntityPacketRewriter1_21_11 entityRewriter = new EntityPacketRewriter1_21_11(this);
    private final BlockItemPacketRewriter1_21_11 itemRewriter = new BlockItemPacketRewriter1_21_11(this);
    private final ParticleRewriter<ClientboundPacket1_21_9> particleRewriter = new ParticleRewriter<>(this);
    private final TagRewriter<ClientboundPacket1_21_9> tagRewriter = new TagRewriter<>(this);
    private final NBTComponentRewriter<ClientboundPacket1_21_9> componentRewriter = new ComponentRewriter1_21_11(this);
    private final RegistryDataRewriter registryDataRewriter = new RegistryDataRewriter(this);

    public Protocol1_21_9To1_21_11() {
        super(ClientboundPacket1_21_9.class, ClientboundPacket1_21_11.class, ServerboundPacket1_21_9.class, ServerboundPacket1_21_9.class);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        registerFinishConfiguration(ClientboundConfigurationPackets1_21_9.FINISH_CONFIGURATION, wrapper -> {
            final PacketWrapper zombieNautilusVariantsPacket = wrapper.create(ClientboundConfigurationPackets1_21_9.REGISTRY_DATA);
            zombieNautilusVariantsPacket.write(Types.STRING, "zombie_nautilus_variant");
            final CompoundTag temperateZombieNautilus = new CompoundTag();
            temperateZombieNautilus.putString("asset_id", "entity/zombie_nautilus/temperate");
            temperateZombieNautilus.put("spawn_conditions", new ListTag<>(CompoundTag.class));
            zombieNautilusVariantsPacket.write(Types.REGISTRY_ENTRY_ARRAY, new RegistryEntry[]{new RegistryEntry("minecraft:pale", temperateZombieNautilus)});
            zombieNautilusVariantsPacket.send(Protocol1_21_9To1_21_11.class);

            final PacketWrapper timelinePacket = wrapper.create(ClientboundConfigurationPackets1_21_9.REGISTRY_DATA);
            timelinePacket.write(Types.STRING, "timeline");
            final RegistryEntry[] timelineEntries = new RegistryEntry[MAPPINGS.timelineRegistry().size()];
            int index = 0;
            for (final Map.Entry<String, Tag> entry : MAPPINGS.timelineRegistry().entrySet()) {
                timelineEntries[index++] = new RegistryEntry(entry.getKey(), entry.getValue());
            }
            timelinePacket.write(Types.REGISTRY_ENTRY_ARRAY, timelineEntries);
            timelinePacket.send(Protocol1_21_9To1_21_11.class);
        });

        registryDataRewriter.addHandler("dimension_type", (key, tag) -> {
            final ByteTag trueTag = new ByteTag((byte) 1);
            final CompoundTag attributes = new CompoundTag();
            tag.put("attributes", attributes);

            if (Key.equals(key, "the_nether")) {
                tag.put("timelines", new ListTag<>(List.of(new StringTag("villager_schedule"))));
                tag.putString("skybox", "none");
                tag.putString("cardinal_light", "nether");
                attributes.putString("visual/sky_light_color", "#7a7aff");
                attributes.putFloat("visual/fog_start_distance", 10F);
                attributes.putFloat("visual/fog_end_distance", 96F);
                attributes.putFloat("gameplay/sky_light_level", 4F);
            } else if (Key.equals(key, "the_end")) {
                tag.put("timelines", new ListTag<>(List.of(new StringTag("villager_schedule"))));
                tag.putString("skybox", "end");
                attributes.putString("visual/fog_color", "#181318");
                attributes.putString("visual/sky_color", "#000000");
                attributes.putString("visual/sky_light_color", "#e580ff");
            } else {
                final ListTag<StringTag> timelines = new ListTag<>(List.of(new StringTag("day"), new StringTag("moon"), new StringTag("early_game")));
                tag.put("timelines", timelines);
            }

            if (!tag.getBoolean("natural")) {
                attributes.putFloat("visual/sky_light_factor", 0F);
            }
            if (tag.getBoolean("ultrawarm")) {
                final CompoundTag defaultDripstoneParticle = new CompoundTag();
                defaultDripstoneParticle.putString("type", "dripping_dripstone_lava");
                attributes.put("visual/default_dripstone_particle", defaultDripstoneParticle);
            }

            moveAttribute(tag, attributes, "cloud_height", "visual/cloud_height", cloudHeight -> {
                if (cloudHeight instanceof NumberTag numberTag) {
                    attributes.putString("visual/cloud_color", "#ccffffff");
                    return new FloatTag(numberTag.asFloat());
                }
                return null;
            }, null);
            moveAttribute(tag, attributes, "has_raids", "gameplay/can_start_raid", Function.identity(), trueTag);
            moveAttribute(tag, attributes, "piglin_safe", "gameplay/piglins_zombify", attributeTag -> ((NumberTag) attributeTag).asBoolean() ? ByteTag.ZERO : trueTag, ByteTag.ZERO);
            moveAttribute(tag, attributes, "respawn_anchor_works", "gameplay/respawn_anchor_works", Function.identity(), trueTag);
            moveAttribute(tag, attributes, "ultrawarm", "gameplay/fast_lava", Function.identity(), ByteTag.ZERO);
            moveAttribute(tag, attributes, "ultrawarm", "gameplay/water_evaporates", Function.identity(), ByteTag.ZERO);
        });
        registryDataRewriter.addHandler("worldgen/biome", (key, tag) -> {
            final CompoundTag effects = tag.getCompoundTag("effects");
            final CompoundTag attributes = new CompoundTag();
            tag.put("attributes", attributes);
            moveAttribute(effects, attributes, "sky_color", "visual/sky_color", Function.identity(), new IntTag(0));
            if (!Key.equals(key, "the_end")) {
                moveAttribute(effects, attributes, "water_fog_color", "visual/water_fog_color", Function.identity(), new IntTag(-16448205));
                moveAttribute(effects, attributes, "fog_color", "visual/fog_color", Function.identity(), new IntTag(0));
            }
        });
        registerClientbound(ClientboundConfigurationPackets1_21_9.REGISTRY_DATA, registryDataRewriter::handle);

        tagRewriter.registerGeneric(ClientboundPackets1_21_9.UPDATE_TAGS);
        tagRewriter.registerGeneric(ClientboundConfigurationPackets1_21_9.UPDATE_TAGS);

        componentRewriter.registerOpenScreen1_14(ClientboundPackets1_21_9.OPEN_SCREEN);
        componentRewriter.registerComponentPacket(ClientboundPackets1_21_9.SET_ACTION_BAR_TEXT);
        componentRewriter.registerComponentPacket(ClientboundPackets1_21_9.SET_TITLE_TEXT);
        componentRewriter.registerComponentPacket(ClientboundPackets1_21_9.SET_SUBTITLE_TEXT);
        componentRewriter.registerBossEvent(ClientboundPackets1_21_9.BOSS_EVENT);
        componentRewriter.registerComponentPacket(ClientboundPackets1_21_9.DISCONNECT);
        componentRewriter.registerTabList(ClientboundPackets1_21_9.TAB_LIST);
        componentRewriter.registerPlayerCombatKill1_20(ClientboundPackets1_21_9.PLAYER_COMBAT_KILL);
        componentRewriter.registerPlayerInfoUpdate1_21_4(ClientboundPackets1_21_9.PLAYER_INFO_UPDATE);
        componentRewriter.registerComponentPacket(ClientboundPackets1_21_9.SYSTEM_CHAT);
        componentRewriter.registerDisguisedChat(ClientboundPackets1_21_9.DISGUISED_CHAT);
        componentRewriter.registerPlayerChat1_21_5(ClientboundPackets1_21_9.PLAYER_CHAT);
        componentRewriter.registerPing();

        particleRewriter.registerLevelParticles1_21_4(ClientboundPackets1_21_9.LEVEL_PARTICLES);
        particleRewriter.registerExplode1_21_9(ClientboundPackets1_21_9.EXPLODE); // Rewrites the included sound and particles

        final SoundRewriter<ClientboundPacket1_21_9> soundRewriter = new SoundRewriter<>(this);
        soundRewriter.registerSound1_19_3(ClientboundPackets1_21_9.SOUND);
        soundRewriter.registerSound1_19_3(ClientboundPackets1_21_9.SOUND_ENTITY);

        new StatisticsRewriter<>(this).register(ClientboundPackets1_21_9.AWARD_STATS);
        new AttributeRewriter<>(this).register1_21(ClientboundPackets1_21_9.UPDATE_ATTRIBUTES);
    }

    @Override
    protected void onMappingDataLoaded() {
        EntityTypes1_21_11.initialize(this);
        mappedTypes().particle.filler(this)
            .reader("block", ParticleType.Readers.BLOCK)
            .reader("block_marker", ParticleType.Readers.BLOCK)
            .reader("dust_pillar", ParticleType.Readers.BLOCK)
            .reader("falling_dust", ParticleType.Readers.BLOCK)
            .reader("block_crumble", ParticleType.Readers.BLOCK)
            .reader("dust", ParticleType.Readers.DUST1_21_2)
            .reader("dust_color_transition", ParticleType.Readers.DUST_TRANSITION1_21_2)
            .reader("vibration", ParticleType.Readers.VIBRATION1_20_3)
            .reader("sculk_charge", ParticleType.Readers.SCULK_CHARGE)
            .reader("shriek", ParticleType.Readers.SHRIEK)
            .reader("entity_effect", ParticleType.Readers.COLOR)
            .reader("tinted_leaves", ParticleType.Readers.COLOR)
            .reader("trail", ParticleType.Readers.TRAIL1_21_4)
            .reader("dragon_breath", ParticleType.Readers.POWER)
            .reader("effect", ParticleType.Readers.SPELL)
            .reader("instant_effect", ParticleType.Readers.SPELL)
            .reader("flash", ParticleType.Readers.COLOR)
            .reader("item", ParticleType.Readers.item(itemRewriter.mappedItemType()));
        mappedTypes().structuredData.filler(this).add(StructuredDataKey.CUSTOM_DATA, StructuredDataKey.MAX_STACK_SIZE, StructuredDataKey.MAX_DAMAGE,
            StructuredDataKey.UNBREAKABLE1_21_5, StructuredDataKey.RARITY, StructuredDataKey.TOOLTIP_DISPLAY, StructuredDataKey.DAMAGE_RESISTANT,
            StructuredDataKey.CUSTOM_NAME, StructuredDataKey.LORE, StructuredDataKey.ENCHANTMENTS1_21_5,
            StructuredDataKey.CUSTOM_MODEL_DATA1_21_4, StructuredDataKey.BLOCKS_ATTACKS, StructuredDataKey.PROVIDES_BANNER_PATTERNS,
            StructuredDataKey.REPAIR_COST, StructuredDataKey.CREATIVE_SLOT_LOCK, StructuredDataKey.ENCHANTMENT_GLINT_OVERRIDE,
            StructuredDataKey.INTANGIBLE_PROJECTILE, StructuredDataKey.STORED_ENCHANTMENTS1_21_5, StructuredDataKey.DYED_COLOR1_21_5,
            StructuredDataKey.MAP_COLOR, StructuredDataKey.MAP_ID, StructuredDataKey.MAP_DECORATIONS, StructuredDataKey.MAP_POST_PROCESSING,
            StructuredDataKey.POTION_CONTENTS1_21_2, StructuredDataKey.SUSPICIOUS_STEW_EFFECTS, StructuredDataKey.WRITABLE_BOOK_CONTENT,
            StructuredDataKey.WRITTEN_BOOK_CONTENT, StructuredDataKey.TRIM1_21_5, StructuredDataKey.DEBUG_STICK_STATE, StructuredDataKey.ENTITY_DATA1_21_9,
            StructuredDataKey.BUCKET_ENTITY_DATA, StructuredDataKey.BLOCK_ENTITY_DATA1_21_9, StructuredDataKey.INSTRUMENT1_21_5,
            StructuredDataKey.RECIPES, StructuredDataKey.LODESTONE_TRACKER, StructuredDataKey.FIREWORK_EXPLOSION, StructuredDataKey.FIREWORKS,
            StructuredDataKey.PROFILE1_21_9, StructuredDataKey.NOTE_BLOCK_SOUND, StructuredDataKey.BANNER_PATTERNS, StructuredDataKey.BASE_COLOR,
            StructuredDataKey.POT_DECORATIONS, StructuredDataKey.BLOCK_STATE, StructuredDataKey.BEES1_21_9, StructuredDataKey.LOCK1_21_2,
            StructuredDataKey.CONTAINER_LOOT, StructuredDataKey.TOOL1_21_5, StructuredDataKey.ITEM_NAME, StructuredDataKey.OMINOUS_BOTTLE_AMPLIFIER,
            StructuredDataKey.FOOD1_21_2, StructuredDataKey.JUKEBOX_PLAYABLE1_21_5, StructuredDataKey.ATTRIBUTE_MODIFIERS1_21_6,
            StructuredDataKey.REPAIRABLE, StructuredDataKey.ENCHANTABLE, StructuredDataKey.CONSUMABLE1_21_2, StructuredDataKey.ATTACK_RANGE,
            StructuredDataKey.USE_COOLDOWN, StructuredDataKey.DAMAGE, StructuredDataKey.EQUIPPABLE1_21_6, StructuredDataKey.ITEM_MODEL,
            StructuredDataKey.GLIDER, StructuredDataKey.TOOLTIP_STYLE, StructuredDataKey.DEATH_PROTECTION, StructuredDataKey.WEAPON,
            StructuredDataKey.POTION_DURATION_SCALE, StructuredDataKey.VILLAGER_VARIANT, StructuredDataKey.WOLF_VARIANT, StructuredDataKey.WOLF_COLLAR,
            StructuredDataKey.FOX_VARIANT, StructuredDataKey.SALMON_SIZE, StructuredDataKey.PARROT_VARIANT, StructuredDataKey.TROPICAL_FISH_PATTERN,
            StructuredDataKey.TROPICAL_FISH_BASE_COLOR, StructuredDataKey.TROPICAL_FISH_PATTERN_COLOR, StructuredDataKey.MOOSHROOM_VARIANT,
            StructuredDataKey.RABBIT_VARIANT, StructuredDataKey.PIG_VARIANT, StructuredDataKey.FROG_VARIANT, StructuredDataKey.HORSE_VARIANT,
            StructuredDataKey.PAINTING_VARIANT, StructuredDataKey.LLAMA_VARIANT, StructuredDataKey.AXOLOTL_VARIANT, StructuredDataKey.CAT_VARIANT,
            StructuredDataKey.CAT_COLLAR, StructuredDataKey.SHEEP_COLOR, StructuredDataKey.SHULKER_COLOR, StructuredDataKey.PROVIDES_TRIM_MATERIAL,
            StructuredDataKey.BREAK_SOUND, StructuredDataKey.COW_VARIANT, StructuredDataKey.CHICKEN_VARIANT, StructuredDataKey.WOLF_SOUND_VARIANT,
            StructuredDataKey.USE_EFFECTS, StructuredDataKey.MINIMUM_ATTACK_CHARGE, StructuredDataKey.DAMAGE_TYPE, StructuredDataKey.PIERCING_WEAPON,
            StructuredDataKey.KINETIC_WEAPON, StructuredDataKey.SWING_ANIMATION, StructuredDataKey.ZOMBIE_NAUTILUS_VARIANT);
        super.onMappingDataLoaded();
    }

    private void moveAttribute(
        final CompoundTag baseTag, final CompoundTag attributes,
        final String key, final String mappedKey,
        final Function<Tag, Tag> tagMapper, @Nullable final Tag defaultTag
    ) {
        final Tag attributeTag = baseTag.get(key);
        if (attributeTag != null) {
            attributes.put(mappedKey, tagMapper.apply(attributeTag));
        } else if (defaultTag != null) {
            attributes.put(mappedKey, defaultTag);
        }
    }

    @Override
    public void init(final UserConnection connection) {
        addEntityTracker(connection, new EntityTrackerBase(connection, EntityTypes1_21_11.PLAYER));
        addItemHasher(connection, new ItemHasherBase(this, connection));
        connection.put(new GameTimeStorage());
    }

    @Override
    public MappingData getMappingData() {
        return MAPPINGS;
    }

    @Override
    public EntityPacketRewriter1_21_11 getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    public BlockItemPacketRewriter1_21_11 getItemRewriter() {
        return itemRewriter;
    }

    @Override
    public RegistryDataRewriter getRegistryDataRewriter() {
        return registryDataRewriter;
    }

    @Override
    public ParticleRewriter<ClientboundPacket1_21_9> getParticleRewriter() {
        return particleRewriter;
    }

    @Override
    public TagRewriter<ClientboundPacket1_21_9> getTagRewriter() {
        return tagRewriter;
    }

    @Override
    public NBTComponentRewriter<ClientboundPacket1_21_9> getComponentRewriter() {
        return componentRewriter;
    }

    @Override
    public Types1_20_5<StructuredDataKeys1_21_5, EntityDataTypes1_21_9> types() {
        return VersionedTypes.V1_21_9;
    }

    @Override
    public Types1_20_5<StructuredDataKeys1_21_11, EntityDataTypes1_21_11> mappedTypes() {
        return VersionedTypes.V1_21_11;
    }

    @Override
    protected PacketTypesProvider<ClientboundPacket1_21_9, ClientboundPacket1_21_11, ServerboundPacket1_21_9, ServerboundPacket1_21_9> createPacketTypesProvider() {
        return new SimplePacketTypesProvider<>(
            packetTypeMap(unmappedClientboundPacketType, ClientboundPackets1_21_9.class, ClientboundConfigurationPackets1_21_9.class),
            packetTypeMap(mappedClientboundPacketType, ClientboundPackets1_21_11.class, ClientboundConfigurationPackets1_21_9.class),
            packetTypeMap(mappedServerboundPacketType, ServerboundPackets1_21_6.class, ServerboundConfigurationPackets1_21_9.class),
            packetTypeMap(unmappedServerboundPacketType, ServerboundPackets1_21_6.class, ServerboundConfigurationPackets1_21_9.class)
        );
    }
}
