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
package com.viaversion.viaversion.protocols.v1_18_2to1_19;

import com.google.gson.JsonElement;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.GameProfile;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_19;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import com.viaversion.viaversion.api.type.types.version.Types1_19;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ServerboundPackets1_17;
import com.viaversion.viaversion.protocols.v1_17_1to1_18.packet.ClientboundPackets1_18;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.data.MappingData1_19;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.packet.ClientboundPackets1_19;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.packet.ServerboundPackets1_19;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.provider.AckSequenceProvider;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.rewriter.EntityPacketRewriter1_19;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.rewriter.ItemPacketRewriter1_19;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.rewriter.WorldPacketRewriter1_19;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.storage.DimensionRegistryStorage;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.storage.NonceStorage1_19;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.storage.SequenceStorage;
import com.viaversion.viaversion.rewriter.CommandRewriter;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.util.CipherUtil;
import com.viaversion.viaversion.util.ComponentUtil;
import java.util.concurrent.ThreadLocalRandom;

public final class Protocol1_18_2To1_19 extends AbstractProtocol<ClientboundPackets1_18, ClientboundPackets1_19, ServerboundPackets1_17, ServerboundPackets1_19> {

    public static final MappingData1_19 MAPPINGS = new MappingData1_19();
    private final EntityPacketRewriter1_19 entityRewriter = new EntityPacketRewriter1_19(this);
    private final ItemPacketRewriter1_19 itemRewriter = new ItemPacketRewriter1_19(this);
    private final ParticleRewriter<ClientboundPackets1_18> particleRewriter = new ParticleRewriter<>(this);
    private final TagRewriter<ClientboundPackets1_18> tagRewriter = new TagRewriter<>(this);

    public Protocol1_18_2To1_19() {
        super(ClientboundPackets1_18.class, ClientboundPackets1_19.class, ServerboundPackets1_17.class, ServerboundPackets1_19.class);
    }

    public static boolean isTextComponentNull(final JsonElement element) {
        return element == null || element.isJsonNull() || (element.isJsonArray() && element.getAsJsonArray().isEmpty());
    }

    public static JsonElement mapTextComponentIfNull(JsonElement component) {
        if (!isTextComponentNull(component)) {
            return component;
        } else {
            return ComponentUtil.emptyJsonComponent();
        }
    }

