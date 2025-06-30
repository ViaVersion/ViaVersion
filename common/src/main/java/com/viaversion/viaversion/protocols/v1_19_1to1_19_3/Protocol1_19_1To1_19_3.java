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
package com.viaversion.viaversion.protocols.v1_19_1to1_19_3;

import com.google.common.primitives.Longs;
import com.google.gson.JsonElement;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.PlayerMessageSignature;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_19_3;
import com.viaversion.viaversion.api.minecraft.signature.SignableCommandArgumentsProvider;
import com.viaversion.viaversion.api.minecraft.signature.model.DecoratableMessage;
import com.viaversion.viaversion.api.minecraft.signature.model.MessageMetadata;
import com.viaversion.viaversion.api.minecraft.signature.storage.ChatSession1_19_1;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import com.viaversion.viaversion.api.type.types.version.Types1_19_3;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.packet.ClientboundPackets1_19_3;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.packet.ServerboundPackets1_19_3;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.rewriter.ComponentRewriter1_19_3;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.rewriter.EntityPacketRewriter1_19_3;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.rewriter.ItemPacketRewriter1_19_3;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.storage.NonceStorage1_19_3;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.storage.ReceivedMessagesStorage;
import com.viaversion.viaversion.protocols.v1_19to1_19_1.packet.ClientboundPackets1_19_1;
import com.viaversion.viaversion.protocols.v1_19to1_19_1.packet.ServerboundPackets1_19_1;
import com.viaversion.viaversion.rewriter.CommandRewriter;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.Pair;
import java.security.SignatureException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class Protocol1_19_1To1_19_3 extends AbstractProtocol<ClientboundPackets1_19_1, ClientboundPackets1_19_3, ServerboundPackets1_19_1, ServerboundPackets1_19_3> {

    public static final MappingData MAPPINGS = new MappingDataBase("1.19", "1.19.3");
    private static final UUID ZERO_UUID = new UUID(0, 0);
    private static final byte[] EMPTY_BYTES = new byte[0];
    private final EntityPacketRewriter1_19_3 entityRewriter = new EntityPacketRewriter1_19_3(this);
    private final ItemPacketRewriter1_19_3 itemRewriter = new ItemPacketRewriter1_19_3(this);
    private final ParticleRewriter<ClientboundPackets1_19_1> particleRewriter = new ParticleRewriter<>(this);
    private final ComponentRewriter1_19_3 componentRewriter = new ComponentRewriter1_19_3(this);
    private final TagRewriter<ClientboundPackets1_19_1> tagRewriter = new TagRewriter<>(this);

    public Protocol1_19_1To1_19_3() {
        super(ClientboundPackets1_19_1.class, ClientboundPackets1_19_3.class, ServerboundPackets1_19_1.class, ServerboundPackets1_19_3.class);
    }

    @Override
    protected void registerPackets() {
        tagRewriter.registerGeneric(ClientboundPackets1_19_1.UPDATE_TAGS);

        particleRewriter.registerLevelParticles1_19(ClientboundPackets1_19_1.LEVEL_PARTICLES);

        entityRewriter.register();
        itemRewriter.register();

        // Rewrite sounds as holders
        final PacketHandler soundHandler = wrapper -> {
            int soundId = wrapper.read(Types.VAR_INT);
            soundId = MAPPINGS.getSoundMappings().getNewId(soundId);
            if (soundId == -1) {
                wrapper.cancel();
                return;
            }

            wrapper.write(Types.SOUND_EVENT, Holder.of(soundId));
        };
        registerClientbound(ClientboundPackets1_19_1.SOUND_ENTITY, soundHandler);
        registerClientbound(ClientboundPackets1_19_1.SOUND, soundHandler);
        registerClientbound(ClientboundPackets1_19_1.CUSTOM_SOUND, ClientboundPackets1_19_3.SOUND, wrapper -> {
            final String soundIdentifier = wrapper.read(Types.STRING);
            wrapper.write(Types.SOUND_EVENT, Holder.of(new SoundEvent(soundIdentifier, null)));
        });

        new StatisticsRewriter<>(this).register(ClientboundPackets1_19_1.AWARD_STATS);

        componentRewriter.registerComponentPacket(ClientboundPackets1_19_1.SYSTEM_CHAT);
        componentRewriter.registerComponentPacket(ClientboundPackets1_19_1.SET_ACTION_BAR_TEXT);
        componentRewriter.registerComponentPacket(ClientboundPackets1_19_1.SET_TITLE_TEXT);
        componentRewriter.registerComponentPacket(ClientboundPackets1_19_1.SET_SUBTITLE_TEXT);
        componentRewriter.registerBossEvent(ClientboundPackets1_19_1.BOSS_EVENT);
        componentRewriter.registerComponentPacket(ClientboundPackets1_19_1.DISCONNECT);
        componentRewriter.registerTabList(ClientboundPackets1_19_1.TAB_LIST);
        componentRewriter.registerOpenScreen1_14(ClientboundPackets1_19_1.OPEN_SCREEN);
        componentRewriter.registerPlayerCombatKill(ClientboundPackets1_19_1.PLAYER_COMBAT_KILL);
        componentRewriter.registerPing();

        final CommandRewriter<ClientboundPackets1_19_1> commandRewriter = new CommandRewriter<>(this) {
            @Override
            public void handleArgument(final PacketWrapper wrapper, final String argumentType) {
                switch (argumentType) {
                    case "minecraft:item_enchantment" -> wrapper.write(Types.STRING, "minecraft:enchantment");
                    case "minecraft:mob_effect" -> wrapper.write(Types.STRING, "minecraft:mob_effect");
                    case "minecraft:entity_summon" -> wrapper.write(Types.STRING, "minecraft:entity_type");
                    default -> super.handleArgument(wrapper, argumentType);
                }
            }

            @Override
            public String handleArgumentType(final String argumentType) {
                return switch (argumentType) {
                    case "minecraft:resource" -> "minecraft:resource_key";
                    case "minecraft:resource_or_tag" -> "minecraft:resource_or_tag_key";
                    case "minecraft:entity_summon", "minecraft:item_enchantment", "minecraft:mob_effect" ->
                        "minecraft:resource";
                    default -> argumentType;
                };
            }
        };
        commandRewriter.registerDeclareCommands1_19(ClientboundPackets1_19_1.COMMANDS);

        registerClientbound(ClientboundPackets1_19_1.SERVER_DATA, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.OPTIONAL_COMPONENT); // Motd
                map(Types.OPTIONAL_STRING); // Encoded icon
                read(Types.BOOLEAN); // Remove previews chat
            }
        });

        // Aaaaand once more
        registerClientbound(ClientboundPackets1_19_1.PLAYER_CHAT, ClientboundPackets1_19_3.DISGUISED_CHAT, new PacketHandlers() {
            @Override
            public void register() {
                read(Types.OPTIONAL_BYTE_ARRAY_PRIMITIVE); // Previous signature
                handler(wrapper -> {
                    final PlayerMessageSignature signature = wrapper.read(Types.PLAYER_MESSAGE_SIGNATURE);

                    // Store message signature for last seen
                    if (!signature.uuid().equals(ZERO_UUID) && signature.signatureBytes().length != 0) {
                        final ReceivedMessagesStorage messagesStorage = wrapper.user().get(ReceivedMessagesStorage.class);
                        messagesStorage.add(signature);
                        if (messagesStorage.tickUnacknowledged() > 64) {
                            messagesStorage.resetUnacknowledgedCount();

                            // Send chat acknowledgement
                            final PacketWrapper chatAckPacket = wrapper.create(ServerboundPackets1_19_1.CHAT_ACK);
                            chatAckPacket.write(Types.PLAYER_MESSAGE_SIGNATURE_ARRAY, messagesStorage.lastSignatures());
                            chatAckPacket.write(Types.OPTIONAL_PLAYER_MESSAGE_SIGNATURE, null);
                            chatAckPacket.sendToServer(Protocol1_19_1To1_19_3.class);
                        }
                    }

                    final String plainMessage = wrapper.read(Types.STRING);
                    JsonElement decoratedMessage = wrapper.read(Types.OPTIONAL_COMPONENT);

                    wrapper.read(Types.LONG); // Timestamp
                    wrapper.read(Types.LONG); // Salt
                    wrapper.read(Types.PLAYER_MESSAGE_SIGNATURE_ARRAY); // Last seen

                    final JsonElement unsignedMessage = wrapper.read(Types.OPTIONAL_COMPONENT);
                    if (unsignedMessage != null) {
                        decoratedMessage = unsignedMessage;
                    }
                    if (decoratedMessage == null) {
                        decoratedMessage = ComponentUtil.plainToJson(plainMessage);
                    }

                    final int filterMaskType = wrapper.read(Types.VAR_INT);
                    if (filterMaskType == 2) { // Partially filtered
                        wrapper.read(Types.LONG_ARRAY_PRIMITIVE); // Mask
                    }

                    wrapper.write(Types.COMPONENT, decoratedMessage);
                    // Keep chat type at the end
                });
            }
        });

        registerServerbound(ServerboundPackets1_19_3.CHAT_COMMAND, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); // Command
                map(Types.LONG); // Timestamp
                map(Types.LONG); // Salt
                handler(wrapper -> {
                    final ChatSession1_19_1 chatSession = wrapper.user().get(ChatSession1_19_1.class);
                    final ReceivedMessagesStorage messagesStorage = wrapper.user().get(ReceivedMessagesStorage.class);

                    final int signatures = wrapper.read(Types.VAR_INT);
                    for (int i = 0; i < signatures; i++) {
                        wrapper.read(Types.STRING); // Argument name
                        wrapper.read(Types.SIGNATURE_BYTES); // Signature
                    }

                    final SignableCommandArgumentsProvider argumentsProvider = Via.getManager().getProviders().get(SignableCommandArgumentsProvider.class);
                    if (chatSession != null && argumentsProvider != null) {
                        final UUID sender = wrapper.user().getProtocolInfo().getUuid();
                        final String message = wrapper.get(Types.STRING, 0);
                        final long timestamp = wrapper.get(Types.LONG, 0);
                        final long salt = wrapper.get(Types.LONG, 1);

                        final List<Pair<String, String>> arguments = argumentsProvider.getSignableArguments(message);
                        wrapper.write(Types.VAR_INT, arguments.size()); // Signature count

                        for (Pair<String, String> argument : arguments) {
                            final MessageMetadata metadata = new MessageMetadata(sender, timestamp, salt);
                            final DecoratableMessage decoratableMessage = new DecoratableMessage(argument.value());

                            final byte[] signature;
                            try {
                                signature = chatSession.signChatMessage(metadata, decoratableMessage, messagesStorage.lastSignatures());
                            } catch (final SignatureException e) {
                                throw new RuntimeException(e);
                            }

                            wrapper.write(Types.STRING, argument.key()); // Argument name
                            wrapper.write(Types.BYTE_ARRAY_PRIMITIVE, signature); // Signature
                        }
                    } else {
                        wrapper.write(Types.VAR_INT, 0); // Signature count
                    }

                    wrapper.write(Types.BOOLEAN, false); // No signed preview

                    messagesStorage.resetUnacknowledgedCount();
                    wrapper.write(Types.PLAYER_MESSAGE_SIGNATURE_ARRAY, messagesStorage.lastSignatures());
                    wrapper.write(Types.OPTIONAL_PLAYER_MESSAGE_SIGNATURE, null); // No last unacknowledged
                });
                read(Types.VAR_INT); // Offset
                read(Types.ACKNOWLEDGED_BIT_SET); // Acknowledged
            }
        });
        registerServerbound(ServerboundPackets1_19_3.CHAT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); // Message
                map(Types.LONG); // Timestamp
                map(Types.LONG); // Salt
                read(Types.OPTIONAL_SIGNATURE_BYTES); // Signature
                handler(wrapper -> {
                    final ChatSession1_19_1 chatSession = wrapper.user().get(ChatSession1_19_1.class);
                    final ReceivedMessagesStorage messagesStorage = wrapper.user().get(ReceivedMessagesStorage.class);

                    if (chatSession != null) {
                        final UUID sender = wrapper.user().getProtocolInfo().getUuid();
                        final String message = wrapper.get(Types.STRING, 0);
                        final long timestamp = wrapper.get(Types.LONG, 0);
                        final long salt = wrapper.get(Types.LONG, 1);

                        final MessageMetadata metadata = new MessageMetadata(sender, timestamp, salt);
                        final DecoratableMessage decoratableMessage = new DecoratableMessage(message);
                        final byte[] signature;
                        try {
                            signature = chatSession.signChatMessage(metadata, decoratableMessage, messagesStorage.lastSignatures());
                        } catch (final SignatureException e) {
                            throw new RuntimeException(e);
                        }

                        wrapper.write(Types.BYTE_ARRAY_PRIMITIVE, signature); // Signature
                        wrapper.write(Types.BOOLEAN, decoratableMessage.isDecorated()); // Signed preview
                    } else {
                        wrapper.write(Types.BYTE_ARRAY_PRIMITIVE, EMPTY_BYTES); // Signature
                        wrapper.write(Types.BOOLEAN, false); // Signed preview
                    }

                    messagesStorage.resetUnacknowledgedCount();
                    wrapper.write(Types.PLAYER_MESSAGE_SIGNATURE_ARRAY, messagesStorage.lastSignatures());
                    wrapper.write(Types.OPTIONAL_PLAYER_MESSAGE_SIGNATURE, null); // No last unacknowledged
                });
                read(Types.VAR_INT); // Offset
                read(Types.ACKNOWLEDGED_BIT_SET); // Acknowledged
            }
        });

        registerClientbound(State.LOGIN, ClientboundLoginPackets.HELLO, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); // Server id
                map(Types.BYTE_ARRAY_PRIMITIVE); // Public key
                handler(wrapper -> {
                    if (wrapper.user().has(ChatSession1_19_1.class)) {
                        wrapper.user().put(new NonceStorage1_19_3(wrapper.passthrough(Types.BYTE_ARRAY_PRIMITIVE))); // Nonce
                    }
                });
            }
        });
        registerServerbound(State.LOGIN, ServerboundLoginPackets.HELLO, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); // Name
                handler(wrapper -> {
                    final ChatSession1_19_1 chatSession = wrapper.user().get(ChatSession1_19_1.class);
                    wrapper.write(Types.OPTIONAL_PROFILE_KEY, chatSession == null ? null : chatSession.getProfileKey()); // Profile Key
                });
                map(Types.OPTIONAL_UUID); // Profile uuid
            }
        });
        registerServerbound(State.LOGIN, ServerboundLoginPackets.ENCRYPTION_KEY, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.BYTE_ARRAY_PRIMITIVE); // Public key
                handler(wrapper -> {
                    final ChatSession1_19_1 chatSession = wrapper.user().get(ChatSession1_19_1.class);

                    final byte[] verifyToken = wrapper.read(Types.BYTE_ARRAY_PRIMITIVE); // Verify token
                    wrapper.write(Types.BOOLEAN, chatSession == null); // Is nonce
                    if (chatSession != null) {
                        final long salt = ThreadLocalRandom.current().nextLong();
                        final byte[] signature;
                        try {
                            signature = chatSession.sign(signer -> {
                                signer.accept(wrapper.user().remove(NonceStorage1_19_3.class).nonce());
                                signer.accept(Longs.toByteArray(salt));
                            });
                        } catch (final SignatureException e) {
                            throw new RuntimeException(e);
                        }
                        wrapper.write(Types.LONG, salt); // Salt
                        wrapper.write(Types.BYTE_ARRAY_PRIMITIVE, signature); // Signature
                    } else {
                        wrapper.write(Types.BYTE_ARRAY_PRIMITIVE, verifyToken); // Nonce
                    }
                });
            }
        });

        cancelServerbound(ServerboundPackets1_19_3.CHAT_SESSION_UPDATE);
        cancelClientbound(ClientboundPackets1_19_1.DELETE_CHAT);
        cancelClientbound(ClientboundPackets1_19_1.PLAYER_CHAT_HEADER);
        cancelClientbound(ClientboundPackets1_19_1.CHAT_PREVIEW);
        cancelClientbound(ClientboundPackets1_19_1.SET_DISPLAY_CHAT_PREVIEW);
        cancelServerbound(ServerboundPackets1_19_3.CHAT_ACK);
    }

    @Override
    protected void onMappingDataLoaded() {
        Types1_19_3.PARTICLE.filler(this)
            .reader("block", ParticleType.Readers.BLOCK)
            .reader("block_marker", ParticleType.Readers.BLOCK)
            .reader("dust", ParticleType.Readers.DUST)
            .reader("falling_dust", ParticleType.Readers.BLOCK)
            .reader("dust_color_transition", ParticleType.Readers.DUST_TRANSITION)
            .reader("item", ParticleType.Readers.ITEM1_13_2)
            .reader("vibration", ParticleType.Readers.VIBRATION1_19)
            .reader("sculk_charge", ParticleType.Readers.SCULK_CHARGE)
            .reader("shriek", ParticleType.Readers.SHRIEK);
        EntityTypes1_19_3.initialize(this);

        tagRewriter.removeTag(RegistryType.ITEM, "minecraft:overworld_natural_logs");
        tagRewriter.removeTag(RegistryType.BLOCK, "minecraft:non_flammable_wood");

        super.onMappingDataLoaded();
    }

    @Override
    public void init(final UserConnection user) {
        user.put(new ReceivedMessagesStorage());
        addEntityTracker(user, new EntityTrackerBase(user, EntityTypes1_19_3.PLAYER));
    }

    @Override
    public MappingData getMappingData() {
        return MAPPINGS;
    }

    @Override
    public EntityPacketRewriter1_19_3 getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    public ItemPacketRewriter1_19_3 getItemRewriter() {
        return itemRewriter;
    }

    @Override
    public ParticleRewriter<ClientboundPackets1_19_1> getParticleRewriter() {
        return particleRewriter;
    }

    @Override
    public ComponentRewriter1_19_3 getComponentRewriter() {
        return componentRewriter;
    }

    @Override
    public TagRewriter<ClientboundPackets1_19_1> getTagRewriter() {
        return tagRewriter;
    }
}
