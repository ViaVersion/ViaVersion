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
package com.viaversion.viaversion.protocols.protocol1_19to1_18_2;

import com.google.gson.JsonElement;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_19;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import com.viaversion.viaversion.api.type.types.version.Types1_19;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.ServerboundPackets1_17;
import com.viaversion.viaversion.protocols.protocol1_18to1_17_1.ClientboundPackets1_18;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.data.MappingData;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.packets.EntityPackets;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.packets.InventoryPackets;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.packets.WorldPackets;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.provider.AckSequenceProvider;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.storage.DimensionRegistryStorage;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.storage.NonceStorage;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.storage.SequenceStorage;
import com.viaversion.viaversion.rewriter.CommandRewriter;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.util.CipherUtil;
import com.viaversion.viaversion.util.ComponentUtil;
import java.util.concurrent.ThreadLocalRandom;

public final class Protocol1_19To1_18_2 extends AbstractProtocol<ClientboundPackets1_18, ClientboundPackets1_19, ServerboundPackets1_17, ServerboundPackets1_19> {

    public static final MappingData MAPPINGS = new MappingData();
    private final EntityPackets entityRewriter = new EntityPackets(this);
    private final InventoryPackets itemRewriter = new InventoryPackets(this);
    private final TagRewriter<ClientboundPackets1_18> tagRewriter = new TagRewriter<>(this);

