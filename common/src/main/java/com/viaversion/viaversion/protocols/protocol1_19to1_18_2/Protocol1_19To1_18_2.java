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
package com.viaversion.viaversion.protocols.protocol1_19to1_18_2;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_19Types;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.rewriter.EntityRewriter;
import com.viaversion.viaversion.api.rewriter.ItemRewriter;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.minecraft.ParticleType;
import com.viaversion.viaversion.api.type.types.version.Types1_19;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.ServerboundPackets1_17;
import com.viaversion.viaversion.protocols.protocol1_18to1_17_1.ClientboundPackets1_18;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.packets.EntityPackets;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.packets.InventoryPackets;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.packets.WorldPackets;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.storage.NonceStorage;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.storage.SequenceStorage;
import com.viaversion.viaversion.rewriter.CommandRewriter;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public final class Protocol1_19To1_18_2 extends AbstractProtocol<ClientboundPackets1_18, ClientboundPackets1_19, ServerboundPackets1_17, ServerboundPackets1_17> {

    public static final MappingData MAPPINGS = new MappingDataBase("1.18", "1.19", true);
    private static final KeyFactory RSA_FACTORY;

    static {
        try {
            RSA_FACTORY = KeyFactory.getInstance("RSA");
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private final EntityPackets entityRewriter = new EntityPackets(this);
    private final InventoryPackets itemRewriter = new InventoryPackets(this);

    public Protocol1_19To1_18_2() {
        super(ClientboundPackets1_18.class, ClientboundPackets1_19.class, ServerboundPackets1_17.class, ServerboundPackets1_17.class);
    }

    @Override
    protected void registerPackets() {
        final TagRewriter tagRewriter = new TagRewriter(this);
        tagRewriter.registerGeneric(ClientboundPackets1_18.TAGS);

        entityRewriter.register();
        itemRewriter.register();
        WorldPackets.register(this);

        cancelClientbound(ClientboundPackets1_18.ADD_VIBRATION_SIGNAL);

        final SoundRewriter soundRewriter = new SoundRewriter(this);
        registerClientbound(ClientboundPackets1_18.SOUND, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // Sound id
                map(Type.VAR_INT); // Source
                map(Type.INT); // X
                map(Type.INT); // Y
                map(Type.INT); // Z
                map(Type.FLOAT); // Volume
                map(Type.FLOAT); // Pitch
                create(Type.LONG, 0L); // Seed
                handler(soundRewriter.getSoundHandler());
            }
        });
        registerClientbound(ClientboundPackets1_18.ENTITY_SOUND, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // Sound id
                map(Type.VAR_INT); // Source
                map(Type.VAR_INT); // Entity id
                map(Type.FLOAT); // Volume
                map(Type.FLOAT); // Pitch
                create(Type.LONG, 0L); // Seed
                handler(soundRewriter.getSoundHandler());
            }
        });
        registerClientbound(ClientboundPackets1_18.NAMED_SOUND, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING); // Sound name
                map(Type.VAR_INT); // Source
                map(Type.INT); // X
                map(Type.INT); // Y
                map(Type.INT); // Z
                map(Type.FLOAT); // Volume
                map(Type.FLOAT); // Pitch
                create(Type.LONG, 0L); // Seed
            }
        });

        final CommandRewriter commandRewriter = new CommandRewriter(this);
        registerClientbound(ClientboundPackets1_18.DECLARE_COMMANDS, new PacketRemapper() {
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
            }
        });

        // Make every message a system message, including player ones; we don't want to analyze and remove player names from the original component
        registerClientbound(ClientboundPackets1_18.CHAT_MESSAGE, ClientboundPackets1_19.SYSTEM_CHAT, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.COMPONENT); // Message
                map(Type.BYTE); // Type
                read(Type.UUID); // Sender
            }
        });

        registerServerbound(ServerboundPackets1_17.CHAT_MESSAGE, new PacketRemapper() {
            @Override
            public void registerMap() {
                read(Type.LONG); // Timestamp
                map(Type.STRING); // Message
                read(Type.LONG); // Salt
                read(Type.BYTE_ARRAY_PRIMITIVE); // Signature
            }
        });

        // Login changes
        registerClientbound(State.LOGIN, ClientboundLoginPackets.GAME_PROFILE.getId(), ClientboundLoginPackets.GAME_PROFILE.getId(), new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.UUID); // UUID
                map(Type.STRING); // Name
                handler(wrapper -> {
                    // No properties
                    wrapper.write(Type.VAR_INT, 0);
                });
            }
        });

        registerClientbound(State.LOGIN, ClientboundLoginPackets.HELLO.getId(), ClientboundLoginPackets.HELLO.getId(), new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING); // Server id
                map(Type.BYTE_ARRAY_PRIMITIVE); // Public key
                map(Type.BYTE_ARRAY_PRIMITIVE); // Nonce
                handler(wrapper -> {
                    final byte[] pubKey = wrapper.get(Type.BYTE_ARRAY_PRIMITIVE, 0);
                    final byte[] nonce = wrapper.get(Type.BYTE_ARRAY_PRIMITIVE, 1);
                    final EncodedKeySpec keySpec = new X509EncodedKeySpec(pubKey);
                    final PublicKey key = RSA_FACTORY.generatePublic(keySpec);
                    final Cipher cipher = Cipher.getInstance(key.getAlgorithm());
                    cipher.init(Cipher.ENCRYPT_MODE, key);
                    wrapper.user().put(new NonceStorage(cipher.doFinal(nonce)));
                });
            }
        });

        registerServerbound(State.LOGIN, ServerboundLoginPackets.HELLO.getId(), ServerboundLoginPackets.HELLO.getId(), new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING); // Name
                handler(wrapper -> {
                    // Read the public key
                    if (wrapper.read(Type.BOOLEAN)) {
                        wrapper.read(Type.NBT);
                    }
                });
            }
        });

        registerServerbound(State.LOGIN, ServerboundLoginPackets.ENCRYPTION_KEY.getId(), ServerboundLoginPackets.ENCRYPTION_KEY.getId(), new PacketRemapper() {
            @Override
            public void registerMap() {
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

    @Override
    protected void onMappingDataLoaded() {
        Types1_19.PARTICLE.filler(this)
                .reader("block", ParticleType.Readers.BLOCK)
                .reader("block_marker", ParticleType.Readers.BLOCK)
                .reader("dust", ParticleType.Readers.DUST)
                .reader("falling_dust", ParticleType.Readers.BLOCK)
                .reader("dust_color_transition", ParticleType.Readers.DUST_TRANSITION)
                .reader("item", ParticleType.Readers.VAR_INT_ITEM)
                .reader("vibration", ParticleType.Readers.VIBRATION)
                .reader("sculk_charge", ParticleType.Readers.SCULK_CHARGE)
                .reader("shriek", ParticleType.Readers.SHRIEK);
    }

    @Override
    public void init(final UserConnection user) {
        user.put(new SequenceStorage());
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