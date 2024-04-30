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
package com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3;

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
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import com.viaversion.viaversion.api.type.types.version.Types1_20_5;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.rewriter.CommandRewriter1_19_4;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ServerboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundConfigurationPackets1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundPacket1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundPackets1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ServerboundPacket1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ServerboundPackets1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.data.MappingData;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.packet.ClientboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.packet.ClientboundPacket1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.packet.ClientboundPackets1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.packet.ServerboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.packet.ServerboundPacket1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.rewriter.BlockItemPacketRewriter1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.rewriter.ComponentRewriter1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.rewriter.EntityPacketRewriter1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.storage.AcknowledgedMessagesStorage;
import com.viaversion.viaversion.rewriter.ComponentRewriter;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import java.util.UUID;

import static com.viaversion.viaversion.util.ProtocolUtil.packetTypeMap;

public final class Protocol1_20_5To1_20_3 extends AbstractProtocol<ClientboundPacket1_20_3, ClientboundPacket1_20_5, ServerboundPacket1_20_3, ServerboundPacket1_20_5> {

    public static final MappingData MAPPINGS = new MappingData();
    // Mojang will remove this in the next release, so if we were to set this to false,
    // people would miss the changes and not fix their plugins before forcefully running into the errors then
    public static boolean strictErrorHandling = System.getProperty("viaversion.strict-error-handling1_20_5", "true").equalsIgnoreCase("true");
    private final EntityPacketRewriter1_20_5 entityRewriter = new EntityPacketRewriter1_20_5(this);
    private final BlockItemPacketRewriter1_20_5 itemRewriter = new BlockItemPacketRewriter1_20_5(this);
    private final TagRewriter<ClientboundPacket1_20_3> tagRewriter = new TagRewriter<>(this);
    private final ComponentRewriter1_20_5<ClientboundPacket1_20_3> componentRewriter = new ComponentRewriter1_20_5<>(this, Types1_20_5.STRUCTURED_DATA);

