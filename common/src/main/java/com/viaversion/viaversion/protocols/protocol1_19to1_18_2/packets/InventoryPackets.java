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
package com.viaversion.viaversion.protocols.protocol1_19to1_18_2.packets;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.data.ParticleMappings;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_18to1_17_1.ClientboundPackets1_18;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.Protocol1_19To1_18_2;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.ServerboundPackets1_19;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.provider.AckSequenceProvider;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.rewriter.RecipeRewriter;
import com.viaversion.viaversion.util.Key;

public final class InventoryPackets extends ItemRewriter<ClientboundPackets1_18, ServerboundPackets1_19, Protocol1_19To1_18_2> {

    public InventoryPackets(Protocol1_19To1_18_2 protocol) {
        super(protocol, Type.ITEM1_13_2, Type.ITEM1_13_2_ARRAY);
    }

    @Override
    public void registerPackets() {
        registerSetCooldown(ClientboundPackets1_18.COOLDOWN);
        registerWindowItems1_17_1(ClientboundPackets1_18.WINDOW_ITEMS);
        registerSetSlot1_17_1(ClientboundPackets1_18.SET_SLOT);
        registerAdvancements(ClientboundPackets1_18.ADVANCEMENTS);
        registerEntityEquipmentArray(ClientboundPackets1_18.ENTITY_EQUIPMENT);
        protocol.registerClientbound(ClientboundPackets1_18.SPAWN_PARTICLE, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT, Type.VAR_INT); // 0 - Particle ID
                map(Type.BOOLEAN); // 1 - Long Distance
                map(Type.DOUBLE); // 2 - X
                map(Type.DOUBLE); // 3 - Y
                map(Type.DOUBLE); // 4 - Z
                map(Type.FLOAT); // 5 - Offset X
                map(Type.FLOAT); // 6 - Offset Y
                map(Type.FLOAT); // 7 - Offset Z
                map(Type.FLOAT); // 8 - Particle Data
                map(Type.INT); // 9 - Particle Count
                handler(wrapper -> {
                    final int id = wrapper.get(Type.VAR_INT, 0);
                    final ParticleMappings particleMappings = protocol.getMappingData().getParticleMappings();
                    if (id == particleMappings.id("vibration")) {
                        wrapper.read(Type.POSITION1_14); // Remove position

                        final String resourceLocation = Key.stripMinecraftNamespace(wrapper.passthrough(Type.STRING));
                        if (resourceLocation.equals("entity")) {
                            wrapper.passthrough(Type.VAR_INT); // Target entity
                            wrapper.write(Type.FLOAT, 0F); // Y offset
                        }
                    }
                });
                handler(getSpawnParticleHandler(Type.VAR_INT));
            }
        });

        registerClickWindow1_17_1(ServerboundPackets1_19.CLICK_WINDOW);
        registerCreativeInvAction(ServerboundPackets1_19.CREATIVE_INVENTORY_ACTION);

        registerWindowPropertyEnchantmentHandler(ClientboundPackets1_18.WINDOW_PROPERTY);

        protocol.registerClientbound(ClientboundPackets1_18.TRADE_LIST, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // Container id
                handler(wrapper -> {
                    final int size = wrapper.read(Type.UNSIGNED_BYTE);
                    wrapper.write(Type.VAR_INT, size);
                    for (int i = 0; i < size; i++) {
                        handleItemToClient(wrapper.user(), wrapper.passthrough(Type.ITEM1_13_2)); // First item
                        handleItemToClient(wrapper.user(), wrapper.passthrough(Type.ITEM1_13_2)); // Result

                        if (wrapper.read(Type.BOOLEAN)) {
                            handleItemToClient(wrapper.user(), wrapper.passthrough(Type.ITEM1_13_2));
                        } else {
                            wrapper.write(Type.ITEM1_13_2, null);
                        }

                        wrapper.passthrough(Type.BOOLEAN); // Out of stock
                        wrapper.passthrough(Type.INT); // Uses
                        wrapper.passthrough(Type.INT); // Max uses
                        wrapper.passthrough(Type.INT); // Xp
                        wrapper.passthrough(Type.INT); // Special price diff
                        wrapper.passthrough(Type.FLOAT); // Price multiplier
                        wrapper.passthrough(Type.INT); //Demand
                    }
                });
            }
        });

        protocol.registerServerbound(ServerboundPackets1_19.PLAYER_DIGGING, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // Action
                map(Type.POSITION1_14); // Block position
                map(Type.UNSIGNED_BYTE); // Direction
                handler(sequenceHandler());
            }
        });
        protocol.registerServerbound(ServerboundPackets1_19.PLAYER_BLOCK_PLACEMENT, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // Hand
                map(Type.POSITION1_14); // Block position
                map(Type.VAR_INT); // Direction
                map(Type.FLOAT); // X
                map(Type.FLOAT); // Y
                map(Type.FLOAT); // Z
                map(Type.BOOLEAN); // Inside
                handler(sequenceHandler());
            }
        });
        protocol.registerServerbound(ServerboundPackets1_19.USE_ITEM, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // Hand
                handler(sequenceHandler());
            }
        });

        new RecipeRewriter<>(protocol).register(ClientboundPackets1_18.DECLARE_RECIPES);
    }

    private PacketHandler sequenceHandler() {
        return wrapper -> {
            final int sequence = wrapper.read(Type.VAR_INT);
            final AckSequenceProvider provider = Via.getManager().getProviders().get(AckSequenceProvider.class);
            provider.handleSequence(wrapper.user(), sequence);
        };
    }
}
