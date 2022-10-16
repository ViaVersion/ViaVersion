/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2022 ViaVersion and contributors
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

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.PlayerMessageSignature;
import com.viaversion.viaversion.api.minecraft.ProfileKey;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_19Types;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.rewriter.EntityRewriter;
import com.viaversion.viaversion.api.rewriter.ItemRewriter;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.BitSetType;
import com.viaversion.viaversion.api.type.types.ByteArrayType;
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

import java.util.BitSet;
import java.util.UUID;

public final class Protocol1_19_3To1_19_1 extends AbstractProtocol<ClientboundPackets1_19_1, ClientboundPackets1_19_3, ServerboundPackets1_19_1, ServerboundPackets1_19_3> {

    public static final MappingData MAPPINGS = new MappingDataBase("1.19", "1.19.3", true);
    private static final BitSetType PROFILE_ACTIONS_ENUM_TYPE = new BitSetType(5);
    private static final ByteArrayType MESSAGE_SIGNATURE_BYTES_TYPE = new ByteArrayType(256);
    private static final UUID ZERO_UUID = new UUID(0, 0);
    private static final byte[] EMPTY_BYTES = new byte[0];
    private final EntityPackets entityRewriter = new EntityPackets(this);
    private final InventoryPackets itemRewriter = new InventoryPackets(this);

    public Protocol1_19_3To1_19_1() {
        super(ClientboundPackets1_19_1.class, ClientboundPackets1_19_3.class, ServerboundPackets1_19_1.class, ServerboundPackets1_19_3.class);
    }

