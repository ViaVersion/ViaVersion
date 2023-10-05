/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2023 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_19_4Types;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.BitSetType;
import com.viaversion.viaversion.api.type.types.ByteArrayType;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ClientboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ClientboundPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ServerboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ServerboundPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.rewriter.EntityPacketRewriter1_20_3;
import java.util.BitSet;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class Protocol1_20_3To1_20_2 extends AbstractProtocol<ClientboundPackets1_20_2, ClientboundPackets1_20_2, ServerboundPackets1_20_2, ServerboundPackets1_20_2> {
    private static final BitSetType PROFILE_ACTIONS_ENUM_TYPE = new BitSetType(6);
    private static final ByteArrayType.OptionalByteArrayType OPTIONAL_SIGNATURE_BYTES_TYPE = new ByteArrayType.OptionalByteArrayType(256);
    private static final ByteArrayType SIGNATURE_BYTES_TYPE = new ByteArrayType(256);
    private final EntityPacketRewriter1_20_3 entityRewriter = new EntityPacketRewriter1_20_3(this);

    public Protocol1_20_3To1_20_2() {
        super(ClientboundPackets1_20_2.class, ClientboundPackets1_20_2.class, ServerboundPackets1_20_2.class, ServerboundPackets1_20_2.class);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        // Components are now (mostly) written as nbt instead of json strings
        registerClientbound(ClientboundPackets1_20_2.ADVANCEMENTS, wrapper -> {
            wrapper.passthrough(Type.BOOLEAN); // Reset/clear
            final int size = wrapper.passthrough(Type.VAR_INT); // Mapping size
            for (int i = 0; i < size; i++) {
                wrapper.passthrough(Type.STRING); // Identifier

                // Parent
                if (wrapper.passthrough(Type.BOOLEAN)) {
                    wrapper.passthrough(Type.STRING);
                }

                // Display data
                if (wrapper.passthrough(Type.BOOLEAN)) {
                    convertComponent(wrapper); // Title
                    convertComponent(wrapper); // Description
                    wrapper.passthrough(Type.ITEM1_20_2); // Icon
                    wrapper.passthrough(Type.VAR_INT); // Frame type
                    final int flags = wrapper.passthrough(Type.INT);
                    if ((flags & 1) != 0) {
                        wrapper.passthrough(Type.STRING); // Background texture
                    }
                    wrapper.passthrough(Type.FLOAT); // X
                    wrapper.passthrough(Type.FLOAT); // Y
                }

                final int requirements = wrapper.passthrough(Type.VAR_INT);
                for (int array = 0; array < requirements; array++) {
                    wrapper.passthrough(Type.STRING_ARRAY);
                }

                wrapper.passthrough(Type.BOOLEAN); // Send telemetry
            }
        });
        registerClientbound(ClientboundPackets1_20_2.TAB_COMPLETE, wrapper -> {
            wrapper.passthrough(Type.VAR_INT); // Transaction id
            wrapper.passthrough(Type.VAR_INT); // Start
            wrapper.passthrough(Type.VAR_INT); // Length

            final int suggestions = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < suggestions; i++) {
                wrapper.passthrough(Type.STRING); // Suggestion
                convertOptionalComponent(wrapper); // Tooltip
            }
        });
        registerClientbound(ClientboundPackets1_20_2.MAP_DATA, wrapper -> {
            wrapper.passthrough(Type.VAR_INT); // Map id
            wrapper.passthrough(Type.BYTE); // Scale
            wrapper.passthrough(Type.BOOLEAN); // Locked
            if (wrapper.passthrough(Type.BOOLEAN)) {
                final int icons = wrapper.passthrough(Type.VAR_INT);
                for (int i = 0; i < icons; i++) {
                    wrapper.passthrough(Type.BYTE); // Type
                    wrapper.passthrough(Type.BYTE); // X
                    wrapper.passthrough(Type.BYTE); // Y
                    wrapper.passthrough(Type.BYTE); // Rotation
                    convertOptionalComponent(wrapper); // Display name
                }
            }
        });
        registerClientbound(ClientboundPackets1_20_2.PLAYER_CHAT, wrapper -> {
            wrapper.passthrough(Type.UUID); // Sender
            wrapper.passthrough(Type.VAR_INT); // Index
            wrapper.passthrough(OPTIONAL_SIGNATURE_BYTES_TYPE); // Signature
            wrapper.passthrough(Type.STRING); // Plain content
            wrapper.passthrough(Type.LONG); // Timestamp
            wrapper.passthrough(Type.LONG); // Salt

            final int lastSeen = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < lastSeen; i++) {
                final int index = wrapper.passthrough(Type.VAR_INT);
                if (index == 0) {
                    wrapper.passthrough(SIGNATURE_BYTES_TYPE);
                }
            }

            convertOptionalComponent(wrapper); // Unsigned content

            final int filterMaskType = wrapper.passthrough(Type.VAR_INT);
            if (filterMaskType == 2) {
                wrapper.passthrough(Type.LONG_ARRAY_PRIMITIVE); // Mask
            }

            wrapper.passthrough(Type.VAR_INT); // Chat type
            convertComponent(wrapper); // Sender
            convertOptionalComponent(wrapper); // Target
        });
        registerClientbound(ClientboundPackets1_20_2.SCOREBOARD_OBJECTIVE, wrapper -> {
            wrapper.passthrough(Type.STRING); // Objective Name
            final byte action = wrapper.passthrough(Type.BYTE); // Mode
            if (action == 0 || action == 2) {
                convertComponent(wrapper); // Display Name
            }
        });
        registerClientbound(ClientboundPackets1_20_2.TEAMS, wrapper -> {
            wrapper.passthrough(Type.STRING); // Team Name
            final byte action = wrapper.passthrough(Type.BYTE); // Mode
            if (action == 0 || action == 2) {
                convertComponent(wrapper); // Display Name
                wrapper.passthrough(Type.BYTE); // Flags
                wrapper.passthrough(Type.STRING); // Name Tag Visibility
                wrapper.passthrough(Type.STRING); // Collision rule
                wrapper.passthrough(Type.VAR_INT); // Color
                convertComponent(wrapper); // Prefix
                convertComponent(wrapper); // Suffix
            }
        });

        registerClientbound(State.CONFIGURATION, ClientboundConfigurationPackets1_20_2.DISCONNECT.getId(), ClientboundConfigurationPackets1_20_2.DISCONNECT.getId(), this::convertComponent);
        registerClientbound(State.CONFIGURATION, ClientboundConfigurationPackets1_20_2.RESOURCE_PACK.getId(), ClientboundConfigurationPackets1_20_2.RESOURCE_PACK.getId(), resourcePackHandler());
        registerClientbound(ClientboundPackets1_20_2.DISCONNECT, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.RESOURCE_PACK, resourcePackHandler());
        registerClientbound(ClientboundPackets1_20_2.SERVER_DATA, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.ACTIONBAR, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.TITLE_TEXT, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.TITLE_SUBTITLE, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.DISGUISED_CHAT, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.SYSTEM_CHAT, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.OPEN_WINDOW, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // Id
                map(Type.VAR_INT); // Window Type
                handler(wrapper -> convertComponent(wrapper));
            }
        });
        registerClientbound(ClientboundPackets1_20_2.TAB_LIST, wrapper -> {
            convertComponent(wrapper);
            convertComponent(wrapper);
        });

        registerClientbound(ClientboundPackets1_20_2.COMBAT_KILL, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // Duration
                handler(wrapper -> convertComponent(wrapper));
            }
        });
        registerClientbound(ClientboundPackets1_20_2.PLAYER_INFO_UPDATE, wrapper -> {
            wrapper.cancel();
            final BitSet actions = wrapper.passthrough(PROFILE_ACTIONS_ENUM_TYPE);
            final int entries = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < entries; i++) {
                wrapper.passthrough(Type.UUID);
                if (actions.get(0)) {
                    wrapper.passthrough(Type.STRING); // Player Name

                    final int properties = wrapper.passthrough(Type.VAR_INT);
                    for (int j = 0; j < properties; j++) {
                        wrapper.passthrough(Type.STRING); // Name
                        wrapper.passthrough(Type.STRING); // Value
                        wrapper.passthrough(Type.OPTIONAL_STRING); // Signature
                    }
                }
                if (actions.get(1) && wrapper.passthrough(Type.BOOLEAN)) {
                    wrapper.passthrough(Type.UUID); // Session UUID
                    wrapper.passthrough(Type.PROFILE_KEY);
                }
                if (actions.get(2)) {
                    wrapper.passthrough(Type.VAR_INT); // Gamemode
                }
                if (actions.get(3)) {
                    wrapper.passthrough(Type.BOOLEAN); // Listed
                }
                if (actions.get(4)) {
                    wrapper.passthrough(Type.VAR_INT); // Latency
                }
                if (actions.get(5)) {
                    convertOptionalComponent(wrapper); // Display name
                }
            }
        });
    }

    private PacketHandler resourcePackHandler() {
        return wrapper -> {
            wrapper.passthrough(Type.STRING); // Url
            wrapper.passthrough(Type.STRING); // Hash
            wrapper.passthrough(Type.BOOLEAN); // Required
            convertOptionalComponent(wrapper);
        };
    }

    private void convertComponent(final PacketWrapper wrapper) throws Exception {
        wrapper.write(Type.NAMELESS_NBT, jsonComponentToTag(wrapper.read(Type.COMPONENT)));
    }

    private void convertOptionalComponent(final PacketWrapper wrapper) throws Exception {
        wrapper.write(Type.OPTIONAL_NAMELESS_NBT, jsonComponentToTag(wrapper.read(Type.OPTIONAL_COMPONENT)));
    }

    public static @Nullable JsonElement tagComponentToJson(@Nullable final CompoundTag tag) {
        if (tag == null) {
            return null;
        }

        final JsonObject object = new JsonObject();
        // TODO
        object.addProperty("text", "Subscribe to ViaVersion+ to see this message");
        return object;
    }

    public static @Nullable CompoundTag jsonComponentToTag(@Nullable final JsonElement component) {
        if (component == null) {
            return null;
        }

        final CompoundTag tag = new CompoundTag();
        // TODO
        tag.put("text", new StringTag("Subscribe to ViaVersion+ to see this message"));
        return tag;
    }

    @Override
    public void init(final UserConnection connection) {
        addEntityTracker(connection, new EntityTrackerBase(connection, Entity1_19_4Types.PLAYER));
    }

    @Override
    protected ServerboundPacketType serverboundFinishConfigurationPacket() {
        return ServerboundConfigurationPackets1_20_2.FINISH_CONFIGURATION;
    }

    @Override
    protected ClientboundPacketType clientboundFinishConfigurationPacket() {
        return ClientboundConfigurationPackets1_20_2.FINISH_CONFIGURATION;
    }

    @Override
    public EntityPacketRewriter1_20_3 getEntityRewriter() {
        return entityRewriter;
    }
}
