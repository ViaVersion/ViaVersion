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
package com.viaversion.viaversion.protocols.protocol1_19to1_18_2.packets;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_16to1_15_2.data.RecipeRewriter1_16;
import com.viaversion.viaversion.protocols.protocol1_18to1_17_1.ClientboundPackets1_18;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.Protocol1_19To1_18_2;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.ServerboundPackets1_19;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.provider.AckSequenceProvider;
import com.viaversion.viaversion.rewriter.ItemRewriter;

public final class InventoryPackets extends ItemRewriter<Protocol1_19To1_18_2> {

    public InventoryPackets(Protocol1_19To1_18_2 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        registerSetCooldown(ClientboundPackets1_18.COOLDOWN);
        registerWindowItems1_17_1(ClientboundPackets1_18.WINDOW_ITEMS, Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT, Type.FLAT_VAR_INT_ITEM);
        registerSetSlot1_17_1(ClientboundPackets1_18.SET_SLOT, Type.FLAT_VAR_INT_ITEM);
        registerAdvancements(ClientboundPackets1_18.ADVANCEMENTS, Type.FLAT_VAR_INT_ITEM);
        registerEntityEquipmentArray(ClientboundPackets1_18.ENTITY_EQUIPMENT, Type.FLAT_VAR_INT_ITEM);
        protocol.registerClientbound(ClientboundPackets1_18.SPAWN_PARTICLE, new PacketRemapper() {
            @Override
            public void registerMap() {
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
                handler(getSpawnParticleHandler(Type.VAR_INT, Type.FLAT_VAR_INT_ITEM));
            }
        });

        registerClickWindow1_17_1(ServerboundPackets1_19.CLICK_WINDOW, Type.FLAT_VAR_INT_ITEM);
        registerCreativeInvAction(ServerboundPackets1_19.CREATIVE_INVENTORY_ACTION, Type.FLAT_VAR_INT_ITEM);

        registerWindowPropertyEnchantmentHandler(ClientboundPackets1_18.WINDOW_PROPERTY);

        protocol.registerClientbound(ClientboundPackets1_18.TRADE_LIST, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // Container id
                handler(wrapper -> {
                    final int size = wrapper.read(Type.UNSIGNED_BYTE);
                    wrapper.write(Type.VAR_INT, size);
                    for (int i = 0; i < size; i++) {
                        handleItemToClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // First item
                        handleItemToClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Result

                        if (wrapper.read(Type.BOOLEAN)) {
                            handleItemToClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
                        } else {
                            wrapper.write(Type.FLAT_VAR_INT_ITEM, null);
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

        protocol.registerServerbound(ServerboundPackets1_19.PLAYER_DIGGING, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // Action
                map(Type.POSITION1_14); // Block position
                map(Type.UNSIGNED_BYTE); // Direction
                handler(sequenceHandler());
            }
        });
        protocol.registerServerbound(ServerboundPackets1_19.PLAYER_BLOCK_PLACEMENT, new PacketRemapper() {
            @Override
            public void registerMap() {
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
        protocol.registerServerbound(ServerboundPackets1_19.USE_ITEM, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // Hand
                handler(sequenceHandler());
            }
        });

        new RecipeRewriter1_16(protocol).registerDefaultHandler(ClientboundPackets1_18.DECLARE_RECIPES);
    }

    private PacketHandler sequenceHandler() {
        return wrapper -> {
            final int sequence = wrapper.read(Type.VAR_INT);
            final AckSequenceProvider provider = Via.getManager().getProviders().get(AckSequenceProvider.class);
            provider.handleSequence(wrapper.user(), sequence);
        };
    }
}