    public Protocol1_19To1_18_2() {
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
        tagRewriter.registerGeneric(ClientboundPackets1_18.TAGS);

        entityRewriter.register();
        itemRewriter.register();
        WorldPackets.register(this);

        cancelClientbound(ClientboundPackets1_18.ADD_VIBRATION_SIGNAL);

        final SoundRewriter<ClientboundPackets1_18> soundRewriter = new SoundRewriter<>(this);
        registerClientbound(ClientboundPackets1_18.SOUND, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // Sound id
                map(Type.VAR_INT); // Source
                map(Type.INT); // X
                map(Type.INT); // Y
                map(Type.INT); // Z
                map(Type.FLOAT); // Volume
                map(Type.FLOAT); // Pitch
                handler(wrapper -> wrapper.write(Type.LONG, randomLong())); // Seed
                handler(soundRewriter.getSoundHandler());
            }
        });
        registerClientbound(ClientboundPackets1_18.ENTITY_SOUND, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // Sound id
                map(Type.VAR_INT); // Source
                map(Type.VAR_INT); // Entity id
                map(Type.FLOAT); // Volume
                map(Type.FLOAT); // Pitch
                handler(wrapper -> wrapper.write(Type.LONG, randomLong())); // Seed
                handler(soundRewriter.getSoundHandler());
            }
        });
        registerClientbound(ClientboundPackets1_18.NAMED_SOUND, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // Sound name
                map(Type.VAR_INT); // Source
                map(Type.INT); // X
                map(Type.INT); // Y
                map(Type.INT); // Z
                map(Type.FLOAT); // Volume
                map(Type.FLOAT); // Pitch
                handler(wrapper -> wrapper.write(Type.LONG, randomLong())); // Seed
            }
        });

        new StatisticsRewriter<>(this).register(ClientboundPackets1_18.STATISTICS);

        final PacketHandler singleNullTextComponentMapper = wrapper -> wrapper.write(Type.COMPONENT, mapTextComponentIfNull(wrapper.read(Type.COMPONENT)));
        registerClientbound(ClientboundPackets1_18.TITLE_TEXT, singleNullTextComponentMapper);
        registerClientbound(ClientboundPackets1_18.TITLE_SUBTITLE, singleNullTextComponentMapper);
        registerClientbound(ClientboundPackets1_18.ACTIONBAR, singleNullTextComponentMapper);
        registerClientbound(ClientboundPackets1_18.SCOREBOARD_OBJECTIVE, wrapper -> {
            wrapper.passthrough(Type.STRING); // Objective Name
            byte action = wrapper.passthrough(Type.BYTE); // Mode
            if (action == 0 || action == 2) {
                wrapper.write(Type.COMPONENT, mapTextComponentIfNull(wrapper.read(Type.COMPONENT))); // Display Name
            }
        });
        registerClientbound(ClientboundPackets1_18.TEAMS, wrapper -> {
            wrapper.passthrough(Type.STRING); // Team Name
            byte action = wrapper.passthrough(Type.BYTE); // Mode
            if (action == 0 || action == 2) {
                wrapper.write(Type.COMPONENT, mapTextComponentIfNull(wrapper.read(Type.COMPONENT))); // Display Name
                wrapper.passthrough(Type.BYTE); // Flags
                wrapper.passthrough(Type.STRING); // Name Tag Visibility
                wrapper.passthrough(Type.STRING); // Collision rule
                wrapper.passthrough(Type.VAR_INT); // Color
                wrapper.write(Type.COMPONENT, mapTextComponentIfNull(wrapper.read(Type.COMPONENT))); // Prefix
                wrapper.write(Type.COMPONENT, mapTextComponentIfNull(wrapper.read(Type.COMPONENT))); // Suffix
            }
        });

        final CommandRewriter<ClientboundPackets1_18> commandRewriter = new CommandRewriter<>(this);
        registerClientbound(ClientboundPackets1_18.DECLARE_COMMANDS, wrapper -> {
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
                    final String argumentType = wrapper.read(Type.STRING);
                    final int argumentTypeId = MAPPINGS.getArgumentTypeMappings().mappedId(argumentType);
                    if (argumentTypeId == -1) {
                        Via.getPlatform().getLogger().warning("Unknown command argument type: " + argumentType);
                    }

                    wrapper.write(Type.VAR_INT, argumentTypeId);
                    commandRewriter.handleArgument(wrapper, argumentType);

                    if ((flags & 0x10) != 0) {
                        wrapper.passthrough(Type.STRING); // Suggestion type
                    }
                }
            }

            wrapper.passthrough(Type.VAR_INT); // Root node index
        });

        // Make every message a system message, including player ones; we don't want to analyze and remove player names from the original component
        registerClientbound(ClientboundPackets1_18.CHAT_MESSAGE, ClientboundPackets1_19.SYSTEM_CHAT, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.COMPONENT); // Message
                handler(wrapper -> {
                    final int type = wrapper.read(Type.BYTE);
                    wrapper.write(Type.VAR_INT, type == 0 ? 1 : type);
                });
                read(Type.UUID); // Sender
            }
        });

        registerServerbound(ServerboundPackets1_19.CHAT_MESSAGE, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // Message
                read(Type.LONG); // Timestamp
                read(Type.LONG); // Salt
                read(Type.BYTE_ARRAY_PRIMITIVE); // Signature
                read(Type.BOOLEAN); // Signed preview
            }
        });
        registerServerbound(ServerboundPackets1_19.CHAT_COMMAND, ServerboundPackets1_17.CHAT_MESSAGE, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // Command
                read(Type.LONG); // Timestamp
                read(Type.LONG); // Salt
                handler(wrapper -> {
                    final String command = wrapper.get(Type.STRING, 0);
                    wrapper.set(Type.STRING, 0, "/" + command);

                    final int signatures = wrapper.read(Type.VAR_INT);
                    for (int i = 0; i < signatures; i++) {
                        wrapper.read(Type.STRING); // Argument name
                        wrapper.read(Type.BYTE_ARRAY_PRIMITIVE); // Signature
                    }
                });
                read(Type.BOOLEAN); // Signed preview
            }
        });
        cancelServerbound(ServerboundPackets1_19.CHAT_PREVIEW);

        // Login changes
        registerClientbound(State.LOGIN, ClientboundLoginPackets.GAME_PROFILE.getId(), ClientboundLoginPackets.GAME_PROFILE.getId(), new PacketHandlers() {
            @Override
            public void register() {
                map(Type.UUID); // UUID
                map(Type.STRING); // Name
                create(Type.VAR_INT, 0); // No properties
            }
        });

        registerClientbound(State.LOGIN, ClientboundLoginPackets.HELLO.getId(), ClientboundLoginPackets.HELLO.getId(), new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // Server id
                handler(wrapper -> {
                    final byte[] publicKey = wrapper.passthrough(Type.BYTE_ARRAY_PRIMITIVE);
                    final byte[] nonce = wrapper.passthrough(Type.BYTE_ARRAY_PRIMITIVE);
                    wrapper.user().put(new NonceStorage(CipherUtil.encryptNonce(publicKey, nonce)));
                });
            }
        });

        registerServerbound(State.LOGIN, ServerboundLoginPackets.HELLO.getId(), ServerboundLoginPackets.HELLO.getId(), new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // Name
                read(Type.OPTIONAL_PROFILE_KEY); // Public profile key
            }
        });

        registerServerbound(State.LOGIN, ServerboundLoginPackets.ENCRYPTION_KEY.getId(), ServerboundLoginPackets.ENCRYPTION_KEY.getId(), new PacketHandlers() {
            @Override
            public void register() {
                map(Type.BYTE_ARRAY_PRIMITIVE); // Keys
                handler(wrapper -> {
                    if (wrapper.read(Type.BOOLEAN)) {
                        // Nonce, just pass it through
                        wrapper.passthrough(Type.BYTE_ARRAY_PRIMITIVE);
                    } else {
                        // 🧂
                        final NonceStorage nonceStorage = wrapper.user().remove(NonceStorage.class);
                        if (nonceStorage == null) {
                            throw new IllegalArgumentException("Server sent nonce is missing");
                        }

                        wrapper.read(Type.LONG); // Salt
                        wrapper.read(Type.BYTE_ARRAY_PRIMITIVE); // Signature
                        wrapper.write(Type.BYTE_ARRAY_PRIMITIVE, nonceStorage.nonce());
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
        super.onMappingDataLoaded();
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

    @Override
    public TagRewriter<ClientboundPackets1_18> getTagRewriter() {
        return tagRewriter;
    }
}