    public Protocol1_20_5To1_20_3() {
        super(ClientboundPacket1_20_3.class, ClientboundPacket1_20_5.class, ServerboundPacket1_20_3.class, ServerboundPacket1_20_5.class);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        tagRewriter.registerGeneric(ClientboundPackets1_20_3.TAGS);
        tagRewriter.registerGeneric(ClientboundConfigurationPackets1_20_3.UPDATE_TAGS);

        final SoundRewriter<ClientboundPacket1_20_3> soundRewriter = new SoundRewriter<>(this);
        soundRewriter.register1_19_3Sound(ClientboundPackets1_20_3.SOUND);
        soundRewriter.register1_19_3Sound(ClientboundPackets1_20_3.ENTITY_SOUND);

        new StatisticsRewriter<>(this).register(ClientboundPackets1_20_3.STATISTICS);

        componentRewriter.registerComponentPacket(ClientboundPackets1_20_3.SYSTEM_CHAT);
        componentRewriter.registerComponentPacket(ClientboundPackets1_20_3.DISGUISED_CHAT);
        componentRewriter.registerCombatKill1_20(ClientboundPackets1_20_3.COMBAT_KILL);

        registerClientbound(State.LOGIN, ClientboundLoginPackets.HELLO, wrapper -> {
            wrapper.passthrough(Type.STRING); // Server ID
            wrapper.passthrough(Type.BYTE_ARRAY_PRIMITIVE); // Public key
            wrapper.passthrough(Type.BYTE_ARRAY_PRIMITIVE); // Challenge
            wrapper.write(Type.BOOLEAN, true); // Authenticate
        });

        registerClientbound(ClientboundPackets1_20_3.SERVER_DATA, wrapper -> {
            wrapper.passthrough(Type.TAG); // MOTD
            wrapper.passthrough(Type.OPTIONAL_BYTE_ARRAY_PRIMITIVE); // Icon

            // Moved to join game
            final boolean enforcesSecureChat = wrapper.read(Type.BOOLEAN);
            final AcknowledgedMessagesStorage storage = wrapper.user().get(AcknowledgedMessagesStorage.class);
            storage.setSecureChatEnforced(enforcesSecureChat);
            if (enforcesSecureChat) {
                // Only send the chat session to the server if we know that it is required
                storage.sendQueuedChatSession(wrapper);
            }
        });

        // Big problem with this update: Without access to the client, this cannot 100% predict the
        // correct offset. This means we have to entirely discard client acknowledgements and fake them.
        registerClientbound(ClientboundPackets1_20_3.PLAYER_CHAT, wrapper -> {
            wrapper.passthrough(Type.UUID); // Sender
            wrapper.passthrough(Type.VAR_INT); // Index
            final byte[] signature = wrapper.passthrough(Type.OPTIONAL_SIGNATURE_BYTES);
            if (signature == null) {
                return;
            }

            // Mimic client behavior for acknowledgements
            final AcknowledgedMessagesStorage storage = wrapper.user().get(AcknowledgedMessagesStorage.class);
            if (storage.add(signature) && storage.offset() > 64) {
                final PacketWrapper chatAck = wrapper.create(ServerboundPackets1_20_3.CHAT_ACK);
                chatAck.write(Type.VAR_INT, storage.offset());
                chatAck.sendToServer(Protocol1_20_5To1_20_3.class);

                storage.clearOffset();
            }
        });
        registerServerbound(ServerboundPackets1_20_5.CHAT_MESSAGE, wrapper -> {
            wrapper.passthrough(Type.STRING); // Message
            wrapper.passthrough(Type.LONG); // Timestamp

            final AcknowledgedMessagesStorage storage = wrapper.user().get(AcknowledgedMessagesStorage.class);
            final long salt = wrapper.read(Type.LONG);
            final byte[] signature = wrapper.read(Type.OPTIONAL_SIGNATURE_BYTES);
            if (storage.isSecureChatEnforced()) {
                // Fake it till you make it
                wrapper.write(Type.LONG, salt);
                wrapper.write(Type.OPTIONAL_SIGNATURE_BYTES, signature);
            } else {
                // Go the safer route and strip the signature. No signature means no verification
                wrapper.write(Type.LONG, 0L);
                wrapper.write(Type.OPTIONAL_SIGNATURE_BYTES, null);
            }

            replaceChatAck(wrapper, storage);
        });
        registerServerbound(ServerboundPackets1_20_5.CHAT_COMMAND_SIGNED, ServerboundPackets1_20_3.CHAT_COMMAND, wrapper -> {
            wrapper.passthrough(Type.STRING); // Command
            wrapper.passthrough(Type.LONG); // Timestamp

            // See above, strip signatures if we can to prevent verification of possibly bad signatures
            final AcknowledgedMessagesStorage storage = wrapper.user().get(AcknowledgedMessagesStorage.class);
            final long salt = wrapper.read(Type.LONG);
            final int signatures = wrapper.read(Type.VAR_INT);
            if (storage.isSecureChatEnforced()) {
                wrapper.write(Type.LONG, salt);
                wrapper.write(Type.VAR_INT, signatures);
                for (int i = 0; i < signatures; i++) {
                    wrapper.passthrough(Type.STRING); // Argument name
                    wrapper.passthrough(Type.SIGNATURE_BYTES); // Signature
                }
            } else {
                // Remove signatures
                wrapper.write(Type.LONG, 0L);
                wrapper.write(Type.VAR_INT, 0); // No signatures
                for (int i = 0; i < signatures; i++) {
                    wrapper.read(Type.STRING); // Argument name
                    wrapper.read(Type.SIGNATURE_BYTES); // Signature
                }
            }

            replaceChatAck(wrapper, storage);
        });
        registerServerbound(ServerboundPackets1_20_5.CHAT_COMMAND, wrapper -> {
            wrapper.passthrough(Type.STRING); // Command

            wrapper.write(Type.LONG, System.currentTimeMillis()); // Timestamp
            wrapper.write(Type.LONG, 0L); // Salt
            wrapper.write(Type.VAR_INT, 0); // No signatures

            writeChatAck(wrapper, wrapper.user().get(AcknowledgedMessagesStorage.class));
        });
        registerServerbound(ServerboundPackets1_20_5.CHAT_SESSION_UPDATE, wrapper -> {
            // Delay this until we know whether the server enforces secure chat
            // The server sends this info in SERVER_DATA, but the client already sends this after receiving the game login
            final AcknowledgedMessagesStorage storage = wrapper.user().get(AcknowledgedMessagesStorage.class);
            if (storage.secureChatEnforced() != null && storage.secureChatEnforced()) {
                // We already know that secure chat is enforced, let it through
                return;
            }

            final UUID sessionId = wrapper.read(Type.UUID);
            final ProfileKey profileKey = wrapper.read(Type.PROFILE_KEY);
            storage.queueChatSession(sessionId, profileKey);

            wrapper.cancel();
        });
        cancelServerbound(ServerboundPackets1_20_5.CHAT_ACK);

        registerClientbound(ClientboundPackets1_20_3.START_CONFIGURATION, wrapper -> wrapper.user().put(new AcknowledgedMessagesStorage()));

        new CommandRewriter1_19_4<>(this).registerDeclareCommands1_19(ClientboundPackets1_20_3.DECLARE_COMMANDS);

        registerClientbound(State.LOGIN, ClientboundLoginPackets.GAME_PROFILE, wrapper -> {
            wrapper.passthrough(Type.UUID); // UUID
            wrapper.passthrough(Type.STRING); // Name

            final int properties = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < properties; i++) {
                wrapper.passthrough(Type.STRING); // Name
                wrapper.passthrough(Type.STRING); // Value
                wrapper.passthrough(Type.OPTIONAL_STRING); // Signature
            }

            wrapper.write(Type.BOOLEAN, strictErrorHandling);
        });

