/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_18_2to1_19.rewriter;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.data.ParticleMappings;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_17_1to1_18.packet.ClientboundPackets1_18;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.Protocol1_18_2To1_19;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.packet.ServerboundPackets1_19;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.provider.AckSequenceProvider;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.rewriter.RecipeRewriter;
import com.viaversion.viaversion.util.Key;

public final class ItemPacketRewriter1_19 extends ItemRewriter<ClientboundPackets1_18, ServerboundPackets1_19, Protocol1_18_2To1_19> {

    public ItemPacketRewriter1_19(Protocol1_18_2To1_19 protocol) {
        super(protocol, Types.ITEM1_13_2, Types.ITEM1_13_2_ARRAY);
    }

    @Override
    public void registerPackets() {
        registerCooldown(ClientboundPackets1_18.COOLDOWN);
        registerSetContent1_17_1(ClientboundPackets1_18.CONTAINER_SET_CONTENT);
        registerSetSlot1_17_1(ClientboundPackets1_18.CONTAINER_SET_SLOT);
        registerAdvancements(ClientboundPackets1_18.UPDATE_ADVANCEMENTS);
        registerSetEquipment(ClientboundPackets1_18.SET_EQUIPMENT);
        protocol.registerClientbound(ClientboundPackets1_18.LEVEL_PARTICLES, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT, Types.VAR_INT); // 0 - Particle ID
                map(Types.BOOLEAN); // 1 - Long Distance
                map(Types.DOUBLE); // 2 - X
                map(Types.DOUBLE); // 3 - Y
                map(Types.DOUBLE); // 4 - Z
                map(Types.FLOAT); // 5 - Offset X
                map(Types.FLOAT); // 6 - Offset Y
                map(Types.FLOAT); // 7 - Offset Z
                map(Types.FLOAT); // 8 - Particle Data
                map(Types.INT); // 9 - Particle Count
                handler(wrapper -> {
                    final int id = wrapper.get(Types.VAR_INT, 0);
                    final ParticleMappings particleMappings = protocol.getMappingData().getParticleMappings();
                    if (id == particleMappings.id("vibration")) {
                        wrapper.read(Types.BLOCK_POSITION1_14); // Remove position

                        final String resourceLocation = Key.stripMinecraftNamespace(wrapper.passthrough(Types.STRING));
                        if (resourceLocation.equals("entity")) {
                            wrapper.passthrough(Types.VAR_INT); // Target entity
                            wrapper.write(Types.FLOAT, 0F); // Y offset
                        }
                    }
                });
                handler(protocol.getParticleRewriter().levelParticlesHandler1_13(Types.VAR_INT));
            }
        });

        registerContainerClick1_17_1(ServerboundPackets1_19.CONTAINER_CLICK);
        registerSetCreativeModeSlot(ServerboundPackets1_19.SET_CREATIVE_MODE_SLOT);

        registerContainerSetData(ClientboundPackets1_18.CONTAINER_SET_DATA);

        protocol.registerClientbound(ClientboundPackets1_18.MERCHANT_OFFERS, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // Container id
                handler(wrapper -> {
                    final int size = wrapper.read(Types.UNSIGNED_BYTE);
                    wrapper.write(Types.VAR_INT, size);
                    for (int i = 0; i < size; i++) {
                        handleItemToClient(wrapper.user(), wrapper.passthrough(Types.ITEM1_13_2)); // First item
                        handleItemToClient(wrapper.user(), wrapper.passthrough(Types.ITEM1_13_2)); // Result

                        if (wrapper.read(Types.BOOLEAN)) {
                            handleItemToClient(wrapper.user(), wrapper.passthrough(Types.ITEM1_13_2));
                        } else {
                            wrapper.write(Types.ITEM1_13_2, null);
                        }

                        wrapper.passthrough(Types.BOOLEAN); // Out of stock
                        wrapper.passthrough(Types.INT); // Uses
                        wrapper.passthrough(Types.INT); // Max uses
                        wrapper.passthrough(Types.INT); // Xp
                        wrapper.passthrough(Types.INT); // Special price diff
                        wrapper.passthrough(Types.FLOAT); // Price multiplier
                        wrapper.passthrough(Types.INT); //Demand
                    }
                });
            }
        });

        protocol.registerServerbound(ServerboundPackets1_19.PLAYER_ACTION, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // Action
                map(Types.BLOCK_POSITION1_14); // Block position
                map(Types.UNSIGNED_BYTE); // Direction
                handler(sequenceHandler());
            }
        });
        protocol.registerServerbound(ServerboundPackets1_19.USE_ITEM_ON, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // Hand
                map(Types.BLOCK_POSITION1_14); // Block position
                map(Types.VAR_INT); // Direction
                map(Types.FLOAT); // X
                map(Types.FLOAT); // Y
                map(Types.FLOAT); // Z
                map(Types.BOOLEAN); // Inside
                handler(sequenceHandler());
            }
        });
        protocol.registerServerbound(ServerboundPackets1_19.USE_ITEM, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // Hand
                handler(sequenceHandler());
            }
        });

        new RecipeRewriter<>(protocol).register(ClientboundPackets1_18.UPDATE_RECIPES);
    }

    private PacketHandler sequenceHandler() {
        return wrapper -> {
            final int sequence = wrapper.read(Types.VAR_INT);
            final AckSequenceProvider provider = Via.getManager().getProviders().get(AckSequenceProvider.class);
            provider.handleSequence(wrapper.user(), sequence);
        };
    }
}
