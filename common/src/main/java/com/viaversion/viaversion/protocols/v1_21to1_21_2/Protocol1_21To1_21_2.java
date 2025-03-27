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
package com.viaversion.viaversion.protocols.v1_21to1_21_2;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_2;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.packet.provider.SimplePacketTypesProvider;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.rewriter.ComponentRewriter;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import com.viaversion.viaversion.api.type.types.version.Types1_21_2;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPacket1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundConfigurationPackets1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPacket1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPacket1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ServerboundPacket1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ServerboundPackets1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.rewriter.BlockItemPacketRewriter1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.rewriter.ComponentRewriter1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.rewriter.EntityPacketRewriter1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.rewriter.ParticleRewriter1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.storage.BundleStateTracker;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.storage.ChunkLoadTracker;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.storage.EntityTracker1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.storage.GroundFlagTracker;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.storage.PlayerPositionStorage;
import com.viaversion.viaversion.rewriter.AttributeRewriter;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import java.util.BitSet;

import static com.viaversion.viaversion.util.ProtocolUtil.packetTypeMap;

public final class Protocol1_21To1_21_2 extends AbstractProtocol<ClientboundPacket1_21, ClientboundPacket1_21_2, ServerboundPacket1_20_5, ServerboundPacket1_21_2> {

    public static final MappingData MAPPINGS = new MappingDataBase("1.21", "1.21.2");
    private final EntityPacketRewriter1_21_2 entityRewriter = new EntityPacketRewriter1_21_2(this);
    private final BlockItemPacketRewriter1_21_2 itemRewriter = new BlockItemPacketRewriter1_21_2(this);
    private final ParticleRewriter1_21_2 particleRewriter = new ParticleRewriter1_21_2(this);
    private final TagRewriter<ClientboundPacket1_21> tagRewriter = new TagRewriter<>(this);
    private final ComponentRewriter1_21_2 componentRewriter = new ComponentRewriter1_21_2(this);
    private final SoundRewriter<ClientboundPacket1_21> soundRewriter = new SoundRewriter<>(this);

    public Protocol1_21To1_21_2() {
        super(ClientboundPacket1_21.class, ClientboundPacket1_21_2.class, ServerboundPacket1_20_5.class, ServerboundPacket1_21_2.class);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        tagRewriter.registerGeneric(ClientboundPackets1_21.UPDATE_TAGS);
        tagRewriter.registerGeneric(ClientboundConfigurationPackets1_21.UPDATE_TAGS);

        componentRewriter.registerOpenScreen1_14(ClientboundPackets1_21.OPEN_SCREEN);
        componentRewriter.registerComponentPacket(ClientboundPackets1_21.SET_ACTION_BAR_TEXT);
        componentRewriter.registerComponentPacket(ClientboundPackets1_21.SET_TITLE_TEXT);
        componentRewriter.registerComponentPacket(ClientboundPackets1_21.SET_SUBTITLE_TEXT);
        componentRewriter.registerBossEvent(ClientboundPackets1_21.BOSS_EVENT);
        componentRewriter.registerComponentPacket(ClientboundPackets1_21.DISCONNECT);
        componentRewriter.registerTabList(ClientboundPackets1_21.TAB_LIST);
        componentRewriter.registerPlayerCombatKill1_20(ClientboundPackets1_21.PLAYER_COMBAT_KILL);
        componentRewriter.registerComponentPacket(ClientboundPackets1_21.SYSTEM_CHAT);
        componentRewriter.registerDisguisedChat(ClientboundPackets1_21.DISGUISED_CHAT);
        componentRewriter.registerPing();

        particleRewriter.registerLevelParticles1_20_5(ClientboundPackets1_21.LEVEL_PARTICLES);

        soundRewriter.registerSound1_19_3(ClientboundPackets1_21.SOUND);
        soundRewriter.registerSound1_19_3(ClientboundPackets1_21.SOUND_ENTITY);

        new StatisticsRewriter<>(this).register(ClientboundPackets1_21.AWARD_STATS);
        new AttributeRewriter<>(this).register1_21(ClientboundPackets1_21.UPDATE_ATTRIBUTES);

        registerServerbound(ServerboundPackets1_21_2.CLIENT_INFORMATION, this::clientInformation);
        registerServerbound(ServerboundConfigurationPackets1_20_5.CLIENT_INFORMATION, this::clientInformation);

        cancelServerbound(ServerboundPackets1_21_2.BUNDLE_ITEM_SELECTED);
        cancelServerbound(ServerboundPackets1_21_2.CLIENT_TICK_END);

        registerClientbound(State.LOGIN, ClientboundLoginPackets.LOGIN_FINISHED, wrapper -> {
            wrapper.passthrough(Types.UUID); // UUID
            wrapper.passthrough(Types.STRING); // Name

            final int properties = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < properties; i++) {
                wrapper.passthrough(Types.STRING); // Name
                wrapper.passthrough(Types.STRING); // Value
                wrapper.passthrough(Types.OPTIONAL_STRING); // Signature
            }

            wrapper.read(Types.BOOLEAN); // Strict error handling
        });

