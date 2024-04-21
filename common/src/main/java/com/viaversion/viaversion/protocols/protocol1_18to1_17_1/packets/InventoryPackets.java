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
package com.viaversion.viaversion.protocols.protocol1_18to1_17_1.packets;

import com.viaversion.viaversion.api.data.ParticleMappings;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_17_1to1_17.ClientboundPackets1_17_1;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.ServerboundPackets1_17;
import com.viaversion.viaversion.protocols.protocol1_18to1_17_1.Protocol1_18To1_17_1;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.rewriter.RecipeRewriter;

public final class InventoryPackets extends ItemRewriter<ClientboundPackets1_17_1, ServerboundPackets1_17, Protocol1_18To1_17_1> {

    public InventoryPackets(Protocol1_18To1_17_1 protocol) {
        super(protocol, Type.ITEM1_13_2, Type.ITEM1_13_2_ARRAY);
    }

    @Override
    public void registerPackets() {
        registerSetCooldown(ClientboundPackets1_17_1.COOLDOWN);
        registerWindowItems1_17_1(ClientboundPackets1_17_1.WINDOW_ITEMS);
        registerTradeList(ClientboundPackets1_17_1.TRADE_LIST);
        registerSetSlot1_17_1(ClientboundPackets1_17_1.SET_SLOT);
        registerAdvancements(ClientboundPackets1_17_1.ADVANCEMENTS);
        registerEntityEquipmentArray(ClientboundPackets1_17_1.ENTITY_EQUIPMENT);

        protocol.registerClientbound(ClientboundPackets1_17_1.EFFECT, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); // Effect id
                map(Type.POSITION1_14); // Location
                map(Type.INT); // Data
                handler(wrapper -> {
                    int id = wrapper.get(Type.INT, 0);
                    int data = wrapper.get(Type.INT, 1);
                    if (id == 1010) { // Play record
                        wrapper.set(Type.INT, 1, protocol.getMappingData().getNewItemId(data));
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_17_1.SPAWN_PARTICLE, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); // Particle id
                map(Type.BOOLEAN); // Override limiter
                map(Type.DOUBLE); // X
                map(Type.DOUBLE); // Y
                map(Type.DOUBLE); // Z
                map(Type.FLOAT); // Offset X
                map(Type.FLOAT); // Offset Y
                map(Type.FLOAT); // Offset Z
                map(Type.FLOAT); // Max speed
                map(Type.INT); // Particle Count
                handler(wrapper -> {
                    int id = wrapper.get(Type.INT, 0);
                    if (id == 2) { // Barrier
                        wrapper.set(Type.INT, 0, 3); // Block marker
                        wrapper.write(Type.VAR_INT, 7754);
                        return;
                    } else if (id == 3) { // Light block
                        wrapper.write(Type.VAR_INT, 7786);
                        return;
                    }

                    ParticleMappings mappings = protocol.getMappingData().getParticleMappings();
                    if (mappings.isBlockParticle(id)) {
                        int data = wrapper.passthrough(Type.VAR_INT);
                        wrapper.set(Type.VAR_INT, 0, protocol.getMappingData().getNewBlockStateId(data));
                    } else if (mappings.isItemParticle(id)) {
                        handleItemToClient(wrapper.user(), wrapper.passthrough(Type.ITEM1_13_2));
                    }

                    int newId = protocol.getMappingData().getNewParticleId(id);
                    if (newId != id) {
                        wrapper.set(Type.INT, 0, newId);
                    }
                });
            }
        });

        new RecipeRewriter<>(protocol).register(ClientboundPackets1_17_1.DECLARE_RECIPES);

        registerClickWindow1_17_1(ServerboundPackets1_17.CLICK_WINDOW);
        registerCreativeInvAction(ServerboundPackets1_17.CREATIVE_INVENTORY_ACTION);
    }
}
