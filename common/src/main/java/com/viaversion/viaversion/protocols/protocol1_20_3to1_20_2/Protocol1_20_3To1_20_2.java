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
package com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_20_3;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.packet.provider.SimplePacketTypesProvider;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import com.viaversion.viaversion.api.type.types.version.Types1_20_3;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.rewriter.CommandRewriter1_19_4;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ClientboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ClientboundPacket1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ClientboundPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ServerboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ServerboundPacket1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ServerboundPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundConfigurationPackets1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundPacket1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundPackets1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ServerboundPacket1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ServerboundPackets1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.rewriter.BlockItemPacketRewriter1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.rewriter.EntityPacketRewriter1_20_3;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.util.ComponentUtil;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.UUID;

import static com.viaversion.viaversion.util.ProtocolUtil.packetTypeMap;

public final class Protocol1_20_3To1_20_2 extends AbstractProtocol<ClientboundPacket1_20_2, ClientboundPacket1_20_3, ServerboundPacket1_20_2, ServerboundPacket1_20_3> {

    public static final MappingData MAPPINGS = new MappingDataBase("1.20.2", "1.20.3");
    private final BlockItemPacketRewriter1_20_3 itemRewriter = new BlockItemPacketRewriter1_20_3(this);
    private final EntityPacketRewriter1_20_3 entityRewriter = new EntityPacketRewriter1_20_3(this);
    private final TagRewriter<ClientboundPacket1_20_2> tagRewriter = new TagRewriter<>(this);