        registerClientbound(ClientboundPackets1_21.SET_TIME, wrapper -> {
            wrapper.passthrough(Types.LONG); // Game time
            long dayTime = wrapper.read(Types.LONG);
            boolean doDaylightCycle = true;
            if (dayTime < 0) {
                dayTime = -dayTime;
                doDaylightCycle = false;
            }
            wrapper.write(Types.LONG, dayTime);
            wrapper.write(Types.BOOLEAN, doDaylightCycle);
        });

        registerClientbound(ClientboundPackets1_21.PLAYER_INFO_UPDATE, wrapper -> {
            final BitSet actions = wrapper.passthroughAndMap(Types.PROFILE_ACTIONS_ENUM1_19_3, Types.PROFILE_ACTIONS_ENUM1_21_2);
            if (!actions.get(5)) { // Update display name
                return;
            }

            final int entries = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < entries; i++) {
                wrapper.passthrough(Types.UUID);
                if (actions.get(0)) {
                    wrapper.passthrough(Types.STRING); // Player Name

                    final int properties = wrapper.passthrough(Types.VAR_INT);
                    for (int j = 0; j < properties; j++) {
                        wrapper.passthrough(Types.STRING); // Name
                        wrapper.passthrough(Types.STRING); // Value
                        wrapper.passthrough(Types.OPTIONAL_STRING); // Signature
                    }
                }
                if (actions.get(1) && wrapper.passthrough(Types.BOOLEAN)) {
                    wrapper.passthrough(Types.UUID); // Session UUID
                    wrapper.passthrough(Types.PROFILE_KEY);
                }
                if (actions.get(2)) {
                    wrapper.passthrough(Types.VAR_INT); // Gamemode
                }
                if (actions.get(3)) {
                    wrapper.passthrough(Types.BOOLEAN); // Listed
                }
                if (actions.get(4)) {
                    wrapper.passthrough(Types.VAR_INT); // Latency
                }
                componentRewriter.processTag(wrapper.user(), wrapper.passthrough(Types.OPTIONAL_TAG));
            }
        });

        registerClientbound(ClientboundPackets1_21.BUNDLE_DELIMITER, wrapper -> wrapper.user().get(BundleStateTracker.class).toggleBundling());
        registerServerbound(ServerboundPackets1_21_2.PONG, wrapper -> {
            final int id = wrapper.passthrough(Types.INT); // id
            final PlayerPositionStorage playerPositionStorage = wrapper.user().get(PlayerPositionStorage.class);
            if (playerPositionStorage != null && playerPositionStorage.checkPong(id)) {
                wrapper.cancel();
            }
        });
    }

    private void clientInformation(final PacketWrapper wrapper) {
        wrapper.passthrough(Types.STRING); // Locale
        wrapper.passthrough(Types.BYTE); // View distance
        wrapper.passthrough(Types.VAR_INT); // Chat visibility
        wrapper.passthrough(Types.BOOLEAN); // Chat colors
        wrapper.passthrough(Types.UNSIGNED_BYTE); // Skin parts
        wrapper.passthrough(Types.VAR_INT); // Main hand
        wrapper.passthrough(Types.BOOLEAN); // Text filtering enabled
        wrapper.passthrough(Types.BOOLEAN); // Allow listing
        wrapper.read(Types.VAR_INT); // Particle status
    }

    @Override
    protected void onMappingDataLoaded() {
        EntityTypes1_21_2.initialize(this);
        Types1_21_2.PARTICLE.filler(this)
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
            .reader("trail", ParticleType.Readers.TRAIL1_21_2)
            .reader("item", ParticleType.Readers.item(Types1_21_2.ITEM));
        Types1_21_2.STRUCTURED_DATA.filler(this).add(StructuredDataKey.CUSTOM_DATA, StructuredDataKey.MAX_STACK_SIZE, StructuredDataKey.MAX_DAMAGE,
            StructuredDataKey.UNBREAKABLE1_20_5, StructuredDataKey.RARITY, StructuredDataKey.HIDE_TOOLTIP, StructuredDataKey.DAMAGE_RESISTANT,
            StructuredDataKey.CUSTOM_NAME, StructuredDataKey.LORE, StructuredDataKey.ENCHANTMENTS1_20_5, StructuredDataKey.CAN_PLACE_ON1_20_5,
            StructuredDataKey.CAN_BREAK1_20_5, StructuredDataKey.CUSTOM_MODEL_DATA1_20_5, StructuredDataKey.HIDE_ADDITIONAL_TOOLTIP,
            StructuredDataKey.REPAIR_COST, StructuredDataKey.CREATIVE_SLOT_LOCK, StructuredDataKey.ENCHANTMENT_GLINT_OVERRIDE,
            StructuredDataKey.INTANGIBLE_PROJECTILE, StructuredDataKey.STORED_ENCHANTMENTS1_20_5, StructuredDataKey.DYED_COLOR1_20_5,
            StructuredDataKey.MAP_COLOR, StructuredDataKey.MAP_ID, StructuredDataKey.MAP_DECORATIONS, StructuredDataKey.MAP_POST_PROCESSING,
            StructuredDataKey.POTION_CONTENTS1_21_2, StructuredDataKey.SUSPICIOUS_STEW_EFFECTS, StructuredDataKey.WRITABLE_BOOK_CONTENT,
            StructuredDataKey.WRITTEN_BOOK_CONTENT, StructuredDataKey.TRIM1_21_2, StructuredDataKey.DEBUG_STICK_STATE, StructuredDataKey.ENTITY_DATA,
            StructuredDataKey.BUCKET_ENTITY_DATA, StructuredDataKey.BLOCK_ENTITY_DATA, StructuredDataKey.INSTRUMENT1_21_2,
            StructuredDataKey.RECIPES, StructuredDataKey.LODESTONE_TRACKER, StructuredDataKey.FIREWORK_EXPLOSION, StructuredDataKey.FIREWORKS,
            StructuredDataKey.PROFILE, StructuredDataKey.NOTE_BLOCK_SOUND, StructuredDataKey.BANNER_PATTERNS, StructuredDataKey.BASE_COLOR,
            StructuredDataKey.POT_DECORATIONS, StructuredDataKey.BLOCK_STATE, StructuredDataKey.BEES, StructuredDataKey.LOCK,
            StructuredDataKey.CONTAINER_LOOT, StructuredDataKey.TOOL1_20_5, StructuredDataKey.ITEM_NAME, StructuredDataKey.OMINOUS_BOTTLE_AMPLIFIER,
            StructuredDataKey.FOOD1_21_2, StructuredDataKey.JUKEBOX_PLAYABLE1_21, StructuredDataKey.ATTRIBUTE_MODIFIERS1_21,
            StructuredDataKey.REPAIRABLE, StructuredDataKey.ENCHANTABLE, StructuredDataKey.CONSUMABLE1_21_2,
            StructuredDataKey.USE_COOLDOWN, StructuredDataKey.DAMAGE, StructuredDataKey.EQUIPPABLE1_21_2, StructuredDataKey.ITEM_MODEL,
            StructuredDataKey.GLIDER, StructuredDataKey.TOOLTIP_STYLE, StructuredDataKey.DEATH_PROTECTION,
            // Volatile thanks to containing item
            StructuredDataKey.CHARGED_PROJECTILES1_21_2, StructuredDataKey.BUNDLE_CONTENTS1_21_2, StructuredDataKey.CONTAINER1_21_2, StructuredDataKey.USE_REMAINDER1_21_2);
        super.onMappingDataLoaded();
    }

    @Override
    public void init(final UserConnection connection) {
        addEntityTracker(connection, new EntityTracker1_21_2(connection));
        connection.put(new BundleStateTracker());
        connection.put(new GroundFlagTracker());

        final ProtocolVersion protocolVersion = connection.getProtocolInfo().protocolVersion();
        if (protocolVersion.olderThan(ProtocolVersion.v1_21_4)) { // Only needed for 1.21.2/1.21.3
            connection.put(new PlayerPositionStorage());
        }

        // <= 1.21.1 clients allowed loaded chunks to get replaced with new data without unloading them first.
        // 1.21.2 introduced a graphical bug where it doesn't properly render the new data unless the chunk is unloaded beforehand.
        // 1.21.4 fixed this bug, so the workaround is no longer needed.
        if (protocolVersion.equals(ProtocolVersion.v1_21_2)) {
            connection.put(new ChunkLoadTracker());
        }
    }

    @Override
    public MappingData getMappingData() {
        return MAPPINGS;
    }

    @Override
    public EntityPacketRewriter1_21_2 getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    public BlockItemPacketRewriter1_21_2 getItemRewriter() {
        return itemRewriter;
    }

    @Override
    public ParticleRewriter1_21_2 getParticleRewriter() {
        return particleRewriter;
    }

    @Override
    public TagRewriter<ClientboundPacket1_21> getTagRewriter() {
        return tagRewriter;
    }

    @Override
    public ComponentRewriter getComponentRewriter() {
        return componentRewriter;
    }

    public SoundRewriter<ClientboundPacket1_21> getSoundRewriter() {
        return soundRewriter;
    }

    @Override
    protected PacketTypesProvider<ClientboundPacket1_21, ClientboundPacket1_21_2, ServerboundPacket1_20_5, ServerboundPacket1_21_2> createPacketTypesProvider() {
        return new SimplePacketTypesProvider<>(
            packetTypeMap(unmappedClientboundPacketType, ClientboundPackets1_21.class, ClientboundConfigurationPackets1_21.class),
            packetTypeMap(mappedClientboundPacketType, ClientboundPackets1_21_2.class, ClientboundConfigurationPackets1_21.class),
            packetTypeMap(mappedServerboundPacketType, ServerboundPackets1_20_5.class, ServerboundConfigurationPackets1_20_5.class),
            packetTypeMap(unmappedServerboundPacketType, ServerboundPackets1_21_2.class, ServerboundConfigurationPackets1_20_5.class)
        );
    }
}
