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
package com.viaversion.viaversion.protocols.protocol1_19_1to1_19;

import com.github.steveice10.opennbt.tag.builtin.ByteTag;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.NumberTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.ProfileKey;
import com.viaversion.viaversion.api.minecraft.nbt.BinaryTagIO;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.kyori.adventure.text.Component;
import com.viaversion.viaversion.libs.kyori.adventure.text.TranslatableComponent;
import com.viaversion.viaversion.libs.kyori.adventure.text.format.NamedTextColor;
import com.viaversion.viaversion.libs.kyori.adventure.text.format.Style;
import com.viaversion.viaversion.libs.kyori.adventure.text.format.TextDecoration;
import com.viaversion.viaversion.libs.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import com.viaversion.viaversion.protocols.protocol1_19_1to1_19.storage.ChatTypeStorage;
import com.viaversion.viaversion.protocols.protocol1_19_1to1_19.storage.NonceStorage;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.ClientboundPackets1_19;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.ServerboundPackets1_19;
import com.viaversion.viaversion.util.CipherUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class Protocol1_19_1To1_19 extends AbstractProtocol<ClientboundPackets1_19, ClientboundPackets1_19_1, ServerboundPackets1_19, ServerboundPackets1_19_1> {

    private static final String CHAT_REGISTRY_SNBT = "{\n" +
            "  \"minecraft:chat_type\": {\n" +
            "    \"type\": \"minecraft:chat_type\",\n" +
            "    \"value\": [\n" +
            "         {\n" +
            "            \"name\":\"minecraft:chat\",\n" +
            "            \"id\":1,\n" +
            "            \"element\":{\n" +
            "               \"chat\":{\n" +
            "                  \"translation_key\":\"chat.type.text\",\n" +
            "                  \"parameters\":[\n" +
            "                     \"sender\",\n" +
            "                     \"content\"\n" +
            "                  ]\n" +
            "               },\n" +
            "               \"narration\":{\n" +
            "                  \"translation_key\":\"chat.type.text.narrate\",\n" +
            "                  \"parameters\":[\n" +
            "                     \"sender\",\n" +
            "                     \"content\"\n" +
            "                  ]\n" +
            "               }\n" +
            "            }\n" +
            "         }" +
            "    ]\n" +
            "  }\n" +
            "}";
    private static final CompoundTag CHAT_REGISTRY;

    static {
        try {
            CHAT_REGISTRY = BinaryTagIO.readString(CHAT_REGISTRY_SNBT).get("minecraft:chat_type");
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

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
                    final int signatures = wrapper.passthrough(Type.VAR_INT);
                    for (int i = 0; i < signatures; i++) {
                        wrapper.passthrough(Type.STRING); // Argument name
                        wrapper.passthrough(Type.BYTE_ARRAY_PRIMITIVE); // Signature
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
                map(Type.UNSIGNED_BYTE); // Gamemode
                map(Type.BYTE); // Previous Gamemode
                map(Type.STRING_ARRAY); // World List
                handler(wrapper -> {
                    final ChatTypeStorage chatTypeStorage = wrapper.user().get(ChatTypeStorage.class);
                    chatTypeStorage.clear();

                    final CompoundTag registry = wrapper.passthrough(Type.NBT);
                    final ListTag chatTypes = ((CompoundTag) registry.get("minecraft:chat_type")).get("value");
                    for (final Tag chatType : chatTypes) {
                        final CompoundTag chatTypeCompound = (CompoundTag) chatType;
                        final NumberTag idTag = chatTypeCompound.get("id");
                        chatTypeStorage.addChatType(idTag.asInt(), chatTypeCompound);
                    }

                    // Replace chat types - they won't actually be used
                    registry.put("minecraft:chat_type", CHAT_REGISTRY.clone());
                });
            }
        });

        registerClientbound(ClientboundPackets1_19.SERVER_DATA, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.OPTIONAL_COMPONENT); // Motd
                map(Type.OPTIONAL_STRING); // Encoded icon
                map(Type.BOOLEAN); // Previews chat
                create(Type.BOOLEAN, false); // Enforces secure chat
            }
        });

        registerServerbound(State.LOGIN, ServerboundLoginPackets.HELLO.getId(), ServerboundLoginPackets.HELLO.getId(), new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // Name
                handler(wrapper -> {
                    // Profile keys are not compatible; replace it with an empty one
                    final ProfileKey profileKey = wrapper.read(Type.OPTIONAL_PROFILE_KEY);
                    wrapper.write(Type.OPTIONAL_PROFILE_KEY, null);
                    if (profileKey == null) {
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

    public static @Nullable ChatDecorationResult decorateChatMessage(final CompoundTag chatType, final int chatTypeId, final JsonElement senderName, @Nullable final JsonElement teamName, final JsonElement message) {
        if (chatType == null) {
            Via.getPlatform().getLogger().warning("Chat message has unknown chat type id " + chatTypeId + ". Message: " + message);
            return null;
        }

        CompoundTag chatData = chatType.<CompoundTag>get("element").get("chat");
        boolean overlay = false;
        if (chatData == null) {
            chatData = chatType.<CompoundTag>get("element").get("overlay");
            if (chatData == null) {
                // Either narration or something we don't know
                return null;
            }

            overlay = true;
        }

        final CompoundTag decoaration = chatData.get("decoration");
        if (decoaration == null) {
            return new ChatDecorationResult(message, overlay);
        }

        final String translationKey = (String) decoaration.get("translation_key").getValue();
        final TranslatableComponent.Builder componentBuilder = Component.translatable().key(translationKey);

        // Add the style
        final CompoundTag style = decoaration.get("style");
        if (style != null) {
            final Style.Builder styleBuilder = Style.style();
            final StringTag color = style.get("color");
            if (color != null) {
                final NamedTextColor textColor = NamedTextColor.NAMES.value(color.getValue());
                if (textColor != null) {
                    styleBuilder.color(NamedTextColor.NAMES.value(color.getValue()));
                }
            }

            for (final String key : TextDecoration.NAMES.keys()) {
                if (style.contains(key)) {
                    styleBuilder.decoration(TextDecoration.NAMES.value(key), style.<ByteTag>get(key).asByte() == 1);
                }
            }
            componentBuilder.style(styleBuilder.build());
        }

        // Add the replacements
        final ListTag parameters = decoaration.get("parameters");
        if (parameters != null) {
            final List<Component> arguments = new ArrayList<>();
            for (final Tag element : parameters) {
                JsonElement argument = null;
                switch ((String) element.getValue()) {
                    case "sender":
                        argument = senderName;
                        break;
                    case "content":
                        argument = message;
                        break;
                    case "team_name":
                        Preconditions.checkNotNull(teamName, "Team name is null");
                        argument = teamName;
                        break;
                    default:
                        Via.getPlatform().getLogger().warning("Unknown parameter for chat decoration: " + element.getValue());
                }
                if (argument != null) {
                    arguments.add(GsonComponentSerializer.gson().deserializeFromTree(argument));
                }
            }
            componentBuilder.args(arguments);
        }
        return new ChatDecorationResult(GsonComponentSerializer.gson().serializeToTree(componentBuilder.build()), overlay);
    }
}
