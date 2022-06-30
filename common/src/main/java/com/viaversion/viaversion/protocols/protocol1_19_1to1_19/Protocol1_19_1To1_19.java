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
package com.viaversion.viaversion.protocols.protocol1_19_1to1_19;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.google.gson.JsonElement;
import com.viaversion.viaversion.api.minecraft.nbt.BinaryTagIO;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.ClientboundPackets1_19;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.ServerboundPackets1_19;

import java.io.IOException;

public final class Protocol1_19_1To1_19 extends AbstractProtocol<ClientboundPackets1_19, ClientboundPackets1_19, ServerboundPackets1_19, ServerboundPackets1_19> {

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

    @Override
    protected void registerPackets() {
        // Skip 1.19 and assume 1.18.2->1.19.1 translation
        registerClientbound(ClientboundPackets1_19.SYSTEM_CHAT, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.COMPONENT); // Content
                handler(wrapper -> {
                    final int type = wrapper.read(Type.VAR_INT);
                    final boolean overlay = type == 2;
                    wrapper.write(Type.BOOLEAN, overlay);
                });
            }
        });
        registerClientbound(ClientboundPackets1_19.PLAYER_CHAT, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.COMPONENT); // Signed content
                map(Type.OPTIONAL_COMPONENT); // Unsigned content
                handler(wrapper -> {
                    // Can only be 1 (chat) or 2 (game info) as per 1.18.2->1.19.0 transformer
                    final int type = wrapper.read(Type.VAR_INT);
                    if (type == 1) {
                        return;
                    }

                    // Send as system message to allow overlay
                    wrapper.cancel();

                    final PacketWrapper systemChatPacket = wrapper.create(ClientboundPackets1_19.SYSTEM_CHAT);
                    JsonElement content = wrapper.get(Type.OPTIONAL_COMPONENT, 0);
                    if (content == null) {
                        content = wrapper.get(Type.COMPONENT, 0);
                    }
                    systemChatPacket.write(Type.COMPONENT, content);
                    systemChatPacket.write(Type.BOOLEAN, true); // Overlay
                    systemChatPacket.send(Protocol1_19_1To1_19.class);
                });
            }
        });

        registerServerbound(State.LOGIN, ServerboundLoginPackets.HELLO.getId(), ServerboundLoginPackets.HELLO.getId(), new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING); // Name
                map(Type.OPTIONAL_PROFILE_KEY); // Public profile key
                read(Type.OPTIONAL_UUID); // Profile uuid
            }
        });

        registerClientbound(ClientboundPackets1_19.JOIN_GAME, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // Entity ID
                map(Type.BOOLEAN); // Hardcore
                map(Type.UNSIGNED_BYTE); // Gamemode
                map(Type.BYTE); // Previous Gamemode
                map(Type.STRING_ARRAY); // World List
                map(Type.NBT); // Registry
                handler(wrapper -> {
                    // Replace chat types
                    final CompoundTag tag = wrapper.get(Type.NBT, 0);
                    tag.put("minecraft:chat_type", CHAT_REGISTRY.clone());
                });
            }
        });
    }
}
