/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2023 ViaVersion and contributors
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

import com.google.gson.JsonElement;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.PlayerMessageSignature;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_19_3Types;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.BitSetType;
import com.viaversion.viaversion.api.type.types.ByteArrayType;
import com.viaversion.viaversion.api.type.types.minecraft.ParticleType;
import com.viaversion.viaversion.api.type.types.version.Types1_19_3;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.libs.kyori.adventure.text.Component;
import com.viaversion.viaversion.libs.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import com.viaversion.viaversion.protocols.protocol1_19_1to1_19.ClientboundPackets1_19_1;
import com.viaversion.viaversion.protocols.protocol1_19_1to1_19.ServerboundPackets1_19_1;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.packets.EntityPackets;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.packets.InventoryPackets;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.storage.ReceivedMessagesStorage;
import com.viaversion.viaversion.rewriter.CommandRewriter;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import java.util.UUID;

public final class Protocol1_19_3To1_19_1 extends AbstractProtocol<ClientboundPackets1_19_1, ClientboundPackets1_19_3, ServerboundPackets1_19_1, ServerboundPackets1_19_3> {

    public static final MappingData MAPPINGS = new MappingDataBase("1.19", "1.19.3");
    private static final ByteArrayType.OptionalByteArrayType OPTIONAL_MESSAGE_SIGNATURE_BYTES_TYPE = new ByteArrayType.OptionalByteArrayType(256);
    private static final ByteArrayType MESSAGE_SIGNATURE_BYTES_TYPE = new ByteArrayType(256);
    private static final BitSetType ACKNOWLEDGED_BIT_SET_TYPE = new BitSetType(20);
    private static final UUID ZERO_UUID = new UUID(0, 0);
    private static final byte[] EMPTY_BYTES = new byte[0];
    private final EntityPackets entityRewriter = new EntityPackets(this);
    private final InventoryPackets itemRewriter = new InventoryPackets(this);

    public Protocol1_19_3To1_19_1() {
        super(ClientboundPackets1_19_1.class, ClientboundPackets1_19_3.class, ServerboundPackets1_19_1.class, ServerboundPackets1_19_3.class);
    }

