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
package com.viaversion.viaversion.protocols.v1_19_3to1_19_4.rewriter;

import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_18;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.packet.ClientboundPackets1_19_3;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.rewriter.RecipeRewriter1_19_3;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.Protocol1_19_3To1_19_4;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ServerboundPackets1_19_4;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ItemRewriter;

public final class ItemPacketRewriter1_19_4 extends ItemRewriter<ClientboundPackets1_19_3, ServerboundPackets1_19_4, Protocol1_19_3To1_19_4> {

    public ItemPacketRewriter1_19_4(final Protocol1_19_3To1_19_4 protocol) {
        super(protocol, Types.ITEM1_13_2, Types.ITEM1_13_2_ARRAY);
    }

    @Override
    public void registerPackets() {
        final BlockRewriter<ClientboundPackets1_19_3> blockRewriter = BlockRewriter.for1_14(protocol);
        blockRewriter.registerBlockEvent(ClientboundPackets1_19_3.BLOCK_EVENT);
        blockRewriter.registerBlockUpdate(ClientboundPackets1_19_3.BLOCK_UPDATE);
        blockRewriter.registerSectionBlocksUpdate(ClientboundPackets1_19_3.SECTION_BLOCKS_UPDATE);
        blockRewriter.registerLevelChunk1_19(ClientboundPackets1_19_3.LEVEL_CHUNK_WITH_LIGHT, ChunkType1_18::new);
        blockRewriter.registerBlockEntityData(ClientboundPackets1_19_3.BLOCK_ENTITY_DATA);

        protocol.registerClientbound(ClientboundPackets1_19_3.LEVEL_EVENT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); // Effect Id
                map(Types.BLOCK_POSITION1_14); // Location
                map(Types.INT); // Data
                handler(wrapper -> {
                    int id = wrapper.get(Types.INT, 0);
                    int data = wrapper.get(Types.INT, 1);
                    if (id == 1010) { // Play record
                        if (data >= 1092 && data <= 1106) {
                            // These IDs are valid records
                            wrapper.set(Types.INT, 1, protocol.getMappingData().getNewItemId(data));
                        } else {
                            // Send stop record instead
                            wrapper.set(Types.INT, 0, 1011);
                            wrapper.set(Types.INT, 1, 0);
                        }
                    } else if (id == 2001) { // Block break + block break sound
                        wrapper.set(Types.INT, 1, protocol.getMappingData().getNewBlockStateId(data));
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_3.OPEN_SCREEN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // Container id
                map(Types.VAR_INT); // Container type
                map(Types.COMPONENT); // Title
                handler(wrapper -> {
                    final int windowType = wrapper.get(Types.VAR_INT, 1);
                    if (windowType >= 21) { // New smithing menu
                        wrapper.set(Types.VAR_INT, 1, windowType + 1);
                    }
                });
            }
        });

        registerCooldown(ClientboundPackets1_19_3.COOLDOWN);
        registerSetContent1_17_1(ClientboundPackets1_19_3.CONTAINER_SET_CONTENT);
        registerSetSlot1_17_1(ClientboundPackets1_19_3.CONTAINER_SET_SLOT);
        registerAdvancements(ClientboundPackets1_19_3.UPDATE_ADVANCEMENTS);
        registerSetEquipment(ClientboundPackets1_19_3.SET_EQUIPMENT);
        registerMerchantOffers1_19(ClientboundPackets1_19_3.MERCHANT_OFFERS);
        registerContainerSetData(ClientboundPackets1_19_3.CONTAINER_SET_DATA);
        registerSetCreativeModeSlot(ServerboundPackets1_19_4.SET_CREATIVE_MODE_SLOT);
        registerContainerClick1_17_1(ServerboundPackets1_19_4.CONTAINER_CLICK);

        new RecipeRewriter1_19_3<>(protocol) {
            @Override
            public void handleCraftingShaped(final PacketWrapper wrapper) {
                super.handleCraftingShaped(wrapper);
                wrapper.write(Types.BOOLEAN, true); // Show notification
            }
        }.register(ClientboundPackets1_19_3.UPDATE_RECIPES);
    }
}