    @Override
    protected void registerPackets() {
        // TODO login probably fucked
        // TODO entities
        // TODO packet enum ids
        final TagRewriter tagRewriter = new TagRewriter(this);
        tagRewriter.registerGeneric(ClientboundPackets1_19_1.TAGS);

        entityRewriter.register();
        itemRewriter.register();

        final SoundRewriter soundRewriter = new SoundRewriter(this);
        soundRewriter.registerSound(ClientboundPackets1_19_1.SOUND);
        soundRewriter.registerSound(ClientboundPackets1_19_1.ENTITY_SOUND);

        new StatisticsRewriter(this).register(ClientboundPackets1_19_1.STATISTICS);

        final CommandRewriter commandRewriter = new CommandRewriter(this);
        registerClientbound(ClientboundPackets1_19_1.DECLARE_COMMANDS, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    final int size = wrapper.passthrough(Type.VAR_INT);
                    for (int i = 0; i < size; i++) {
                        final byte flags = wrapper.passthrough(Type.BYTE);
                        wrapper.passthrough(Type.VAR_INT_ARRAY_PRIMITIVE); // Children indices
                        if ((flags & 0x08) != 0) {
                            wrapper.passthrough(Type.VAR_INT); // Redirect node index
                        }

                        final int nodeType = flags & 0x03;
                        if (nodeType == 1 || nodeType == 2) { // Literal/argument node
                            wrapper.passthrough(Type.STRING); // Name
                        }

                        if (nodeType == 2) { // Argument node
                            final int argumentTypeId = wrapper.read(Type.VAR_INT);
                            final int mappedArgumentTypeId = MAPPINGS.getArgumentTypeMappings().mappings().getNewId(argumentTypeId);
                            Preconditions.checkArgument(mappedArgumentTypeId != -1, "Unknown command argument type id: " + argumentTypeId);
                            wrapper.write(Type.VAR_INT, mappedArgumentTypeId);

                            final String identifier = MAPPINGS.getArgumentTypeMappings().identifier(argumentTypeId);
                            commandRewriter.handleArgument(wrapper, identifier);
                            switch (identifier) {
                                case "minecraft:item_enchantment":
                                    wrapper.write(Type.STRING, "minecraft:enchantment");
                                    break;
                                case "minecraft:mob_effect":
                                    wrapper.write(Type.STRING, "minecraft:mob_effect");
                                    break;
                                case "minecraft:entity_summon":
                                    wrapper.write(Type.STRING, "minecraft:entity_type");
                                    break;
                            }

                            if ((flags & 0x10) != 0) {
                                wrapper.passthrough(Type.STRING); // Suggestion type
                            }
                        }
                    }

                    wrapper.passthrough(Type.VAR_INT); // Root node index
                });
            }
        });

        registerClientbound(ClientboundPackets1_19_1.PLAYER_INFO, ClientboundPackets1_19_3.PLAYER_INFO_UPDATE, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    final int action = wrapper.read(Type.VAR_INT);
                    if (action == 4) { // Remove player
                        // Write into new packet type
                        final int entries = wrapper.passthrough(Type.VAR_INT);
                        final UUID[] uuidsToRemove = new UUID[entries];
                        for (int i = 0; i < entries; i++) {
                            uuidsToRemove[i] = wrapper.read(Type.UUID);
                        }
                        wrapper.write(Type.UUID_ARRAY, uuidsToRemove);
                        wrapper.setPacketType(ClientboundPackets1_19_3.PLAYER_INFO_REMOVE);
                        return;
                    }

                    final BitSet set = new BitSet(5);
                    if (action == 0) {
                        // Includes profile key, gamemode, latency, and display name update - also update listed
                        set.set(0, 5);
                    } else {
                        // Update listed added at 3, initialize chat added at index 1
                        set.set(action > 2 ? action + 2 : action + 1);
                    }

                    wrapper.write(PROFILE_ACTIONS_ENUM_TYPE, set);
                    final int entries = wrapper.passthrough(Type.VAR_INT);
                    for (int i = 0; i < entries; i++) {
                        wrapper.passthrough(Type.UUID); // UUID
                        if (action == 0) { // Add player
                            wrapper.passthrough(Type.STRING); // Player Name

                            final int properties = wrapper.passthrough(Type.VAR_INT);
                            for (int j = 0; j < properties; j++) {
                                wrapper.passthrough(Type.STRING); // Name
                                wrapper.passthrough(Type.STRING); // Value
                                if (wrapper.passthrough(Type.BOOLEAN)) {
                                    wrapper.passthrough(Type.STRING); // Signature
                                }
                            }

                            final int gamemode = wrapper.read(Type.VAR_INT);
                            final int ping = wrapper.read(Type.VAR_INT);
                            final JsonElement displayName = wrapper.passthrough(Type.BOOLEAN) ? wrapper.read(Type.COMPONENT) : null;
                            final ProfileKey profileKey = wrapper.read(Type.OPTIONAL_PROFILE_KEY);

                            // Salvage signed chat
                            wrapper.write(Type.UUID, UUID.randomUUID());
                            wrapper.write(Type.OPTIONAL_PROFILE_KEY, profileKey);

                            wrapper.write(Type.VAR_INT, gamemode);
                            wrapper.write(Type.BOOLEAN, true); // Also update listed
                            wrapper.write(Type.VAR_INT, ping);
                            wrapper.write(Type.OPTIONAL_COMPONENT, displayName);
                        } else if (action == 1 || action == 2) { // Update gamemode/update latency
                            wrapper.passthrough(Type.VAR_INT);
                        } else if (action == 3) { // Update display name
                            final JsonElement displayName = wrapper.passthrough(Type.BOOLEAN) ? wrapper.read(Type.COMPONENT) : null;
                            wrapper.write(Type.OPTIONAL_COMPONENT, displayName);
                        }
                    }
                });
            }
        });
        registerClientbound(ClientboundPackets1_19_1.SERVER_DATA, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.OPTIONAL_COMPONENT); // Motd
                map(Type.OPTIONAL_STRING); // Encoded icon
                read(Type.BOOLEAN); // Remove previews chat
            }
        });

        // Aaaaand once more
        registerClientbound(ClientboundPackets1_19_1.PLAYER_CHAT, ClientboundPackets1_19_3.DISGUISED_CHAT, new PacketRemapper() {
            @Override
            public void registerMap() {
                read(Type.OPTIONAL_BYTE_ARRAY_PRIMITIVE); // Previous signature
                handler(wrapper -> {
                    final PlayerMessageSignature signature = wrapper.read(Type.PLAYER_MESSAGE_SIGNATURE);

                    final String plainText = wrapper.read(Type.STRING);
                    JsonElement component = wrapper.read(Type.OPTIONAL_COMPONENT);
                    final JsonElement unsignedComponent = wrapper.read(Type.OPTIONAL_COMPONENT);
                    if (unsignedComponent != null) {
                        component = unsignedComponent;
                    }
                    if (component == null) {
                        component = GsonComponentSerializer.gson().serializeToTree(Component.text(plainText));
                    }


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


                    wrapper.read(Type.LONG); // Timestamp
                    wrapper.read(Type.LONG); // Salt
                    wrapper.read(Type.PLAYER_MESSAGE_SIGNATURE_ARRAY); // Last seen
                    final int filterMaskType = wrapper.read(Type.VAR_INT);
                    if (filterMaskType == 2) { // Partially filtered
                        wrapper.read(Type.LONG_ARRAY_PRIMITIVE); // Mask
                    }

                    wrapper.write(Type.COMPONENT, component);
                    // Keep chat type at the end
                });
            }
        });

        registerServerbound(ServerboundPackets1_19_3.CHAT_COMMAND, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING); // Command
                map(Type.LONG); // Timestamp
                map(Type.LONG); // Salt
                handler(wrapper -> {
                    final int signatures = wrapper.passthrough(Type.VAR_INT);
                    for (int i = 0; i < signatures; i++) {
                        wrapper.passthrough(Type.STRING); // Argument name
                        final byte[] signature = wrapper.read(MESSAGE_SIGNATURE_BYTES_TYPE);
                        wrapper.write(Type.BYTE_ARRAY_PRIMITIVE, signature);
                    }

                    wrapper.write(Type.BOOLEAN, false); // No signed preview

                    final ReceivedMessagesStorage messagesStorage = wrapper.user().get(ReceivedMessagesStorage.class);
                    messagesStorage.resetUnacknowledgedCount();
                    wrapper.write(Type.PLAYER_MESSAGE_SIGNATURE_ARRAY, messagesStorage.lastSignatures());
                    wrapper.write(Type.OPTIONAL_PLAYER_MESSAGE_SIGNATURE, null); // No last unacknowledged
                });
                read(Type.VAR_INT); // Offset
                read(new BitSetType(20)); // Acknowledged
            }
        });
        registerServerbound(ServerboundPackets1_19_3.CHAT_MESSAGE, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING); // Command
                map(Type.LONG); // Timestamp
                map(Type.LONG); // Salt
                handler(wrapper -> {
                    final byte[] signature = wrapper.read(Type.BOOLEAN) ? wrapper.read(MESSAGE_SIGNATURE_BYTES_TYPE) : null;
                    wrapper.write(Type.BYTE_ARRAY_PRIMITIVE, signature != null ? signature : EMPTY_BYTES);
                    wrapper.write(Type.BOOLEAN, false); // No signed preview

                    final ReceivedMessagesStorage messagesStorage = wrapper.user().get(ReceivedMessagesStorage.class);
                    messagesStorage.resetUnacknowledgedCount();
                    wrapper.write(Type.PLAYER_MESSAGE_SIGNATURE_ARRAY, messagesStorage.lastSignatures());
                    wrapper.write(Type.OPTIONAL_PLAYER_MESSAGE_SIGNATURE, null); // No last unacknowledged
                });
                read(Type.VAR_INT); // Offset
                read(new BitSetType(20)); // Acknowledged
            }
        });

        registerServerbound(State.LOGIN, ServerboundLoginPackets.HELLO.getId(), ServerboundLoginPackets.HELLO.getId(), new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING); // Name
                read(Type.UUID); // Session UUID
            }
        });

        cancelClientbound(ClientboundPackets1_19_1.DELETE_CHAT_MESSAGE);
        cancelClientbound(ClientboundPackets1_19_1.PLAYER_CHAT_HEADER);
        cancelClientbound(ClientboundPackets1_19_1.CHAT_PREVIEW);
        cancelClientbound(ClientboundPackets1_19_1.SET_DISPLAY_CHAT_PREVIEW);
        cancelServerbound(ServerboundPackets1_19_3.CHAT_ACK);
    }

    @Override
    protected void onMappingDataLoaded() {
        entityRewriter.onMappingDataLoaded();
    }

    @Override
    public void init(final UserConnection user) {
        addEntityTracker(user, new EntityTrackerBase(user, Entity1_19Types.PLAYER));
    }

    @Override
    public MappingData getMappingData() {
        return MAPPINGS;
    }

    @Override
    public EntityRewriter getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    public ItemRewriter getItemRewriter() {
        return itemRewriter;
    }
}
