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
package com.viaversion.viaversion.protocols.v1_20_5to1_21;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.ChatType;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.packet.provider.SimplePacketTypesProvider;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.api.type.types.version.VersionedTypesHolder;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundPacket1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPacket1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.data.AttributeModifierMappings1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.data.MappingData1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundConfigurationPackets1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPacket1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.rewriter.BlockItemPacketRewriter1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.rewriter.ComponentRewriter1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.rewriter.EntityPacketRewriter1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.storage.EfficiencyAttributeStorage;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.storage.PlayerPositionStorage;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.rewriter.text.JsonNBTComponentRewriter;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.UUIDUtil;
import java.util.Locale;
import java.util.UUID;

import static com.viaversion.viaversion.util.ProtocolUtil.packetTypeMap;

public final class Protocol1_20_5To1_21 extends AbstractProtocol<ClientboundPacket1_20_5, ClientboundPacket1_21, ServerboundPacket1_20_5, ServerboundPacket1_20_5> {

    public static final MappingData1_21 MAPPINGS = new MappingData1_21();
    private final EntityPacketRewriter1_21 entityRewriter = new EntityPacketRewriter1_21(this);
    private final BlockItemPacketRewriter1_21 itemRewriter = new BlockItemPacketRewriter1_21(this);
    private final ParticleRewriter<ClientboundPacket1_20_5> particleRewriter = new ParticleRewriter<>(this);
    private final TagRewriter<ClientboundPacket1_20_5> tagRewriter = new TagRewriter<>(this);
    private final JsonNBTComponentRewriter<ClientboundPacket1_20_5> componentRewriter = new ComponentRewriter1_21(this);

    public Protocol1_20_5To1_21() {
        super(ClientboundPacket1_20_5.class, ClientboundPacket1_21.class, ServerboundPacket1_20_5.class, ServerboundPacket1_20_5.class);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        tagRewriter.registerGeneric(ClientboundPackets1_20_5.UPDATE_TAGS);
        tagRewriter.registerGeneric(ClientboundConfigurationPackets1_20_5.UPDATE_TAGS);

        final SoundRewriter<ClientboundPacket1_20_5> soundRewriter = new SoundRewriter<>(this);
        soundRewriter.registerSound1_19_3(ClientboundPackets1_20_5.SOUND);
        soundRewriter.registerSound1_19_3(ClientboundPackets1_20_5.SOUND_ENTITY);

        new StatisticsRewriter<>(this).register(ClientboundPackets1_20_5.AWARD_STATS);

        componentRewriter.registerOpenScreen1_14(ClientboundPackets1_20_5.OPEN_SCREEN);
        componentRewriter.registerComponentPacket(ClientboundPackets1_20_5.SET_ACTION_BAR_TEXT);
        componentRewriter.registerComponentPacket(ClientboundPackets1_20_5.SET_TITLE_TEXT);
        componentRewriter.registerComponentPacket(ClientboundPackets1_20_5.SET_SUBTITLE_TEXT);
        componentRewriter.registerBossEvent(ClientboundPackets1_20_5.BOSS_EVENT);
        componentRewriter.registerComponentPacket(ClientboundPackets1_20_5.DISCONNECT);
        componentRewriter.registerTabList(ClientboundPackets1_20_5.TAB_LIST);
        componentRewriter.registerPlayerCombatKill1_20(ClientboundPackets1_20_5.PLAYER_COMBAT_KILL);
        componentRewriter.registerComponentPacket(ClientboundPackets1_20_5.SYSTEM_CHAT);

        particleRewriter.registerLevelParticles1_20_5(ClientboundPackets1_20_5.LEVEL_PARTICLES);
        particleRewriter.registerExplode1_20_5(ClientboundPackets1_20_5.EXPLODE); // Rewrites the included sound and particles

        registerClientbound(ClientboundPackets1_20_5.DISGUISED_CHAT, wrapper -> {
            componentRewriter.processTag(wrapper.user(), wrapper.passthrough(Types.TAG)); // Message

            // Holder time
            final int chatType = wrapper.read(Types.VAR_INT);
            wrapper.write(ChatType.TYPE, Holder.of(chatType));
        });
        registerClientbound(ClientboundPackets1_20_5.PLAYER_CHAT, wrapper -> {
            wrapper.passthrough(Types.UUID); // Sender
            wrapper.passthrough(Types.VAR_INT); // Index
            wrapper.passthrough(Types.OPTIONAL_SIGNATURE_BYTES); // Signature
            wrapper.passthrough(Types.STRING); // Plain content
            wrapper.passthrough(Types.LONG); // Timestamp
            wrapper.passthrough(Types.LONG); // Salt

            final int lastSeen = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < lastSeen; i++) {
                final int index = wrapper.passthrough(Types.VAR_INT);
                if (index == 0) {
                    wrapper.passthrough(Types.SIGNATURE_BYTES);
                }
            }

            componentRewriter.processTag(wrapper.user(), wrapper.passthrough(Types.OPTIONAL_TAG)); // Unsigned content

            final int filterMaskType = wrapper.passthrough(Types.VAR_INT);
            if (filterMaskType == 2) {
                wrapper.passthrough(Types.LONG_ARRAY_PRIMITIVE); // Mask
            }

            final int chatType = wrapper.read(Types.VAR_INT);
            wrapper.write(ChatType.TYPE, Holder.of(chatType));
            componentRewriter.processTag(wrapper.user(), wrapper.passthrough(Types.TAG)); // Name
            componentRewriter.processTag(wrapper.user(), wrapper.passthrough(Types.OPTIONAL_TAG)); // Target Name
        });

