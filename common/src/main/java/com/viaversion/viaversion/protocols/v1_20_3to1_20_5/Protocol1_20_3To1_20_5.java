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
package com.viaversion.viaversion.protocols.v1_20_3to1_20_5;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.ProfileKey;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_20_5;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.packet.provider.SimplePacketTypesProvider;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import com.viaversion.viaversion.api.type.types.version.Types1_20_5;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.rewriter.CommandRewriter1_19_4;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ClientboundConfigurationPackets1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ClientboundPacket1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ClientboundPackets1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ServerboundPacket1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ServerboundPackets1_20_3;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.MappingData1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundPacket1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPacket1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.rewriter.BlockItemPacketRewriter1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.rewriter.ComponentRewriter1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.rewriter.EntityPacketRewriter1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.rewriter.ParticleRewriter1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.storage.AcknowledgedMessagesStorage;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.storage.ArmorTrimStorage;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ServerboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.rewriter.text.JsonNBTComponentRewriter;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.util.ProtocolLogger;
import java.util.BitSet;
import java.util.UUID;

import static com.viaversion.viaversion.util.ProtocolUtil.packetTypeMap;

public final class Protocol1_20_3To1_20_5 extends AbstractProtocol<ClientboundPacket1_20_3, ClientboundPacket1_20_5, ServerboundPacket1_20_3, ServerboundPacket1_20_5> {

    public static final MappingData1_20_5 MAPPINGS = new MappingData1_20_5();
    public static final ProtocolLogger LOGGER = new ProtocolLogger(Protocol1_20_3To1_20_5.class);
    // Mojang will remove this in the next release, so if we were to set this to false,
    // people would miss the changes and not fix their plugins before forcefully running into the errors then
    public static boolean strictErrorHandling = System.getProperty("viaversion.strict-error-handling1_20_5", "true").equalsIgnoreCase("true");
    private final EntityPacketRewriter1_20_5 entityRewriter = new EntityPacketRewriter1_20_5(this);
    private final BlockItemPacketRewriter1_20_5 itemRewriter = new BlockItemPacketRewriter1_20_5(this);
    private final ParticleRewriter1_20_5 particleRewriter = new ParticleRewriter1_20_5(this);
    private final TagRewriter<ClientboundPacket1_20_3> tagRewriter = new TagRewriter<>(this);
    private final ComponentRewriter1_20_5<ClientboundPacket1_20_3> componentRewriter = new ComponentRewriter1_20_5<>(this, Types1_20_5.STRUCTURED_DATA);

