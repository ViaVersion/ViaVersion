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
package com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1;

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
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.BitSetType;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import com.viaversion.viaversion.api.type.types.version.Types1_19_3;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import com.viaversion.viaversion.protocols.protocol1_19_1to1_19.ClientboundPackets1_19_1;
import com.viaversion.viaversion.protocols.protocol1_19_1to1_19.ServerboundPackets1_19_1;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.packets.EntityPackets;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.packets.InventoryPackets;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.storage.NonceStorage;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.storage.ReceivedMessagesStorage;
import com.viaversion.viaversion.rewriter.CommandRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.Pair;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class Protocol1_19_3To1_19_1 extends AbstractProtocol<ClientboundPackets1_19_1, ClientboundPackets1_19_3, ServerboundPackets1_19_1, ServerboundPackets1_19_3> {

    public static final MappingData MAPPINGS = new MappingDataBase("1.19", "1.19.3");
    private static final UUID ZERO_UUID = new UUID(0, 0);
    private static final byte[] EMPTY_BYTES = new byte[0];
    private final EntityPackets entityRewriter = new EntityPackets(this);
    private final InventoryPackets itemRewriter = new InventoryPackets(this);
    private final TagRewriter<ClientboundPackets1_19_1> tagRewriter = new TagRewriter<>(this);

    public Protocol1_19_3To1_19_1() {
        super(ClientboundPackets1_19_1.class, ClientboundPackets1_19_3.class, ServerboundPackets1_19_1.class, ServerboundPackets1_19_3.class);
    }

    @Override
    protected void registerPackets() {
        // Flint and steel was hardcoded before 1.19.3 to ignite a creeper; has been moved to a tag - adding this ensures offhand doesn't trigger as well
        tagRewriter.addTagRaw(RegistryType.ITEM, "minecraft:creeper_igniters", 733); // 733 = flint_and_steel 1.19.3

        tagRewriter.addEmptyTags(RegistryType.ITEM, "minecraft:bookshelf_books", "minecraft:hanging_signs", "minecraft:stripped_logs");
        tagRewriter.addEmptyTags(RegistryType.BLOCK, "minecraft:all_hanging_signs", "minecraft:ceiling_hanging_signs", "minecraft:invalid_spawn_inside",
                "minecraft:stripped_logs", "minecraft:wall_hanging_signs");
        tagRewriter.registerGeneric(ClientboundPackets1_19_1.TAGS);

        entityRewriter.register();
        itemRewriter.register();

        // Rewrite sounds as holders
        final PacketHandler soundHandler = wrapper -> {
            int soundId = wrapper.read(Type.VAR_INT);
            soundId = MAPPINGS.getSoundMappings().getNewId(soundId);
            if (soundId == -1) {
                wrapper.cancel();
                return;
            }

            wrapper.write(Type.SOUND_EVENT, Holder.of(soundId));
        };
        registerClientbound(ClientboundPackets1_19_1.ENTITY_SOUND, soundHandler);
        registerClientbound(ClientboundPackets1_19_1.SOUND, soundHandler);
        registerClientbound(ClientboundPackets1_19_1.NAMED_SOUND, ClientboundPackets1_19_3.SOUND, wrapper -> {
            final String soundIdentifier = wrapper.read(Type.STRING);
            wrapper.write(Type.SOUND_EVENT, Holder.of(new SoundEvent(soundIdentifier, null)));
        });

        new StatisticsRewriter<>(this).register(ClientboundPackets1_19_1.STATISTICS);

        final CommandRewriter<ClientboundPackets1_19_1> commandRewriter = new CommandRewriter<ClientboundPackets1_19_1>(this) {
            @Override
            public void handleArgument(final PacketWrapper wrapper, final String argumentType) throws Exception {
                switch (argumentType) {
                    case "minecraft:item_enchantment":
                        wrapper.write(Type.STRING, "minecraft:enchantment");
                        break;
                    case "minecraft:mob_effect":
                        wrapper.write(Type.STRING, "minecraft:mob_effect");
                        break;
                    case "minecraft:entity_summon":
                        wrapper.write(Type.STRING, "minecraft:entity_type");
                        break;
                    default:
                        super.handleArgument(wrapper, argumentType);
                        break;
                }
            }

            @Override
            public String handleArgumentType(final String argumentType) {
                switch (argumentType) {
                    case "minecraft:resource":
                        return "minecraft:resource_key";
                    case "minecraft:resource_or_tag":
                        return "minecraft:resource_or_tag_key";
                    case "minecraft:entity_summon":
                    case "minecraft:item_enchantment":
                    case "minecraft:mob_effect":
                        return "minecraft:resource";
                }
                return argumentType;
            }
        };
        commandRewriter.registerDeclareCommands1_19(ClientboundPackets1_19_1.DECLARE_COMMANDS);

        registerClientbound(ClientboundPackets1_19_1.SERVER_DATA, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.OPTIONAL_COMPONENT); // Motd
                map(Type.OPTIONAL_STRING); // Encoded icon
                read(Type.BOOLEAN); // Remove previews chat
            }
        });

        // Aaaaand once more
        registerClientbound(ClientboundPackets1_19_1.PLAYER_CHAT, ClientboundPackets1_19_3.DISGUISED_CHAT, new PacketHandlers() {
            @Override
            public void register() {
                read(Type.OPTIONAL_BYTE_ARRAY_PRIMITIVE); // Previous signature
                handler(wrapper -> {
                    final PlayerMessageSignature signature = wrapper.read(Type.PLAYER_MESSAGE_SIGNATURE);

                    // Store message signature for last seen
                    if (!signature.uuid().equals(ZERO_UUID) && signature.signatureBytes().length != 0) {
                        final ReceivedMessagesStorage messagesStorage = wrapper.user().get(ReceivedMessagesStorage.class);
                        messagesStorage.add(signature);
                        if (messagesStorage.tickUnacknowledged() > 64) {
                            messagesStorage.resetUnacknowledgedCount();

                            // Send chat acknowledgement
                            final PacketWrapper chatAckPacket = wrapper.create(ServerboundPackets1_19_1.CHAT_ACK);
                            chatAckPacket.write(Type.PLAYER_MESSAGE_SIGNATURE_ARRAY, messagesStorage.lastSignatures());
                            chatAckPacket.write(Type.OPTIONAL_PLAYER_MESSAGE_SIGNATURE, null);
                            chatAckPacket.sendToServer(Protocol1_19_3To1_19_1.class);
                        }
                    }

                    final String plainMessage = wrapper.read(Type.STRING);
                    JsonElement decoratedMessage = wrapper.read(Type.OPTIONAL_COMPONENT);

                    wrapper.read(Type.LONG); // Timestamp
                    wrapper.read(Type.LONG); // Salt
                    wrapper.read(Type.PLAYER_MESSAGE_SIGNATURE_ARRAY); // Last seen

                    final JsonElement unsignedMessage = wrapper.read(Type.OPTIONAL_COMPONENT);
                    if (unsignedMessage != null) {
                        decoratedMessage = unsignedMessage;
                    }
                    if (decoratedMessage == null) {
                        decoratedMessage = ComponentUtil.plainToJson(plainMessage);
                    }

                    final int filterMaskType = wrapper.read(Type.VAR_INT);
                    if (filterMaskType == 2) { // Partially filtered
                        wrapper.read(Type.LONG_ARRAY_PRIMITIVE); // Mask
                    }

                    wrapper.write(Type.COMPONENT, decoratedMessage);
                    // Keep chat type at the end
                });
            }
        });

        registerServerbound(ServerboundPackets1_19_3.CHAT_COMMAND, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // Command
                map(Type.LONG); // Timestamp
                map(Type.LONG); // Salt
                handler(wrapper -> {
                    final ChatSession1_19_1 chatSession = wrapper.user().get(ChatSession1_19_1.class);
                    final ReceivedMessagesStorage messagesStorage = wrapper.user().get(ReceivedMessagesStorage.class);

                    final int signatures = wrapper.read(Type.VAR_INT);
                    for (int i = 0; i < signatures; i++) {
                        wrapper.read(Type.STRING); // Argument name
                        wrapper.read(Type.SIGNATURE_BYTES); // Signature
                    }

                    final SignableCommandArgumentsProvider argumentsProvider = Via.getManager().getProviders().get(SignableCommandArgumentsProvider.class);
                    if (chatSession != null && argumentsProvider != null) {
                        final UUID sender = wrapper.user().getProtocolInfo().getUuid();
                        final String message = wrapper.get(Type.STRING, 0);
                        final long timestamp = wrapper.get(Type.LONG, 0);
                        final long salt = wrapper.get(Type.LONG, 1);

                        final List<Pair<String, String>> arguments = argumentsProvider.getSignableArguments(message);
                        wrapper.write(Type.VAR_INT, arguments.size()); // Signature count

                        for (Pair<String, String> argument : arguments) {
                            final MessageMetadata metadata = new MessageMetadata(sender, timestamp, salt);
                            final DecoratableMessage decoratableMessage = new DecoratableMessage(argument.value());

                            final byte[] signature = chatSession.signChatMessage(metadata, decoratableMessage, messagesStorage.lastSignatures());

                            wrapper.write(Type.STRING, argument.key()); // Argument name
                            wrapper.write(Type.BYTE_ARRAY_PRIMITIVE, signature); // Signature
                        }
                    } else {
                        wrapper.write(Type.VAR_INT, 0); // Signature count
                    }

                    wrapper.write(Type.BOOLEAN, false); // No signed preview

                    messagesStorage.resetUnacknowledgedCount();
                    wrapper.write(Type.PLAYER_MESSAGE_SIGNATURE_ARRAY, messagesStorage.lastSignatures());
                    wrapper.write(Type.OPTIONAL_PLAYER_MESSAGE_SIGNATURE, null); // No last unacknowledged
                });
                read(Type.VAR_INT); // Offset
                read(Type.ACKNOWLEDGED_BIT_SET); // Acknowledged
            }
        });
        registerServerbound(ServerboundPackets1_19_3.CHAT_MESSAGE, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // Message
                map(Type.LONG); // Timestamp
                map(Type.LONG); // Salt
                read(Type.OPTIONAL_SIGNATURE_BYTES); // Signature
                handler(wrapper -> {
                    final ChatSession1_19_1 chatSession = wrapper.user().get(ChatSession1_19_1.class);
                    final ReceivedMessagesStorage messagesStorage = wrapper.user().get(ReceivedMessagesStorage.class);

                    if (chatSession != null) {
                        final UUID sender = wrapper.user().getProtocolInfo().getUuid();
                        final String message = wrapper.get(Type.STRING, 0);
                        final long timestamp = wrapper.get(Type.LONG, 0);
                        final long salt = wrapper.get(Type.LONG, 1);

                        final MessageMetadata metadata = new MessageMetadata(sender, timestamp, salt);
                        final DecoratableMessage decoratableMessage = new DecoratableMessage(message);
                        final byte[] signature = chatSession.signChatMessage(metadata, decoratableMessage, messagesStorage.lastSignatures());

                        wrapper.write(Type.BYTE_ARRAY_PRIMITIVE, signature); // Signature
                        wrapper.write(Type.BOOLEAN, decoratableMessage.isDecorated()); // Signed preview
                    } else {
                        wrapper.write(Type.BYTE_ARRAY_PRIMITIVE, EMPTY_BYTES); // Signature
                        wrapper.write(Type.BOOLEAN, false); // Signed preview
                    }

                    messagesStorage.resetUnacknowledgedCount();
                    wrapper.write(Type.PLAYER_MESSAGE_SIGNATURE_ARRAY, messagesStorage.lastSignatures());
                    wrapper.write(Type.OPTIONAL_PLAYER_MESSAGE_SIGNATURE, null); // No last unacknowledged
                });
                read(Type.VAR_INT); // Offset
                read(Type.ACKNOWLEDGED_BIT_SET); // Acknowledged
            }
        });

        registerClientbound(State.LOGIN, ClientboundLoginPackets.HELLO.getId(), ClientboundLoginPackets.HELLO.getId(), new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // Server id
                map(Type.BYTE_ARRAY_PRIMITIVE); // Public key
                handler(wrapper -> {
                    if (wrapper.user().has(ChatSession1_19_1.class)) {
                        wrapper.user().put(new NonceStorage(wrapper.passthrough(Type.BYTE_ARRAY_PRIMITIVE))); // Nonce
                    }
                });
            }
        });
        registerServerbound(State.LOGIN, ServerboundLoginPackets.HELLO.getId(), ServerboundLoginPackets.HELLO.getId(), new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // Name
                handler(wrapper -> {
                    final ChatSession1_19_1 chatSession = wrapper.user().get(ChatSession1_19_1.class);
                    wrapper.write(Type.OPTIONAL_PROFILE_KEY, chatSession == null ? null : chatSession.getProfileKey()); // Profile Key
                });
                map(Type.OPTIONAL_UUID); // Profile uuid
            }
        });
        registerServerbound(State.LOGIN, ServerboundLoginPackets.ENCRYPTION_KEY.getId(), ServerboundLoginPackets.ENCRYPTION_KEY.getId(), new PacketHandlers() {
            @Override
            public void register() {
                map(Type.BYTE_ARRAY_PRIMITIVE); // Public key
                handler(wrapper -> {
                    final ChatSession1_19_1 chatSession = wrapper.user().get(ChatSession1_19_1.class);

                    final byte[] verifyToken = wrapper.read(Type.BYTE_ARRAY_PRIMITIVE); // Verify token
                    wrapper.write(Type.BOOLEAN, chatSession == null); // Is nonce
                    if (chatSession != null) {
                        final long salt = ThreadLocalRandom.current().nextLong();
                        final byte[] signature = chatSession.sign(signer -> {
                            signer.accept(wrapper.user().remove(NonceStorage.class).nonce());
                            signer.accept(Longs.toByteArray(salt));
                        });
                        wrapper.write(Type.LONG, salt); // Salt
                        wrapper.write(Type.BYTE_ARRAY_PRIMITIVE, signature); // Signature
                    } else {
                        wrapper.write(Type.BYTE_ARRAY_PRIMITIVE, verifyToken); // Nonce
                    }
                });
            }
        });

        cancelServerbound(ServerboundPackets1_19_3.CHAT_SESSION_UPDATE);
        cancelClientbound(ClientboundPackets1_19_1.DELETE_CHAT_MESSAGE);
        cancelClientbound(ClientboundPackets1_19_1.PLAYER_CHAT_HEADER);
        cancelClientbound(ClientboundPackets1_19_1.CHAT_PREVIEW);
        cancelClientbound(ClientboundPackets1_19_1.SET_DISPLAY_CHAT_PREVIEW);
        cancelServerbound(ServerboundPackets1_19_3.CHAT_ACK);
    }

    @Override
    protected void onMappingDataLoaded() {
        super.onMappingDataLoaded();
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
    public EntityPackets getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    public InventoryPackets getItemRewriter() {
        return itemRewriter;
    }

    @Override
    public TagRewriter<ClientboundPackets1_19_1> getTagRewriter() {
        return tagRewriter;
    }
}