    public Protocol1_20_3To1_20_2() {
        super(ClientboundPacket1_20_2.class, ClientboundPacket1_20_3.class, ServerboundPacket1_20_2.class, ServerboundPacket1_20_3.class);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        cancelServerbound(ServerboundPackets1_20_3.CONTAINER_SLOT_STATE_CHANGED);

        tagRewriter.registerGeneric(ClientboundPackets1_20_2.TAGS);

        final SoundRewriter<ClientboundPacket1_20_2> soundRewriter = new SoundRewriter<>(this);
        soundRewriter.register1_19_3Sound(ClientboundPackets1_20_2.SOUND);
        soundRewriter.register1_19_3Sound(ClientboundPackets1_20_2.ENTITY_SOUND);

        new StatisticsRewriter<>(this).register(ClientboundPackets1_20_2.STATISTICS);
        new CommandRewriter1_19_4<>(this).registerDeclareCommands1_19(ClientboundPackets1_20_2.DECLARE_COMMANDS);

        registerClientbound(ClientboundPackets1_20_2.UPDATE_SCORE, wrapper -> {
            wrapper.passthrough(Type.STRING); // Owner

            final int action = wrapper.read(Type.VAR_INT);
            final String objectiveName = wrapper.read(Type.STRING);

            if (action == 1) { // Reset score
                wrapper.write(Type.OPTIONAL_STRING, objectiveName.isEmpty() ? null : objectiveName);
                wrapper.setPacketType(ClientboundPackets1_20_3.RESET_SCORE);
                return;
            }

            wrapper.write(Type.STRING, objectiveName);
            wrapper.passthrough(Type.VAR_INT); // Score

            // Null display and number format
            wrapper.write(Type.OPTIONAL_TAG, null);
            wrapper.write(Type.BOOLEAN, false);
        });
        registerClientbound(ClientboundPackets1_20_2.SCOREBOARD_OBJECTIVE, wrapper -> {
            wrapper.passthrough(Type.STRING); // Objective Name
            final byte action = wrapper.passthrough(Type.BYTE); // Method
            if (action == 0 || action == 2) {
                convertComponent(wrapper); // Display Name
                wrapper.passthrough(Type.VAR_INT); // Render type
                wrapper.write(Type.BOOLEAN, false); // Null number format
            }
        });

        registerServerbound(ServerboundPackets1_20_3.UPDATE_JIGSAW_BLOCK, wrapper -> {
            wrapper.passthrough(Type.POSITION1_14); // Position
            wrapper.passthrough(Type.STRING); // Name
            wrapper.passthrough(Type.STRING); // Target
            wrapper.passthrough(Type.STRING); // Pool
            wrapper.passthrough(Type.STRING); // Final state
            wrapper.passthrough(Type.STRING); // Joint type
            wrapper.read(Type.VAR_INT); // Selection priority
            wrapper.read(Type.VAR_INT); // Placement priority
        });

        // Components are now (mostly) written as nbt instead of json strings
        registerClientbound(ClientboundPackets1_20_2.ADVANCEMENTS, wrapper -> {
            wrapper.passthrough(Type.BOOLEAN); // Reset/clear
            final int size = wrapper.passthrough(Type.VAR_INT); // Mapping size
            for (int i = 0; i < size; i++) {
                wrapper.passthrough(Type.STRING); // Identifier
                wrapper.passthrough(Type.OPTIONAL_STRING); // Parent

                // Display data
                if (wrapper.passthrough(Type.BOOLEAN)) {
                    convertComponent(wrapper); // Title
                    convertComponent(wrapper); // Description
                    itemRewriter.handleItemToClient(wrapper.user(), wrapper.passthrough(Type.ITEM1_20_2)); // Icon
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
                    wrapper.passthrough(Type.VAR_INT); // Type
                    wrapper.passthrough(Type.BYTE); // X
                    wrapper.passthrough(Type.BYTE); // Y
                    wrapper.passthrough(Type.BYTE); // Rotation
                    convertOptionalComponent(wrapper); // Display name
                }
            }
        });
        registerClientbound(ClientboundPackets1_20_2.BOSSBAR, wrapper -> {
            wrapper.passthrough(Type.UUID); // Id

            final int action = wrapper.passthrough(Type.VAR_INT);
            if (action == 0 || action == 3) {
                convertComponent(wrapper);
            }
        });
        registerClientbound(ClientboundPackets1_20_2.PLAYER_CHAT, wrapper -> {
            wrapper.passthrough(Type.UUID); // Sender
            wrapper.passthrough(Type.VAR_INT); // Index
            wrapper.passthrough(Type.OPTIONAL_SIGNATURE_BYTES); // Signature
            wrapper.passthrough(Type.STRING); // Plain content
            wrapper.passthrough(Type.LONG); // Timestamp
            wrapper.passthrough(Type.LONG); // Salt

            final int lastSeen = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < lastSeen; i++) {
                final int index = wrapper.passthrough(Type.VAR_INT);
                if (index == 0) {
                    wrapper.passthrough(Type.SIGNATURE_BYTES);
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

        registerClientbound(ClientboundConfigurationPackets1_20_2.DISCONNECT, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.DISCONNECT, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.RESOURCE_PACK, ClientboundPackets1_20_3.RESOURCE_PACK_PUSH, resourcePackHandler(ClientboundPackets1_20_3.RESOURCE_PACK_POP));
        registerClientbound(ClientboundPackets1_20_2.SERVER_DATA, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.ACTIONBAR, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.TITLE_TEXT, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.TITLE_SUBTITLE, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.DISGUISED_CHAT, wrapper -> {
            convertComponent(wrapper);
            wrapper.passthrough(Type.VAR_INT); // Chat type
            convertComponent(wrapper); // Name
            convertOptionalComponent(wrapper); // Target name
        });
        registerClientbound(ClientboundPackets1_20_2.SYSTEM_CHAT, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.OPEN_WINDOW, wrapper -> {
            wrapper.passthrough(Type.VAR_INT); // Container id

            final int containerTypeId = wrapper.read(Type.VAR_INT);
            wrapper.write(Type.VAR_INT, MAPPINGS.getMenuMappings().getNewId(containerTypeId));

            convertComponent(wrapper);
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
            final BitSet actions = wrapper.passthrough(Type.PROFILE_ACTIONS_ENUM);
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

        registerServerbound(ServerboundPackets1_20_3.RESOURCE_PACK_STATUS, resourcePackStatusHandler());

        registerServerbound(ServerboundConfigurationPackets1_20_2.RESOURCE_PACK, resourcePackStatusHandler());
        registerClientbound(ClientboundConfigurationPackets1_20_2.RESOURCE_PACK, ClientboundConfigurationPackets1_20_3.RESOURCE_PACK_PUSH, resourcePackHandler(ClientboundConfigurationPackets1_20_3.RESOURCE_PACK_POP));
        registerClientbound(ClientboundConfigurationPackets1_20_2.UPDATE_TAGS, tagRewriter.getGenericHandler());
    }

    private PacketHandler resourcePackStatusHandler() {
        return wrapper -> {
            wrapper.read(Type.UUID); // Pack UUID

            final int action = wrapper.read(Type.VAR_INT);
            if (action == 4) { // Downloaded
                wrapper.cancel();
            } else if (action > 4) { // Invalid url, failed reload, and discarded
                wrapper.write(Type.VAR_INT, 2); // Failed download
            } else {
                wrapper.write(Type.VAR_INT, action);
            }
        };
    }

    private PacketHandler resourcePackHandler(final ClientboundPacketType popType) {
        return wrapper -> {
            // Drop old resource packs first
            final PacketWrapper dropPacksPacket = wrapper.create(popType);
            dropPacksPacket.write(Type.OPTIONAL_UUID, null);
            dropPacksPacket.send(Protocol1_20_3To1_20_2.class);

            // Use the hash to write a pack uuid
            final String url = wrapper.read(Type.STRING);
            final String hash = wrapper.read(Type.STRING);
            wrapper.write(Type.UUID, UUID.nameUUIDFromBytes(url.getBytes(StandardCharsets.UTF_8)));
            wrapper.write(Type.STRING, url);
            wrapper.write(Type.STRING, hash);
            wrapper.passthrough(Type.BOOLEAN); // Required
            convertOptionalComponent(wrapper);
        };
    }

    private void convertComponent(final PacketWrapper wrapper) throws Exception {
        wrapper.write(Type.TAG, ComponentUtil.jsonToTag(wrapper.read(Type.COMPONENT)));
    }

    private void convertOptionalComponent(final PacketWrapper wrapper) throws Exception {
        wrapper.write(Type.OPTIONAL_TAG, ComponentUtil.jsonToTag(wrapper.read(Type.OPTIONAL_COMPONENT)));
    }

    @Override
    protected void onMappingDataLoaded() {
        super.onMappingDataLoaded();
        EntityTypes1_20_3.initialize(this);
        Types1_20_3.PARTICLE.filler(this)
                .reader("block", ParticleType.Readers.BLOCK)
                .reader("block_marker", ParticleType.Readers.BLOCK)
                .reader("dust", ParticleType.Readers.DUST)
                .reader("falling_dust", ParticleType.Readers.BLOCK)
                .reader("dust_color_transition", ParticleType.Readers.DUST_TRANSITION)
                .reader("item", ParticleType.Readers.ITEM1_20_2)
                .reader("vibration", ParticleType.Readers.VIBRATION1_20_3)
                .reader("sculk_charge", ParticleType.Readers.SCULK_CHARGE)
                .reader("shriek", ParticleType.Readers.SHRIEK);
    }

    @Override
    public void init(final UserConnection connection) {
        addEntityTracker(connection, new EntityTrackerBase(connection, EntityTypes1_20_3.PLAYER));
    }

    @Override
    public MappingData getMappingData() {
        return MAPPINGS;
    }

    @Override
    public BlockItemPacketRewriter1_20_3 getItemRewriter() {
        return itemRewriter;
    }

    @Override
    public EntityPacketRewriter1_20_3 getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    public TagRewriter<ClientboundPacket1_20_2> getTagRewriter() {
        return tagRewriter;
    }

    @Override
    protected PacketTypesProvider<ClientboundPacket1_20_2, ClientboundPacket1_20_3, ServerboundPacket1_20_2, ServerboundPacket1_20_3> createPacketTypesProvider() {
        return new SimplePacketTypesProvider<>(
                packetTypeMap(unmappedClientboundPacketType, ClientboundPackets1_20_2.class, ClientboundConfigurationPackets1_20_2.class),
                packetTypeMap(mappedClientboundPacketType, ClientboundPackets1_20_3.class, ClientboundConfigurationPackets1_20_3.class),
                packetTypeMap(mappedServerboundPacketType, ServerboundPackets1_20_2.class, ServerboundConfigurationPackets1_20_2.class),
                packetTypeMap(unmappedServerboundPacketType, ServerboundPackets1_20_3.class, ServerboundConfigurationPackets1_20_2.class)
        );
    }
}