    public Protocol1_20_3To1_20_5() {
        super(ClientboundPacket1_20_3.class, ClientboundPacket1_20_5.class, ServerboundPacket1_20_3.class, ServerboundPacket1_20_5.class);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        tagRewriter.registerGeneric(ClientboundPackets1_20_3.UPDATE_TAGS);
        tagRewriter.registerGeneric(ClientboundConfigurationPackets1_20_3.UPDATE_TAGS);

        final SoundRewriter<ClientboundPacket1_20_3> soundRewriter = new SoundRewriter<>(this);
        soundRewriter.registerSound1_19_3(ClientboundPackets1_20_3.SOUND);
        soundRewriter.registerSound1_19_3(ClientboundPackets1_20_3.SOUND_ENTITY);

        new StatisticsRewriter<>(this).register(ClientboundPackets1_20_3.AWARD_STATS);

        componentRewriter.registerComponentPacket(ClientboundPackets1_20_3.SYSTEM_CHAT);
        componentRewriter.registerComponentPacket(ClientboundPackets1_20_3.DISGUISED_CHAT);
        componentRewriter.registerPlayerCombatKill1_20(ClientboundPackets1_20_3.PLAYER_COMBAT_KILL);

        // People add item hovers to all sorts of weird places...
        componentRewriter.registerOpenScreen1_14(ClientboundPackets1_20_3.OPEN_SCREEN);
        componentRewriter.registerComponentPacket(ClientboundPackets1_20_3.SET_ACTION_BAR_TEXT);
        componentRewriter.registerComponentPacket(ClientboundPackets1_20_3.SET_TITLE_TEXT);
        componentRewriter.registerComponentPacket(ClientboundPackets1_20_3.SET_SUBTITLE_TEXT);
        componentRewriter.registerBossEvent(ClientboundPackets1_20_3.BOSS_EVENT);
        componentRewriter.registerComponentPacket(ClientboundPackets1_20_3.DISCONNECT);
        componentRewriter.registerTabList(ClientboundPackets1_20_3.TAB_LIST);
        componentRewriter.registerPlayerInfoUpdate1_20_3(ClientboundPackets1_20_3.PLAYER_INFO_UPDATE);
        componentRewriter.registerPing();

        registerClientbound(State.LOGIN, ClientboundLoginPackets.HELLO, wrapper -> {
            wrapper.passthrough(Types.STRING); // Server ID
            wrapper.passthrough(Types.BYTE_ARRAY_PRIMITIVE); // Public key
            wrapper.passthrough(Types.BYTE_ARRAY_PRIMITIVE); // Challenge
            wrapper.write(Types.BOOLEAN, true); // Authenticate
        });

        registerClientbound(ClientboundPackets1_20_3.SERVER_DATA, wrapper -> {
            wrapper.passthrough(Types.TAG); // MOTD
            wrapper.passthrough(Types.OPTIONAL_BYTE_ARRAY_PRIMITIVE); // Icon

            // Moved to join game
            final boolean enforcesSecureChat = wrapper.read(Types.BOOLEAN);
            final AcknowledgedMessagesStorage storage = wrapper.user().get(AcknowledgedMessagesStorage.class);
            storage.setSecureChatEnforced(enforcesSecureChat);
            if (enforcesSecureChat) {
                // Only send the chat session to the server if we know that it is required
                storage.sendQueuedChatSession(wrapper);
            }
        });

        registerServerbound(ServerboundPackets1_20_5.CHAT, wrapper -> {
            wrapper.passthrough(Types.STRING); // Message
            wrapper.passthrough(Types.LONG); // Timestamp

            final AcknowledgedMessagesStorage storage = wrapper.user().get(AcknowledgedMessagesStorage.class);
            final long salt = wrapper.read(Types.LONG);
            final byte[] signature = wrapper.read(Types.OPTIONAL_SIGNATURE_BYTES);
            if (storage.isSecureChatEnforced()) {
                // Fake it till you make it
                wrapper.write(Types.LONG, salt);
                wrapper.write(Types.OPTIONAL_SIGNATURE_BYTES, signature);
            } else {
                // Go the safer route and strip the signature. No signature means no verification
                wrapper.write(Types.LONG, 0L);
                wrapper.write(Types.OPTIONAL_SIGNATURE_BYTES, null);
            }

            fixChatAck(wrapper, storage);
        });
        registerServerbound(ServerboundPackets1_20_5.CHAT_COMMAND_SIGNED, ServerboundPackets1_20_3.CHAT_COMMAND, wrapper -> {
            wrapper.passthrough(Types.STRING); // Command
            wrapper.passthrough(Types.LONG); // Timestamp

            // See above, strip signatures if we can to prevent verification of possibly bad signatures
            final AcknowledgedMessagesStorage storage = wrapper.user().get(AcknowledgedMessagesStorage.class);
            final long salt = wrapper.read(Types.LONG);
            final int signatures = wrapper.read(Types.VAR_INT);
            if (storage.isSecureChatEnforced()) {
                wrapper.write(Types.LONG, salt);
                wrapper.write(Types.VAR_INT, signatures);
                for (int i = 0; i < signatures; i++) {
                    wrapper.passthrough(Types.STRING); // Argument name
                    wrapper.passthrough(Types.SIGNATURE_BYTES); // Signature
                }
            } else {
                // Remove signatures
                wrapper.write(Types.LONG, 0L);
                wrapper.write(Types.VAR_INT, 0); // No signatures
                for (int i = 0; i < signatures; i++) {
                    wrapper.read(Types.STRING); // Argument name
                    wrapper.read(Types.SIGNATURE_BYTES); // Signature
                }
            }

            fixChatAck(wrapper, storage);
        });
        registerServerbound(ServerboundPackets1_20_5.CHAT_ACK, wrapper -> {
            final int offset = wrapper.read(Types.VAR_INT);
            final int fixedOffset = wrapper.user().get(AcknowledgedMessagesStorage.class).accumulateAckCount(offset);
            if (fixedOffset > 0) {
                wrapper.write(Types.VAR_INT, fixedOffset);
            } else {
                wrapper.cancel();
            }
        });
        registerServerbound(ServerboundPackets1_20_5.CHAT_COMMAND, wrapper -> {
            wrapper.passthrough(Types.STRING); // Command

            wrapper.write(Types.LONG, System.currentTimeMillis()); // Timestamp
            wrapper.write(Types.LONG, 0L); // Salt
            wrapper.write(Types.VAR_INT, 0); // No signatures

            writeSpoofedChatAck(wrapper, wrapper.user().get(AcknowledgedMessagesStorage.class));
        });
        registerServerbound(ServerboundPackets1_20_5.CHAT_SESSION_UPDATE, wrapper -> {
            // Delay this until we know whether the server enforces secure chat
            // The server sends this info in SERVER_DATA, but the client already sends this after receiving the game login
            final AcknowledgedMessagesStorage storage = wrapper.user().get(AcknowledgedMessagesStorage.class);
            if (storage.secureChatEnforced() != null && storage.secureChatEnforced()) {
                // We already know that secure chat is enforced, let it through
                return;
            }

            final UUID sessionId = wrapper.read(Types.UUID);
            final ProfileKey profileKey = wrapper.read(Types.PROFILE_KEY);
            storage.queueChatSession(sessionId, profileKey);

            wrapper.cancel();
        });

        registerClientbound(ClientboundPackets1_20_3.START_CONFIGURATION, wrapper -> wrapper.user().put(new AcknowledgedMessagesStorage()));

        new CommandRewriter1_19_4<>(this).registerDeclareCommands1_19(ClientboundPackets1_20_3.COMMANDS);

        registerClientbound(State.LOGIN, ClientboundLoginPackets.LOGIN_FINISHED, wrapper -> {
            wrapper.passthrough(Types.UUID); // UUID
            wrapper.passthrough(Types.STRING); // Name

            final int properties = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < properties; i++) {
                wrapper.passthrough(Types.STRING); // Name
                wrapper.passthrough(Types.STRING); // Value
                wrapper.passthrough(Types.OPTIONAL_STRING); // Signature
            }

            wrapper.write(Types.BOOLEAN, strictErrorHandling);
        });