    @Override
    protected void registerPackets() {
        final TagRewriter<ClientboundPackets1_19_1> tagRewriter = new TagRewriter<>(this);

        // Flint and steel was hardcoded before 1.19.3 to ignite a creeper; has been moved to a tag - adding this ensures offhand doesn't trigger as well
        tagRewriter.addTagRaw(RegistryType.ITEM, "minecraft:creeper_igniters", 733); // 733 = flint_and_steel 1.19.3

        tagRewriter.addEmptyTags(RegistryType.ITEM, "minecraft:bookshelf_books", "minecraft:hanging_signs", "minecraft:stripped_logs");
        tagRewriter.addEmptyTags(RegistryType.BLOCK, "minecraft:all_hanging_signs", "minecraft:ceiling_hanging_signs", "minecraft:invalid_spawn_inside",
                "minecraft:stripped_logs", "minecraft:wall_hanging_signs");
        tagRewriter.registerGeneric(ClientboundPackets1_19_1.TAGS);

        entityRewriter.register();
        itemRewriter.register();

        final SoundRewriter<ClientboundPackets1_19_1> soundRewriter = new SoundRewriter<>(this);
        registerClientbound(ClientboundPackets1_19_1.ENTITY_SOUND, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // Sound id
                handler(soundRewriter.getSoundHandler());
                handler(wrapper -> {
                    // 0 means a resource location will be written
                    final int soundId = wrapper.get(Type.VAR_INT, 0);
                    wrapper.set(Type.VAR_INT, 0, soundId + 1);
                });
            }
        });
        registerClientbound(ClientboundPackets1_19_1.SOUND, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // Sound id
                handler(soundRewriter.getSoundHandler());
                handler(wrapper -> {
                    // 0 means a resource location will be written
                    final int soundId = wrapper.get(Type.VAR_INT, 0);
                    wrapper.set(Type.VAR_INT, 0, soundId + 1);
                });
            }
        });
        registerClientbound(ClientboundPackets1_19_1.NAMED_SOUND, ClientboundPackets1_19_3.SOUND, wrapper -> {
            wrapper.write(Type.VAR_INT, 0);
            wrapper.passthrough(Type.STRING); // Sound identifier
            wrapper.write(Type.OPTIONAL_FLOAT, null); // No fixed range
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
                        decoratedMessage = GsonComponentSerializer.gson().serializeToTree(Component.text(plainMessage));
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
                    final int signatures = wrapper.read(Type.VAR_INT);
                    wrapper.write(Type.VAR_INT, 0);
                    for (int i = 0; i < signatures; i++) {
                        wrapper.read(Type.STRING); // Argument name
                        wrapper.read(MESSAGE_SIGNATURE_BYTES_TYPE); // Signature
                    }

                    wrapper.write(Type.BOOLEAN, false); // No signed preview

                    final ReceivedMessagesStorage messagesStorage = wrapper.user().get(ReceivedMessagesStorage.class);
                    messagesStorage.resetUnacknowledgedCount();
                    wrapper.write(Type.PLAYER_MESSAGE_SIGNATURE_ARRAY, messagesStorage.lastSignatures());
                    wrapper.write(Type.OPTIONAL_PLAYER_MESSAGE_SIGNATURE, null); // No last unacknowledged
                });
                read(Type.VAR_INT); // Offset
                read(ACKNOWLEDGED_BIT_SET_TYPE); // Acknowledged
            }
        });
        registerServerbound(ServerboundPackets1_19_3.CHAT_MESSAGE, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // Command
                map(Type.LONG); // Timestamp
                // Salt
                read(Type.LONG);
                create(Type.LONG, 0L);
                handler(wrapper -> {
                    // Remove signature
                    wrapper.read(OPTIONAL_MESSAGE_SIGNATURE_BYTES_TYPE); // Signature
                    wrapper.write(Type.BYTE_ARRAY_PRIMITIVE, EMPTY_BYTES);
                    wrapper.write(Type.BOOLEAN, false); // No signed preview

                    final ReceivedMessagesStorage messagesStorage = wrapper.user().get(ReceivedMessagesStorage.class);
                    messagesStorage.resetUnacknowledgedCount();
                    wrapper.write(Type.PLAYER_MESSAGE_SIGNATURE_ARRAY, messagesStorage.lastSignatures());
                    wrapper.write(Type.OPTIONAL_PLAYER_MESSAGE_SIGNATURE, null); // No last unacknowledged
                });
                read(Type.VAR_INT); // Offset
                read(ACKNOWLEDGED_BIT_SET_TYPE); // Acknowledged
            }
        });

        // Remove the key once again
        registerServerbound(State.LOGIN, ServerboundLoginPackets.HELLO.getId(), ServerboundLoginPackets.HELLO.getId(), new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // Name
                create(Type.OPTIONAL_PROFILE_KEY, null);
            }
        });
        registerServerbound(State.LOGIN, ServerboundLoginPackets.ENCRYPTION_KEY.getId(), ServerboundLoginPackets.ENCRYPTION_KEY.getId(), new PacketHandlers() {
            @Override
            public void register() {
                map(Type.BYTE_ARRAY_PRIMITIVE); // Keys
                create(Type.BOOLEAN, true); // Is nonce
                map(Type.BYTE_ARRAY_PRIMITIVE); // Encrypted challenge
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
                .reader("item", ParticleType.Readers.VAR_INT_ITEM)
                .reader("vibration", ParticleType.Readers.VIBRATION1_19)
                .reader("sculk_charge", ParticleType.Readers.SCULK_CHARGE)
                .reader("shriek", ParticleType.Readers.SHRIEK);
        Entity1_19_3Types.initialize(this);
    }

    @Override
    public void init(final UserConnection user) {
        user.put(new ReceivedMessagesStorage());
        addEntityTracker(user, new EntityTrackerBase(user, Entity1_19_3Types.PLAYER));
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
}
