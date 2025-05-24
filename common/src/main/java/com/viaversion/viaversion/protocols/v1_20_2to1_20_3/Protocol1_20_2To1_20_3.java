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
package com.viaversion.viaversion.protocols.v1_20_2to1_20_3;

import com.viaversion.viaversion.api.Via;
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
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import com.viaversion.viaversion.api.type.types.version.Types1_20_3;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.rewriter.CommandRewriter1_19_4;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ClientboundConfigurationPackets1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ClientboundPacket1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ClientboundPackets1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ServerboundPacket1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ServerboundPackets1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.rewriter.BlockItemPacketRewriter1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.rewriter.EntityPacketRewriter1_20_3;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ClientboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ClientboundPacket1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ClientboundPackets1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ServerboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ServerboundPacket1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ServerboundPackets1_20_2;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.util.ComponentUtil;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.UUID;

import static com.viaversion.viaversion.util.ProtocolUtil.packetTypeMap;

public final class Protocol1_20_2To1_20_3 extends AbstractProtocol<ClientboundPacket1_20_2, ClientboundPacket1_20_3, ServerboundPacket1_20_2, ServerboundPacket1_20_3> {

    public static final MappingData MAPPINGS = new MappingDataBase("1.20.2", "1.20.3");
    private final BlockItemPacketRewriter1_20_3 itemRewriter = new BlockItemPacketRewriter1_20_3(this);
    private final ParticleRewriter<ClientboundPacket1_20_2> particleRewriter = new ParticleRewriter<>(this);
    private final EntityPacketRewriter1_20_3 entityRewriter = new EntityPacketRewriter1_20_3(this);
    private final TagRewriter<ClientboundPacket1_20_2> tagRewriter = new TagRewriter<>(this);