        cancelServerbound(State.LOGIN, ServerboundLoginPackets.COOKIE_RESPONSE.getId());
        cancelServerbound(ServerboundConfigurationPackets1_20_5.COOKIE_RESPONSE);
        cancelServerbound(ServerboundConfigurationPackets1_20_5.SELECT_KNOWN_PACKS);
        cancelServerbound(ServerboundPackets1_20_5.COOKIE_RESPONSE);
        cancelServerbound(ServerboundPackets1_20_5.DEBUG_SAMPLE_SUBSCRIPTION);
    }

    private void fixChatAck(final PacketWrapper wrapper, final AcknowledgedMessagesStorage storage) {
        final int offset = wrapper.read(Types.VAR_INT);
        final BitSet acknowledged = wrapper.read(Types.ACKNOWLEDGED_BIT_SET);
        final int fixedOffset = storage.updateFromMessage(offset, acknowledged);
        wrapper.write(Types.VAR_INT, fixedOffset);
        // Never change this, as this message (and future ones) are signed with it
        wrapper.write(Types.ACKNOWLEDGED_BIT_SET, acknowledged);
    }

    private void writeSpoofedChatAck(final PacketWrapper wrapper, final AcknowledgedMessagesStorage storage) {
        // As we don't have the new state from the client, replay what we last received
        wrapper.write(Types.VAR_INT, 0); // Offset
        wrapper.write(Types.ACKNOWLEDGED_BIT_SET, storage.createSpoofedAck()); // Acknowledged
    }

    @Override
    protected void onMappingDataLoaded() {
        EntityTypes1_20_5.initialize(this);
        Types1_20_5.PARTICLE.filler(this)
            .reader("block", ParticleType.Readers.BLOCK)
            .reader("block_marker", ParticleType.Readers.BLOCK)
            .reader("dust", ParticleType.Readers.DUST)
            .reader("dust_pillar", ParticleType.Readers.BLOCK)
            .reader("falling_dust", ParticleType.Readers.BLOCK)
            .reader("dust_color_transition", ParticleType.Readers.DUST_TRANSITION)
            .reader("item", ParticleType.Readers.item(Types1_20_5.ITEM))
            .reader("vibration", ParticleType.Readers.VIBRATION1_20_3)
            .reader("sculk_charge", ParticleType.Readers.SCULK_CHARGE)
            .reader("shriek", ParticleType.Readers.SHRIEK)
            .reader("entity_effect", ParticleType.Readers.COLOR);
        Types1_20_5.STRUCTURED_DATA.filler(this)
            .add(StructuredDataKey.CUSTOM_DATA).add(StructuredDataKey.MAX_STACK_SIZE).add(StructuredDataKey.MAX_DAMAGE)
            .add(StructuredDataKey.DAMAGE).add(StructuredDataKey.UNBREAKABLE1_20_5).add(StructuredDataKey.RARITY)
            .add(StructuredDataKey.HIDE_TOOLTIP).add(StructuredDataKey.FOOD1_20_5).add(StructuredDataKey.FIRE_RESISTANT)
            .add(StructuredDataKey.CUSTOM_NAME).add(StructuredDataKey.LORE).add(StructuredDataKey.ENCHANTMENTS1_20_5)
            .add(StructuredDataKey.CAN_PLACE_ON1_20_5).add(StructuredDataKey.CAN_BREAK1_20_5).add(StructuredDataKey.ATTRIBUTE_MODIFIERS1_20_5)
            .add(StructuredDataKey.CUSTOM_MODEL_DATA1_20_5).add(StructuredDataKey.HIDE_ADDITIONAL_TOOLTIP).add(StructuredDataKey.REPAIR_COST)
            .add(StructuredDataKey.CREATIVE_SLOT_LOCK).add(StructuredDataKey.ENCHANTMENT_GLINT_OVERRIDE).add(StructuredDataKey.INTANGIBLE_PROJECTILE)
            .add(StructuredDataKey.STORED_ENCHANTMENTS1_20_5).add(StructuredDataKey.DYED_COLOR1_20_5).add(StructuredDataKey.MAP_COLOR)
            .add(StructuredDataKey.MAP_ID).add(StructuredDataKey.MAP_DECORATIONS).add(StructuredDataKey.MAP_POST_PROCESSING)
            .add(StructuredDataKey.CHARGED_PROJECTILES1_20_5).add(StructuredDataKey.BUNDLE_CONTENTS1_20_5).add(StructuredDataKey.POTION_CONTENTS1_20_5)
            .add(StructuredDataKey.SUSPICIOUS_STEW_EFFECTS).add(StructuredDataKey.WRITABLE_BOOK_CONTENT).add(StructuredDataKey.WRITTEN_BOOK_CONTENT)
            .add(StructuredDataKey.TRIM1_20_5).add(StructuredDataKey.DEBUG_STICK_STATE).add(StructuredDataKey.ENTITY_DATA)
            .add(StructuredDataKey.BUCKET_ENTITY_DATA).add(StructuredDataKey.BLOCK_ENTITY_DATA).add(StructuredDataKey.INSTRUMENT1_20_5)
            .add(StructuredDataKey.RECIPES).add(StructuredDataKey.LODESTONE_TRACKER).add(StructuredDataKey.FIREWORK_EXPLOSION)
            .add(StructuredDataKey.FIREWORKS).add(StructuredDataKey.PROFILE).add(StructuredDataKey.NOTE_BLOCK_SOUND)
            .add(StructuredDataKey.BANNER_PATTERNS).add(StructuredDataKey.BASE_COLOR).add(StructuredDataKey.POT_DECORATIONS)
            .add(StructuredDataKey.CONTAINER1_20_5).add(StructuredDataKey.BLOCK_STATE).add(StructuredDataKey.BEES)
            .add(StructuredDataKey.LOCK).add(StructuredDataKey.CONTAINER_LOOT).add(StructuredDataKey.TOOL1_20_5)
            .add(StructuredDataKey.ITEM_NAME).add(StructuredDataKey.OMINOUS_BOTTLE_AMPLIFIER);

        tagRewriter.renameTag(RegistryType.ITEM, "minecraft:axolotl_tempt_items", "minecraft:axolotl_food");
        tagRewriter.removeTag(RegistryType.ITEM, "minecraft:tools");
        tagRewriter.addEmptyTags(RegistryType.BLOCK, "minecraft:badlands_terracotta");
        tagRewriter.addEmptyTags(RegistryType.ITEM, "minecraft:enchantable/mace");

        super.onMappingDataLoaded();
    }

    @Override
    public void init(final UserConnection connection) {
        addEntityTracker(connection, new EntityTrackerBase(connection, EntityTypes1_20_5.PLAYER));
        connection.put(new AcknowledgedMessagesStorage());
        connection.put(new ArmorTrimStorage());
    }

    @Override
    public MappingData1_20_5 getMappingData() {
        return MAPPINGS;
    }

    @Override
    public ProtocolLogger getLogger() {
        return LOGGER;
    }

    @Override
    public EntityPacketRewriter1_20_5 getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    public BlockItemPacketRewriter1_20_5 getItemRewriter() {
        return itemRewriter;
    }

    @Override
    public ParticleRewriter1_20_5 getParticleRewriter() {
        return particleRewriter;
    }

    @Override
    public TagRewriter<ClientboundPacket1_20_3> getTagRewriter() {
        return tagRewriter;
    }

    public JsonNBTComponentRewriter<ClientboundPacket1_20_3> getComponentRewriter() {
        return componentRewriter;
    }

    @Override
    protected PacketTypesProvider<ClientboundPacket1_20_3, ClientboundPacket1_20_5, ServerboundPacket1_20_3, ServerboundPacket1_20_5> createPacketTypesProvider() {
        return new SimplePacketTypesProvider<>(
            packetTypeMap(unmappedClientboundPacketType, ClientboundPackets1_20_3.class, ClientboundConfigurationPackets1_20_3.class),
            packetTypeMap(mappedClientboundPacketType, ClientboundPackets1_20_5.class, ClientboundConfigurationPackets1_20_5.class),
            packetTypeMap(mappedServerboundPacketType, ServerboundPackets1_20_3.class, ServerboundConfigurationPackets1_20_2.class),
            packetTypeMap(unmappedServerboundPacketType, ServerboundPackets1_20_5.class, ServerboundConfigurationPackets1_20_5.class)
        );
    }
}