        registerClientbound(ClientboundPackets1_20_5.UPDATE_ATTRIBUTES, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Entity ID

            final int size = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < size; i++) {
                final int attributeId = wrapper.read(Types.VAR_INT);
                wrapper.write(Types.VAR_INT, MAPPINGS.getNewAttributeId(attributeId));
                wrapper.passthrough(Types.DOUBLE); // Base
                final int modifierSize = wrapper.passthrough(Types.VAR_INT);
                for (int j = 0; j < modifierSize; j++) {
                    final UUID uuid = wrapper.read(Types.UUID);
                    wrapper.write(Types.STRING, mapAttributeUUID(uuid, null));
                    wrapper.passthrough(Types.DOUBLE); // Amount
                    wrapper.passthrough(Types.BYTE); // Operation
                }
            }
        });

        registerClientbound(ClientboundPackets1_20_5.PROJECTILE_POWER, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Id
            final double xPower = wrapper.read(Types.DOUBLE);
            final double yPower = wrapper.read(Types.DOUBLE);
            final double zPower = wrapper.read(Types.DOUBLE);
            final double accelerationPower = Math.sqrt(xPower * xPower + yPower * yPower + zPower * zPower);
            wrapper.write(Types.DOUBLE, accelerationPower);
        });
    }

    public static String mapAttributeUUID(final UUID uuid, final String name) {
        String id = AttributeModifierMappings1_21.uuidToId(uuid);
        if (id != null) {
            return id;
        }
        if (name != null) {
            id = AttributeModifierMappings1_21.nameToId(name);
        }
        return id != null ? id : uuid.toString().toLowerCase(Locale.ROOT);
    }

    public static UUID mapAttributeId(final String id) {
        UUID uuid = AttributeModifierMappings1_21.idToUuid(id);
        if (uuid != null) {
            return uuid;
        }

        uuid = UUIDUtil.parseUUID(Key.stripNamespace(id).toUpperCase(Locale.ROOT));
        return uuid != null ? uuid : UUID.randomUUID();
    }

    @Override
    protected void onMappingDataLoaded() {
        super.onMappingDataLoaded();

        VersionedTypes.V1_21.particle.filler(this)
            .reader("block", ParticleType.Readers.BLOCK)
            .reader("block_marker", ParticleType.Readers.BLOCK)
            .reader("dust", ParticleType.Readers.DUST)
            .reader("dust_pillar", ParticleType.Readers.BLOCK)
            .reader("falling_dust", ParticleType.Readers.BLOCK)
            .reader("dust_color_transition", ParticleType.Readers.DUST_TRANSITION)
            .reader("item", ParticleType.Readers.item(VersionedTypes.V1_21.item))
            .reader("vibration", ParticleType.Readers.VIBRATION1_20_3)
            .reader("sculk_charge", ParticleType.Readers.SCULK_CHARGE)
            .reader("shriek", ParticleType.Readers.SHRIEK)
            .reader("entity_effect", ParticleType.Readers.COLOR);
        VersionedTypes.V1_21.structuredData.filler(this)
            .add(StructuredDataKey.CUSTOM_DATA).add(StructuredDataKey.MAX_STACK_SIZE).add(StructuredDataKey.MAX_DAMAGE)
            .add(StructuredDataKey.DAMAGE).add(StructuredDataKey.UNBREAKABLE1_20_5).add(StructuredDataKey.RARITY)
            .add(StructuredDataKey.HIDE_TOOLTIP).add(StructuredDataKey.FIRE_RESISTANT)
            .add(StructuredDataKey.CUSTOM_NAME).add(StructuredDataKey.LORE).add(StructuredDataKey.ENCHANTMENTS1_20_5)
            .add(StructuredDataKey.CAN_PLACE_ON1_20_5).add(StructuredDataKey.CAN_BREAK1_20_5)
            .add(StructuredDataKey.CUSTOM_MODEL_DATA1_20_5).add(StructuredDataKey.HIDE_ADDITIONAL_TOOLTIP).add(StructuredDataKey.REPAIR_COST)
            .add(StructuredDataKey.CREATIVE_SLOT_LOCK).add(StructuredDataKey.ENCHANTMENT_GLINT_OVERRIDE).add(StructuredDataKey.INTANGIBLE_PROJECTILE)
            .add(StructuredDataKey.STORED_ENCHANTMENTS1_20_5).add(StructuredDataKey.DYED_COLOR1_20_5).add(StructuredDataKey.MAP_COLOR)
            .add(StructuredDataKey.MAP_ID).add(StructuredDataKey.MAP_DECORATIONS).add(StructuredDataKey.MAP_POST_PROCESSING)
            .add(StructuredDataKey.POTION_CONTENTS1_20_5)
            .add(StructuredDataKey.SUSPICIOUS_STEW_EFFECTS).add(StructuredDataKey.WRITABLE_BOOK_CONTENT).add(StructuredDataKey.WRITTEN_BOOK_CONTENT)
            .add(StructuredDataKey.TRIM1_20_5).add(StructuredDataKey.DEBUG_STICK_STATE).add(StructuredDataKey.ENTITY_DATA)
            .add(StructuredDataKey.BUCKET_ENTITY_DATA).add(StructuredDataKey.BLOCK_ENTITY_DATA).add(StructuredDataKey.INSTRUMENT1_20_5)
            .add(StructuredDataKey.RECIPES).add(StructuredDataKey.LODESTONE_TRACKER).add(StructuredDataKey.FIREWORK_EXPLOSION)
            .add(StructuredDataKey.FIREWORKS).add(StructuredDataKey.PROFILE).add(StructuredDataKey.NOTE_BLOCK_SOUND)
            .add(StructuredDataKey.BANNER_PATTERNS).add(StructuredDataKey.BASE_COLOR).add(StructuredDataKey.POT_DECORATIONS)
            .add(StructuredDataKey.BLOCK_STATE).add(StructuredDataKey.BEES)
            .add(StructuredDataKey.LOCK).add(StructuredDataKey.CONTAINER_LOOT).add(StructuredDataKey.TOOL1_20_5)
            .add(StructuredDataKey.ITEM_NAME).add(StructuredDataKey.OMINOUS_BOTTLE_AMPLIFIER)
            .add(StructuredDataKey.FOOD1_21).add(StructuredDataKey.JUKEBOX_PLAYABLE1_21).add(StructuredDataKey.ATTRIBUTE_MODIFIERS1_21);

        tagRewriter.addEmptyTags(RegistryType.BLOCK, "minecraft:blocks_wind_charge_explosions");
        tagRewriter.addEmptyTags(RegistryType.ENTITY, "minecraft:can_turn_in_boats", "minecraft:deflects_projectiles", "minecraft:immune_to_infested",
            "minecraft:immune_to_oozing", "minecraft:no_anger_from_wind_charge");
        tagRewriter.addTag(RegistryType.ENCHANTMENT, "minecraft:curse", 10, 41); // Binding and vanishing curse
        // Add other enchantment tags empty
        tagRewriter.addEmptyTags(RegistryType.ENCHANTMENT, "double_trade_price", "in_enchanting_table", "non_treasure", "on_mob_spawn_equipment", "on_random_loot",
            "on_traded_equipment", "prevents_bee_spawns_when_mining", "prevents_decorated_pot_shattering", "prevents_ice_melting", "prevents_infested_spawns", "smelts_loot",
            "tooltip_order", "tradeable", "treasure", "exclusive_set/armor", "exclusive_set/boots", "exclusive_set/bow", "exclusive_set/crossbow", "exclusive_set/damage",
            "exclusive_set/mining", "exclusive_set/riptide");
    }

    @Override
    public void init(final UserConnection connection) {
        addEntityTracker(connection, new EntityTrackerBase(connection, EntityTypes1_20_5.PLAYER));
        connection.put(new EfficiencyAttributeStorage());
        connection.put(new PlayerPositionStorage());
    }

    @Override
    public MappingData1_21 getMappingData() {
        return MAPPINGS;
    }

    @Override
    public EntityPacketRewriter1_21 getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    public BlockItemPacketRewriter1_21 getItemRewriter() {
        return itemRewriter;
    }

    @Override
    public ParticleRewriter<ClientboundPacket1_20_5> getParticleRewriter() {
        return particleRewriter;
    }

    @Override
    public TagRewriter<ClientboundPacket1_20_5> getTagRewriter() {
        return tagRewriter;
    }

    @Override
    public JsonNBTComponentRewriter<ClientboundPacket1_20_5> getComponentRewriter() {
        return componentRewriter;
    }

    @Override
    public VersionedTypesHolder types() {
        return VersionedTypes.V1_20_5;
    }

    @Override
    public VersionedTypesHolder mappedTypes() {
        return VersionedTypes.V1_21;
    }

    @Override
    protected PacketTypesProvider<ClientboundPacket1_20_5, ClientboundPacket1_21, ServerboundPacket1_20_5, ServerboundPacket1_20_5> createPacketTypesProvider() {
        return new SimplePacketTypesProvider<>(
            packetTypeMap(unmappedClientboundPacketType, ClientboundPackets1_20_5.class, ClientboundConfigurationPackets1_20_5.class),
            packetTypeMap(mappedClientboundPacketType, ClientboundPackets1_21.class, ClientboundConfigurationPackets1_21.class),
            packetTypeMap(mappedServerboundPacketType, ServerboundPackets1_20_5.class, ServerboundConfigurationPackets1_20_5.class),
            packetTypeMap(unmappedServerboundPacketType, ServerboundPackets1_20_5.class, ServerboundConfigurationPackets1_20_5.class)
        );
    }
}