    public Protocol1_20_2To1_20_3() {
        super(ClientboundPacket1_20_2.class, ClientboundPacket1_20_3.class, ServerboundPacket1_20_2.class, ServerboundPacket1_20_3.class);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        cancelServerbound(ServerboundPackets1_20_3.CONTAINER_SLOT_STATE_CHANGED);

        tagRewriter.registerGeneric(ClientboundPackets1_20_2.UPDATE_TAGS);
        tagRewriter.registerGeneric(ClientboundConfigurationPackets1_20_2.UPDATE_TAGS);

        final SoundRewriter<ClientboundPacket1_20_2> soundRewriter = new SoundRewriter<>(this);
        soundRewriter.registerSound1_19_3(ClientboundPackets1_20_2.SOUND);
        soundRewriter.registerSound1_19_3(ClientboundPackets1_20_2.SOUND_ENTITY);

        new StatisticsRewriter<>(this).register(ClientboundPackets1_20_2.AWARD_STATS);
        new CommandRewriter1_19_4<>(this).registerDeclareCommands1_19(ClientboundPackets1_20_2.COMMANDS);

        registerClientbound(ClientboundPackets1_20_2.SET_SCORE, wrapper -> {
            wrapper.passthrough(Types.STRING); // Owner

            final int action = wrapper.read(Types.VAR_INT);
            final String objectiveName = wrapper.read(Types.STRING);

            if (action == 1) { // Reset score
                wrapper.write(Types.OPTIONAL_STRING, objectiveName.isEmpty() ? null : objectiveName);
                wrapper.setPacketType(ClientboundPackets1_20_3.RESET_SCORE);
                return;
            }

            wrapper.write(Types.STRING, objectiveName);
            wrapper.passthrough(Types.VAR_INT); // Score

            // Null display and number format
            wrapper.write(Types.OPTIONAL_TAG, null);
            wrapper.write(Types.BOOLEAN, false);
        });
        registerClientbound(ClientboundPackets1_20_2.SET_OBJECTIVE, wrapper -> {
            wrapper.passthrough(Types.STRING); // Objective Name
            final byte action = wrapper.passthrough(Types.BYTE); // Method
            if (action == 0 || action == 2) {
                convertComponent(wrapper); // Display Name
                final int render = wrapper.passthrough(Types.VAR_INT); // Render type
                if (render == 0 && Via.getConfig().hideScoreboardNumbers()) { // 0 = "integer", 1 = "hearts"
                    wrapper.write(Types.BOOLEAN, true); // has number format
                    wrapper.write(Types.VAR_INT, 0); // Blank format
                } else {
                    wrapper.write(Types.BOOLEAN, false); // has number format
                }
            }
        });

        registerServerbound(ServerboundPackets1_20_3.SET_JIGSAW_BLOCK, wrapper -> {
            wrapper.passthrough(Types.BLOCK_POSITION1_14); // Position
            wrapper.passthrough(Types.STRING); // Name
            wrapper.passthrough(Types.STRING); // Target
            wrapper.passthrough(Types.STRING); // Pool
            wrapper.passthrough(Types.STRING); // Final state
            wrapper.passthrough(Types.STRING); // Joint type
            wrapper.read(Types.VAR_INT); // Selection priority
            wrapper.read(Types.VAR_INT); // Placement priority
        });

        // Components are now (mostly) written as nbt instead of json strings
        registerClientbound(ClientboundPackets1_20_2.UPDATE_ADVANCEMENTS, wrapper -> {
            wrapper.passthrough(Types.BOOLEAN); // Reset/clear
            final int size = wrapper.passthrough(Types.VAR_INT); // Mapping size
            for (int i = 0; i < size; i++) {
                wrapper.passthrough(Types.STRING); // Identifier
                wrapper.passthrough(Types.OPTIONAL_STRING); // Parent

                // Display data
                if (wrapper.passthrough(Types.BOOLEAN)) {
                    convertComponent(wrapper); // Title
                    convertComponent(wrapper); // Description
                    itemRewriter.handleItemToClient(wrapper.user(), wrapper.passthrough(Types.ITEM1_20_2)); // Icon
                    wrapper.passthrough(Types.VAR_INT); // Frame type
                    final int flags = wrapper.passthrough(Types.INT);
                    if ((flags & 1) != 0) {
                        wrapper.passthrough(Types.STRING); // Background texture
                    }
                    wrapper.passthrough(Types.FLOAT); // X
                    wrapper.passthrough(Types.FLOAT); // Y
                }

                final int requirements = wrapper.passthrough(Types.VAR_INT);
                for (int array = 0; array < requirements; array++) {
                    wrapper.passthrough(Types.STRING_ARRAY);
                }

                wrapper.passthrough(Types.BOOLEAN); // Send telemetry
            }
        });
        registerClientbound(ClientboundPackets1_20_2.COMMAND_SUGGESTIONS, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Transaction id
            wrapper.passthrough(Types.VAR_INT); // Start
            wrapper.passthrough(Types.VAR_INT); // Length

            final int suggestions = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < suggestions; i++) {
                wrapper.passthrough(Types.STRING); // Suggestion
                convertOptionalComponent(wrapper); // Tooltip
            }
        });
        registerClientbound(ClientboundPackets1_20_2.MAP_ITEM_DATA, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Map id
            wrapper.passthrough(Types.BYTE); // Scale
            wrapper.passthrough(Types.BOOLEAN); // Locked
            if (wrapper.passthrough(Types.BOOLEAN)) {
                final int icons = wrapper.passthrough(Types.VAR_INT);
                for (int i = 0; i < icons; i++) {
                    wrapper.passthrough(Types.VAR_INT); // Type
                    wrapper.passthrough(Types.BYTE); // X
                    wrapper.passthrough(Types.BYTE); // Y
                    wrapper.passthrough(Types.BYTE); // Rotation
                    convertOptionalComponent(wrapper); // Display name
                }
            }
        });
        registerClientbound(ClientboundPackets1_20_2.BOSS_EVENT, wrapper -> {
            wrapper.passthrough(Types.UUID); // Id

            final int action = wrapper.passthrough(Types.VAR_INT);
            if (action == 0 || action == 3) {
                convertComponent(wrapper);
            }
        });
        registerClientbound(ClientboundPackets1_20_2.PLAYER_CHAT, wrapper -> {
            wrapper.passthrough(Types.UUID); // Sender
            wrapper.passthrough(Types.VAR_INT); // Index
            wrapper.passthrough(Types.OPTIONAL_SIGNATURE_BYTES); // Signature
            wrapper.passthrough(Types.STRING); // Plain content
            wrapper.passthrough(Types.LONG); // Timestamp
            wrapper.passthrough(Types.LONG); // Salt

            final int lastSeen = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < lastSeen; i++) {
                final int index = wrapper.passthrough(Types.VAR_INT);
                if (index == 0) {
                    wrapper.passthrough(Types.SIGNATURE_BYTES);
                }
            }

            convertOptionalComponent(wrapper); // Unsigned content

            final int filterMaskType = wrapper.passthrough(Types.VAR_INT);
            if (filterMaskType == 2) {
                wrapper.passthrough(Types.LONG_ARRAY_PRIMITIVE); // Mask
            }

            wrapper.passthrough(Types.VAR_INT); // Chat type
            convertComponent(wrapper); // Sender
            convertOptionalComponent(wrapper); // Target
        });
        registerClientbound(ClientboundPackets1_20_2.SET_PLAYER_TEAM, wrapper -> {
            wrapper.passthrough(Types.STRING); // Team Name
            final byte action = wrapper.passthrough(Types.BYTE); // Mode
            if (action == 0 || action == 2) {
                convertComponent(wrapper); // Display Name
                wrapper.passthrough(Types.BYTE); // Flags
                wrapper.passthrough(Types.STRING); // Name Tag Visibility
                wrapper.passthrough(Types.STRING); // Collision rule
                wrapper.passthrough(Types.VAR_INT); // Color
                convertComponent(wrapper); // Prefix
                convertComponent(wrapper); // Suffix
            }
        });

        registerClientbound(ClientboundConfigurationPackets1_20_2.DISCONNECT, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.DISCONNECT, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.RESOURCE_PACK, ClientboundPackets1_20_3.RESOURCE_PACK_PUSH, resourcePackHandler(ClientboundPackets1_20_3.RESOURCE_PACK_POP));
        registerClientbound(ClientboundPackets1_20_2.SERVER_DATA, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.SET_ACTION_BAR_TEXT, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.SET_TITLE_TEXT, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.SET_SUBTITLE_TEXT, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.DISGUISED_CHAT, wrapper -> {
            convertComponent(wrapper);
            wrapper.passthrough(Types.VAR_INT); // Chat type
            convertComponent(wrapper); // Name
            convertOptionalComponent(wrapper); // Target name
        });
        registerClientbound(ClientboundPackets1_20_2.SYSTEM_CHAT, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.OPEN_SCREEN, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Container id

            final int containerTypeId = wrapper.read(Types.VAR_INT);
            wrapper.write(Types.VAR_INT, MAPPINGS.getMenuMappings().getNewId(containerTypeId));

            convertComponent(wrapper);
        });
        registerClientbound(ClientboundPackets1_20_2.TAB_LIST, wrapper -> {
            convertComponent(wrapper);
            convertComponent(wrapper);
        });

        registerClientbound(ClientboundPackets1_20_2.PLAYER_COMBAT_KILL, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // Player ID
                handler(wrapper -> convertComponent(wrapper));
            }
        });
        registerClientbound(ClientboundPackets1_20_2.PLAYER_INFO_UPDATE, wrapper -> {
            final BitSet actions = wrapper.passthrough(Types.PROFILE_ACTIONS_ENUM1_19_3);
            final int entries = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < entries; i++) {
                wrapper.passthrough(Types.UUID);
                if (actions.get(0)) {
                    wrapper.passthrough(Types.STRING); // Player Name
                    wrapper.passthrough(Types.PROFILE_PROPERTY_ARRAY);
                }
                if (actions.get(1) && wrapper.passthrough(Types.BOOLEAN)) {
                    wrapper.passthrough(Types.UUID); // Session UUID
                    wrapper.passthrough(Types.PROFILE_KEY);
                }
                if (actions.get(2)) {
                    wrapper.passthrough(Types.VAR_INT); // Gamemode
                }
                if (actions.get(3)) {
                    wrapper.passthrough(Types.BOOLEAN); // Listed
                }
                if (actions.get(4)) {
                    wrapper.passthrough(Types.VAR_INT); // Latency
                }
                if (actions.get(5)) {
                    convertOptionalComponent(wrapper); // Display name
                }
            }
        });

        registerServerbound(ServerboundPackets1_20_3.RESOURCE_PACK, resourcePackStatusHandler());

        registerServerbound(ServerboundConfigurationPackets1_20_2.RESOURCE_PACK, resourcePackStatusHandler());
        registerClientbound(ClientboundConfigurationPackets1_20_2.RESOURCE_PACK, ClientboundConfigurationPackets1_20_3.RESOURCE_PACK_PUSH, resourcePackHandler(ClientboundConfigurationPackets1_20_3.RESOURCE_PACK_POP));
    }

    private PacketHandler resourcePackStatusHandler() {
        return wrapper -> {
            wrapper.read(Types.UUID); // Pack UUID

            final int action = wrapper.read(Types.VAR_INT);
            if (action == 4) { // Downloaded
                wrapper.cancel();
            } else if (action > 4) { // Invalid url, failed reload, and discarded
                wrapper.write(Types.VAR_INT, 2); // Failed download
            } else {
                wrapper.write(Types.VAR_INT, action);
            }
        };
    }

    private PacketHandler resourcePackHandler(final ClientboundPacketType popType) {
        return wrapper -> {
            // Drop old resource packs first
            final PacketWrapper dropPacksPacket = wrapper.create(popType);
            dropPacksPacket.write(Types.OPTIONAL_UUID, null);
            dropPacksPacket.send(Protocol1_20_2To1_20_3.class);

            // Use the hash to write a pack uuid
            final String url = wrapper.read(Types.STRING);
            final String hash = wrapper.read(Types.STRING);
            wrapper.write(Types.UUID, UUID.nameUUIDFromBytes(url.getBytes(StandardCharsets.UTF_8)));
            wrapper.write(Types.STRING, url);
            wrapper.write(Types.STRING, hash);
            wrapper.passthrough(Types.BOOLEAN); // Required
            convertOptionalComponent(wrapper);
        };
    }

    private void convertComponent(final PacketWrapper wrapper) {
        wrapper.write(Types.TAG, ComponentUtil.jsonToTag(wrapper.read(Types.COMPONENT)));
    }

    private void convertOptionalComponent(final PacketWrapper wrapper) {
        wrapper.write(Types.OPTIONAL_TAG, ComponentUtil.jsonToTag(wrapper.read(Types.OPTIONAL_COMPONENT)));
    }

    @Override
    protected void onMappingDataLoaded() {
        EntityTypes1_20_3.initialize(this);
        Types1_20_3.PARTICLE.filler(this)
            .reader("block", ParticleType.Readers.BLOCK)
            .reader("block_marker", ParticleType.Readers.BLOCK)
            .reader("dust", ParticleType.Readers.DUST)
            .reader("falling_dust", ParticleType.Readers.BLOCK)
            .reader("dust_color_transition", ParticleType.Readers.DUST_TRANSITION)
            .reader("item", ParticleType.Readers.item(Types.ITEM1_20_2))
            .reader("vibration", ParticleType.Readers.VIBRATION1_20_3)
            .reader("sculk_charge", ParticleType.Readers.SCULK_CHARGE)
            .reader("shriek", ParticleType.Readers.SHRIEK);

        super.onMappingDataLoaded();
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
    public ParticleRewriter<ClientboundPacket1_20_2> getParticleRewriter() {
        return particleRewriter;
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
