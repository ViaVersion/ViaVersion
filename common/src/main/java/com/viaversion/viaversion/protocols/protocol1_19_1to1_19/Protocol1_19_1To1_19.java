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
package com.viaversion.viaversion.protocols.protocol1_19_1to1_19;

import com.github.steveice10.opennbt.tag.builtin.ByteTag;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.NumberTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
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
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import com.viaversion.viaversion.protocols.protocol1_19_1to1_19.data.ChatDecorationResult;
import com.viaversion.viaversion.protocols.protocol1_19_1to1_19.data.ChatRegistry;
import com.viaversion.viaversion.protocols.protocol1_19_1to1_19.storage.ChatTypeStorage;
import com.viaversion.viaversion.protocols.protocol1_19_1to1_19.storage.NonceStorage;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.ClientboundPackets1_19;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.ServerboundPackets1_19;
import com.viaversion.viaversion.util.CipherUtil;
import com.viaversion.viaversion.util.Pair;
import com.viaversion.viaversion.util.TagUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.viaversion.viaversion.util.SerializerVersion;
import net.lenni0451.mcstructs.core.TextFormatting;
import net.lenni0451.mcstructs.text.ATextComponent;
import net.lenni0451.mcstructs.text.Style;
import net.lenni0451.mcstructs.text.components.TranslationComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class Protocol1_19_1To1_19 extends AbstractProtocol<ClientboundPackets1_19, ClientboundPackets1_19_1, ServerboundPackets1_19, ServerboundPackets1_19_1> {

    public Protocol1_19_1To1_19() {
        super(ClientboundPackets1_19.class, ClientboundPackets1_19_1.class, ServerboundPackets1_19.class, ServerboundPackets1_19_1.class);
    }

    @Override
    protected void registerPackets() {
        registerClientbound(ClientboundPackets1_19.SYSTEM_CHAT, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.COMPONENT); // Content
                handler(wrapper -> {
                    final int type = wrapper.read(Type.VAR_INT);
                    final boolean overlay = type == 2;
                    wrapper.write(Type.BOOLEAN, overlay);
                });
            }
        });
        registerClientbound(ClientboundPackets1_19.PLAYER_CHAT, ClientboundPackets1_19_1.SYSTEM_CHAT, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    // Back to system chat
                    final JsonElement signedContent = wrapper.read(Type.COMPONENT);
                    final JsonElement unsignedContent = wrapper.read(Type.OPTIONAL_COMPONENT);
                    final int chatTypeId = wrapper.read(Type.VAR_INT);
                    wrapper.read(Type.UUID); // Sender UUID
                    final JsonElement senderName = wrapper.read(Type.COMPONENT);
                    final JsonElement teamName = wrapper.read(Type.OPTIONAL_COMPONENT);

                    final CompoundTag chatType = wrapper.user().get(ChatTypeStorage.class).chatType(chatTypeId);
                    final ChatDecorationResult decorationResult = decorateChatMessage(chatType, chatTypeId, senderName, teamName, unsignedContent != null ? unsignedContent : signedContent);
                    if (decorationResult == null) {
                        wrapper.cancel();
                        return;
                    }

                    wrapper.write(Type.COMPONENT, decorationResult.content());
                    wrapper.write(Type.BOOLEAN, decorationResult.overlay());
                });
                read(Type.LONG); // Timestamp
                read(Type.LONG); // Salt
                read(Type.BYTE_ARRAY_PRIMITIVE); // Signature
            }
        });
        registerServerbound(ServerboundPackets1_19_1.CHAT_MESSAGE, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // Message
                map(Type.LONG); // Timestamp
                map(Type.LONG); // Salt
                map(Type.BYTE_ARRAY_PRIMITIVE); // Signature
                map(Type.BOOLEAN); // Signed preview
                handler(wrapper -> {
                    final ChatSession1_19_0 chatSession = wrapper.user().get(ChatSession1_19_0.class);

                    if (chatSession != null) {
                        final UUID sender = wrapper.user().getProtocolInfo().getUuid();
                        final String message = wrapper.get(Type.STRING, 0);
                        final long timestamp = wrapper.get(Type.LONG, 0);
                        final long salt = wrapper.get(Type.LONG, 1);

                        final MessageMetadata metadata = new MessageMetadata(sender, timestamp, salt);
                        final DecoratableMessage decoratableMessage = new DecoratableMessage(message);
                        final byte[] signature = chatSession.signChatMessage(metadata, decoratableMessage);

                        wrapper.set(Type.BYTE_ARRAY_PRIMITIVE, 0, signature); // Signature
                        wrapper.set(Type.BOOLEAN, 0, decoratableMessage.isDecorated()); // Signed preview
                    }
                });
                read(Type.PLAYER_MESSAGE_SIGNATURE_ARRAY); // Last seen messages
                read(Type.OPTIONAL_PLAYER_MESSAGE_SIGNATURE); // Last received message
            }
        });
        registerServerbound(ServerboundPackets1_19_1.CHAT_COMMAND, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // Command
                map(Type.LONG); // Timestamp
                map(Type.LONG); // Salt
                handler(wrapper -> {
                    final ChatSession1_19_0 chatSession = wrapper.user().get(ChatSession1_19_0.class);
                    final SignableCommandArgumentsProvider argumentsProvider = Via.getManager().getProviders().get(SignableCommandArgumentsProvider.class);

                    final int signatures = wrapper.read(Type.VAR_INT);
                    for (int i = 0; i < signatures; i++) {
                        wrapper.read(Type.STRING); // Argument name
                        wrapper.read(Type.BYTE_ARRAY_PRIMITIVE); // Signature
                    }

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

                            final byte[] signature = chatSession.signChatMessage(metadata, decoratableMessage);

                            wrapper.write(Type.STRING, argument.key()); // Argument name
                            wrapper.write(Type.BYTE_ARRAY_PRIMITIVE, signature); // Signature
                        }
                    } else {
                        wrapper.write(Type.VAR_INT, 0); // Signature count
                    }
                });
                map(Type.BOOLEAN); // Signed preview
                read(Type.PLAYER_MESSAGE_SIGNATURE_ARRAY); // Last seen messages
                read(Type.OPTIONAL_PLAYER_MESSAGE_SIGNATURE); // Last received message
            }
        });
        cancelServerbound(ServerboundPackets1_19_1.CHAT_ACK);

        registerClientbound(ClientboundPackets1_19.JOIN_GAME, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); // Entity ID
                map(Type.BOOLEAN); // Hardcore
                map(Type.BYTE); // Gamemode
                map(Type.BYTE); // Previous Gamemode
                map(Type.STRING_ARRAY); // World List
                handler(wrapper -> {
                    final ChatTypeStorage chatTypeStorage = wrapper.user().get(ChatTypeStorage.class);
                    chatTypeStorage.clear();

                    final CompoundTag registry = wrapper.passthrough(Type.NAMED_COMPOUND_TAG);
                    final ListTag<CompoundTag> chatTypes = TagUtil.removeRegistryEntries(registry, "chat_type");
                    for (final CompoundTag chatType : chatTypes) {
                        final NumberTag idTag = chatType.getNumberTag("id");
                        chatTypeStorage.addChatType(idTag.asInt(), chatType);
                    }

                    // Replace chat types - they won't actually be used
                    registry.put("minecraft:chat_type", ChatRegistry.chatRegistry());
                });
            }
        });

        registerClientbound(ClientboundPackets1_19.SERVER_DATA, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.OPTIONAL_COMPONENT); // Motd
                map(Type.OPTIONAL_STRING); // Encoded icon
                map(Type.BOOLEAN); // Previews chat
                create(Type.BOOLEAN, Via.getConfig().enforceSecureChat()); // Enforces secure chat
            }
        });

        registerServerbound(State.LOGIN, ServerboundLoginPackets.HELLO.getId(), ServerboundLoginPackets.HELLO.getId(), new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // Name
                handler(wrapper -> {
                    final ProfileKey profileKey = wrapper.read(Type.OPTIONAL_PROFILE_KEY); // Profile Key

                    final ChatSession1_19_0 chatSession = wrapper.user().get(ChatSession1_19_0.class);
                    wrapper.write(Type.OPTIONAL_PROFILE_KEY, chatSession == null ? null : chatSession.getProfileKey()); // Profile Key

                    if (profileKey == null || chatSession != null) {
                        // Modified client that doesn't include the profile key, or already done in 1.18->1.19 protocol; no need to map it
                        wrapper.user().put(new NonceStorage(null));
                    }
                });
                read(Type.OPTIONAL_UUID); // Profile uuid
            }
        });
        registerClientbound(State.LOGIN, ClientboundLoginPackets.HELLO.getId(), ClientboundLoginPackets.HELLO.getId(), new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // Server id
                handler(wrapper -> {
                    if (wrapper.user().has(NonceStorage.class)) {
                        return;
                    }

                    final byte[] publicKey = wrapper.passthrough(Type.BYTE_ARRAY_PRIMITIVE);
                    final byte[] nonce = wrapper.passthrough(Type.BYTE_ARRAY_PRIMITIVE);
                    wrapper.user().put(new NonceStorage(CipherUtil.encryptNonce(publicKey, nonce)));
                });
            }
        });
        registerServerbound(State.LOGIN, ServerboundLoginPackets.ENCRYPTION_KEY.getId(), ServerboundLoginPackets.ENCRYPTION_KEY.getId(), new PacketHandlers() {
            @Override
            public void register() {
                map(Type.BYTE_ARRAY_PRIMITIVE); // Keys
                handler(wrapper -> {
                    final NonceStorage nonceStorage = wrapper.user().remove(NonceStorage.class);
                    if (nonceStorage.nonce() == null) {
                        return;
                    }

                    final boolean isNonce = wrapper.read(Type.BOOLEAN);
                    wrapper.write(Type.BOOLEAN, true);
                    if (!isNonce) { // Should never be true at this point, but /shrug otherwise
                        wrapper.read(Type.LONG); // Salt
                        wrapper.read(Type.BYTE_ARRAY_PRIMITIVE); // Signature
                        wrapper.write(Type.BYTE_ARRAY_PRIMITIVE, nonceStorage.nonce());
                    }
                });
            }
        });
        registerClientbound(State.LOGIN, ClientboundLoginPackets.CUSTOM_QUERY.getId(), ClientboundLoginPackets.CUSTOM_QUERY.getId(), new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT);
                map(Type.STRING);
                handler(wrapper -> {
                    String identifier = wrapper.get(Type.STRING, 0);
                    if (identifier.equals("velocity:player_info")) {
                        byte[] data = wrapper.passthrough(Type.REMAINING_BYTES);
                        // Velocity modern forwarding version above 1 includes the players public key.
                        // This is an issue because the server will expect a 1.19 key and receive a 1.19.1 key.
                        // Velocity modern forwarding versions: https://github.com/PaperMC/Velocity/blob/1a3fba4250553702d9dcd05731d04347bfc24c9f/proxy/src/main/java/com/velocitypowered/proxy/connection/VelocityConstants.java#L27-L29
                        // And the version can be specified with a single byte: https://github.com/PaperMC/Velocity/blob/1a3fba4250553702d9dcd05731d04347bfc24c9f/proxy/src/main/java/com/velocitypowered/proxy/connection/backend/LoginSessionHandler.java#L88
                        if (data.length == 1 && data[0] > 1) {
                            data[0] = 1;
                        } else if (data.length == 0) { // Or the version is omitted (default version would be used)
                            data = new byte[]{1};
                            wrapper.set(Type.REMAINING_BYTES, 0, data);
                        } else {
                            Via.getPlatform().getLogger().warning("Received unexpected data in velocity:player_info (length=" + data.length + ")");
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

    public static @Nullable ChatDecorationResult decorateChatMessage(
            final CompoundTag chatType,
            final int chatTypeId,
            final JsonElement senderName,
            @Nullable final JsonElement teamName,
            final JsonElement message
    ) {
        if (chatType == null) {
            Via.getPlatform().getLogger().warning("Chat message has unknown chat type id " + chatTypeId + ". Message: " + message);
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
        final List<ATextComponent> arguments = new ArrayList<>();
        if (parameters != null) {
            for (final StringTag element : parameters) {
                JsonElement argument = null;
                switch (element.getValue()) {
                    case "sender":
                        argument = senderName;
                        break;
                    case "content":
                        argument = message;
                        break;
                    case "team_name":
                    case "target": // So that this method can also be used in VB
                        Preconditions.checkNotNull(targetName, "Team name is null");
                        argument = targetName;
                        break;
                    default:
                        Via.getPlatform().getLogger().warning("Unknown parameter for chat decoration: " + element.getValue());
                }
                if (argument != null) {
                    arguments.add(SerializerVersion.V1_18.toComponent(argument));
                }
            }
        }

        return SerializerVersion.V1_18.toJson(new TranslationComponent(translationKey, arguments));
    }
}
