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
package com.viaversion.viaversion.protocols.v1_17_1to1_18.rewriter;

import com.viaversion.viaversion.api.data.ParticleMappings;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ServerboundPackets1_17;
import com.viaversion.viaversion.protocols.v1_17_1to1_18.Protocol1_17_1To1_18;
import com.viaversion.viaversion.protocols.v1_17to1_17_1.packet.ClientboundPackets1_17_1;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.rewriter.RecipeRewriter;

public final class ItemPacketRewriter1_18 extends ItemRewriter<ClientboundPackets1_17_1, ServerboundPackets1_17, Protocol1_17_1To1_18> {

    public ItemPacketRewriter1_18(Protocol1_17_1To1_18 protocol) {
        super(protocol, Types.ITEM1_13_2, Types.ITEM1_13_2_ARRAY);
    }

    @Override
    public void registerPackets() {
        registerCooldown(ClientboundPackets1_17_1.COOLDOWN);
        registerSetContent1_17_1(ClientboundPackets1_17_1.CONTAINER_SET_CONTENT);
        registerMerchantOffers1_15(ClientboundPackets1_17_1.MERCHANT_OFFERS);
        registerSetSlot1_17_1(ClientboundPackets1_17_1.CONTAINER_SET_SLOT);
        registerAdvancements(ClientboundPackets1_17_1.UPDATE_ADVANCEMENTS);
        registerSetEquipment(ClientboundPackets1_17_1.SET_EQUIPMENT);

        protocol.registerClientbound(ClientboundPackets1_17_1.LEVEL_EVENT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); // Effect id
                map(Types.BLOCK_POSITION1_14); // Location
                map(Types.INT); // Data
                handler(wrapper -> {
                    int id = wrapper.get(Types.INT, 0);
                    int data = wrapper.get(Types.INT, 1);
                    if (id == 1010) { // Play record
                        wrapper.set(Types.INT, 1, protocol.getMappingData().getNewItemId(data));
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_17_1.LEVEL_PARTICLES, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); // Particle id
                map(Types.BOOLEAN); // Override limiter
                map(Types.DOUBLE); // X
                map(Types.DOUBLE); // Y
                map(Types.DOUBLE); // Z
                map(Types.FLOAT); // Offset X
                map(Types.FLOAT); // Offset Y
                map(Types.FLOAT); // Offset Z
                map(Types.FLOAT); // Max speed
                map(Types.INT); // Particle Count
                handler(wrapper -> {
                    int id = wrapper.get(Types.INT, 0);
                    if (id == 2) { // Barrier
                        wrapper.set(Types.INT, 0, 3); // Block marker
                        wrapper.write(Types.VAR_INT, 7754);
                        return;
                    } else if (id == 3) { // Light block
                        wrapper.write(Types.VAR_INT, 7786);
                        return;
                    }

                    ParticleMappings mappings = protocol.getMappingData().getParticleMappings();
                    if (mappings.isBlockParticle(id)) {
                        int data = wrapper.passthrough(Types.VAR_INT);
                        wrapper.set(Types.VAR_INT, 0, protocol.getMappingData().getNewBlockStateId(data));
                    } else if (mappings.isItemParticle(id)) {
                        handleItemToClient(wrapper.user(), wrapper.passthrough(Types.ITEM1_13_2));
                    }

                    int newId = protocol.getMappingData().getNewParticleId(id);
                    if (newId != id) {
                        wrapper.set(Types.INT, 0, newId);
                    }
                });
            }
        });

        new RecipeRewriter<>(protocol).register(ClientboundPackets1_17_1.UPDATE_RECIPES);

        registerContainerClick1_17_1(ServerboundPackets1_17.CONTAINER_CLICK);
        registerSetCreativeModeSlot(ServerboundPackets1_17.SET_CREATIVE_MODE_SLOT);
    }
}