    @Override
    protected void registerPackets() {
        tagRewriter.registerGeneric(ClientboundPackets1_18.UPDATE_TAGS);

        entityRewriter.register();
        itemRewriter.register();
        WorldPacketRewriter1_19.register(this);

        cancelClientbound(ClientboundPackets1_18.ADD_VIBRATION_SIGNAL);

        final SoundRewriter<ClientboundPackets1_18> soundRewriter = new SoundRewriter<>(this);
        registerClientbound(ClientboundPackets1_18.SOUND, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // Sound id
                map(Types.VAR_INT); // Source
                map(Types.INT); // X
                map(Types.INT); // Y
                map(Types.INT); // Z
                map(Types.FLOAT); // Volume
                map(Types.FLOAT); // Pitch
                handler(wrapper -> wrapper.write(Types.LONG, randomLong())); // Seed
                handler(soundRewriter.getSoundHandler());
            }
        });
        registerClientbound(ClientboundPackets1_18.SOUND_ENTITY, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // Sound id
                map(Types.VAR_INT); // Source
                map(Types.VAR_INT); // Entity id
                map(Types.FLOAT); // Volume
                map(Types.FLOAT); // Pitch
                handler(wrapper -> wrapper.write(Types.LONG, randomLong())); // Seed
                handler(soundRewriter.getSoundHandler());
            }
        });
        registerClientbound(ClientboundPackets1_18.CUSTOM_SOUND, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); // Sound name
                map(Types.VAR_INT); // Source
                map(Types.INT); // X
                map(Types.INT); // Y
                map(Types.INT); // Z
                map(Types.FLOAT); // Volume
                map(Types.FLOAT); // Pitch
                handler(wrapper -> wrapper.write(Types.LONG, randomLong())); // Seed
            }
        });

        new StatisticsRewriter<>(this).register(ClientboundPackets1_18.AWARD_STATS);

        final PacketHandler singleNullTextComponentMapper = wrapper -> wrapper.write(Types.COMPONENT, mapTextComponentIfNull(wrapper.read(Types.COMPONENT)));
        registerClientbound(ClientboundPackets1_18.SET_TITLE_TEXT, singleNullTextComponentMapper);
        registerClientbound(ClientboundPackets1_18.SET_SUBTITLE_TEXT, singleNullTextComponentMapper);
        registerClientbound(ClientboundPackets1_18.SET_ACTION_BAR_TEXT, singleNullTextComponentMapper);
        registerClientbound(ClientboundPackets1_18.SET_OBJECTIVE, wrapper -> {
            wrapper.passthrough(Types.STRING); // Objective Name
            byte action = wrapper.passthrough(Types.BYTE); // Mode
            if (action == 0 || action == 2) {
                wrapper.write(Types.COMPONENT, mapTextComponentIfNull(wrapper.read(Types.COMPONENT))); // Display Name
            }
        });
        registerClientbound(ClientboundPackets1_18.SET_PLAYER_TEAM, wrapper -> {
            wrapper.passthrough(Types.STRING); // Team Name
            byte action = wrapper.passthrough(Types.BYTE); // Mode
            if (action == 0 || action == 2) {
                wrapper.write(Types.COMPONENT, mapTextComponentIfNull(wrapper.read(Types.COMPONENT))); // Display Name
                wrapper.passthrough(Types.BYTE); // Flags
                wrapper.passthrough(Types.STRING); // Name Tag Visibility
                wrapper.passthrough(Types.STRING); // Collision rule
                wrapper.passthrough(Types.VAR_INT); // Color
                wrapper.write(Types.COMPONENT, mapTextComponentIfNull(wrapper.read(Types.COMPONENT))); // Prefix
                wrapper.write(Types.COMPONENT, mapTextComponentIfNull(wrapper.read(Types.COMPONENT))); // Suffix
            }
        });

        final CommandRewriter<ClientboundPackets1_18> commandRewriter = new CommandRewriter<>(this);
        registerClientbound(ClientboundPackets1_18.COMMANDS, wrapper -> {
            final int size = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < size; i++) {
                final byte flags = wrapper.passthrough(Types.BYTE);
                wrapper.passthrough(Types.VAR_INT_ARRAY_PRIMITIVE); // Children indices
                if ((flags & 0x08) != 0) {
                    wrapper.passthrough(Types.VAR_INT); // Redirect node index
                }

                final int nodeType = flags & 0x03;
                if (nodeType == 1 || nodeType == 2) { // Literal/argument node
                    wrapper.passthrough(Types.STRING); // Name
                }

                if (nodeType == 2) { // Argument node
                    final String argumentType = wrapper.read(Types.STRING);
                    final int argumentTypeId = MAPPINGS.getArgumentTypeMappings().mappedId(argumentType);
                    if (argumentTypeId == -1) {
                        getLogger().warning("Unknown command argument type: " + argumentType);
                    }

                    wrapper.write(Types.VAR_INT, argumentTypeId);
                    commandRewriter.handleArgument(wrapper, argumentType);

                    if ((flags & 0x10) != 0) {
                        wrapper.passthrough(Types.STRING); // Suggestion type
                    }
                }
            }

            wrapper.passthrough(Types.VAR_INT); // Root node index
        });

        // Make every message a system message, including player ones; we don't want to analyze and remove player names from the original component
        registerClientbound(ClientboundPackets1_18.CHAT, ClientboundPackets1_19.SYSTEM_CHAT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.COMPONENT); // Message
                handler(wrapper -> {
                    final int type = wrapper.read(Types.BYTE);
                    wrapper.write(Types.VAR_INT, type == 0 ? 1 : type);
                });
                read(Types.UUID); // Sender
            }
        });

        registerServerbound(ServerboundPackets1_19.CHAT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); // Message
                read(Types.LONG); // Timestamp
                read(Types.LONG); // Salt
                read(Types.BYTE_ARRAY_PRIMITIVE); // Signature
                read(Types.BOOLEAN); // Signed preview
            }
        });
        registerServerbound(ServerboundPackets1_19.CHAT_COMMAND, ServerboundPackets1_17.CHAT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); // Command
                read(Types.LONG); // Timestamp
                read(Types.LONG); // Salt
                handler(wrapper -> {
                    final String command = wrapper.get(Types.STRING, 0);
                    wrapper.set(Types.STRING, 0, "/" + command);

                    final int signatures = wrapper.read(Types.VAR_INT);
                    for (int i = 0; i < signatures; i++) {
                        wrapper.read(Types.STRING); // Argument name
                        wrapper.read(Types.BYTE_ARRAY_PRIMITIVE); // Signature
                    }
                });
                read(Types.BOOLEAN); // Signed preview
            }
        });
        cancelServerbound(ServerboundPackets1_19.CHAT_PREVIEW);

        // Login changes
        registerClientbound(State.LOGIN, ClientboundLoginPackets.LOGIN_FINISHED, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.UUID); // UUID
                map(Types.STRING); // Name
                create(Types.PROFILE_PROPERTY_ARRAY, new GameProfile.Property[0]); // No properties
            }
        });

        registerClientbound(State.LOGIN, ClientboundLoginPackets.HELLO, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); // Server id
                handler(wrapper -> {
                    final byte[] publicKey = wrapper.passthrough(Types.BYTE_ARRAY_PRIMITIVE);
                    final byte[] nonce = wrapper.passthrough(Types.BYTE_ARRAY_PRIMITIVE);
                    wrapper.user().put(new NonceStorage1_19(CipherUtil.encryptNonce(publicKey, nonce)));
                });
            }
        });

        registerServerbound(State.LOGIN, ServerboundLoginPackets.HELLO, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); // Name
                read(Types.OPTIONAL_PROFILE_KEY); // Public profile key
            }
        });

        registerServerbound(State.LOGIN, ServerboundLoginPackets.ENCRYPTION_KEY, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.BYTE_ARRAY_PRIMITIVE); // Keys
                handler(wrapper -> {
                    if (wrapper.read(Types.BOOLEAN)) {
                        // Nonce, just pass it through
                        wrapper.passthrough(Types.BYTE_ARRAY_PRIMITIVE);
                    } else {
                        // 🧂
                        final NonceStorage1_19 nonceStorage = wrapper.user().remove(NonceStorage1_19.class);
                        if (nonceStorage == null) {
                            throw new IllegalArgumentException("Server sent nonce is missing");
                        }

                        wrapper.read(Types.LONG); // Salt
                        wrapper.read(Types.BYTE_ARRAY_PRIMITIVE); // Signature
                        wrapper.write(Types.BYTE_ARRAY_PRIMITIVE, nonceStorage.nonce());
                    }
                });
            }
        });
    }

    private static long randomLong() {
        return ThreadLocalRandom.current().nextLong();
    }

    @Override
    protected void onMappingDataLoaded() {
        Types1_19.PARTICLE.filler(this)
            .reader("block", ParticleType.Readers.BLOCK)
            .reader("block_marker", ParticleType.Readers.BLOCK)
            .reader("dust", ParticleType.Readers.DUST)
            .reader("falling_dust", ParticleType.Readers.BLOCK)
            .reader("dust_color_transition", ParticleType.Readers.DUST_TRANSITION)
            .reader("item", ParticleType.Readers.ITEM1_13_2)
            .reader("vibration", ParticleType.Readers.VIBRATION1_19)
            .reader("sculk_charge", ParticleType.Readers.SCULK_CHARGE)
            .reader("shriek", ParticleType.Readers.SHRIEK);
        EntityTypes1_19.initialize(this);

        tagRewriter.removeTag(RegistryType.ITEM, "minecraft:occludes_vibration_signals");
        tagRewriter.renameTag(RegistryType.ITEM, "minecraft:carpets", "minecraft:wool_carpets");
        tagRewriter.renameTag(RegistryType.BLOCK, "minecraft:carpets", "minecraft:wool_carpets");
        tagRewriter.renameTag(RegistryType.BLOCK, "minecraft:polar_bears_spawnable_on_in_frozen_ocean", "minecraft:polar_bears_spawnable_on_alternate");
        tagRewriter.addEmptyTags(RegistryType.ITEM, "minecraft:chest_boats", "minecraft:dampens_vibrations", "minecraft:mangrove_logs", "minecraft:overworld_natural_logs");
        tagRewriter.addEmptyTags(RegistryType.BLOCK, "minecraft:ancient_city_replaceable", "minecraft:convertable_to_mud", "minecraft:dampens_vibrations",
            "minecraft:frog_prefer_jump_to", "minecraft:frogs_spawnable_on", "minecraft:mangrove_logs", "minecraft:mangrove_logs_can_grow_through",
            "minecraft:mangrove_roots_can_grow_through", "minecraft:nether_carver_replaceables", "minecraft:overworld_carver_replaceables",
            "minecraft:overworld_natural_logs", "minecraft:sculk_replaceable", "minecraft:sculk_replaceable_world_gen", "minecraft:snaps_goat_horn");
        tagRewriter.addEmptyTag(RegistryType.ENTITY, "minecraft:frog_food");
        tagRewriter.addEmptyTags(RegistryType.GAME_EVENT, "minecraft:allay_can_listen", "minecraft:shrieker_can_listen", "minecraft:warden_can_listen");

        super.onMappingDataLoaded();
    }

    @Override
    public void register(final ViaProviders providers) {
        providers.register(AckSequenceProvider.class, new AckSequenceProvider());
    }

    @Override
    public void init(final UserConnection user) {
        if (!user.has(DimensionRegistryStorage.class)) {
            user.put(new DimensionRegistryStorage());
        }
        user.put(new SequenceStorage());
        addEntityTracker(user, new EntityTrackerBase(user, EntityTypes1_19.PLAYER));
    }

    @Override
    public MappingData1_19 getMappingData() {
        return MAPPINGS;
    }

    @Override
    public EntityPacketRewriter1_19 getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    public ItemPacketRewriter1_19 getItemRewriter() {
        return itemRewriter;
    }

    @Override
    public ParticleRewriter<ClientboundPackets1_18> getParticleRewriter() {
        return particleRewriter;
    }

    @Override
    public TagRewriter<ClientboundPackets1_18> getTagRewriter() {
        return tagRewriter;
    }
}
