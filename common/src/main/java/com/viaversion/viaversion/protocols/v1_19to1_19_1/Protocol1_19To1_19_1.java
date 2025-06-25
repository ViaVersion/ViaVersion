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
package com.viaversion.viaversion.protocols.v1_19to1_19_1;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.ProfileKey;
import com.viaversion.viaversion.api.minecraft.signature.SignableCommandArgumentsProvider;
import com.viaversion.viaversion.api.minecraft.signature.model.DecoratableMessage;
import com.viaversion.viaversion.api.minecraft.signature.model.MessageMetadata;
import com.viaversion.viaversion.api.minecraft.signature.storage.ChatSession1_19_0;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.mcstructs.text.Style;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.TextFormatting;
import com.viaversion.viaversion.libs.mcstructs.text.components.TranslationComponent;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.packet.ClientboundPackets1_19;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.packet.ServerboundPackets1_19;
import com.viaversion.viaversion.protocols.v1_19to1_19_1.data.ChatDecorationResult;
import com.viaversion.viaversion.protocols.v1_19to1_19_1.data.ChatRegistry1_19_1;
import com.viaversion.viaversion.protocols.v1_19to1_19_1.packet.ClientboundPackets1_19_1;
import com.viaversion.viaversion.protocols.v1_19to1_19_1.packet.ServerboundPackets1_19_1;
import com.viaversion.viaversion.protocols.v1_19to1_19_1.storage.ChatTypeStorage;
import com.viaversion.viaversion.protocols.v1_19to1_19_1.storage.NonceStorage1_19_1;
import com.viaversion.viaversion.util.CipherUtil;
import com.viaversion.viaversion.util.Pair;
import com.viaversion.viaversion.util.ProtocolLogger;
import com.viaversion.viaversion.util.SerializerVersion;
import com.viaversion.viaversion.util.TagUtil;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class Protocol1_19To1_19_1 extends AbstractProtocol<ClientboundPackets1_19, ClientboundPackets1_19_1, ServerboundPackets1_19, ServerboundPackets1_19_1> {

    public static final ProtocolLogger LOGGER = new ProtocolLogger(Protocol1_19To1_19_1.class);

    public Protocol1_19To1_19_1() {
        super(ClientboundPackets1_19.class, ClientboundPackets1_19_1.class, ServerboundPackets1_19.class, ServerboundPackets1_19_1.class);
    }

    @Override
    protected void registerPackets() {
        registerClientbound(ClientboundPackets1_19.SYSTEM_CHAT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.COMPONENT); // Content
                handler(wrapper -> {
                    final int type = wrapper.read(Types.VAR_INT);
                    final boolean overlay = type == 2;
                    wrapper.write(Types.BOOLEAN, overlay);
                });
            }
        });
        registerClientbound(ClientboundPackets1_19.PLAYER_CHAT, ClientboundPackets1_19_1.SYSTEM_CHAT, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    // Back to system chat
                    final JsonElement signedContent = wrapper.read(Types.COMPONENT);
                    final JsonElement unsignedContent = wrapper.read(Types.OPTIONAL_COMPONENT);
                    final int chatTypeId = wrapper.read(Types.VAR_INT);
                    wrapper.read(Types.UUID); // Sender UUID
                    final JsonElement senderName = wrapper.read(Types.COMPONENT);
                    final JsonElement teamName = wrapper.read(Types.OPTIONAL_COMPONENT);

                    final CompoundTag chatType = wrapper.user().get(ChatTypeStorage.class).chatType(chatTypeId);
                    final ChatDecorationResult decorationResult = decorateChatMessage(chatType, chatTypeId, senderName, teamName, unsignedContent != null ? unsignedContent : signedContent);
                    if (decorationResult == null) {
                        wrapper.cancel();
                        return;
                    }

                    wrapper.write(Types.COMPONENT, decorationResult.content());
                    wrapper.write(Types.BOOLEAN, decorationResult.overlay());
                });
                read(Types.LONG); // Timestamp
                read(Types.LONG); // Salt
                read(Types.BYTE_ARRAY_PRIMITIVE); // Signature
            }
        });
        registerServerbound(ServerboundPackets1_19_1.CHAT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); // Message
                map(Types.LONG); // Timestamp
                map(Types.LONG); // Salt
                map(Types.BYTE_ARRAY_PRIMITIVE); // Signature
                map(Types.BOOLEAN); // Signed preview
                handler(wrapper -> {
                    final ChatSession1_19_0 chatSession = wrapper.user().get(ChatSession1_19_0.class);

                    if (chatSession != null) {
                        final UUID sender = wrapper.user().getProtocolInfo().getUuid();
                        final String message = wrapper.get(Types.STRING, 0);
                        final long timestamp = wrapper.get(Types.LONG, 0);
                        final long salt = wrapper.get(Types.LONG, 1);

                        final MessageMetadata metadata = new MessageMetadata(sender, timestamp, salt);
                        final DecoratableMessage decoratableMessage = new DecoratableMessage(message);
                        final byte[] signature;
                        try {
                            signature = chatSession.signChatMessage(metadata, decoratableMessage);
                        } catch (final SignatureException e) {
                            throw new RuntimeException(e);
                        }

                        wrapper.set(Types.BYTE_ARRAY_PRIMITIVE, 0, signature); // Signature
                        wrapper.set(Types.BOOLEAN, 0, decoratableMessage.isDecorated()); // Signed preview
                    }
                });
                read(Types.PLAYER_MESSAGE_SIGNATURE_ARRAY); // Last seen messages
                read(Types.OPTIONAL_PLAYER_MESSAGE_SIGNATURE); // Last received message
            }
        });
        registerServerbound(ServerboundPackets1_19_1.CHAT_COMMAND, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); // Command
                map(Types.LONG); // Timestamp
                map(Types.LONG); // Salt
                handler(wrapper -> {
                    final ChatSession1_19_0 chatSession = wrapper.user().get(ChatSession1_19_0.class);
                    final SignableCommandArgumentsProvider argumentsProvider = Via.getManager().getProviders().get(SignableCommandArgumentsProvider.class);

                    final int signatures = wrapper.read(Types.VAR_INT);
                    for (int i = 0; i < signatures; i++) {
                        wrapper.read(Types.STRING); // Argument name
                        wrapper.read(Types.BYTE_ARRAY_PRIMITIVE); // Signature
                    }

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
                                signature = chatSession.signChatMessage(metadata, decoratableMessage);
                            } catch (final SignatureException e) {
                                throw new RuntimeException(e);
                            }

                            wrapper.write(Types.STRING, argument.key()); // Argument name
                            wrapper.write(Types.BYTE_ARRAY_PRIMITIVE, signature); // Signature
                        }
                    } else {
                        wrapper.write(Types.VAR_INT, 0); // Signature count
                    }
                });
                map(Types.BOOLEAN); // Signed preview
                read(Types.PLAYER_MESSAGE_SIGNATURE_ARRAY); // Last seen messages
                read(Types.OPTIONAL_PLAYER_MESSAGE_SIGNATURE); // Last received message
            }
        });
        cancelServerbound(ServerboundPackets1_19_1.CHAT_ACK);

        registerClientbound(ClientboundPackets1_19.LOGIN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); // Entity ID
                map(Types.BOOLEAN); // Hardcore
                map(Types.BYTE); // Gamemode
                map(Types.BYTE); // Previous Gamemode
                map(Types.STRING_ARRAY); // World List
                handler(wrapper -> {
                    final ChatTypeStorage chatTypeStorage = wrapper.user().get(ChatTypeStorage.class);
                    chatTypeStorage.clear();

                    final CompoundTag registry = wrapper.passthrough(Types.NAMED_COMPOUND_TAG);
                    final ListTag<CompoundTag> chatTypes = TagUtil.removeRegistryEntries(registry, "chat_type");
                    for (final CompoundTag chatType : chatTypes) {
                        final NumberTag idTag = chatType.getNumberTag("id");
                        chatTypeStorage.addChatType(idTag.asInt(), chatType);
                    }

                    // Replace chat types - they won't actually be used
                    registry.put("minecraft:chat_type", ChatRegistry1_19_1.chatRegistry());
                });
            }
        });

        registerClientbound(ClientboundPackets1_19.SERVER_DATA, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.OPTIONAL_COMPONENT); // Motd
                map(Types.OPTIONAL_STRING); // Encoded icon
                map(Types.BOOLEAN); // Previews chat
                create(Types.BOOLEAN, Via.getConfig().enforceSecureChat()); // Enforces secure chat
            }
        });

        registerServerbound(State.LOGIN, ServerboundLoginPackets.HELLO, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); // Name
                handler(wrapper -> {
                    final ProfileKey profileKey = wrapper.read(Types.OPTIONAL_PROFILE_KEY); // Profile Key

                    final ChatSession1_19_0 chatSession = wrapper.user().get(ChatSession1_19_0.class);
                    wrapper.write(Types.OPTIONAL_PROFILE_KEY, chatSession == null ? null : chatSession.getProfileKey()); // Profile Key

                    if (profileKey == null || chatSession != null) {
                        // Modified client that doesn't include the profile key, or already done in 1.18->1.19 protocol; no need to map it
                        wrapper.user().put(new NonceStorage1_19_1(null));
                    }
                });
                read(Types.OPTIONAL_UUID); // Profile uuid
            }
        });
        registerClientbound(State.LOGIN, ClientboundLoginPackets.HELLO, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); // Server id
                handler(wrapper -> {
                    if (wrapper.user().has(NonceStorage1_19_1.class)) {
                        return;
                    }

                    final byte[] publicKey = wrapper.passthrough(Types.BYTE_ARRAY_PRIMITIVE);
                    final byte[] nonce = wrapper.passthrough(Types.BYTE_ARRAY_PRIMITIVE);
                    wrapper.user().put(new NonceStorage1_19_1(CipherUtil.encryptNonce(publicKey, nonce)));
                });
            }
        });
        registerServerbound(State.LOGIN, ServerboundLoginPackets.ENCRYPTION_KEY, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.BYTE_ARRAY_PRIMITIVE); // Keys
                handler(wrapper -> {
                    final NonceStorage1_19_1 nonceStorage = wrapper.user().remove(NonceStorage1_19_1.class);
                    if (nonceStorage.nonce() == null) {
                        return;
                    }

                    final boolean isNonce = wrapper.read(Types.BOOLEAN);
                    wrapper.write(Types.BOOLEAN, true);
                    if (!isNonce) { // Should never be true at this point, but /shrug otherwise
                        wrapper.read(Types.LONG); // Salt
                        wrapper.read(Types.BYTE_ARRAY_PRIMITIVE); // Signature
                        wrapper.write(Types.BYTE_ARRAY_PRIMITIVE, nonceStorage.nonce());
                    }
                });
            }
        });
        registerClientbound(State.LOGIN, ClientboundLoginPackets.CUSTOM_QUERY, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT);
                map(Types.STRING);
                handler(wrapper -> {
                    String identifier = wrapper.get(Types.STRING, 0);
                    if (identifier.equals("velocity:player_info")) {
                        byte[] data = wrapper.passthrough(Types.REMAINING_BYTES);
                        // Velocity modern forwarding version above 1 includes the players public key.
                        // This is an issue because the server will expect a 1.19 key and receive a 1.19.1 key.
                        // Velocity modern forwarding versions: https://github.com/PaperMC/Velocity/blob/1a3fba4250553702d9dcd05731d04347bfc24c9f/proxy/src/main/java/com/velocitypowered/proxy/connection/VelocityConstants.java#L27-L29
                        // And the version can be specified with a single byte: https://github.com/PaperMC/Velocity/blob/1a3fba4250553702d9dcd05731d04347bfc24c9f/proxy/src/main/java/com/velocitypowered/proxy/connection/backend/LoginSessionHandler.java#L88
                        if (data.length == 1 && data[0] > 1) {
                            data[0] = 1;
                        } else if (data.length == 0) { // Or the version is omitted (default version would be used)
                            data = new byte[]{1};
                            wrapper.set(Types.REMAINING_BYTES, 0, data);
                        } else {
                            LOGGER.warning("Received unexpected data in velocity:player_info (length=" + data.length + ")");
                        }
                    }
                });
            }
        });
    }

    @Override
    public void init(final UserConnection connection) {
        connection.put(new ChatTypeStorage());
    }

    @Override
    public ProtocolLogger getLogger() {
        return LOGGER;
    }

    public static @Nullable ChatDecorationResult decorateChatMessage(
        final CompoundTag chatType,
        final int chatTypeId,
        final JsonElement senderName,
        @Nullable final JsonElement teamName,
        final JsonElement message
    ) {
        if (chatType == null) {
            LOGGER.warning("Chat message has unknown chat type id " + chatTypeId + ". Message: " + message);
            return null;
        }

        CompoundTag chatData = chatType.getCompoundTag("element").getCompoundTag("chat");
        boolean overlay = false;
        if (chatData == null) {
            chatData = chatType.getCompoundTag("element").getCompoundTag("overlay");
            if (chatData == null) {
                // Either narration or something we don't know
                return null;
            }

            overlay = true;
        }

        final CompoundTag decoration = chatData.getCompoundTag("decoration");
        if (decoration == null) {
            return new ChatDecorationResult(message, overlay);
        }

        return new ChatDecorationResult(translatabaleComponentFromTag(decoration, senderName, teamName, message), overlay);
    }

    public static JsonElement translatabaleComponentFromTag(
        final CompoundTag tag,
        final JsonElement senderName,
        @Nullable final JsonElement targetName,
        final JsonElement message
    ) {
        final String translationKey = tag.getStringTag("translation_key").getValue();
        final Style style = new Style();

        // Add the style
        final CompoundTag styleTag = tag.getCompoundTag("style");
        if (styleTag != null) {
            final StringTag color = styleTag.getStringTag("color");
            if (color != null) {
                final TextFormatting textColor = TextFormatting.getByName(color.getValue());
                if (textColor != null) {
                    style.setFormatting(textColor);
                }
            }

            for (final Map.Entry<String, TextFormatting> entry : TextFormatting.FORMATTINGS.entrySet()) {
                final NumberTag formattingTag = styleTag.getNumberTag(entry.getKey());
                if (!(formattingTag instanceof ByteTag)) {
                    continue;
                }

                final boolean value = formattingTag.asBoolean();
                final TextFormatting formatting = entry.getValue();
                if (formatting == TextFormatting.OBFUSCATED) {
                    style.setObfuscated(value);
                } else if (formatting == TextFormatting.BOLD) {
                    style.setBold(value);
                } else if (formatting == TextFormatting.STRIKETHROUGH) {
                    style.setStrikethrough(value);
                } else if (formatting == TextFormatting.UNDERLINE) {
                    style.setUnderlined(value);
                } else if (formatting == TextFormatting.ITALIC) {
                    style.setItalic(value);
                }
            }
        }

        // Add the replacements
        final ListTag<StringTag> parameters = tag.getListTag("parameters", StringTag.class);
        final List<TextComponent> arguments = new ArrayList<>();
        if (parameters != null) {
            for (final StringTag element : parameters) {
                JsonElement argument = null;
                switch (element.getValue()) {
                    case "sender" -> argument = senderName;
                    case "content" -> argument = message;
                    case "team_name", "target" /*So that this method can also be used in VB*/ -> {
                        Preconditions.checkNotNull(targetName, "Team name is null");
                        argument = targetName;
                    }
                    default -> LOGGER.warning("Unknown parameter for chat decoration: " + element.getValue());
                }
                if (argument != null) {
                    arguments.add(SerializerVersion.V1_18.toComponent(argument));
                }
            }
        }

        return SerializerVersion.V1_18.toJson(new TranslationComponent(translationKey, arguments));
    }
}