        cancelServerbound(State.LOGIN, ServerboundLoginPackets.COOKIE_RESPONSE.getId());
        cancelServerbound(ServerboundConfigurationPackets1_20_5.COOKIE_RESPONSE);
        cancelServerbound(ServerboundConfigurationPackets1_20_5.SELECT_KNOWN_PACKS);
        cancelServerbound(ServerboundPackets1_20_5.COOKIE_RESPONSE);
        cancelServerbound(ServerboundPackets1_20_5.DEBUG_SAMPLE_SUBSCRIPTION);
    }

    private void replaceChatAck(final PacketWrapper wrapper, final AcknowledgedMessagesStorage storage) throws Exception {
        wrapper.read(Type.VAR_INT); // Offset
        wrapper.read(Type.ACKNOWLEDGED_BIT_SET); // Acknowledged
        writeChatAck(wrapper, storage);
    }

    private void writeChatAck(final PacketWrapper wrapper, final AcknowledgedMessagesStorage storage) {
        wrapper.write(Type.VAR_INT, storage.offset());
        wrapper.write(Type.ACKNOWLEDGED_BIT_SET, storage.toAck());
        storage.clearOffset();
    }

    @Override
    protected void onMappingDataLoaded() {
        super.onMappingDataLoaded();

        EntityTypes1_20_5.initialize(this);
        Types1_20_5.PARTICLE.filler(this)
            .reader("block", ParticleType.Readers.BLOCK)
            .reader("block_marker", ParticleType.Readers.BLOCK)
            .reader("dust", ParticleType.Readers.DUST)
            .reader("dust_pillar", ParticleType.Readers.BLOCK)
            .reader("falling_dust", ParticleType.Readers.BLOCK)
            .reader("dust_color_transition", ParticleType.Readers.DUST_TRANSITION)
            .reader("item", ParticleType.Readers.ITEM1_20_5)
            .reader("vibration", ParticleType.Readers.VIBRATION1_20_3)
            .reader("sculk_charge", ParticleType.Readers.SCULK_CHARGE)
            .reader("shriek", ParticleType.Readers.SHRIEK)
            .reader("entity_effect", ParticleType.Readers.COLOR);
        Types1_20_5.STRUCTURED_DATA.filler(this)
            .add(StructuredDataKey.CUSTOM_DATA).add(StructuredDataKey.MAX_STACK_SIZE).add(StructuredDataKey.MAX_DAMAGE)
            .add(StructuredDataKey.DAMAGE).add(StructuredDataKey.UNBREAKABLE).add(StructuredDataKey.RARITY)
            .add(StructuredDataKey.HIDE_TOOLTIP).add(StructuredDataKey.FOOD).add(StructuredDataKey.FIRE_RESISTANT)
            .add(StructuredDataKey.CUSTOM_NAME).add(StructuredDataKey.LORE).add(StructuredDataKey.ENCHANTMENTS)
            .add(StructuredDataKey.CAN_PLACE_ON).add(StructuredDataKey.CAN_BREAK).add(StructuredDataKey.ATTRIBUTE_MODIFIERS)
            .add(StructuredDataKey.CUSTOM_MODEL_DATA).add(StructuredDataKey.HIDE_ADDITIONAL_TOOLTIP).add(StructuredDataKey.REPAIR_COST)
            .add(StructuredDataKey.CREATIVE_SLOT_LOCK).add(StructuredDataKey.ENCHANTMENT_GLINT_OVERRIDE).add(StructuredDataKey.INTANGIBLE_PROJECTILE)
            .add(StructuredDataKey.STORED_ENCHANTMENTS).add(StructuredDataKey.DYED_COLOR).add(StructuredDataKey.MAP_COLOR)
            .add(StructuredDataKey.MAP_ID).add(StructuredDataKey.MAP_DECORATIONS).add(StructuredDataKey.MAP_POST_PROCESSING)
            .add(StructuredDataKey.CHARGED_PROJECTILES).add(StructuredDataKey.BUNDLE_CONTENTS).add(StructuredDataKey.POTION_CONTENTS)
            .add(StructuredDataKey.SUSPICIOUS_STEW_EFFECTS).add(StructuredDataKey.WRITABLE_BOOK_CONTENT).add(StructuredDataKey.WRITTEN_BOOK_CONTENT)
            .add(StructuredDataKey.TRIM).add(StructuredDataKey.DEBUG_STICK_STATE).add(StructuredDataKey.ENTITY_DATA)
            .add(StructuredDataKey.BUCKET_ENTITY_DATA).add(StructuredDataKey.BLOCK_ENTITY_DATA).add(StructuredDataKey.INSTRUMENT)
            .add(StructuredDataKey.RECIPES).add(StructuredDataKey.LODESTONE_TRACKER).add(StructuredDataKey.FIREWORK_EXPLOSION)
            .add(StructuredDataKey.FIREWORKS).add(StructuredDataKey.PROFILE).add(StructuredDataKey.NOTE_BLOCK_SOUND)
            .add(StructuredDataKey.BANNER_PATTERNS).add(StructuredDataKey.BASE_COLOR).add(StructuredDataKey.POT_DECORATIONS)
            .add(StructuredDataKey.CONTAINER).add(StructuredDataKey.BLOCK_STATE).add(StructuredDataKey.BEES)
            .add(StructuredDataKey.LOCK).add(StructuredDataKey.CONTAINER_LOOT).add(StructuredDataKey.TOOL)
            .add(StructuredDataKey.ITEM_NAME).add(StructuredDataKey.OMINOUS_BOTTLE_AMPLIFIER);

        tagRewriter.addTag(RegistryType.ITEM, "minecraft:dyeable", 853, 854, 855, 856, 1120);
    }

    @Override
    public void init(final UserConnection connection) {
        addEntityTracker(connection, new EntityTrackerBase(connection, EntityTypes1_20_5.PLAYER));
        connection.put(new AcknowledgedMessagesStorage());
    }

    @Override
    public MappingData getMappingData() {
        return MAPPINGS;
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
    public TagRewriter<ClientboundPacket1_20_3> getTagRewriter() {
        return tagRewriter;
    }

    public ComponentRewriter<ClientboundPacket1_20_3> getComponentRewriter() {
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